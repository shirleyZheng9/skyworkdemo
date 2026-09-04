package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用DTO
 * 迁移对应关系: Thrift struct ToolCall
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "工具调用DTO")
public class ToolCallDTO {

  @Schema(description = "索引")
  private Long index;

  @Schema(description = "ID")
  private String id;

  @Schema(description = "工具类型")
  private ToolTypeDTO type;

  @Schema(description = "函数调用")
  private FunctionCallDTO functionCall;
}
