package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具DTO
 * 迁移对应关系: Thrift struct Tool
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "工具DTO")
public class ToolDTO {

  @Schema(description = "工具类型")
  private ToolTypeDTO type;

  @Schema(description = "函数信息")
  private FunctionDTO function;
}
