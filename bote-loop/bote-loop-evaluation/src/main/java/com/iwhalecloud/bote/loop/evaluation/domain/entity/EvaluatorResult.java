package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器结果实体
 * 对应Go: entity.EvaluatorResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorResult {

  /**
   * 分数
   * 对应Go: Score *float64
   */
  private Double score;

  /**
   * 修正信息
   * 对应Go: Correction *Correction
   */
  private Correction correction;

  /**
   * 推理过程
   * 对应Go: Reasoning string
   */
  private String reasoning;
}
