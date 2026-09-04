package com.iwhalecloud.bote.service.dashboard;

import com.iwhalecloud.bote.dto.dashboard.ConversationMessageTrendItemVO;
import com.iwhalecloud.bote.dto.dashboard.DistributionItemVO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageDistributionItemVO;
import com.iwhalecloud.bote.dto.dashboard.OverviewMetricVO;
import com.iwhalecloud.bote.dto.dashboard.TodayDynamicsVO;
import java.util.List;

/**
 * 数据概览看板服务。
 *
 * @author zhengxueli
 * @since 2026-08-28
 */
public interface IDataDashboardService {

  /**
   * 查询顶部资源概览。
   *
   * <p>该接口返回当前租户可见的核心资源总量和今日新增量。今日新增使用创建时间统计，
   * 不是和昨日快照对比得到的净增长。</p>
   *
   * @param tenantId 当前租户 ID
   */
  List<OverviewMetricVO> queryOverviewMetrics(Long tenantId);

  /**
   * 查询智能体模式分布。
   *
   * @param tenantId 当前租户 ID
   */
  List<DistributionItemVO> queryAgentModeDistribution(Long tenantId);

  /**
   * 查询知识库文件类型分布。
   *
   * <p>统计当前租户本地有效知识库中的全部有效文档，不限制创建时间；按扩展名映射为
   * 文档、表格、图片、音视频和其他五类，五类数量合计等于纳入统计的文档总数。</p>
   *
   * @param tenantId 当前租户 ID
   * @return 固定包含 document、spreadsheet、image、audioVideo、other 五项的分布结果
   */
  List<DistributionItemVO> queryKnowledgeFileDistribution(Long tenantId);

  /** 查询模型累计使用分布，按产品系列返回累计调用次数，不计算占比。 */
  List<ModelUsageDistributionItemVO> queryModelUsageDistribution(Long tenantId);

  /** 查询今日动态，包含新增资源、工具调用、模型调用和知识构建文档。 */
  TodayDynamicsVO queryTodayDynamics(Long tenantId);

  /** 查询最近三十天每日对话消息量趋势，缺失日期补 0。 */
  List<ConversationMessageTrendItemVO> queryConversationMessageTrend(Long tenantId);
}
