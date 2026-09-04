package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorGroupDTO;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量创建数据集数据响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchCreateDatasetItemsResponse {

  @JsonProperty("added_items")
  private Map<Long, Long> addedItems;

  @JsonProperty("errors")
  private List<ItemErrorGroupDTO> errors;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
