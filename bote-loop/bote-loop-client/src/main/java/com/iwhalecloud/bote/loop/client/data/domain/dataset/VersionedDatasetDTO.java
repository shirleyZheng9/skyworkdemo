package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionedDatasetDTO {

  @JsonProperty("version")
  private DatasetVersionDTO version;
  @JsonProperty("dataset")
  private DatasetDTO dataset;

}
