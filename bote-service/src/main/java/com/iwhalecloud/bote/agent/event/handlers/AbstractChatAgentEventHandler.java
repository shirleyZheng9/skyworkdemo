package com.iwhalecloud.bote.agent.event.handlers;

import com.iwhalecloud.bote.agent.event.AgentEvent;
import com.iwhalecloud.bote.agent.event.ErrorAgentEvent;
import com.iwhalecloud.bote.agent.event.NonStreamResponseEvent;
import com.iwhalecloud.bote.agent.event.SseAgentEvent;
import com.iwhalecloud.bote.agent.event.StreamResponseEndEvent;
import com.iwhalecloud.bote.agent.event.StreamResponseEvent;
import com.iwhalecloud.bote.agent.event.ToolCallEndEvent;
import com.iwhalecloud.bote.agent.event.ToolCallStartEvent;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.event.ToolCallEvent;
import com.iwhalecloud.bote.common.sse.event.ToolCallResultEvent;
import com.iwhalecloud.bote.dto.chat.ReplyDTO;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 用于聊天窗口的通用智能体事件处理器抽象类
 *
 * @author bianjp
 * @since 2026-03-21
 */
public abstract class AbstractChatAgentEventHandler implements Consumer<AgentEvent> {
  /** 回复消息 ID。使用一个虚拟的消息 ID 用于关联所有回复事件，以确保前端当作一条消息渲染 */
  protected final String msgId = Long.toString(System.currentTimeMillis());
  /** 是否已开始思考内容 */
  protected final AtomicBoolean isReasoningStarted = new AtomicBoolean(false);
  /** 是否失败 */
  @Getter
  private boolean failed;
  /** 错误信息 */
  @Getter
  @Nullable
  private String errorMsg;
  /** 异常 */
  @Getter
  @Nullable
  private Exception errorThrown;
  /** 回复列表 */
  @Getter
  private final List<ReplyDTO> replies = new ArrayList<>();

  @Override
  public void accept(AgentEvent event) {
    // 如果收到了非流式回复，表示思考内容已结束，发送思考内容结束标记
    // 使用 <think> 标签而非 reasoning SSE 事件返回思考内容，以方便前端支持有多段思考内容且与正文、工具调用交叉出现的情况（可能会调用多次大模型，每次调用都会有思考内容）
    if (isReasoningStarted.get() && !(event instanceof StreamResponseEvent)) {
      sendText("</think>");
      isReasoningStarted.set(false);
    }
    if (event instanceof StreamResponseEvent(ChatCompletionResponse response)) {
      sendStreamReply(response, isReasoningStarted);
    }
    else if (event instanceof StreamResponseEndEvent(ChatCompletionResponse response)) {
      collectResponse(response);
    }
    else if (event instanceof NonStreamResponseEvent(ChatCompletionResponse response)) {
      collectResponse(response);
      sendNonStreamReply(response);
    }
    else if (event instanceof ToolCallStartEvent(ToolCallEvent toolCallEvent)) {
      sendEvent(ChatMessageType.TOOL_CALL, toolCallEvent);
    }
    else if (event instanceof ToolCallEndEvent(ToolCallResultEvent toolCallResultEvent)) {
      sendEvent(ChatMessageType.TOOL_CALL_RESULT, toolCallResultEvent);
    }
    else if (event instanceof SseAgentEvent(ChatMessageType msgType, Object msgContent)) {
      replies.add(new ReplyDTO(msgType, msgContent, null, null));
      sendEvent(msgType, msgContent);
    }
    else if (event instanceof ErrorAgentEvent(String message, Exception exception)) {
      failed = true;
      errorMsg = message;
      errorThrown = exception;
      sendError(message, exception);
    }
  }

  /**
   * 收集大模型响应
   */
  private void collectResponse(ChatCompletionResponse response) {
    if (CollectionUtils.isNotEmpty(response.getChoices())) {
      // 只处理正文，不处理思考内容
      String messageContent = response.getMessageContent();
      if (StringUtils.isNotEmpty(messageContent)) {
        replies.add(new ReplyDTO(ChatMessageType.TEXT, messageContent, null, null));
      }
    }
  }

  /**
   * 发送流式回复，只处理思考内容、文本内容
   */
  private void sendStreamReply(ChatCompletionResponse response, AtomicBoolean isReasoningStarted) {
    AssistantMessage message = response.getDelta();
    if (message == null) {
      return;
    }
    String reasoning = message.getReasoningContent();
    String content = message.getContent();
    if (StringUtils.isNotEmpty(reasoning)) {
      // 发送思考内容开始标记
      if (!isReasoningStarted.get()) {
        isReasoningStarted.set(true);
        sendText("<think>");
      }
      sendText(reasoning);
    }
    if (StringUtils.isNotEmpty(content)) {
      // 如果正文不为空，表示思考内容已结束，发送思考内容结束标记
      if (isReasoningStarted.get()) {
        isReasoningStarted.set(false);
        sendText("</think>");
      }
      sendText(content);
    }
  }

  /**
   * 发送非流式回复，只处理思考内容、文本内容
   */
  private void sendNonStreamReply(ChatCompletionResponse response) {
    if (CollectionUtils.isEmpty(response.getChoices())) {
      return;
    }
    AssistantMessage message = response.getChoices().getFirst().getMessage();
    if (message == null) {
      return;
    }
    String reasoning = message.getReasoningContent();
    String content = message.getContent();
    if (StringUtils.isNotEmpty(reasoning)) {
      sendText("<think>" + reasoning + "</think>");
    }
    if (StringUtils.isNotEmpty(content)) {
      sendText(content);
    }
  }

  /**
   * 发送文本
   */
  protected abstract void sendText(String content);

  /**
   * 发送事件
   */
  protected abstract void sendEvent(ChatMessageType type, Object data);

  /**
   * 发送错误
   */
  protected abstract void sendError(String message, @Nullable Exception exception);

}
