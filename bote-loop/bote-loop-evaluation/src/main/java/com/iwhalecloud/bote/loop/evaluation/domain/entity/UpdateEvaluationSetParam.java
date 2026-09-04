package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新评估集参数
 * 对应Go: UpdateEvaluationSetParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEvaluationSetParam {

  private Long spaceId;
  private Long evaluationSetId;
  private String name;
  private String description;
  private Long catalogItemId;
}
