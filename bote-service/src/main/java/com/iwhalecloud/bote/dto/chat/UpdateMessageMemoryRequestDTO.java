package com.iwhalecloud.bote.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 更新消息记忆内容请求
 *
 * @author bianjp
 * @since 2025-04-10
 */
@Getter
@Setter
@ToString
@Schema(description = "更新消息记忆内容请求")
public class UpdateMessageMemoryRequestDTO {
  @Schema(description = "消息 ID")
  private Long msgId;
  @Schema(description = "是否追加，默认否")
  private Boolean append;
  @Schema(description = "记忆内容")
  private Object content;
}
