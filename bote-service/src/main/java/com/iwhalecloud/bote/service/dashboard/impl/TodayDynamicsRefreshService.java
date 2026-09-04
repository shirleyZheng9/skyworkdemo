package com.iwhalecloud.bote.service.dashboard.impl;

import com.iwhalecloud.bote.cache.DataDashboardCache;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.common.tenant.ThreadContextRunner;
import com.iwhalecloud.bote.dto.dashboard.OverviewMetricCountDTO;
import com.iwhalecloud.bote.dto.dashboard.TodayDynamicsVO;
import com.iwhalecloud.bote.mapper.dashboard.DataDashboardMapper;
import com.iwhalecloud.bote.service.dashboard.support.DashboardRefreshCoordinator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 今日动态缓存刷新器。
 *
 * <p>今日动态包含当天新增资源和当天调用统计。首页展示不需要毫秒级实时，因此使用短
 * 逻辑缓存削峰；缓存缺失时同步查库，逻辑过期时返回旧值并后台刷新。</p>
 *
 * @author zhengxueli
 * @since 2026-09-01
 */
@Component
public class TodayDynamicsRefreshService {

  private static final Logger logger = LoggerFactory.getLogger(TodayDynamicsRefreshService.class);

  /**
   * 缓存刷新任务名称。
   * <p>该名称会参与本机去重和 Redis 分布式锁构造，发布后不要随意修改。</p>
   */
  private static final String CACHE_NAME = "dashboardTodayDynamics";

  private final DataDashboardMapper dataDashboardMapper;

  private final DataDashboardCache dataDashboardCache;

  private final DashboardRefreshCoordinator refreshCoordinator;

  public TodayDynamicsRefreshService(
    DataDashboardMapper dataDashboardMapper,
    DataDashboardCache dataDashboardCache,
    DashboardRefreshCoordinator refreshCoordinator
  ) {
    this.dataDashboardMapper = dataDashboardMapper;
    this.dataDashboardCache = dataDashboardCache;
    this.refreshCoordinator = refreshCoordinator;
  }

  /**
   * 请求后台刷新今日动态缓存。
   *
   * <p>逻辑过期命中时调用该方法。真实数据库查询在线程池中执行，不占用当前 HTTP
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
   * 冷缓存同步加载真实今日动态。
   *
   * <p>缓存不存在时，接口必须返回真实数据，因此这里由请求线程直接查库。写缓存失败只记录
   * 告警，不影响本次真实统计返回。</p>
   */
  public TodayDynamicsVO loadCurrentDynamics(Long tenantId, LocalDate statisticsDate) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(statisticsDate, "统计日期不能为空");
    TodayDynamicsVO dynamics = loadDynamicsWithTenantContext(tenantId, statisticsDate);
    try {
      dataDashboardCache.putTodayDynamics(tenantId, statisticsDate, dynamics);
    }
    catch (RuntimeException e) {
      logger.warn("写入今日动态缓存失败，本次仍返回数据库真实结果，tenantId={}, statisticsDate={}",
        tenantId, statisticsDate, e);
    }
    return dynamics;
  }

  /** 后台刷新路径：查询真实统计并覆盖 Redis；异常交给协调器统一记录。 */
  private void refreshTenant(Long tenantId, LocalDate statisticsDate) {
    TodayDynamicsVO dynamics = loadDynamicsWithTenantContext(tenantId, statisticsDate);
    dataDashboardCache.putTodayDynamics(tenantId, statisticsDate, dynamics);
  }

  /**
   * 带租户上下文执行 Mapper 查询。
   *
   * <p>看板 Mapper 已显式传 tenantId，但异步线程中仍补齐租户上下文，和其他看板刷新器保持一致。</p>
   */
  private TodayDynamicsVO loadDynamicsWithTenantContext(Long tenantId, LocalDate statisticsDate) {
    return ThreadContextRunner.runWithTenant(
      tenantId,
      () -> loadDynamics(tenantId, statisticsDate)
    );
  }

  /** 按统计日期生成左闭右开的自然日边界，并查询数据库真实统计。 */
  private TodayDynamicsVO loadDynamics(Long tenantId, LocalDate statisticsDate) {
    // 今日开始时间作为左闭边界：created_time/start_time >= todayStart。
    LocalDateTime todayStart = statisticsDate.atStartOfDay();
    // 明日零点作为右开边界：created_time/start_time < tomorrowStart，避免 23:59:59 精度问题。
    LocalDateTime tomorrowStart = todayStart.plusDays(1);
    // 今日动态只需要新增智能体和新增知识库，使用专用轻量查询避免额外统计模型、插件和 MCP。
    List<OverviewMetricCountDTO> todayResourceIncreases =
      dataDashboardMapper.selectTodayResourceIncreases(tenantId, todayStart, tomorrowStart);
    // 空库或异常 Mapper 返回 null 时按空列表处理，今日新增最终会补 0。
    List<OverviewMetricCountDTO> safeTodayResourceIncreases =
      todayResourceIncreases == null ? List.of() : todayResourceIncreases;
    // 将今日新增资源按 code 转成 Map，后面只取 agent 和 knowledge 两类。
    Map<String, OverviewMetricCountDTO> overviewByCode =
      new HashMap<>(safeTodayResourceIncreases.size());
    // 遍历今日新增统计结果，防御性跳过异常 null 项。
    for (OverviewMetricCountDTO metric : safeTodayResourceIncreases) {
      // Mapper 正常不会返回 null，这里防御后续改造引入的脏数据。
      if (metric != null) {
        // code 是稳定业务编码，不能用中文 name 做程序判断。
        overviewByCode.put(metric.getCode(), metric);
      }
    }
    // 今日大模型调用次数按模型使用日志的 start_time 统计，只查当天仍在当前表的实时日志。
    long modelInvokeCount = normalizeCount(
      dataDashboardMapper.selectModelInvokeCount(tenantId, todayStart, tomorrowStart)
    );
    // 今日知识构建文档按 process_completed_at 和构建完成状态统计，表示当天新增“已学习/构建完成”的文档。
    long builtDocumentCount = normalizeCount(
      dataDashboardMapper.selectBuiltDocumentCount(
        tenantId,
        todayStart,
        tomorrowStart,
        KnowledgeConsts.DOCUMENT_STATUS_FINISH
      )
    );
    // 按前端今日动态卡片需要的字段一次性返回，所有数量都已做 null/负数归零处理。
    return new TodayDynamicsVO(
      normalizeCount(getTodayIncrease(overviewByCode, "agent")),
      normalizeCount(getTodayIncrease(overviewByCode, "knowledge")),
      modelInvokeCount,
      builtDocumentCount,
      LocalDateTime.now()
    );
  }

  /** 从概览统计 Map 中读取当天新增数量，缺失时按 0 处理。 */
  private Long getTodayIncrease(Map<String, OverviewMetricCountDTO> overviewByCode, String code) {
    // 根据稳定 code 查找对应概览指标。
    OverviewMetricCountDTO metric = overviewByCode.get(code);
    // 缺失指标时返回 0，避免影响今日动态整体展示。
    return metric == null ? 0L : metric.getTodayIncrease();
  }

  /** 将数据库返回的 null 或异常负数统一归零。 */
  private long normalizeCount(Long count) {
    // null 表示 SQL 没有返回结果，负数不符合计数语义，统一按 0 展示。
    return count == null || count < 0L ? 0L : count;
  }
}
