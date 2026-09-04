package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关键词过滤器实体
 * 对应Go: entity.KeywordFilter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordFilter {

  /**
   * 数据项快照过滤器
   * 对应Go: ItemSnapshotFilter *ItemSnapshotFilter
   */
  private ItemSnapshotFilterDO itemSnapshotFilter;

  /**
   * 评估目标数据过滤器列表
   * 对应Go: EvalTargetDataFilters []*FieldFilter
   */
  private List<FieldFilterDO> evalTargetDataFilters;

  /**
   * 关键词
   * 对应Go: Keyword *string
   */
  private String keyword;
}
