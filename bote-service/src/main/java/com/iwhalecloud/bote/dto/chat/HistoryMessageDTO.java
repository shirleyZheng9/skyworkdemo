package com.iwhalecloud.bote.dto.chat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 历史消息
 *
 * <p>用于会话记忆功能查询历史消息</p>
 *
 * @author bianjp
 * @since 2025-04-10
 */
@Getter
@Setter
@ToString
public class HistoryMessageDTO {
  /** 消息类型 */
  private String msgType;
  /** 消息角色 */
  private String msgRole;
  /** 消息内容 */
  private String msgText;
  /** 记忆内容 */
  private String memoryContent;
}
