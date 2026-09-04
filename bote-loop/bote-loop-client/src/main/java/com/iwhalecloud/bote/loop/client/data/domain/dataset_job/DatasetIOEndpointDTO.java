package com.iwhalecloud.bote.loop.client.data.domain.dataset_job;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集IO端点数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetIOEndpointDTO {

  @JsonProperty("file")
  private DatasetIOFileDTO file;

  @JsonProperty("dataset")
  private DatasetIODatasetDTO dataset;
}
