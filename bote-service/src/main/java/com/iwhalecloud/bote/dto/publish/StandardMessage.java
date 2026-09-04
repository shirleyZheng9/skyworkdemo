package com.iwhalecloud.bote.dto.publish;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 标准消息格式
 *
 * @author system
 * @since 2025-01-09
 */
@Getter
@Setter
@Schema(description = "标准消息格式")
public class StandardMessage {
  @Schema(description = "消息唯一ID")
  private String messageId;
  @Schema(description = "用户ID")
  private String userId;
  @Schema(description = "用户名称")
  private String userName;
  @Schema(description = "消息类型：text|image|voice|video|file")
  private String messageType;
  @Schema(description = "消息内容")
  private String content;
  @Schema(description = "消息时间戳")
  private Long timestamp;
  @Schema(description = "会话ID")
  private String sessionId;
  @Schema(description = "渠道类型")
  private String channelType;
  @Schema(description = "原始消息体")
  private Object rawMessage;
  @Schema(description = "群ID")
  private String groupId;
}
