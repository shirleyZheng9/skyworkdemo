package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器运行结果
 * 对应Go: EvaluatorRunResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorRunResult {

  /**
   * 输出数据
   * 对应Go: Output *entity.EvaluatorOutputData
   */
  private EvaluatorOutputData output;

  /**
   * 运行状态
   * 对应Go: RunStatus entity.EvaluatorRunStatus
   */
  private EvaluatorRunStatus runStatus;

  /**
   * 追踪ID
   * 对应Go: TraceID string
   */
  private String traceId;
}
