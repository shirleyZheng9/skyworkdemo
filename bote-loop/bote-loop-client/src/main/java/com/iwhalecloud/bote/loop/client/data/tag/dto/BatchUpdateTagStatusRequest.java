package com.iwhalecloud.bote.loop.client.data.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagStatusDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Batch Update Tag Status Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchUpdateTagStatusRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("tag_key_ids")
  private List<Long> tagKeyIds;

  @JsonProperty("to_status")
  private TagStatusDTO toStatus;

  @JsonProperty("Base")
  private Base base;
}
