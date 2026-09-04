package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetItemDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量创建数据集数据请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchCreateDatasetItemsRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("items")
  private List<DatasetItemDTO> items;

  @JsonProperty("skip_invalid_items")
  private Boolean skipInvalidItems;

  @JsonProperty("allow_partial_add")
  private Boolean allowPartialAdd;

  @JsonProperty("Base")
  private Base base;
}
