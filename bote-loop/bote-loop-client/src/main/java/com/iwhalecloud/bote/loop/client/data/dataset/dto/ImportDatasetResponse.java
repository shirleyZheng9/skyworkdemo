package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入数据集响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportDatasetResponse {

  @JsonProperty("job_id")
  private Long jobId;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
