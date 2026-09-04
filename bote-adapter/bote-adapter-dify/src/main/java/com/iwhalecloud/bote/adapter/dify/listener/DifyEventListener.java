package com.iwhalecloud.bote.adapter.dify.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.adapter.dify.enums.DifySseEventEnum;
import com.iwhalecloud.bote.adapter.dify.util.ReasoningContentUtil;
import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ServerSentEvent;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Builder;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * Dify SSE事件监听器
 *
 * @author qian.sisheng
 * @since 2025-10-17
 */
@SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
@SuppressWarnings("PMD.GuardLogStatement")
public class DifyEventListener implements Consumer<ServerSentEvent> {
  private static final Logger logger = LoggerFactory.getLogger(DifyEventListener.class);

  /** SSE 事件处理器 */
  @Nullable
  private final Consumer<SseEvent> sseEventHandler;
  /** 片段处理器 */
  @Nullable
  private final Consumer<ChatCompletionResponse> partialHandler;
  /** 是否转换推理内容（将 content 中 {@code <think>} 标签内的内容转为 reasoning_content） */
  private boolean convertReasoning;
  /** 推理内容返回状态 */
  private ReasoningState reasoningState = ReasoningState.INITIAL;
  /** 是否已发送推理内容 */
  private boolean sentReasoning = false;
  /** 推理策略 */
  private final ThinkingStrategy thinkingStrategy;

  @Builder
  public DifyEventListener(@Nullable Consumer<SseEvent> sseEventHandler,
                           @Nullable Consumer<ChatCompletionResponse> partialHandler,
                           boolean convertReasoning,
                           @Nullable ThinkingStrategy thinkingStrategy) {
    this.sseEventHandler = sseEventHandler;
    this.partialHandler = partialHandler;
    this.convertReasoning = convertReasoning;
    this.thinkingStrategy = thinkingStrategy != null ? thinkingStrategy : ThinkingStrategy.DEFAULT;
  }

  @Override
  public void accept(ServerSentEvent event) {
    logger.trace("Received dify SSE event: id={}, type={}, data={}", event.id(), event.event(), event.data());
    Map<String, Object> result = JsonUtil.parseJson(event.data(), new TypeReference<>() {
    });
    DifySseEventEnum eventType = DifySseEventEnum.of(MapUtils.getString(result, "event"));
    if (eventType == null) {
      logger.warn("Unknown dify SSE event: id={}, type={}, data={}", event.id(), event.event(), event.data());
      return;
    }

    switch (eventType) {
      case ERROR:
        String error = StringUtils.defaultIfEmpty(MapUtils.getString(result, "message"), "未知错误");
        throw new BssException("调用 Dify 失败: " + error);
      case DifySseEventEnum.MESSAGE:
        String content = MapUtils.getString(result, "answer");
        if (StringUtils.isNotEmpty(content)) {
          handleMessageEvent(content);
        }
        break;
      case DifySseEventEnum.MESSAGE_END:
        if (partialHandler != null) {
          partialHandler.accept(ChatCompletionResponse.ofDelta(new AssistantMessage(), true));
        }
        break;
      default:
        // 忽略不需要关心的事件
        break;
    }
  }

  /**
   * 处理消息事件
   */
  private void handleMessageEvent(String content) {
    if (convertReasoning) {
      AssistantMessage delta = new AssistantMessage(content);
      // 推理内容包含在 <think>推理内容</think>正文内容 或 <details><summary>推理</summary>推理内容</details>正文内容
      boolean sendMessage = convertReasoningContent(delta);
      if (sendMessage) {
        // 只有在应该显示推理内容时才发送推理内容
        if (StringUtils.isNotEmpty(delta.getReasoningContent())) {
          // 如果是推理内容，检查是否应该显示
          if (ThinkingStrategy.shouldShowReasoning(thinkingStrategy)) {
            emitDelta(delta);
          }
        }
        else {
          // 如果是普通内容，正常发送
          emitDelta(delta);
        }
      }
    }
    else {
      if (sseEventHandler != null) {
        sseEventHandler.accept(SseEvent.ofText(content));
      }
      else if (partialHandler != null) {
        partialHandler.accept(ChatCompletionResponse.ofDelta(new AssistantMessage(content), false));
      }
    }
  }

  /**
   * 转换推理内容
   *
   * @return 是否发送消息。对于 ChatCompletion
   */
  private boolean convertReasoningContent(AssistantMessage delta) {
    // 是否需要发送消息。推理内容的开始、结束标记不需要发送
    boolean sendMessage = true;
    switch (reasoningState) {
      case INITIAL:
        sendMessage = handleInitialState(delta.getContent());
        break;
      case REASONING_STARTED:
        sendMessage = handleReasoningStartedState(delta);
        break;
      case REASONING_ENDED:
        // 忽略推理内容和正文之间的空白字符
        if (StringUtils.isBlank(delta.getContent())) {
          sendMessage = false;
        }
        else {
          delta.setContent(StringUtils.stripStart(delta.getContent(), null));
          reasoningState = ReasoningState.CONTENT_STARTED;
          convertReasoning = false;
        }
        break;
      default:
        break;
    }
    return sendMessage;
  }

