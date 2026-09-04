package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交评估器版本请求
 * 对应Go: evaluator.SubmitEvaluatorVersionRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "提交评估器版本请求")
public class SubmitEvaluatorVersionRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 评估器ID
   * 对应Go: EvaluatorID int64
   */
  @Schema(description = "评估器ID")
  private Long evaluatorId;

  /**
   * 版本号
   * 对应Go: Version string
   */
  @Schema(description = "版本号")
  private String version;

  /**
   * 描述
   * 对应Go: Description *string
   */
  @Schema(description = "描述")
  private String description;

  /**
   * 客户端ID
   * 对应Go: Cid *string
   */
  @Schema(description = "客户端ID")
  private String cid;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @Schema(description = "基础信息")
  private Base base;
}
