package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 根据源目标批量获取评测目标响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetEvalTargetsBySourceResponse {

  @JsonProperty("eval_targets")
  private List<EvalTargetDTO> evalTargets;

  @JsonProperty("base_resp")
  private BaseResp baseResp;
}
