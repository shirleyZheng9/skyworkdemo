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
 * 列表数据集版本数据请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDatasetItemsByVersionRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("version_id")
  private Long versionId;

  @JsonProperty("page_size")
  private Integer pageSize;

  @JsonProperty("page_number")
  private Integer pageNumber;

  @JsonProperty("order_bys")
  private List<OrderByDTO> orderBys;

  @JsonProperty("Base")
  private Base base;
}
