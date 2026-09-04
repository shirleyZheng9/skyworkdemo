package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取数据集版本请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDatasetVersionRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("version_id")
  private Long versionId;

  @JsonProperty("with_deleted")
  private Boolean withDeleted;

  @JsonProperty("Base")
  private Base base;
}
