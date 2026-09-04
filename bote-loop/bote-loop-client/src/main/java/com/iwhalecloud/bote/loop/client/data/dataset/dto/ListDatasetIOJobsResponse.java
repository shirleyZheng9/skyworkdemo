package com.iwhalecloud.bote.loop.client.data.dataset.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIOJobDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表数据集IO任务响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDatasetIOJobsResponse {

  @JsonProperty("jobs")
  private List<DatasetIOJobDTO> jobs;

  @JsonProperty("total")
  private Long total;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
