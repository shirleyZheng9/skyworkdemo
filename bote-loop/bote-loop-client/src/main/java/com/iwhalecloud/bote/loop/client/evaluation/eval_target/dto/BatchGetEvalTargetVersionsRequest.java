package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取评测目标版本请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetEvalTargetVersionsRequest {

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("eval_target_version_ids")
  private List<Long> evalTargetVersionIds;

  @JsonProperty("need_source_info")
  private Boolean needSourceInfo;

  @JsonProperty("base")
  private Base base;
}
