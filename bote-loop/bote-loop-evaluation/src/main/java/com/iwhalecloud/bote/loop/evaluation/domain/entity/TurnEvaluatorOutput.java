package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮次评估器输出实体
 * 对应Go: entity.TurnEvaluatorOutput
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnEvaluatorOutput {

  /**
   * 评估器记录映射
   * key: 评估器版本ID, value: 评估器记录
   * 对应Go: EvaluatorRecords map[int64]*EvaluatorRecord
   */
  private Map<Long, EvaluatorRecord> evaluatorRecords;
}
