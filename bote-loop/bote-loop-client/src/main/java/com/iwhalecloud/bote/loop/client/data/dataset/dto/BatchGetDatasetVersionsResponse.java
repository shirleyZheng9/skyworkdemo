package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.VersionedDatasetDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取数据集版本响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetDatasetVersionsResponse {

  @JsonProperty("versioned_dataset")
  private List<VersionedDatasetDTO> versionedDataset;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
