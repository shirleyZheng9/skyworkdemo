package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取评测集数据请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetEvaluationSetItemRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("evaluation_set_id")
  private Long evaluationSetId;

  @JsonProperty("item_id")
  private Long itemId;

  @JsonProperty("Base")
  private Base base;
}
