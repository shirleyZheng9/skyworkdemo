package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldSchemaDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新数据集字段请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDatasetSchemaRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("fields")
  private List<FieldSchemaDTO> fields;

  @JsonProperty("Base")
  private Base base;
}
