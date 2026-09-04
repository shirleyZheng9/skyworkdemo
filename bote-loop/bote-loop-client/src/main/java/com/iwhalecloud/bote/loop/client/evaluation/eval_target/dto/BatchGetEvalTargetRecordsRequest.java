package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取评测目标记录请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetEvalTargetRecordsRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("eval_target_record_ids")
  private List<Long> evalTargetRecordIds;

  @JsonProperty("base")
  private Base base;
}
