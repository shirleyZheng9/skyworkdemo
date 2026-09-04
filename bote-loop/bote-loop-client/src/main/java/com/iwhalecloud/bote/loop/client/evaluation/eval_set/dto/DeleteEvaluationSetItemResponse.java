package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除评测集数据响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteEvaluationSetItemResponse {

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
