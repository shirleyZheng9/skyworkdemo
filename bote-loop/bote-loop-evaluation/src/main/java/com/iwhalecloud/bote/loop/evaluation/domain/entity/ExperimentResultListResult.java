package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验结果列表结果
 * 对应Go: MGetExperimentResultResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperimentResultListResult {

  /**
   * 列评估器列表
   * 对应Go: ColumnEvaluators []*entity.ColumnEvaluator
   */
  private List<ColumnEvaluator> columnEvaluators;

  /**
   * 列评估集字段列表
   * 对应Go: ColumnEvalSetFields []*entity.ColumnEvalSetField
   */
  private List<ColumnEvalSetField> columnEvalSetFields;

  /**
   * 数据项结果列表
   * 对应Go: ItemResults []*entity.ItemResult
   */
  private List<ItemResult> itemResults;

  /**
   * 总数
   * 对应Go: Total int64
   */
  private Long total;
}
