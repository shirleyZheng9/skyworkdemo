package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建数据集版本请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDatasetVersionRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("version")
  private String version;

  @JsonProperty("desc")
  private String desc;

  @JsonProperty("Base")
  private Base base;
}
