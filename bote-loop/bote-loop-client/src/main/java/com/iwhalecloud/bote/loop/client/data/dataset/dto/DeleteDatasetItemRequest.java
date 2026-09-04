package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除数据集数据请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteDatasetItemRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("item_id")
  private Long itemId;

  @JsonProperty("Base")
  private Base base;
}
