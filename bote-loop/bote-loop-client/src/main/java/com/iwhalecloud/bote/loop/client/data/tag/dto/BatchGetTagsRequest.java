package com.iwhalecloud.bote.loop.client.data.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Batch Get Tags Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetTagsRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("tag_key_ids")
  private List<Long> tagKeyIds;

  @JsonProperty("Base")
  private Base base;
}
