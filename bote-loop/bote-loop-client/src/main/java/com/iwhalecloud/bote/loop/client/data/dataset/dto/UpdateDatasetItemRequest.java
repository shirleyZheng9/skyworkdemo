package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldDataDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemDataDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新数据集数据请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDatasetItemRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("item_id")
  private Long itemId;

  @JsonProperty("data")
  private List<FieldDataDTO> data;

  @JsonProperty("repeated_data")
  private List<ItemDataDTO> repeatedData;

  @JsonProperty("Base")
  private Base base;
}
