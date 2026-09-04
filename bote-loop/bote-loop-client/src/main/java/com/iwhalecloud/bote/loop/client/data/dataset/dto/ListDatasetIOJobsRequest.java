package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.JobStatusDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.JobTypeDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表数据集IO任务请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDatasetIOJobsRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("types")
  private List<JobTypeDTO> types;

  @JsonProperty("statuses")
  private List<JobStatusDTO> statuses;

  @JsonProperty("Base")
  private Base base;
}
