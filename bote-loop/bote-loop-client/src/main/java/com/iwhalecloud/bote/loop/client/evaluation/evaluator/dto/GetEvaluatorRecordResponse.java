package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorRecordDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取评估器记录响应
 * 对应Go: evaluator.GetEvaluatorRecordResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetEvaluatorRecordResponse {

  /**
   * 评估器记录
   * 对应Go: Record *evaluator.EvaluatorRecord
   */
  @JsonProperty("record")
  private EvaluatorRecordDTO record;

  /**
   * 基础响应信息
   * 对应Go: BaseResp *base.BaseResp
   */
  @JsonProperty("BaseResp")
  private BaseResp baseResp;
}
