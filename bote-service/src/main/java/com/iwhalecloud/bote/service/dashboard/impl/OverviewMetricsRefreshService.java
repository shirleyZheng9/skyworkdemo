package com.iwhalecloud.bote.service.dashboard.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.DataDashboardCache;
import com.iwhalecloud.bote.doc.common.tenant.ThreadContextRunner;
import com.iwhalecloud.bote.dto.dashboard.OverviewMetricCountDTO;
import com.iwhalecloud.bote.dto.plugin.request.QueryPluginRequest;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition;
import com.iwhalecloud.bote.mapper.dashboard.DataDashboardMapper;
import com.iwhalecloud.bote.service.dashboard.support.DashboardRefreshCoordinator;
import com.iwhalecloud.bote.service.plugin.IPluginManageService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 顶部资源概览缓存刷新器。
 *
 * <p>该类集中处理“查真实数据库统计”和“写 Redis 缓存”两件事。HTTP 查询接口只负责
 * 优先读缓存、必要时调用这里同步加载或提交后台刷新，避免主 Service 里混入太多缓存细节。</p>
 *
 * @author zhengxueli
 * @since 2026-08-31
 */
@Component
public class OverviewMetricsRefreshService {

  private static final Logger logger = LoggerFactory.getLogger(OverviewMetricsRefreshService.class);

  /**
   * 缓存刷新任务名称。
   * <p>该名称会参与本机去重和 Redis 分布式锁构造，发布后不要随意修改。</p>
   */
  private static final String CACHE_NAME = "dashboardOverviewMetrics";

  private final DataDashboardMapper dataDashboardMapper;

  private final DataDashboardCache dataDashboardCache;

  private final DashboardRefreshCoordinator refreshCoordinator;

  private final IPluginManageService pluginManageService;

  public OverviewMetricsRefreshService(
    DataDashboardMapper dataDashboardMapper,
    DataDashboardCache dataDashboardCache,
    DashboardRefreshCoordinator refreshCoordinator,
    IPluginManageService pluginManageService
  ) {
    this.dataDashboardMapper = dataDashboardMapper;
    this.dataDashboardCache = dataDashboardCache;
    this.refreshCoordinator = refreshCoordinator;
    this.pluginManageService = pluginManageService;
  }

  /**
   * 请求后台刷新顶部概览缓存。
   *
   * <p>逻辑过期命中时调用该方法。真正的数据库查询会在线程池中执行，不占用当前 HTTP
   * 请求线程；同一租户同一天的重复刷新会被 {@link DashboardRefreshCoordinator} 合并。</p>
   */
  public void requestRefresh(Long tenantId, LocalDate statisticsDate) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(statisticsDate, "统计日期不能为空");
    refreshCoordinator.requestRefresh(
      CACHE_NAME + ":" + statisticsDate,
      tenantId,
      () -> refreshTenant(tenantId, statisticsDate)
    );
  }

  /**
   * 冷缓存同步加载真实统计。
   *
   * <p>缓存不存在时，接口必须返回真实数据，因此这里由请求线程直接查库。写缓存失败只记录
   * 告警，不影响本次真实统计返回。</p>
   */
  public List<OverviewMetricCountDTO> loadCurrentMetrics(Long tenantId, LocalDate statisticsDate) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(statisticsDate, "统计日期不能为空");
    List<OverviewMetricCountDTO> metrics = loadMetricsWithTenantContext(tenantId, statisticsDate);
    try {
      dataDashboardCache.putOverviewMetrics(tenantId, statisticsDate, metrics);
    }
    catch (RuntimeException e) {
      logger.warn("写入顶部资源概览缓存失败，本次仍返回数据库真实结果，tenantId={}, statisticsDate={}",
        tenantId, statisticsDate, e);
    }
    return metrics;
  }

  /** 后台刷新路径：查询真实统计并覆盖 Redis；异常交给协调器统一记录。 */
  private void refreshTenant(Long tenantId, LocalDate statisticsDate) {
    List<OverviewMetricCountDTO> metrics = loadMetricsWithTenantContext(tenantId, statisticsDate);
    dataDashboardCache.putOverviewMetrics(tenantId, statisticsDate, metrics);
  }

  /**
   * 带租户上下文执行 Mapper 查询。
   *
   * <p>看板接口本身已经传 tenantId，但项目中部分底层能力还会读取线程上下文租户；
   * 这里和知识库文件分布刷新保持一致，避免异步线程缺失租户上下文。</p>
   */
  private List<OverviewMetricCountDTO> loadMetricsWithTenantContext(
    Long tenantId,
    LocalDate statisticsDate
  ) {
    return ThreadContextRunner.runWithTenant(
      tenantId,
      () -> loadMetrics(tenantId, statisticsDate)
    );
  }

  /** 按统计日期生成左闭右开的自然日边界，并查询数据库真实统计。 */
  private List<OverviewMetricCountDTO> loadMetrics(Long tenantId, LocalDate statisticsDate) {
    LocalDateTime todayStart = statisticsDate.atStartOfDay();
    LocalDateTime tomorrowStart = todayStart.plusDays(1);
    List<OverviewMetricCountDTO> metrics = new ArrayList<>(
      dataDashboardMapper.selectOverviewMetrics(tenantId, todayStart, tomorrowStart)
    );
    mergeExternalPluginCount(tenantId, metrics);
    return metrics;
  }

  /** 将外部插件市场的授权插件总数合并到本地 MCP 数量中。 */
  private void mergeExternalPluginCount(Long tenantId, List<OverviewMetricCountDTO> metrics) {
    OverviewMetricCountDTO toolMetric = metrics.stream()
      .filter(metric -> metric != null && "tool".equals(metric.getCode()))
      .findFirst()
      .orElseGet(() -> {
        OverviewMetricCountDTO metric = new OverviewMetricCountDTO();
        metric.setCode("tool");
        metric.setTotal(0L);
        metrics.add(metric);
        return metric;
      });

    long mcpCount = toolMetric.getTotal() == null ? 0L : Math.max(toolMetric.getTotal(), 0L);
    toolMetric.setTotal(mcpCount);
    // 插件/MCP 卡片不再统计较昨日变化，兼容现有返回字段并固定为 0。
    toolMetric.setTodayIncrease(0L);

    QueryPluginRequest request = new QueryPluginRequest();
    request.setTenantId(tenantId);
    request.setPageNum(1);
    request.setPageSize(1);
    try {
      PageInfo<PluginDefinition> pluginPage = pluginManageService.queryAuthPluginPage(request);
      long pluginCount = pluginPage == null ? 0L : Math.max(pluginPage.getTotal(), 0L);
      toolMetric.setTotal(mcpCount + pluginCount);
    }
    catch (RuntimeException e) {
      // 外部市场不可用时仍返回本地 MCP 数量，避免拖挂整个数据看板。
      logger.warn("查询外部插件市场总数失败，仅返回本地 MCP 数量，tenantId={}", tenantId, e);
    }
  }
}
