package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估集版本结果
 * 对应Go: EvaluationSetVersionResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationSetVersionResult {

  private EvaluationSetVersion version;
  private EvaluationSet evaluationSet;
}
