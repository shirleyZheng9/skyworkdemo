package com.iwhalecloud.bote.loop.client.data.domain.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseInfo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签信息数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagInfoDTO {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("appID")
  private Integer appID;

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("version_num")
  private Integer versionNum;

  @JsonProperty("version")
  private String version;

  @JsonProperty("tag_key_id")
  private Long tagKeyId;

  @JsonProperty("tag_key_name")
  private String tagKeyName;

  @JsonProperty("description")
  private String description;

  @JsonProperty("status")
  private TagStatusDTO status;

  @JsonProperty("tag_type")
  private TagTypeDTO tagType;

  @JsonProperty("parent_tag_key_id")
  private Long parentTagKeyId;

  @JsonProperty("tag_values")
  private List<TagValueDTO> tagValues;

  @JsonProperty("change_logs")
  private List<ChangeLogDTO> changeLogs;

  @JsonProperty("content_type")
  private TagContentTypeDTO contentType;

  @JsonProperty("content_spec")
  private TagContentSpecDTO contentSpec;

  @JsonProperty("domain_type_list")
  private List<TagDomainTypeDTO> domainTypeList;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;
}
