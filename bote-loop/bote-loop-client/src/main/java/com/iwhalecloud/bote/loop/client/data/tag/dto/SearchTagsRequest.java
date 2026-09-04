package com.iwhalecloud.bote.loop.client.data.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.OrderByDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagContentTypeDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagDomainTypeDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Search Tags Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchTagsRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("tag_key_name_like")
  private String tagKeyNameLike;

  @JsonProperty("created_bys")
  private List<String> createdBys;

  @JsonProperty("domain_types")
  private List<TagDomainTypeDTO> domainTypes;

  @JsonProperty("content_types")
  private List<TagContentTypeDTO> contentTypes;

  @JsonProperty("tag_key_name")
  private String tagKeyName;

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
