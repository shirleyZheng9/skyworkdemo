package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评估集版本参数
 * 对应Go: CreateEvaluationSetVersionParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEvaluationSetVersionParam {

  private Long spaceId;
  private Long evaluationSetId;
  private String version;
  private String description;
}
