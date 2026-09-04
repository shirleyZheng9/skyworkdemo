package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新评估器请求
 * 对应Go: evaluator.UpdateEvaluatorRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "更新评估器请求")
public class UpdateEvaluatorRequest {

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
   * 评估器类型
   * 对应Go: EvaluatorType evaluator.EvaluatorType
   */
  @Schema(description = "评估器类型")
  private EvaluatorTypeDTO evaluatorType;

  /**
   * 展示用名称
   * 对应Go: Name *string
   */
  @Schema(description = "展示用名称")
  private String name;

  /**
   * 描述
   * 对应Go: Description *string
   */
  @Schema(description = "描述")
  private String description;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @Schema(description = "基础信息")
  private Base base;

  @Schema(description = "目录 ID")
  private Long catalogItemId;
}
