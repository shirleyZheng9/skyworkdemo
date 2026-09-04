package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取默认提示词评测器工具请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "获取默认提示词评测器工具请求")
public class GetDefaultPromptEvaluatorToolsRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "基础信息")
  private Base base;
}
