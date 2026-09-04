package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次结果过滤器实体
 * 对应Go: entity.ExptTurnResultFilter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptTurnResultFilter {

  /**
   * 轮次运行状态过滤器列表
   * 对应Go: TrunRunStateFilters []*TurnRunStateFilter
   */
  private List<TurnRunStateFilter> turnRunStateFilters;

  /**
   * 数据项运行状态过滤器列表
   * 对应Go: ItemRunStateFilters []*ItemRunStateFilter
   */
  private List<ItemRunStateFilter> itemRunStateFilters;

  /**
   * 分数过滤器列表
   * 对应Go: ScoreFilters []*ScoreFilter
   */
  private List<ScoreFilter> scoreFilters;
}
