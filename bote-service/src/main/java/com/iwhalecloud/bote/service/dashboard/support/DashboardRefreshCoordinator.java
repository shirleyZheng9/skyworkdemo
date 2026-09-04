package com.iwhalecloud.bote.service.dashboard.support;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 看板缓存异步刷新的通用协调器。
 *
 * <p>该组件只处理技术问题，不包含任何具体统计 SQL：</p>
 * <ol>
 *   <li>把刷新动作提交到应用线程池，保证 HTTP 请求线程不等待聚合查询；</li>
 *   <li>通过 {@link #pendingRefreshes} 对“缓存名称 + 租户”去重，防止单实例重复刷新；</li>
 *   <li>通过 {@link #refreshPermits} 限制单实例所有看板刷新任务的数据库并发量；</li>
 *   <li>通过 Redis 锁保证多实例环境下只有一个实例执行同一租户、同一指标的刷新；</li>
 *   <li>刷新失败时只记录日志，不删除旧缓存，调用方仍可继续使用旧数据。</li>
 * </ol>
 *
 * <p>后续新增看板缓存时，只需要调用
 * {@link #requestRefresh(String, Long, Runnable)} 并传入具体刷新动作，不要再次复制线程池、
 * Semaphore 或 Redis 锁代码。</p>
 *
 * @author zhengxueli
 * @since 2026-08-29
 */
@Component
public class DashboardRefreshCoordinator {

  private static final Logger logger = LoggerFactory.getLogger(DashboardRefreshCoordinator.class);

  /**
   * 单实例所有看板指标合计最多同时执行两个数据库刷新任务。
   * 这里限制的是后台聚合 SQL 并发，不限制普通看板缓存读取。
   */
  private static final int MAX_CONCURRENT_REFRESHES = 2;

  /**
   * 分布式锁的兜底过期时间。正常任务结束会主动释放；如果实例宕机，锁也会在两分钟后
   * 自动消失，避免该租户的缓存永远无法刷新。
   */
  private static final int REFRESH_LOCK_TTL_SECONDS = 2 * 60;

  private static final String REFRESH_LOCK_PREFIX = "dashboard:refresh-lock:";

  /** 只删除当前任务持有的锁，避免旧任务误删锁过期后由其他实例重新取得的新锁。 */
  private static final String RELEASE_LOCK_SCRIPT =
    "if redis.call('get', KEYS[1]) == ARGV[1] then " +
      "return redis.call('del', KEYS[1]) else return 0 end";

  private final ICacheClient cacheClient;
  private final TaskExecutor taskExecutor;

  /**
   * 本实例已提交但尚未结束的刷新键。使用并发 Set 可以原子完成“是否首次提交”的判断。
   */
  private final Set<RefreshKey> pendingRefreshes = ConcurrentHashMap.newKeySet();

  /** 所有看板刷新共享同一个并发闸门，防止新增指标后各自限流却仍把数据库压满。 */
  private final Semaphore refreshPermits = new Semaphore(MAX_CONCURRENT_REFRESHES);

  public DashboardRefreshCoordinator(
    CacheFactory cacheFactory,
    @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor
  ) {
    this.cacheClient = cacheFactory.getCacheClient(
      CacheConsts.GROUP_PLATFORM,
      REFRESH_LOCK_PREFIX
    );
    this.taskExecutor = taskExecutor;
  }

  /**
   * 提交一次租户级看板缓存刷新。
   *
   * <p>本方法只负责快速提交，调用线程不会等待 Semaphore、Redis 锁或数据库查询。
   * 相同的 {@code cacheName + tenantId} 已经处于刷新中时，本次请求直接合并到已有任务。</p>
   *
   * @param cacheName 稳定且唯一的缓存业务名称，用于任务去重和构造分布式锁键
   * @param tenantId 当前租户 ID
   * @param refreshAction 获得刷新资格后执行的“查询数据库并覆盖缓存”动作
   */
  public void requestRefresh(String cacheName, Long tenantId, Runnable refreshAction) {
    Assert.hasText(cacheName, "看板缓存名称不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(refreshAction, "看板缓存刷新动作不能为空");

    RefreshKey refreshKey = new RefreshKey(cacheName, tenantId);
    if (!pendingRefreshes.add(refreshKey)) {
      return;
    }
    try {
      taskExecutor.execute(() -> executeRefresh(refreshKey, refreshAction));
    }
    catch (RuntimeException e) {
      // 线程池拒绝任务时必须移除去重标记，否则该租户后续无法再次提交刷新。
      pendingRefreshes.remove(refreshKey);
      logger.warn("提交看板缓存刷新任务失败，cacheName={}, tenantId={}", cacheName, tenantId, e);
    }
  }

  /** 在线程池中执行限流、跨实例互斥及业务刷新动作。 */
  private void executeRefresh(RefreshKey refreshKey, Runnable refreshAction) {
    boolean permitAcquired = false;
    String lockToken = null;
    try {
      // 等待发生在后台线程，不会占用或拖慢 HTTP 请求线程。
      refreshPermits.acquire();
      permitAcquired = true;

      lockToken = tryAcquireDistributedLock(refreshKey);
      if (lockToken != null) {
        refreshAction.run();
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.warn("看板缓存刷新任务被中断，cacheName={}, tenantId={}",
        refreshKey.cacheName(), refreshKey.tenantId(), e);
    }
    catch (RuntimeException e) {
      // 旧缓存不在这里删除，失败后由下一次变更事件或逻辑过期访问继续尝试。
      logger.warn("异步刷新看板缓存失败，保留旧缓存，cacheName={}, tenantId={}",
        refreshKey.cacheName(), refreshKey.tenantId(), e);
    }
    finally {
      if (lockToken != null) {
        releaseDistributedLockQuietly(refreshKey, lockToken);
      }
      if (permitAcquired) {
        refreshPermits.release();
      }
      pendingRefreshes.remove(refreshKey);
    }
  }

  /** 返回 token 表示取得锁，返回 null 表示其他实例正在刷新相同数据。 */
  @Nullable
  private String tryAcquireDistributedLock(RefreshKey refreshKey) {
    String token = UUID.randomUUID().toString();
    Boolean acquired = cacheClient.opsForValue().setIfAbsent(
      buildLockKey(refreshKey),
      token,
      REFRESH_LOCK_TTL_SECONDS,
      TimeUnit.SECONDS
    );
    return Boolean.TRUE.equals(acquired) ? token : null;
  }

  /**
   * 使用 token 比对后原子释放锁。释放失败不影响业务结果，锁会依靠 TTL 自动回收。
   */
  private void releaseDistributedLockQuietly(RefreshKey refreshKey, String lockToken) {
    try {
      cacheClient.execute(RELEASE_LOCK_SCRIPT, buildLockKey(refreshKey), lockToken);
    }
    catch (RuntimeException e) {
      logger.warn("释放看板缓存刷新锁失败，锁将自动过期，cacheName={}, tenantId={}",
        refreshKey.cacheName(), refreshKey.tenantId(), e);
    }
  }

  /** CacheFactory 已添加统一前缀，此处只需拼接业务名称和租户 ID。 */
  private static String buildLockKey(RefreshKey refreshKey) {
    return refreshKey.cacheName() + CacheConsts.COLON + refreshKey.tenantId();
  }

  /** 一个刷新任务的唯一业务身份，同时作为本地去重 Set 的键。 */
  private record RefreshKey(String cacheName, Long tenantId) {
  }
}
