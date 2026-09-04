package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 清空数据集数据响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClearDatasetItemResponse {

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
