package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取数据集响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDatasetResponse {

  @JsonProperty("dataset")
  private DatasetDTO dataset;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
