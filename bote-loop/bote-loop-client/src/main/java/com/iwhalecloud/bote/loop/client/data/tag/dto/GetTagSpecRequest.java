package com.iwhalecloud.bote.loop.client.data.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Get Tag Spec Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTagSpecRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("Base")
  private Base base;
}
