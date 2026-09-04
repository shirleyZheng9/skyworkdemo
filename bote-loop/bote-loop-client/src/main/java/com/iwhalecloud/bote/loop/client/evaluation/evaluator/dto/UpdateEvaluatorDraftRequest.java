package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorContentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新评估器草稿请求
 * 对应Go: evaluator.UpdateEvaluatorDraftRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "更新评估器草稿请求")
public class UpdateEvaluatorDraftRequest {

  /**
   * 评估器ID
   * 对应Go: EvaluatorID int64
   */
  @Schema(description = "评估器ID")
  private Long evaluatorId;

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 评估器内容
   * 对应Go: EvaluatorContent *evaluator.EvaluatorContent
   */
  @Schema(description = "评估器内容")
  private EvaluatorContentDTO evaluatorContent;

  /**
   * 评估器类型
   * 对应Go: EvaluatorType evaluator.EvaluatorType
   */
  @Schema(description = "评估器类型")
  private EvaluatorTypeDTO evaluatorType;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @Schema(description = "基础信息")
  private Base base;
}
