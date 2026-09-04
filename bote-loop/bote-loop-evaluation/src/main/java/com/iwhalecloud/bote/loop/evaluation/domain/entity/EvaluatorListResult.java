package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器列表结果
 * 对应Go: ListEvaluatorResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorListResult {

  /**
   * 评估器列表
   * 对应Go: Evaluators []*entity.Evaluator
   */
  private List<Evaluator> evaluators;

  /**
   * 总数
   * 对应Go: Total int64
   */
  private Long total;
}
