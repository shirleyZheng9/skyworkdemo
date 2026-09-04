package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取评测目标版本响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetEvalTargetVersionResponse {

  @JsonProperty("eval_target")
  private EvalTargetDTO evalTarget;

  @JsonProperty("base_resp")
  private BaseResp baseResp;
}
