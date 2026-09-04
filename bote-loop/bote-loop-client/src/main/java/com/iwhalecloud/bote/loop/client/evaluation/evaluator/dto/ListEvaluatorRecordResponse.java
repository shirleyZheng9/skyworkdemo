package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorRecordDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表评测器记录响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluatorRecordResponse {

  @JsonProperty("evaluator_records")
  private List<EvaluatorRecordDTO> evaluatorRecords;

  @JsonProperty("total")
  private Long total;

  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
