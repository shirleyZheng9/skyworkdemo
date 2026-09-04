package com.iwhalecloud.bote.common.sse.event;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

/**
 * 知识库对话日志
 *
 * @author bianjp
 * @since 2025-06-10
 */
@Getter
public class KnowledgeChatLogSseEvent extends SseEvent {
  /** 对话日志 ID */
  private final String chatLogId;

  public KnowledgeChatLogSseEvent(String chatLogId) {
    this.chatLogId = chatLogId;
  }

  @Override
  public ChatMessageType getMsgType() {
    return ChatMessageType.KNOWLEDGE_CHAT_LOG;
  }

  @Override
  public Object getMsgContent() {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("chatLogId", chatLogId);
    return data;
  }
}
