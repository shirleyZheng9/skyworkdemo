package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取数据集请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetDatasetsRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("dataset_ids")
  private List<Long> datasetIds;

  @JsonProperty("with_deleted")
  private Boolean withDeleted;

  @JsonProperty("Base")
  private Base base;
}
