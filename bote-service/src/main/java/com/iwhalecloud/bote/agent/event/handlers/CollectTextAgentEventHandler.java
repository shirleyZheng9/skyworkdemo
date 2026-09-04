package com.iwhalecloud.bote.agent.event.handlers;

import com.iwhalecloud.bote.agent.event.AgentEvent;
import com.iwhalecloud.bote.agent.event.ErrorAgentEvent;
import com.iwhalecloud.bote.agent.event.NonStreamResponseEvent;
import com.iwhalecloud.bote.agent.event.StreamResponseEndEvent;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 收集文本回复内容的通用智能体事件处理器
 *
 * @author bianjp
 * @since 2026-03-19
 */
public class CollectTextAgentEventHandler implements Consumer<AgentEvent> {
  /** 回复片段列表，包括思考内容、正文、错误，不做区分 */
  private final List<String> replyParts = new ArrayList<>();

  @Override
  public void accept(AgentEvent event) {
    if (event instanceof StreamResponseEndEvent(ChatCompletionResponse response)) {
      processResponse(response);
    }
    else if (event instanceof NonStreamResponseEvent(ChatCompletionResponse response)) {
      processResponse(response);
    }
    else if (event instanceof ErrorAgentEvent errorAgentEvent) {
      replyParts.add(errorAgentEvent.message());
    }
  }

  /**
   * 处理大模型响应
   */
  private void processResponse(ChatCompletionResponse response) {
    if (CollectionUtils.isNotEmpty(response.getChoices())) {
      AssistantMessage message = response.getChoices().getFirst().getMessage();
      if (message != null) {
        String reasoning = message.getReasoningContent();
        String content = message.getContent();
        if (StringUtils.isNotEmpty(reasoning)) {
          replyParts.add(reasoning.trim());
        }
        if (StringUtils.isNotEmpty(content)) {
          replyParts.add(content.trim());
        }
      }
    }
  }

  /**
   * 获取文本回复内容
   */
  public String getReplyContent() {
    if (replyParts.isEmpty()) {
      return "[未生成回复]";
    }
    // Markdown 需要使用两个换行符区分不同段落
    return String.join("\n\n", replyParts);
  }
}
