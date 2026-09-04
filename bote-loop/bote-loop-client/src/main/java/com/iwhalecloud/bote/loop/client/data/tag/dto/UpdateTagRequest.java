package com.iwhalecloud.bote.loop.client.data.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagContentSpecDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagContentTypeDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagDomainTypeDTO;
import com.iwhalecloud.bote.loop.client.data.domain.tag.TagValueDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update Tag Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTagRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("tag_key_id")
  private Long tagKeyId;

  @JsonProperty("tag_key_name")
  private String tagKeyName;

  @JsonProperty("description")
  private String description;

  @JsonProperty("tag_content_spec")
  private TagContentSpecDTO tagContentSpec;

  @JsonProperty("tag_values")
  private List<TagValueDTO> tagValues;

  @JsonProperty("tag_domain_types")
  private List<TagDomainTypeDTO> tagDomainTypes;

  @JsonProperty("tag_content_type")
  private TagContentTypeDTO tagContentType;

  @JsonProperty("version")
  private String version;

  @JsonProperty("Base")
  private Base base;
}
