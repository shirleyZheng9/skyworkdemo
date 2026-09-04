package com.iwhalecloud.bote.agent.event.handlers;

import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.dto.chat.ChatWebSocketEventType;
import com.iwhalecloud.bote.websocket.context.WebSocketChatContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jspecify.annotations.Nullable;

/**
 * 用于聊天窗口的通用智能体事件处理器 - WebSocket 协议
 *
 * @author bianjp
 * @since 2026-03-23
 */
public class WebSocketAgentEventHandler extends AbstractChatAgentEventHandler {
  private final WebSocketChatContext context;

  public WebSocketAgentEventHandler(WebSocketChatContext context) {
    this.context = context;
  }

  @Override
  protected void sendText(String content) {
    ChatResponsePayload payload = new ChatResponsePayload(msgId, ChatMessageType.TEXT, content);
    context.sendMessage(ChatWebSocketEventType.CHAT_RESPONSE.getCode(), payload);
  }

  @Override
  protected void sendEvent(ChatMessageType type, Object data) {
    ChatResponsePayload payload = new ChatResponsePayload(msgId, type, data);
    context.sendMessage(ChatWebSocketEventType.CHAT_RESPONSE.getCode(), payload);
  }

  @Override
  protected void sendError(String message, @Nullable Exception exception) {
    ChatResponsePayload payload = new ChatResponsePayload(msgId, ChatMessageType.ERROR, message);
    context.sendMessage(ChatWebSocketEventType.CHAT_RESPONSE.getCode(), payload);
    if (exception != null) {
      ChatResponsePayload exceptionPayload = new ChatResponsePayload(msgId, ChatMessageType.EXCEPTION, ExceptionUtils.getStackTrace(exception));
      context.sendMessage(ChatWebSocketEventType.CHAT_RESPONSE.getCode(), exceptionPayload);
    }
  }

  /**
   * 结束单次会话
   */
  public void complete() {
    ChatResponsePayload payload = new ChatResponsePayload(msgId, ChatMessageType.DONE, ChatConsts.COMPLETIONS_DONE);
    context.sendMessage(ChatWebSocketEventType.CHAT_RESPONSE.getCode(), payload);
  }

  /**
   * 会话响应事件的数据
   *
   * @param id 事件 ID
   * @param event 事件类型
   * @param data 事件数据
   */
  public record ChatResponsePayload(String id, ChatMessageType event, Object data) {
  }
}