  /**
   * 转换推理内容，处理初始状态
   *
   * @return 是否要发送消息
   */
  private boolean handleInitialState(String content) {
    // 查找第一个标签开始位置
    int startIdx = ReasoningContentUtil.indexOfAny(content, "<think>", "</summary>");
    if (startIdx >= 0) {
      reasoningState = ReasoningState.REASONING_STARTED;
      // 处理标签后的内容
      String tag = content.startsWith("<think>", startIdx) ? "<think>" : "</summary>";
      String afterTag = content.substring(startIdx + tag.length());
      if (StringUtils.isNotEmpty(afterTag)) {
        // 如果有内容，需要立即处理
        AssistantMessage delta = new AssistantMessage();
        delta.setReasoningContent(afterTag);
        delta.setContent(null);
        sentReasoning = true;
        // 只有在应该显示推理内容时才发送
        if (ThinkingStrategy.shouldShowReasoning(thinkingStrategy)) {
          emitDelta(delta);
        }
      }
      return false;
    }
    // 没有推理内容，停止转换
    reasoningState = ReasoningState.CONTENT_STARTED;
    convertReasoning = false;
    return true;
  }

  /**
   * 转换推理内容，处理推理内容已开始状态
   *
   * @return 是否要发送消息
   */
  private boolean handleReasoningStartedState(AssistantMessage delta) {
    String text = StringUtils.defaultString(delta.getContent());
    // 查找结束标签位置
    int endIdx = ReasoningContentUtil.indexOfAny(text, "</think>", "</details>");

    if (endIdx >= 0) {
      // 处理结束标签前的内容
      String before = text.substring(0, endIdx);
      if (!sentReasoning) {
        before = StringUtils.stripStart(before, null);
      }
      if (StringUtils.isNotEmpty(before)) {
        delta.setReasoningContent(before);
        delta.setContent(null);
        sentReasoning = true;
        // 只有在应该显示推理内容时才发送
        if (ThinkingStrategy.shouldShowReasoning(thinkingStrategy)) {
          emitDelta(delta);
        }
      }
      // 处理结束标签后的内容
      String endTage = text.startsWith("</think>", endIdx) ? "</think>" : "</details>";
      String after = text.substring(endIdx + endTage.length());
      after = StringUtils.stripStart(after, null);
      reasoningState = ReasoningState.CONTENT_STARTED;
      convertReasoning = false;
      if (StringUtils.isNotEmpty(after)) {
        AssistantMessage contentDelta = new AssistantMessage(after);
        emitDelta(contentDelta);
      }
      return false;
    }
    // 处理没有结束标签的情况
    String reasoningContent = sentReasoning ? text : StringUtils.stripStart(text, null);
    if (StringUtils.isEmpty(reasoningContent)) {
      return false;
    }
    delta.setReasoningContent(reasoningContent);
    delta.setContent(null);
    sentReasoning = true;
    // 只有在应该显示推理内容时才返回true（发送消息）
    return ThinkingStrategy.shouldShowReasoning(thinkingStrategy);
  }

  /**
   * 发送一个增量片段
   */
  private void emitDelta(AssistantMessage msg) {
    if (sseEventHandler != null) {
      String content = StringUtils.defaultString(msg.getContent());
      String reasoningContent = StringUtils.defaultString(msg.getReasoningContent());
      // 发送推理内容 - 只有在应该显示推理内容时才发送
      if (StringUtils.isNotEmpty(reasoningContent) && ThinkingStrategy.shouldShowReasoning(thinkingStrategy)) {
        sseEventHandler.accept(SseEvent.ofReasoning(reasoningContent));
      }
      // 发送普通内容
      if (StringUtils.isNotEmpty(content)) {
        sseEventHandler.accept(SseEvent.ofText(content));
      }
    }
    else if (partialHandler != null) {
      // 检查是否应该发送推理内容
      if (StringUtils.isNotEmpty(msg.getReasoningContent()) && !ThinkingStrategy.shouldShowReasoning(thinkingStrategy)) {
        // 如果不应该显示推理内容，创建一个不包含推理内容的副本
        partialHandler.accept(ChatCompletionResponse.ofDelta(new AssistantMessage(msg.getContent()), false));
      }
      else {
        partialHandler.accept(ChatCompletionResponse.ofDelta(msg, false));
      }
    }
  }

  /**
   * 推理内容返回状态
   */
  private enum ReasoningState {
    /** 初始状态，尚未收到任何内容 */
    INITIAL,
    /** 推理已开始 */
    REASONING_STARTED,
    /** 推理已结束，正文尚未开始 */
    REASONING_ENDED,
    /** 正文已开始 */
    CONTENT_STARTED
  }
}
