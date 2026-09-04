package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取评估器版本请求
 * 对应Go: evaluator.BatchGetEvaluatorVersionsRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取评估器版本请求")
public class BatchGetEvaluatorVersionsRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 评估器版本ID列表
   * 对应Go: EvaluatorVersionIds []int64
   */
  @Schema(description = "评估器版本ID列表")
  private List<Long> evaluatorVersionIds;

  /**
   * 是否查询已删除的评估器，默认不查询
   * 对应Go: IncludeDeleted *bool
   */
  @Schema(description = "是否查询已删除的评估器")
  private Boolean includeDeleted;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @Schema(description = "基础信息")
  private Base base;
}
