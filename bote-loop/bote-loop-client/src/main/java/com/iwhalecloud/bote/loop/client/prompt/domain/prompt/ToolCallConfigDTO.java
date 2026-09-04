package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用配置DTO
 * 迁移对应关系: Thrift struct ToolCallConfig
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "工具调用配置DTO")
public class ToolCallConfigDTO {

  @Schema(description = "工具选择类型")
  private ToolChoiceTypeDTO toolChoice;
}
