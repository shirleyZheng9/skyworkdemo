package com.iwhalecloud.bote.loop.client.data.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update Tag Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTagResponse {

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
