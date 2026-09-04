package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器版本列表结果
 * 对应Go: ListEvaluatorVersionResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorVersionListResult {

  /**
   * 评估器版本列表
   * 对应Go: EvaluatorVersions []*entity.Evaluator
   */
  private List<Evaluator> evaluatorVersions;

  /**
   * 总数
   * 对应Go: Total int64
   */
  private Long total;
}
