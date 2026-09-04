package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试消息DTO
 * 迁移对应关系: Thrift struct DebugMessage
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugMessageDTO {

  @Schema(description = "角色信息")
  private RoleDTO role;

  @Schema(description = "消息内容")
  private String content;

  @Schema(description = "推理内容")
  private String reasoningContent;

  @Schema(description = "内容部分列表")
  private List<ContentPartDTO> parts;

  @Schema(description = "工具调用ID")
  private String toolCallId;

  @Schema(description = "工具调用列表")
  private List<DebugToolCallDTO> toolCalls;

  @Schema(description = "调试ID")
  private String debugId;

  @Schema(description = "输入令牌数")
  private Long inputTokens;

  @Schema(description = "输出令牌数")
  private Long outputTokens;

  @Schema(description = "耗时（毫秒）")
  private Long costMs;
}
