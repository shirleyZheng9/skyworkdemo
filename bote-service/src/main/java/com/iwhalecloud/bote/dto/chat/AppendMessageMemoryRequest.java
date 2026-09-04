package com.iwhalecloud.bote.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 追加消息记忆内容请求
 *
 * @author bianjp
 * @since 2025-04-10
 */
@Getter
@Setter
@ToString
@Schema(description = "追加消息记忆内容请求")
public class AppendMessageMemoryRequest {
  @Schema(description = "旧记忆内容")
  private Object oldContent;
  @Schema(description = "追加的记忆内容")
  private Object content;
}
