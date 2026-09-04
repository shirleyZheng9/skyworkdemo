package com.iwhalecloud.bote.service.dashboard.impl;

import com.iwhalecloud.bote.cache.DataDashboardCache;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.doc.common.tenant.ThreadContextRunner;
import com.iwhalecloud.bote.dto.dashboard.KnowledgeFileTypeCountDTO;
import com.iwhalecloud.bote.mapper.dashboard.DataDashboardMapper;
import com.iwhalecloud.bote.service.dashboard.support.DashboardRefreshCoordinator;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshEventListener;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 知识库文件类型分布的异步刷新器。
 *
 * <p>同时承接两种刷新来源：看板读取到逻辑过期缓存，以及项目现有的 knowledge
 * 缓存刷新事件。热缓存的后台刷新通过 {@link DashboardRefreshCoordinator} 统一完成；
 * 冷缓存则由请求线程调用 {@link #loadCurrentCounts(Long)} 获取首次展示所需的真实数据。
 * 本类集中保留该指标的查询、结果规范化和缓存写入逻辑，避免同步与异步路径口径不一致。</p>
 *
 * @author zhengxueli
 * @since 2026-08-29
 */
@Component
public class KnowledgeFileDistributionRefreshService implements Refreshable, IRefreshEventListener {

  private static final Logger logger =
    LoggerFactory.getLogger(KnowledgeFileDistributionRefreshService.class);

  /** 同时用于刷新框架注册和通用协调器的任务去重，发布后不要随意修改。
   * 知识库文件类型分布缓存的唯一名称 */
  private static final String CACHE_NAME = "dashboardKnowledgeFileDistribution";

  /** Mapper 正常只返回这五类；出现未知编码时会在 Java 层合并到 other。 */
  private static final Set<String> CATEGORY_CODES =
    Set.of("document", "spreadsheet", "image", "audioVideo", "other");

  private final DataDashboardMapper dataDashboardMapper;
  private final DataDashboardCache dataDashboardCache;
  private final DashboardRefreshCoordinator refreshCoordinator;

  public KnowledgeFileDistributionRefreshService(
    DataDashboardMapper dataDashboardMapper,
    DataDashboardCache dataDashboardCache,
    DashboardRefreshCoordinator refreshCoordinator
  ) {
    this.dataDashboardMapper = dataDashboardMapper;
    this.dataDashboardCache = dataDashboardCache;
    this.refreshCoordinator = refreshCoordinator;
  }

  /**
   * 请求后台刷新。
   *
   * <p>该入口可由 HTTP 缓存读取和知识库变更事件共同调用。所有技术性并发控制都由
   * 通用协调器处理，因此本方法只描述“刷新哪个指标、哪个租户以及执行什么动作”。</p>
   */
  public void requestRefresh(Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    refreshCoordinator.requestRefresh(CACHE_NAME, tenantId, () -> refreshTenant(tenantId));
  }

  /**
   * 冷缓存时同步加载当前租户的真实统计，并写入 Redis。
   *
   * <p>该方法仅用于“缓存中完全没有可返回数据”的首次访问。缓存写异常只记录告警，不会覆盖
   * 或丢弃数据库结果。后续请求在 Redis 恢复前仍会查询数据库，这是保证正确性的降级取舍。</p>
   *
   * @param tenantId 当前租户 ID
   * @return document、spreadsheet、image、audioVideo、other 五类的真实统计
   */
  public Map<String, Long> loadCurrentCounts(Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Map<String, Long> counts = loadCountsWithTenantContext(tenantId);
    try {
      writeCache(tenantId, counts);
    }
    catch (RuntimeException e) {
      logger.warn("写入知识库文件类型分布缓存失败，本次仍返回数据库真实结果，tenantId={}",
        tenantId, e);
    }
    return counts;
  }

  /**
   * 执行当前指标真正的业务刷新：带上租户上下文查询四表聚合，并一次性覆盖 Redis。
   * 该方法只会由通用协调器在线程池中调用，不会运行在 HTTP 请求线程。
   */
  private void refreshTenant(Long tenantId) {
    Map<String, Long> counts = loadCountsWithTenantContext(tenantId);
    // 后台刷新写缓存失败时保留异常，让协调器记录一次完整的刷新失败，避免产生“已刷新”的假象。
    writeCache(tenantId, counts);
  }

  /** 同步首次加载与异步刷新共用同一查询入口，确保租户上下文和统计口径完全一致。 */
  private Map<String, Long> loadCountsWithTenantContext(Long tenantId) {
    return ThreadContextRunner.runWithTenant(
      tenantId,
      () -> loadKnowledgeFileCounts(tenantId)
    );
  }

  /** 按统计结果是否为空选择缓存逻辑有效期，并统一执行 Redis 写入。 */
  private void writeCache(Long tenantId, Map<String, Long> counts) {
    dataDashboardCache.putKnowledgeFileDistribution(tenantId, counts, sumCounts(counts) == 0L);
  }

  /**
   * 把数据库实际返回的分类归并为页面固定的五类。
   */
  private Map<String, Long> loadKnowledgeFileCounts(Long tenantId) {
    Map<String, Long> counts = createZeroCountMap();
    List<KnowledgeFileTypeCountDTO> databaseCounts =
      dataDashboardMapper.selectKnowledgeFileTypeCounts(tenantId);
    if (databaseCounts == null) {
      return counts;
    }
    for (KnowledgeFileTypeCountDTO item : databaseCounts) {
      if (item == null) {
        continue;
      }
      String category = CATEGORY_CODES.contains(item.getCode()) ? item.getCode() : "other";
      counts.merge(category, normalizeCount(item.getCount()), Long::sum);
    }
    return counts;
  }

  /** 创建完整且顺序稳定的四分类全零结果，便于数据库缺项时补零。 */
  private static Map<String, Long> createZeroCountMap() {
    Map<String, Long> counts = new LinkedHashMap<>();
    counts.put("document", 0L);
    counts.put("spreadsheet", 0L);
    counts.put("image", 0L);
    counts.put("audioVideo", 0L);
    counts.put("other", 0L);
    return counts;
  }

  /** 将数据库或异常缓存中的 null、负数归零，防止产生负数统计和负占比。 */
  private static long normalizeCount(Long count) {
    return count == null ? 0L : Math.max(count, 0L);
  }

  /** 计算文件大类数量总和，用于选择空数据或正常数据的逻辑有效期。 */
  private static long sumCounts(Map<String, Long> counts) {
    long total = 0L;
    for (Long count : counts.values()) {
      total = Math.addExact(total, normalizeCount(count));
    }
    return total;
  }

  @Override
  public String getCacheName() {
    return CACHE_NAME;
  }

  /** 复用知识库增删改已经发布的缓存事件，无需修改原有业务入口。 */
  @Override
  public String getDependentCacheName() {
    return CacheConsts.CACHE_NAME_KNOWLEDGE;
  }

  /**
   * 其他应用实例收到 knowledge 广播后会进入这里。
   * 该依赖关系用于文档服务与看板服务分开部署的场景。
   */
  @Override
  public void refreshLocalCache(List<String> keys) {
    requestRefreshByKnowledgeKeys(keys);
  }

  /**
   * 发起 knowledge 刷新的当前实例不会消费自己的广播，因此通过事件监听器补齐本机刷新。
   * 本机监听与跨实例依赖刷新最终都会进入相同的协调器，并由去重和分布式锁合并任务。
   */
  @Override
  public void onRefresh(
    IRefreshCacheService refreshCacheService,
    String cacheName,
    List<String> keys
  ) {
    if (CacheConsts.CACHE_NAME_KNOWLEDGE.equals(cacheName)) {
      requestRefreshByKnowledgeKeys(keys);
    }
  }

  /**
   * knowledge 缓存键格式为 tenantId:knowledgeId。看板按租户聚合，所以只提取第一段，
   * 同一批事件中一个租户包含多个知识库键时使用 distinct 合并为一次刷新。
   */
  private void requestRefreshByKnowledgeKeys(List<String> keys) {
    if (keys == null || keys.isEmpty()) {
      // 全量事件没有租户维度，现有缓存仍会按逻辑过期自行刷新，避免全库扫描租户。
      return;
    }
    keys.stream()
      .map(KnowledgeFileDistributionRefreshService::parseTenantId)
      .filter(java.util.Objects::nonNull)
      .distinct()
      .forEach(this::requestRefresh);
  }

  /** 从知识库缓存键中安全解析 tenantId，异常键只记录告警，不影响原缓存刷新流程。 */
  private static Long parseTenantId(String knowledgeCacheKey) {
    if (knowledgeCacheKey == null) {
      return null;
    }
    int separatorIndex = knowledgeCacheKey.indexOf(CacheConsts.COLON);
    String tenantPart = separatorIndex < 0
      ? knowledgeCacheKey
      : knowledgeCacheKey.substring(0, separatorIndex);
    try {
      return Long.valueOf(tenantPart);
    }
    catch (NumberFormatException e) {
      logger.warn("忽略无法解析租户的 knowledge 缓存键，key={}", knowledgeCacheKey);
      return null;
    }
  }
}
