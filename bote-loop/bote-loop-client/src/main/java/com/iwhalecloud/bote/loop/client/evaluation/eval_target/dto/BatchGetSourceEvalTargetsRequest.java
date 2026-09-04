package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetTypeDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取源评测目标请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetSourceEvalTargetsRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("source_target_ids")
  private List<String> sourceTargetIds;

  @JsonProperty("target_type")
  private EvalTargetTypeDTO targetType;

  @JsonProperty("base")
  private Base base;
}
