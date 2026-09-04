package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dataset DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetDTO {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("schema_id")
  private Long schemaId;

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("status")
  private DatasetStatusDTO status;

  @JsonProperty("category")
  private DatasetCategoryDTO category;

  @JsonProperty("biz_category")
  private String bizCategory;

  @JsonProperty("schema")
  private DatasetSchemaDTO schema;

  @JsonProperty("security_level")
  private SecurityLevelDTO securityLevel;

  @JsonProperty("visibility")
  private DatasetVisibilityDTO visibility;

  @JsonProperty("spec")
  private DatasetSpecDTO spec;

  @JsonProperty("features")
  private DatasetFeaturesDTO features;

  @JsonProperty("latest_version")
  private String latestVersion;

  @JsonProperty("next_version_num")
  private Long nextVersionNum;

  @JsonProperty("item_count")
  private Long itemCount;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private Long createdAt;

  @JsonProperty("updated_by")
  private String updatedBy;

  @JsonProperty("updated_at")
  private Long updatedAt;

  @JsonProperty("expired_at")
  private Long expiredAt;

  @JsonProperty("change_uncommitted")
  private Boolean changeUncommitted;
  @JsonProperty("catalog_item_id")
  private Long catalogItemId;
}
