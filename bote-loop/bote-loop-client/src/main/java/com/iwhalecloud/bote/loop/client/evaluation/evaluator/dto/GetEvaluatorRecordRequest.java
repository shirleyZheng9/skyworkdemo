package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.base.Base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取评估器记录请求
 * 对应Go: evaluator.GetEvaluatorRecordRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetEvaluatorRecordRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID int64
   */
  @JsonProperty("workspace_id")
  private Long workspaceId;

  /**
   * 评估器记录ID
   * 对应Go: EvaluatorRecordID int64
   */
  @JsonProperty("evaluator_record_id")
  private Long evaluatorRecordId;

  /**
   * 是否查询已删除的，默认不查询
   * 对应Go: IncludeDeleted *bool
   */
  @JsonProperty("include_deleted")
  private Boolean includeDeleted;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @JsonProperty("Base")
  private Base base;
}
