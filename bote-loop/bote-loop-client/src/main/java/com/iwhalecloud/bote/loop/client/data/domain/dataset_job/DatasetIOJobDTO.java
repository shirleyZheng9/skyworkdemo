package com.iwhalecloud.bote.loop.client.data.domain.dataset_job;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorGroupDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集IO任务数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetIOJobDTO {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("job_type")
  private JobTypeDTO jobType;

  @JsonProperty("source")
  private DatasetIOEndpointDTO source;

  @JsonProperty("target")
  private DatasetIOEndpointDTO target;

  @JsonProperty("field_mappings")
  private List<FieldMappingDTO> fieldMappings;

  @JsonProperty("option")
  private DatasetIOJobOptionDTO option;

  @JsonProperty("status")
  private JobStatusDTO status;

  @JsonProperty("progress")
  private DatasetIOJobProgressDTO progress;

  @JsonProperty("errors")
  private List<ItemErrorGroupDTO> errors;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private Long createdAt;

  @JsonProperty("updated_by")
  private String updatedBy;

  @JsonProperty("updated_at")
  private Long updatedAt;

  @JsonProperty("started_at")
  private Long startedAt;

  @JsonProperty("ended_at")
  private Long endedAt;
}
