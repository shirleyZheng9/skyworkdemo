package com.iwhalecloud.bote.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 顶部资源概览卡片。
 *
 * <p>total 和 todayIncrease 均使用 Long，避免大租户资源量超过 Integer 上限。
 * 今日新增不是“较昨日净增长”，删除、恢复和状态变更不会反向扣减该值。</p>
 *
 * @author zhengxueli
 * @since 2026-08-31
 */
@Getter
@ToString
@AllArgsConstructor
@Schema(description = "顶部资源概览卡片")
public class OverviewMetricVO {

  /** 前端用于区分卡片和绑定图标的稳定编码，不使用中文名称做程序判断。 */
  @Schema(description = "指标编码", example = "agent")
  private final String code;

  /** 页面直接展示的卡片标题，由后端统一维护，避免多端文案不一致。 */
  @Schema(description = "指标名称", example = "智能体总数")
  private final String name;

  /** 当前统计口径下仍然有效、可见的资源数量。 */
  @Schema(description = "有效资源总量", example = "128")
  private final Long total;

  /** 创建时间落在今天自然日内的资源数量，和 total 使用同一套业务过滤条件。 */
  @Schema(description = "今日新增数量", example = "6")
  private final Long todayIncrease;
}
