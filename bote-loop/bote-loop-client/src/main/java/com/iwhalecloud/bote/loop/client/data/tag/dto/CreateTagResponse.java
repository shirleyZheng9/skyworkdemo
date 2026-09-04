package com.iwhalecloud.bote.loop.client.data.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create Tag Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTagResponse {

  @JsonProperty("tag_key_id")
  private Long tagKeyId;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
