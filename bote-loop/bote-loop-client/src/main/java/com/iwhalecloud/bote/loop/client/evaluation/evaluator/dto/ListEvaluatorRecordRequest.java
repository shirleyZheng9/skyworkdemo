package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.OrderByDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表评测器记录请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluatorRecordRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("evaluator_id")
  private Long evaluatorId;

  @JsonProperty("page_size")
  private Integer pageSize;

  @JsonProperty("page_number")
  private Integer pageNumber;

  @JsonProperty("order_bys")
  private List<OrderByDTO> orderBys;

  @JsonProperty("Base")
  private Base base;
}
