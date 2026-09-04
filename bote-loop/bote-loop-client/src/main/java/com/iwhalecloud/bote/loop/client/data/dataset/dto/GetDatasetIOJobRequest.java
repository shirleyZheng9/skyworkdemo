package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取数据集IO任务请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDatasetIOJobRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("job_id")
  private Long jobId;

  @JsonProperty("Base")
  private Base base;
}
