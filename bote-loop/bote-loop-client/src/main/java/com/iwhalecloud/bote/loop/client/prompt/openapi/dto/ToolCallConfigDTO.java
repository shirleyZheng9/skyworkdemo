package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用配置DTO
 * 对应Thrift: ToolCallConfig
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "工具调用配置DTO")
public class ToolCallConfigDTO {

  /**
   * 工具选择类型
   * 对应Thrift字段: tool_choice
   */
  @Schema(description = "工具选择类型")
  private ToolChoiceTypeDTO toolChoice;
}
