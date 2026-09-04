package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评测目标响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEvalTargetResponse {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("version_id")
  private Long versionId;

  @JsonProperty("base_resp")
  private BaseResp baseResp;
}
