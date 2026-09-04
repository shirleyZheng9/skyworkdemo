package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验结果实体
 * 对应Go: entity.ExperimentResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperimentResult {

  /**
   * 实验ID
   * 对应Go: ExperimentID int64
   */
  private Long experimentId;

  /**
   * 载荷数据
   * 对应Go: Payload *ExperimentTurnPayload
   */
  private ExperimentTurnPayload payload;
}
