package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新实验请求
 * 对应Go: expt.UpdateExperimentRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "更新实验请求")
public class UpdateExperimentRequest {

  /**
   * 工作空间ID
   * 对应Go: WorkspaceID int64
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 实验ID
   * 对应Go: ExptID int64
   */
  @Schema(description = "实验ID")
  private Long exptId;

  /**
   * 名称
   * 对应Go: Name *string
   */
  @Schema(description = "实验名称")
  private String name;

  /**
   * 描述
   * 对应Go: Desc *string
   */
  @Schema(description = "实验描述")
  private String desc;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @Schema(description = "基础信息")
  private Base base;

  @Schema(description = "目录 ID")
  private Long catalogItemId;
}
