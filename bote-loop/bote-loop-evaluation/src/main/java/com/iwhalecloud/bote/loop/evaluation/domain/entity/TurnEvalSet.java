package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮次评估集实体
 * 对应Go: entity.TurnEvalSet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnEvalSet {

  /**
   * 轮次数据
   * 对应Go: Turn *Turn
   */
  private Turn turn;
}
