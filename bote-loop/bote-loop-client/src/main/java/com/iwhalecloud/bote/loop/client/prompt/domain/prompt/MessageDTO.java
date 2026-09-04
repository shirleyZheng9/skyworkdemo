package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息DTO
 * 迁移对应关系: Thrift struct Message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "消息DTO")
public class MessageDTO {

  @Schema(description = "角色")
  private RoleDTO role;

  @Schema(description = "推理内容")
  private String reasoningContent;

  @Schema(description = "内容")
  private String content;

  @Schema(description = "内容部分列表")
  private List<ContentPartDTO> parts;

  @Schema(description = "工具调用ID")
  private String toolCallId;

  @Schema(description = "工具调用列表")
  private List<ToolCallDTO> toolCalls;

  public MessageDTO(String content, RoleDTO role) {
    this.role = role;
    this.content = content;
  }
}
