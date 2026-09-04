package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试工具调用DTO
 * 迁移对应关系: Thrift struct DebugToolCall
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugToolCallDTO {

  @Schema(description = "工具调用信息")
  private ToolCallDTO toolCall;

  @Schema(description = "模拟响应")
  private String mockResponse;

  @Schema(description = "调试跟踪键")
  private String debugTraceKey;
}
