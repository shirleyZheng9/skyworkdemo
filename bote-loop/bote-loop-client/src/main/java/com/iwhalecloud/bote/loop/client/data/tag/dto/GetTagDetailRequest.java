package com.iwhalecloud.bote.loop.client.data.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.OrderByDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Get Tag Detail Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTagDetailRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("tag_key_id")
  private Long tagKeyId;

  @JsonProperty("page_number")
  private Integer pageNumber;

  @JsonProperty("page_size")
  private Integer pageSize;

  @JsonProperty("page_token")
  private String pageToken;

  @JsonProperty("order_by")
  private OrderByDTO orderBy;

  @JsonProperty("Base")
  private Base base;
}
