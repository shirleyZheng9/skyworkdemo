package com.iwhalecloud.bote.agent.event.handlers;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.orchestration.reply.ReplyHandler;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.ChatReplyHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.lang.Nullable;

/**
 * 用于聊天窗口的通用智能体事件处理器 - SSE 协议
 *
 * @author bianjp
 * @since 2026-03-19
 */
public class SseChatAgentEventHandler extends AbstractChatAgentEventHandler {
  /** 回复处理器 */
  private final ReplyHandler replyHandler;

  public SseChatAgentEventHandler(ChatContext context) {
    this.replyHandler = new ChatReplyHandler(context);
  }

  public SseChatAgentEventHandler(ReplyHandler replyHandler) {
    this.replyHandler = replyHandler;
  }

  @Override
  protected void sendText(String content) {
    replyHandler.sendStreamText(ChatMessageType.TEXT, msgId, content);
  }

  @Override
  protected void sendEvent(ChatMessageType type, Object data) {
    replyHandler.sendMessage(type, msgId, data);
  }

  @Override
  protected void sendError(String message, @Nullable Exception exception) {
    replyHandler.sendStreamText(ChatMessageType.ERROR, msgId, message);
    if (exception != null) {
      replyHandler.sendStreamText(ChatMessageType.EXCEPTION, msgId, ExceptionUtils.getStackTrace(exception));
    }
  }
}
