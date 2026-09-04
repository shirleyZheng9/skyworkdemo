package com.iwhalecloud.bote.service.dashboard.impl;

import com.iwhalecloud.bote.cache.DataDashboardCache;
import com.iwhalecloud.bote.doc.common.tenant.ThreadContextRunner;
import com.iwhalecloud.bote.dto.dashboard.ConversationMessageTrendCountDTO;
import com.iwhalecloud.bote.mapper.dashboard.DataDashboardMapper;
import com.iwhalecloud.bote.service.dashboard.support.DashboardRefreshCoordinator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 每日对话消息量趋势缓存刷新器。
 *
 * <p>该趋势需要扫描最近三十天当前和历史会话消息。为了稳定首页加载耗时，本类负责
 * 把数据库按天聚合结果写入 Redis；查询接口命中缓存时直接补齐并返回 30 天序列。</p>
 *
 * @author zhengxueli
 * @since 2026-09-01
 */
@Component
public class ConversationMessageTrendRefreshService {

  private static final Logger logger =
    LoggerFactory.getLogger(ConversationMessageTrendRefreshService.class);

  /**
   * 缓存刷新任务名称。
   * <p>该名称会参与本机去重和 Redis 分布式锁构造，发布后不要随意修改。</p>
   */
  private static final String CACHE_NAME = "dashboardConversationMessageTrend";

  /** 最近三十天趋势包含今天，因此起始日期 = 结束日期 - 29 天。 */
  private static final int CONVERSATION_TREND_DAYS = 30;

  private final DataDashboardMapper dataDashboardMapper;

  private final DataDashboardCache dataDashboardCache;

  private final DashboardRefreshCoordinator refreshCoordinator;

  public ConversationMessageTrendRefreshService(
    DataDashboardMapper dataDashboardMapper,
    DataDashboardCache dataDashboardCache,
    DashboardRefreshCoordinator refreshCoordinator
  ) {
    this.dataDashboardMapper = dataDashboardMapper;
    this.dataDashboardCache = dataDashboardCache;
    this.refreshCoordinator = refreshCoordinator;
  }

  /**
   * 请求后台刷新每日消息趋势缓存。
   *
   * <p>逻辑过期命中时调用该方法。真正的数据库聚合在线程池中执行，不占用当前 HTTP
   * 请求线程；同一租户同一结束日期的重复刷新会被 {@link DashboardRefreshCoordinator} 合并。</p>
   */
  public void requestRefresh(Long tenantId, LocalDate endDate) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(endDate, "统计结束日期不能为空");
    refreshCoordinator.requestRefresh(
      CACHE_NAME + ":" + endDate,
      tenantId,
      () -> refreshTenant(tenantId, endDate)
    );
  }

  /**
   * 冷缓存同步加载真实趋势，并写入 Redis。
   *
   * <p>缓存不存在时，接口必须返回真实数据，因此这里由请求线程直接查库。写缓存失败只记录
   * 告警，不影响本次真实统计返回。</p>
   */
  public List<ConversationMessageTrendCountDTO> loadCurrentCounts(
    Long tenantId,
    LocalDate endDate
  ) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(endDate, "统计结束日期不能为空");
    List<ConversationMessageTrendCountDTO> counts = loadCountsWithTenantContext(tenantId, endDate);
    try {
      dataDashboardCache.putConversationMessageTrend(tenantId, endDate, counts);
    }
    catch (RuntimeException e) {
      logger.warn("写入每日对话消息量趋势缓存失败，本次仍返回数据库真实结果，tenantId={}, endDate={}",
        tenantId, endDate, e);
    }
    return counts;
  }

  /** 后台刷新路径：查询真实统计并覆盖 Redis；异常交给协调器统一记录。 */
  private void refreshTenant(Long tenantId, LocalDate endDate) {
    List<ConversationMessageTrendCountDTO> counts = loadCountsWithTenantContext(tenantId, endDate);
    dataDashboardCache.putConversationMessageTrend(tenantId, endDate, counts);
  }

  /**
   * 带租户上下文执行 Mapper 查询。
   *
   * <p>Mapper 已显式传 tenantId，但异步线程中仍补齐租户上下文，和其他看板刷新器保持一致。</p>
   */
  private List<ConversationMessageTrendCountDTO> loadCountsWithTenantContext(
    Long tenantId,
    LocalDate endDate
  ) {
    return ThreadContextRunner.runWithTenant(
      tenantId,
      () -> loadCounts(tenantId, endDate)
    );
  }

  /** 根据结束日期计算最近三十天左闭右开时间窗，并查询数据库聚合结果。 */
  private List<ConversationMessageTrendCountDTO> loadCounts(Long tenantId, LocalDate endDate) {
    // 起始日期向前推 29 天，与结束日期合计正好 30 个自然日。
    LocalDate startDate = endDate.minusDays(CONVERSATION_TREND_DAYS - 1L);
    // 查询左闭时间边界，覆盖起始日期 00:00:00 之后的消息。
    LocalDateTime startTime = startDate.atStartOfDay();
    // 查询右开时间边界，取结束日期次日 00:00:00，避免数据库时间精度差异。
    LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();
    // 当前表和历史表由 Mapper 内部 UNION ALL 覆盖，返回有消息日期的聚合行。
    return dataDashboardMapper.selectConversationMessageTrend(tenantId, startTime, endTime);
  }
}
