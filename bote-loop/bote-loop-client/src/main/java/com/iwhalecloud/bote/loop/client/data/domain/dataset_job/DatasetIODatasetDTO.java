package com.iwhalecloud.bote.loop.client.data.domain.dataset_job;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集IO数据集数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetIODatasetDTO {

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("version_id")
  private Long versionId;
}
