package com.iwhalecloud.bote.mapper.dashboard;

import com.iwhalecloud.bote.dto.dashboard.AgentModeCountDTO;
import com.iwhalecloud.bote.dto.dashboard.ConversationMessageTrendCountDTO;
import com.iwhalecloud.bote.dto.dashboard.KnowledgeFileTypeCountDTO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageStatDTO;
import com.iwhalecloud.bote.dto.dashboard.OverviewMetricCountDTO;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 数据概览看板统计 Mapper。
 *
 * @author zhengxueli
 * @since 2026-08-28
 */
public interface DataDashboardMapper {

  /**
   * 查询顶部资源概览。
   *
   * <p>每类资源只做一次总量 + 今日新增条件聚合，不读取列表字段，不关联用户、目录等展示表。
   * 调用方传入同一组今日时间边界，保证页面所有“今日新增”指标处在同一个自然日口径内。
   * 如果该方法未来被其他接口复用且未传时间边界，SQL 会把 todayIncrease 返回为 0，
   * 不会用半截时间条件统计。</p>
   *
   * @param tenantId 当前租户 ID
   * @param todayStart 今日零点，按应用部署时区计算；为空时不统计今日新增
   * @param tomorrowStart 明日零点，作为今日查询的右开边界；为空时不统计今日新增
   * @return agent、knowledge、model、tool 四类资源统计
   */
  List<OverviewMetricCountDTO> selectOverviewMetrics(
    @Param("tenantId") Long tenantId,
    // 今日开始时间，左闭区间：created_time >= todayStart；为空时今日新增返回 0。
    @Param("todayStart") LocalDateTime todayStart,
    // 明日开始时间，右开区间：created_time < tomorrowStart；为空时今日新增返回 0。
    @Param("tomorrowStart") LocalDateTime tomorrowStart
  );

  /**
   * 查询今日动态需要的资源新增量。
   *
   * <p>今日动态只展示新增智能体和新增知识库，不复用顶部概览查询，避免额外统计模型、
   * 插件和 MCP 总量。</p>
   *
   * @param tenantId 当前租户 ID
   * @param todayStart 今日零点，左闭区间
   * @param tomorrowStart 明日零点，右开区间
   * @return agent、knowledge 两类今日新增统计
   */
  List<OverviewMetricCountDTO> selectTodayResourceIncreases(
    @Param("tenantId") Long tenantId,
    // 今日开始时间，左闭区间：created_time >= todayStart。
    @Param("todayStart") LocalDateTime todayStart,
    // 明日开始时间，右开区间：created_time < tomorrowStart。
    @Param("tomorrowStart") LocalDateTime tomorrowStart
  );

  /**
   * 按智能体模式统计有效智能体数量。
   *
   * @param tenantId 当前租户 ID
   * @return 数据库中实际存在的模式及数量；没有数据时返回空列表
   */
  List<AgentModeCountDTO> selectAgentModeCounts(@Param("tenantId") Long tenantId);

  /**
   * 统计当前租户有效知识库中的有效文档，并按页面文件大类分组。
   *
   * <p>查询以 bt_document 为统计主体，确保聊天附件、头像、插件生成文件等
   * 非知识库文件不会进入统计；bt_knowledge_base 仅用于过滤有效的本地知识库，
   * bt_file_info/bt_file 用于取得稳定关联下的物理文件扩展类型。</p>
   *
   * @param tenantId 当前租户 ID
   * @return 数据库实际存在的大类及数量；没有数据时返回空列表
   */
  List<KnowledgeFileTypeCountDTO> selectKnowledgeFileTypeCounts(@Param("tenantId") Long tenantId);

  /** 查询当前租户自统计功能上线以来的累计模型调用量。 */
  List<ModelUsageStatDTO> selectModelUsageTotals(@Param("tenantId") Long tenantId);

  /** 查询今日模型调用次数。 */
  Long selectModelInvokeCount(
    @Param("tenantId") Long tenantId,
    @Param("startTime") LocalDateTime startTime,
    @Param("endTime") LocalDateTime endTime
  );

  /** 查询今日知识构建完成文档数。 */
  Long selectBuiltDocumentCount(
    @Param("tenantId") Long tenantId,
    @Param("startTime") LocalDateTime startTime,
    @Param("endTime") LocalDateTime endTime,
    @Param("successStatus") String successStatus
  );

  /** 查询指定时间范围内每日用户消息量。 */
  List<ConversationMessageTrendCountDTO> selectConversationMessageTrend(
    @Param("tenantId") Long tenantId,
    @Param("startTime") LocalDateTime startTime,
    @Param("endTime") LocalDateTime endTime
  );
}
