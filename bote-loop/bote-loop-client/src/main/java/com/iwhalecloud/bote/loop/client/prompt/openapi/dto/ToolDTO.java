package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具DTO
 * 对应Thrift: Tool
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "工具DTO")
public class ToolDTO {

  /**
   * 工具类型
   * 对应Thrift字段: type
   */
  @Schema(description = "工具类型")
  private ToolTypeDTO type;

  /**
   * 函数定义
   * 对应Thrift字段: function
   */
  @Schema(description = "函数定义")
  private FunctionDTO function;
}
