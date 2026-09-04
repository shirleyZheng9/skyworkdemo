package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评测目标请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEvalTargetRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("param")
  private CreateEvalTargetParamDTO param;

  @JsonProperty("base")
  private Base base;
}
