package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取数据集数据响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDatasetItemResponse {

  @JsonProperty("item")
  private DatasetItemDTO item;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
