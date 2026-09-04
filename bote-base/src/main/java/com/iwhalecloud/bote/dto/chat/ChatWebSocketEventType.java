package com.iwhalecloud.bote.dto.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * WebSocket 对话接口的事件类型
 *
 * @author bianjp
 * @since 2026-03-23
 */
@Getter
@RequiredArgsConstructor
public enum ChatWebSocketEventType {
  /** 对话请求 */
  CHAT_REQUEST("chat.request"),
  /** 对话响应 */
  CHAT_RESPONSE("chat.response"),
  /** 取消对话 */
  CHAT_CANCEL("chat.cancel"),
  /** 工具调用请求 */
  TOOL_REQUEST("tool.request"),
  /** 工具调用响应 */
  TOOL_RESPONSE("tool.response"),
  /** 错误 */
  ERROR("error");

  /** 事件类型代码 */
  private final String code;

  /**
   * 根据事件类型代码获取事件类型
   *
   */
  @Nullable
  public static ChatWebSocketEventType of(String code) {
    for (ChatWebSocketEventType type : values()) {
      if (type.getCode().equals(code)) {
        return type;
      }
    }
    return null;
  }
}
