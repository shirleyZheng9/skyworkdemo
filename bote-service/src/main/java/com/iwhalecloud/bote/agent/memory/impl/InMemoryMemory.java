package com.iwhalecloud.bote.agent.memory.impl;

import com.iwhalecloud.bote.agent.memory.ShortTermMemory;
import com.iwhalecloud.bote.dto.agent.MessageMetadata;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import java.util.ArrayList;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 基于内存的短期记忆实现
 *
 * @author bianjp
 * @since 2026-03-09
 */
public class InMemoryMemory implements ShortTermMemory {
  /** 消息列表 */
  private final List<Message> messages = new ArrayList<>();

  @Override
  public List<Message> getMessages() {
    return messages;
  }

  @Override
  public List<ToolCall> getToolCalls() {
    return messages.stream()
      .filter(m -> m instanceof AssistantMessage assistantMessage && assistantMessage.hasToolCall())
      .map(m -> ((AssistantMessage) m).getToolCall())
      .toList();
  }

  @Override
  public void addMessage(Message message, @Nullable MessageMetadata metadata) {
    messages.add(message);
  }

  @Override
  public void compressIfNecessary() {
    // 内存实现暂不支持压缩
  }
}
