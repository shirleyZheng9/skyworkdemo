package com.iwhalecloud.bote.loop.client.data.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetCategoryDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.OrderByDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表数据集请求DTO
 * 迁移对应关系: Go语言ListDatasetsRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDatasetsRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("dataset_ids")
  private List<Long> datasetIds;

  @JsonProperty("name")
  private String name;

  @JsonProperty("created_bys")
  private List<String> createdBys;

  @JsonProperty("category")
  private DatasetCategoryDTO category;

  @JsonProperty("biz_categorys")
  private List<String> bizCategorys;
  private String catalogItemId;

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
