package com.iwhalecloud.bote.loop.client.data.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Get Tag Spec Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTagSpecResponse {

  @JsonProperty("max_height")
  private Long maxHeight;

  @JsonProperty("max_width")
  private Long maxWidth;

  @JsonProperty("max_total")
  private Long maxTotal;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
