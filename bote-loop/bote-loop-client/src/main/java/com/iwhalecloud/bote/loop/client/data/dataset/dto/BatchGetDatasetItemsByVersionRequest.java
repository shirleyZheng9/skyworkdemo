package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取数据集版本数据请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetDatasetItemsByVersionRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("version_id")
  private Long versionId;

  @JsonProperty("item_ids")
  private List<Long> itemIds;

  @JsonProperty("Base")
  private Base base;
}
