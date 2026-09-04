package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取模板信息请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "获取模板信息请求")
public class GetTemplateInfoRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "模板ID")
  private String templateId;

  @Schema(description = "基础信息")
  private Base base;
}
