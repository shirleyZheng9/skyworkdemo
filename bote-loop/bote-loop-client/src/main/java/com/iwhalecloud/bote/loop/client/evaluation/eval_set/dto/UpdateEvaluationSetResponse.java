package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新评测集响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEvaluationSetResponse {

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
