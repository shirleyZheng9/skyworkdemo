package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取评测集数据响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetEvaluationSetItemResponse {

  @JsonProperty("evaluation_set_item")
  private EvaluationSetItemDTO evaluationSetItem;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
