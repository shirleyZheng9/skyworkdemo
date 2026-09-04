package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮次目标输出实体
 * 对应Go: entity.TurnTargetOutput
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnTargetOutput {

  /**
   * 评估目标记录
   * 对应Go: EvalTargetRecord *EvalTargetRecord
   */
  private EvalTargetRecord evalTargetRecord;
}
