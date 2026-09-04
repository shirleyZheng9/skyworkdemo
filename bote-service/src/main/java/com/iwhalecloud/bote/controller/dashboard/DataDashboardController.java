package com.iwhalecloud.bote.controller.dashboard;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.dashboard.ConversationMessageTrendItemVO;
import com.iwhalecloud.bote.dto.dashboard.DistributionItemVO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageDistributionItemVO;
import com.iwhalecloud.bote.dto.dashboard.OverviewMetricVO;
import com.iwhalecloud.bote.dto.dashboard.TodayDynamicsVO;
import com.iwhalecloud.bote.service.dashboard.IDataDashboardService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据概览看板接口。
 *
 * @author zhengxueli
 * @since 2026-08-28
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/dataDashboard", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "数据引擎：数据概览看板")
public class DataDashboardController {

  private final IDataDashboardService dataDashboardService;

  /**
   * 查询顶部资源概览。
   * 前端首屏顶部卡片使用该接口一次性获取智能体、知识库、模型、插件/MCP 四类
   */
  @Operation(summary = "查询顶部资源概览")
  @PostMapping("queryOverviewMetrics")
  public ResultVO<List<OverviewMetricVO>> queryOverviewMetrics() {
    // 看板数据必须按当前登录租户隔离，tenantId 不允许由前端传入。
    Long currentTenantId = TenantIdUtil.getTenantId();
    Assert.notNull(currentTenantId, "无法获取当前租户，请重新选择租户或重新登录");
    // Service 负责统计口径、固定展示顺序和异常数据清洗，Controller 只做入口编排。
    return ResultVO.success(dataDashboardService.queryOverviewMetrics(currentTenantId));
  }

  /**
   * 查询智能体模式分布。
   */
  @Operation(summary = "查询智能体模式分布")
  @PostMapping("queryAgentModeDistribution")
  public ResultVO<List<DistributionItemVO>> queryAgentModeDistribution() {
    Long currentTenantId = TenantIdUtil.getTenantId();
    Assert.notNull(currentTenantId, "无法获取当前租户，请重新选择租户或重新登录");
    return ResultVO.success(dataDashboardService.queryAgentModeDistribution(currentTenantId));
  }

  /**
   * 查询知识库文件类型分布。
   * 统计范围为当前租户本地有效知识库中的全部有效文档，固定返回文档、表格、图片、音视频、其他
   * 五项；有数据时五项占比合计严格为 100.00%。
   *
   * @return 当前租户知识库文件类型数量及占比
   */
  @Operation(summary = "查询知识库文件类型分布")
  @PostMapping("queryKnowledgeFileDistribution")
  public ResultVO<List<DistributionItemVO>> queryKnowledgeFileDistribution() {
    Long currentTenantId = TenantIdUtil.getTenantId();
    Assert.notNull(currentTenantId, "无法获取当前租户，请重新选择租户或重新登录");
    return ResultVO.success(dataDashboardService.queryKnowledgeFileDistribution(currentTenantId));
  }

  /**
   * 查询自统计功能上线以来的模型累计使用分布。
   * 能识别产品系列的模型按产品系列聚合；无法识别产品系列的历史、失效、删除及未知模型
   * 合并为“自建/其他”。接口只返回累计调用次数，不计算百分比。
   *
   * @return 当前租户模型累计调用次数
   */
  @Operation(summary = "查询模型累计使用分布")
  @PostMapping("queryModelUsageDistribution")
  public ResultVO<List<ModelUsageDistributionItemVO>> queryModelUsageDistribution() {
    Long currentTenantId = TenantIdUtil.getTenantId();
    Assert.notNull(currentTenantId, "无法获取当前租户，请重新选择租户或重新登录");
    return ResultVO.success(dataDashboardService.queryModelUsageDistribution(currentTenantId));
  }

  /**
   * 查询今日动态。
   */
  @Operation(summary = "查询今日动态")
  @PostMapping("queryTodayDynamics")
  public ResultVO<TodayDynamicsVO> queryTodayDynamics() {
    // 当前租户只从登录上下文读取，避免前端传 tenantId 越权查询其他团队数据。
    Long currentTenantId = TenantIdUtil.getTenantId();
    // 租户为空时直接失败，避免 Service 层用 null 拼出全量统计。
    Assert.notNull(currentTenantId, "无法获取当前租户，请重新选择租户或重新登录");
    // 今日动态只做当前租户的实时统计，统一由 Service 维护各指标口径。
    return ResultVO.success(dataDashboardService.queryTodayDynamics(currentTenantId));
  }

  /**
   * 查询最近三十天每日对话消息量趋势。
   */
  @Operation(summary = "查询每日对话消息量趋势")
  @PostMapping("queryConversationMessageTrend")
  public ResultVO<List<ConversationMessageTrendItemVO>> queryConversationMessageTrend() {
    // 趋势数据同样按登录租户隔离，不接收前端 tenantId。
    Long currentTenantId = TenantIdUtil.getTenantId();
    // 缺少租户上下文时不继续查询，防止误扫多租户会话消息表。
    Assert.notNull(currentTenantId, "无法获取当前租户，请重新选择租户或重新登录");
    // Service 固定返回最近三十天并补齐空日期，前端可以直接渲染折线图。
    return ResultVO.success(dataDashboardService.queryConversationMessageTrend(currentTenantId));
  }
}
