package com.iwhalecloud.bote.loop.client.data.domain.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseInfo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签值数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagValueDTO {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("tag_key_id")
  private Long tagKeyId;

  @JsonProperty("tag_value_id")
  private Long tagValueId;

  @JsonProperty("tag_value_name")
  private String tagValueName;

  @JsonProperty("description")
  private String description;

  @JsonProperty("status")
  private TagStatusDTO status;

  @JsonProperty("version_num")
  private Integer versionNum;

  @JsonProperty("parent_tag_value_id")
  private Long parentTagValueId;

  @JsonProperty("children")
  private List<TagValueDTO> children;

  @JsonProperty("is_system")
  private Boolean isSystem;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;
}
