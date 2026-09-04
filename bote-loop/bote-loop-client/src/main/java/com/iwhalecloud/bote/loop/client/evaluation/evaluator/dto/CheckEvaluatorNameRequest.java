package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检查评估器名称请求
 * 对应Go: evaluator.CheckEvaluatorNameRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "检查评估器名称请求")
public class CheckEvaluatorNameRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 名称
   * 对应Go: Name string
   */
  @Schema(description = "名称")
  private String name;

  /**
   * 评估器ID
   * 对应Go: EvaluatorID *int64
   */
  @Schema(description = "评估器ID")
  private Long evaluatorId;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @Schema(description = "基础信息")
  private Base base;
}
