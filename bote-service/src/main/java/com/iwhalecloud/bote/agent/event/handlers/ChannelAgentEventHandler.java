package com.iwhalecloud.bote.agent.event.handlers;

import com.iwhalecloud.bote.agent.event.AgentEvent;
import com.iwhalecloud.bote.agent.event.ErrorAgentEvent;
import com.iwhalecloud.bote.agent.event.NonStreamResponseEvent;
import com.iwhalecloud.bote.agent.event.StreamResponseEndEvent;
import com.iwhalecloud.bote.dto.publish.StandardMessage;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.service.publish.platform.PlatformAdapter;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;

/**
 * 用于渠道的通用智能体事件处理器
 *
 * @author bianjp
 * @since 2026-03-19
 */
public final class ChannelAgentEventHandler implements Consumer<AgentEvent> {
  /** 平台适配器 */
  private final PlatformAdapter adapter;
  /** 源消息（用于确定回复发给谁） */
  private final StandardMessage sourceMessage;

  public ChannelAgentEventHandler(PlatformAdapter adapter, StandardMessage sourceMessage) {
    this.adapter = adapter;
    this.sourceMessage = sourceMessage;
  }

  @Override
  public void accept(AgentEvent event) {
    if (event instanceof StreamResponseEndEvent(ChatCompletionResponse response)) {
      sendResponse(response);
    }
    else if (event instanceof NonStreamResponseEvent(ChatCompletionResponse response)) {
      sendResponse(response);
    }
    else if (event instanceof ErrorAgentEvent errorAgentEvent) {
      adapter.replyMessage(sourceMessage, buildReply(errorAgentEvent.message()), false);
    }
  }

  /**
   * 发送大模型回复
   */
  private void sendResponse(ChatCompletionResponse response) {
    AssistantMessage message = response.getMessage();
    if (message == null) {
      return;
    }
    String reasoning = message.getReasoningContent();
    String content = message.getContent();
    if (StringUtils.isNotEmpty(reasoning)) {
      adapter.replyMessage(sourceMessage, buildReply(reasoning), false);
    }
    if (StringUtils.isNotEmpty(content)) {
      adapter.replyMessage(sourceMessage, buildReply(content), false);
    }
  }

  /**
   * 构造回复消息
   */
  private StandardMessage buildReply(String text) {
    StandardMessage message = new StandardMessage();
    message.setMessageType("text");
    message.setContent(text);
    message.setTimestamp(System.currentTimeMillis());
    return message;
  }


}
