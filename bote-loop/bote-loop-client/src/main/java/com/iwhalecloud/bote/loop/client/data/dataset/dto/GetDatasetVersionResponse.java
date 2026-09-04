package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetVersionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取数据集版本响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDatasetVersionResponse {

  @JsonProperty("version")
  private DatasetVersionDTO version;
  @JsonProperty("dataset")
  private DatasetDTO dataset;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
