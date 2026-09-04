package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetRecordDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取评测目标记录响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetEvalTargetRecordsResponse {

  @JsonProperty("eval_target_records")
  private List<EvalTargetRecordDTO> evalTargetRecords;

  @JsonProperty("base_resp")
  private BaseResp baseResp;
}
