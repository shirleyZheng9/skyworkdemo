package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评估集参数
 * 对应Go: CreateEvaluationSetParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEvaluationSetParam {

  private Long spaceId;
  private String name;
  private String description;
  private EvaluationSetSchema evaluationSetSchema;
  private BizCategory bizCategory;
  private Session session;
  private Long catalogItemId;
}
