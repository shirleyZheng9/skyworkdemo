package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetItemDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表数据集版本数据响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDatasetItemsByVersionResponse {

  @JsonProperty("items")
  private List<DatasetItemDTO> items;

  @JsonProperty("total")
  private Long total;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
