package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIOFileDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIOJobOptionDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.FieldMappingDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入数据集请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportDatasetRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("file")
  private DatasetIOFileDTO file;

  @JsonProperty("field_mappings")
  private List<FieldMappingDTO> fieldMappings;

  @JsonProperty("option")
  private DatasetIOJobOptionDTO option;

  @JsonProperty("Base")
  private Base base;
}
