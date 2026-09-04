package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dataset Schema DTO
 * 对应Go: DatasetSchema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetSchemaDTO {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("fields")
  private List<FieldSchemaDTO> fields;

  @JsonProperty("immutable")
  private Boolean immutable;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private Long createdAt;

  @JsonProperty("updated_by")
  private String updatedBy;

  @JsonProperty("updated_at")
  private Long updatedAt;

  @JsonProperty("update_version")
  private Long updateVersion;
}
