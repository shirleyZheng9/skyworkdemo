package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取数据集字段请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDatasetSchemaRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("with_deleted")
  private Boolean withDeleted;

  @JsonProperty("Base")
  private Base base;
}
