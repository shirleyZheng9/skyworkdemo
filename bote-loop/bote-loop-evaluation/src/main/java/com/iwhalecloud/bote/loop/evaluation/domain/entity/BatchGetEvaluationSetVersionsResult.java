package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取评估集版本结果
 * 对应Go: BatchGetEvaluationSetVersionsResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetEvaluationSetVersionsResult {

  private EvaluationSetVersion version;
  private EvaluationSet evaluationSet;
}
