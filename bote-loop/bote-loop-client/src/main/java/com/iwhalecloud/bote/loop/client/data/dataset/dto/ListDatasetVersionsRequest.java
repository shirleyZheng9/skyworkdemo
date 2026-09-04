package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.OrderByDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表数据集版本请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDatasetVersionsRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("version_like")
  private String versionLike;

  @JsonProperty("page_size")
  private Integer pageSize;

  @JsonProperty("page_number")
  private Integer pageNumber;

  @JsonProperty("page_token")
  private String pageToken;

  @JsonProperty("order_bys")
  private List<OrderByDTO> orderBys;

  @JsonProperty("Base")
  private Base base;
}
