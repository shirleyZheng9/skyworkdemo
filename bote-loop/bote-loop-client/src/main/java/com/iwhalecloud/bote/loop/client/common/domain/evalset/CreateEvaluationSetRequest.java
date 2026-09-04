package com.iwhalecloud.bote.loop.client.common.domain.evalset;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.SessionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.BizCategoryDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetSchemaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评测集请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEvaluationSetRequest {
  private Long workspaceId;
  private String name;
  private String description;
  private EvaluationSetSchemaDTO evaluationSetSchema;
  private BizCategoryDTO bizCategory;
  private SessionDTO session;
  private Base base;
}
