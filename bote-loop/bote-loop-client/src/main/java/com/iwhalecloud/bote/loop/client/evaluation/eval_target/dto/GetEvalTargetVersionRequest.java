package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取评测目标版本请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetEvalTargetVersionRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("eval_target_version_id")
  private Long evalTargetVersionId;

  @JsonProperty("base")
  private Base base;
}
