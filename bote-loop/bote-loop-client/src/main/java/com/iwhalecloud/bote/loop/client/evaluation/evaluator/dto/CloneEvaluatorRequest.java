package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 克隆评估器请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloneEvaluatorRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("evaluator_id")
  private Long evaluatorId;

  @JsonProperty("base")
  private Base base;
}
