package com.iwhalecloud.bote.common.lock.zookeeper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockException;
import com.iwhalecloud.bote.common.lock.DistributedLockException.ErrorCodes;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bote.common.lock.LockHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Duration;
import java.util.Map;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zookeeper分布式锁工厂实现
 *
 * <p>负责创建和管理Zookeeper分布式锁实例，实现通用分布式锁工厂接口。</p>
 *
 * <p>特性：</p>
 * <ul>
 *   <li>锁实例缓存：避免重复创建相同路径的锁实例</li>
 *   <li>路径规范化：自动处理锁路径格式</li>
 *   <li>多种锁类型：支持互斥锁、读锁、写锁</li>
 *   <li>线程安全：工厂方法是线程安全的</li>
 * </ul>
 *
 * @since 2025-08-21
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class ZookeeperDistributedLockFactory implements DistributedLockFactory {

  private static final Logger logger = LoggerFactory.getLogger(ZookeeperDistributedLockFactory.class);

  /** Curator客户端 */
  private final CuratorFramework curatorFramework;

  /** 配置属性 */
  private final ZookeeperDistributedLockProperties properties;

  /** 锁实例缓存（使用Guava Cache） */
  private final Cache<String, DistributedLock> lockCache;

  /** 是否已关闭 */
  private volatile boolean shutdown = false;

  /**
   * 构造函数
   *
   * @param curatorFramework Curator客户端
   * @param properties 配置属性
   */
  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  @SuppressWarnings("PMD.GuardLogStatement")
  public ZookeeperDistributedLockFactory(CuratorFramework curatorFramework,
                                         ZookeeperDistributedLockProperties properties) {
    if (curatorFramework == null) {
      throw new IllegalArgumentException("CuratorFramework不能为空");
    }
    if (properties == null) {
      throw new IllegalArgumentException("配置属性不能为空");
    }

    this.curatorFramework = curatorFramework;
    this.properties = properties;

    // 初始化Guava Cache，使用配置的过期时间和最大缓存大小
    this.lockCache = CacheBuilder.newBuilder()
      .maximumSize(properties.getMaxCacheSize())
      .expireAfterWrite(Duration.ofMillis(properties.getCacheExpireTimeMs()))
      .recordStats()
      .build();

    logger.info("初始化Zookeeper分布式锁工厂: connectString={}, namespace={}, cacheExpire={}ms, maxCacheSize={}",
      properties.getConnectString(), properties.getNamespace(), properties.getCacheExpireTimeMs(), properties.getMaxCacheSize());
  }

  @Override
  public String getFactoryType() {
    return "zookeeper";
  }

  @Override
  public boolean isAvailable() {
    return !shutdown && curatorFramework.getState() == CuratorFrameworkState.STARTED;
  }

  @Override
  public DistributedLock getMutexLock(String lockKey) {
    return getLock(lockKey, DistributedLock.LockType.MUTEX);
  }

  @Override
  public DistributedLock getReadLock(String lockKey) {
    return getLock(lockKey, DistributedLock.LockType.READ);
  }

  @Override
  public DistributedLock getWriteLock(String lockKey) {
    return getLock(lockKey, DistributedLock.LockType.WRITE);
  }

  @Override
  public DistributedLock getLock(String lockKey, DistributedLock.LockType lockType) {
    checkAvailable();
    validateLockKey(lockKey);
    validateLockType(lockType);

    if (!properties.isCacheEnabled()) {
      return createLock(lockKey, lockType);
    }

    String cacheKey = buildCacheKey(lockKey, lockType);

    try {
      // 使用Guava Cache的get方法，自动处理缓存未命中的情况
      // Guava Cache会自动统计命中率，无需手动统计
      DistributedLock lock = lockCache.get(cacheKey, () -> {
        logger.debug("创建新的锁实例: key={}, type={}", lockKey, lockType);
        return doCreateLock(lockKey, lockType);
      });

      logger.debug("获取锁实例: key={}, type={}", lockKey, lockType);
      return lock;
    }
    catch (Exception e) {
      logger.error("获取锁实例失败: key={}, type={}", lockKey, lockType, e);
      throw new DistributedLockException(ErrorCodes.UNKNOWN_ERROR, "获取锁实例失败: " + lockKey, lockKey, e);
    }
  }

  @Override
  public DistributedLock createLock(String lockKey, DistributedLock.LockType lockType) {
    checkAvailable();
    validateLockKey(lockKey);
    validateLockType(lockType);

    logger.debug("创建新的锁实例(不缓存): key={}, type={}", lockKey, lockType);
    return doCreateLock(lockKey, lockType);
  }

  @Override
  public DistributedLock getBizLock(String bizType, String bizId) {
    return getBizLock(bizType, bizId, DistributedLock.LockType.MUTEX);
  }

  @Override
  public DistributedLock getBizLock(String bizType, String bizId, DistributedLock.LockType lockType) {
    String lockKey = LockHelper.formatBizLockKey(bizType, bizId);
    return getLock(lockKey, lockType);
  }

  @Override
  public DistributedLock getLockWithExpire(String lockKey, DistributedLock.LockType lockType, long expireTimeMs) {
    // Zookeeper锁基于会话，不支持自定义过期时间
    logger.warn("Zookeeper锁不支持自定义过期时间，忽略expireTimeMs参数: {}", expireTimeMs);
    return getLock(lockKey, lockType);
  }

  @Override
  public DistributedLock getLockWithAttributes(String lockKey, DistributedLock.LockType lockType,
                                               Map<String, String> attributes) {
    // 基础实现不支持自定义属性
    logger.warn("Zookeeper锁基础实现不支持自定义属性，忽略attributes参数");
    return getLock(lockKey, lockType);
  }

  @Override
  public boolean lockExists(String lockKey) {
    checkAvailable();
    validateLockKey(lockKey);

    try {
      String lockPath = properties.buildLockPath(lockKey);
      return curatorFramework.checkExists().forPath(lockPath) != null;
    }
    catch (Exception e) {
      logger.error("检查锁是否存在失败: key={}", lockKey, e);
      return false;
    }
  }

  @Override
  public void forceDeleteLock(String lockKey) {
    checkAvailable();
    validateLockKey(lockKey);

    try {
      String lockPath = properties.buildLockPath(lockKey);
      logger.warn("强制删除锁: key={}, path={}", lockKey, lockPath);

      if (curatorFramework.checkExists().forPath(lockPath) != null) {
        curatorFramework.delete().deletingChildrenIfNeeded().forPath(lockPath);
        logger.info("成功删除锁: key={}, path={}", lockKey, lockPath);
      }

      // 清除相关缓存
      clearCacheByLockKey(lockKey);

    }
    catch (Exception e) {
      logger.error("强制删除锁失败: key={}", lockKey, e);
      throw new DistributedLockException(
        DistributedLockException.ErrorCodes.RELEASE_FAILED,
        "强制删除锁失败: " + lockKey,
        lockKey,
        e
      );
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void clearCache(String lockKey) {
    if (lockKey != null && !lockKey.trim().isEmpty()) {
      clearCacheByLockKey(lockKey);
    }
    else {
      logger.info("清除所有锁缓存, 缓存数量: {}", lockCache.size());
      lockCache.invalidateAll();
    }
  }

  @Override
  public void shutdown() {
    if (!shutdown) {
      logger.info("关闭Zookeeper分布式锁工厂，缓存统计: {}", lockCache.stats());
      shutdown = true;
      lockCache.invalidateAll();
      // 注意：不关闭CuratorFramework，因为它可能被其他组件使用
    }
  }

  /**
   * 实际创建锁的方法
   */
  private DistributedLock doCreateLock(String lockKey, DistributedLock.LockType lockType) {
    String lockPath = properties.buildLockPath(lockKey);
    return new ZookeeperDistributedLock(curatorFramework, lockKey, lockPath, lockType, properties.getMaxWaitTimeMs());
  }

  /**
   * 构建缓存键
   */
  private String buildCacheKey(String lockKey, DistributedLock.LockType lockType) {
    return lockKey + ":" + lockType.name();
  }

  /**
   * 根据锁键清除缓存
   */
  private void clearCacheByLockKey(String lockKey) {
    for (DistributedLock.LockType type : DistributedLock.LockType.values()) {
      String cacheKey = buildCacheKey(lockKey, type);
      lockCache.invalidate(cacheKey);
      logger.debug("清除锁缓存: key={}, type={}", lockKey, type);
    }
  }

  /**
   * 检查工厂是否可用
   */
  private void checkAvailable() {
    if (!isAvailable()) {
      throw new DistributedLockException(
        DistributedLockException.ErrorCodes.SERVICE_UNAVAILABLE,
        "Zookeeper分布式锁工厂不可用"
      );
    }
  }

  /**
   * 验证锁键
   */
  private void validateLockKey(String lockKey) {
    if (lockKey == null || lockKey.trim().isEmpty()) {
      throw new IllegalArgumentException("锁标识不能为空");
    }
  }

  /**
   * 验证锁类型
   */
  private void validateLockType(DistributedLock.LockType lockType) {
    if (lockType == null) {
      throw new IllegalArgumentException("锁类型不能为空");
    }
  }
}
