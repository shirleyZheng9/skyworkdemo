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
 * 批量获取评估器记录响应
 * 对应Go: evaluator.BatchGetEvaluatorRecordsResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetEvaluatorRecordsResponse {

  /**
   * 评估器记录列表
   * 对应Go: Records []*evaluator.EvaluatorRecord
   */
  @JsonProperty("records")
  private List<EvaluatorRecordDTO> records;

  /**
   * 基础响应信息
   * 对应Go: BaseResp *base.BaseResp
   */
  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
