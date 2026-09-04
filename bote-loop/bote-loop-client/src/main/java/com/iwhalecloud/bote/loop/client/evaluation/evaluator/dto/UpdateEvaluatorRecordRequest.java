package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.CorrectionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新评估器记录请求
 * 对应Go: evaluator.UpdateEvaluatorRecordRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "更新评估器记录请求")
public class UpdateEvaluatorRecordRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 评估器记录ID
   * 对应Go: EvaluatorRecordID int64
   */
  @Schema(description = "评估器记录ID")
  private Long evaluatorRecordId;

  /**
   * 修正信息
   * 对应Go: Correction *evaluator.Correction
   */
  @Schema(description = "修正信息")
  private CorrectionDTO correction;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @Schema(description = "基础信息")
  private Base base;
}
