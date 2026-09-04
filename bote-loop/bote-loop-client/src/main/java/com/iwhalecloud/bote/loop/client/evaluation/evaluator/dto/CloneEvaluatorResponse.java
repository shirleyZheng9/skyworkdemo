package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 克隆评估器响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloneEvaluatorResponse {

  @JsonProperty("evaluator_id")
  private Long evaluatorId;

  @JsonProperty("base_resp")
  private BaseResp baseResp;
}
