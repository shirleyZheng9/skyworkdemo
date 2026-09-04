package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估目标执行结果
 * 对应Go: EvalTargetExecuteResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvalTargetExecuteResult {

  /**
   * 输出数据
   * 对应Go: OutputData *entity.EvalTargetOutputData
   */
  private EvalTargetOutputData outputData;

  /**
   * 运行状态
   * 对应Go: Status entity.EvalTargetRunStatus
   */
  private EvalTargetRunStatus status;

  /**
   * 错误信息
   * 对应Go: Error error
   */
  private String error;

  /**
   * 运行时间（毫秒）
   * 对应Go: RunTimeMs int64
   */
  private Long runTimeMs;

  /**
   * 追踪ID
   * 对应Go: TraceID string
   */
  private String traceId;

  /**
   * 执行ID
   * 对应Go: ExecutionID string
   */
  private String executionId;
}
