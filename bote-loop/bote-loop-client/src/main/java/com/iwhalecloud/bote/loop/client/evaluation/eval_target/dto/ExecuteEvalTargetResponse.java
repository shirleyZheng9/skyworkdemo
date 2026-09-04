package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetRecordDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 执行评测目标响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecuteEvalTargetResponse {

  @JsonProperty("eval_target_record")
  private EvalTargetRecordDTO evalTargetRecord;

  @JsonProperty("base_resp")
  private BaseResp baseResp;
}
