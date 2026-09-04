package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次载荷实体
 * 对应Go: entity.ExperimentTurnPayload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperimentTurnPayload {

  /**
   * 轮次ID
   * 对应Go: TurnID int64
   */
  private Long turnId;

  /**
   * 评测数据集数据
   * 对应Go: EvalSet *TurnEvalSet
   */
  private TurnEvalSet evalSet;

  /**
   * 评测对象结果
   * 对应Go: TargetOutput *TurnTargetOutput
   */
  private TurnTargetOutput targetOutput;

  /**
   * 评测规则执行结果
   * 对应Go: EvaluatorOutput *TurnEvaluatorOutput
   */
  private TurnEvaluatorOutput evaluatorOutput;

  /**
   * 评测系统相关数据日志、error
   * 对应Go: SystemInfo *TurnSystemInfo
   */
  private TurnSystemInfo systemInfo;
}
