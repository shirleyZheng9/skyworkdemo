package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建数据集版本响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDatasetVersionResponse {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
