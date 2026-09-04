package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetInputDataDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 执行评测目标请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecuteEvalTargetRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("eval_target_id")
  private Long evalTargetId;

  @JsonProperty("eval_target_version_id")
  private Long evalTargetVersionId;

  @JsonProperty("input_data")
  private EvalTargetInputDataDTO inputData;

  @JsonProperty("experiment_run_id")
  private Long experimentRunId;

  @JsonProperty("base")
  private Base base;
}
