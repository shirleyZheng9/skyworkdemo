package com.iwhalecloud.bote.entity.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话消息附件 Entity
 *
 * @author auto
 * @since 2024-10-28
 */
@Getter
@Setter
@ToString
@Schema(description = "会话消息附件")
@Table(name = "bt_bot_session_msg_file")
public class SessionMsgFileEntity {
  @Schema(description = "主键")
  private Long relaId;
  @Schema(description = "消息 ID")
  private Long msgId;
  @Schema(description = "会话 ID")
  private Long sessionId;
  @Schema(description = "文件 ID")
  private Long fileId;
}
