package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集版本数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetVersionDTO {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("schema_id")
  private Long schemaId;

  @JsonProperty("version")
  private String version;

  @JsonProperty("version_num")
  private Long versionNum;

  @JsonProperty("description")
  private String description;

  @JsonProperty("dataset_brief")
  private String datasetBrief;

  @JsonProperty("item_count")
  private Long itemCount;

  @JsonProperty("snapshot_status")
  private SnapshotStatusDTO snapshotStatus;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private Long createdAt;

  @JsonProperty("disabled_at")
  private Long disabledAt;
}
