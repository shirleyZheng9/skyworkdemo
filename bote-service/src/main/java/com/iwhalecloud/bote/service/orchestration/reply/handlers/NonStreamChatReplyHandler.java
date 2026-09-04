package com.iwhalecloud.bote.service.orchestration.reply.handlers;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.service.orchestration.reply.AbstractReplyHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 对话接口的回复处理器（非 SSE）
 *
 * <p>参照 {@link ChatReplyHandler} 实现，用于非 SSE 方式调度执行引擎：不推送 SSE 事件，仅在内存中累积回复文本，
 * 供调用方获取后按渠道推送（如钉钉/飞书 webhook）。</p>
 *
 * @author chen.linfa
 * @since 2026-03-11
 */
public class NonStreamChatReplyHandler extends AbstractReplyHandler {

  /** 累积的文本回复内容（用于渠道推送） */
  private final StringBuilder textContent = new StringBuilder();
  /** 累积的推理内容（可选，用于日志或展示） */
  private final StringBuilder reasoningContent = new StringBuilder();

  public NonStreamChatReplyHandler(@Nullable String clientId) {
    super(null, clientId);
  }

  @Override
  public void sendStreamText(ChatMessageType type, String msgId, String partialText) {
    if (StringUtils.isEmpty(partialText)) {
      return;
    }
    if (ChatMessageType.TEXT.equals(type)) {
      textContent.append(partialText);
    }
    else if (ChatMessageType.REASONING.equals(type)) {
      reasoningContent.append(partialText);
    }
    // 不推送 SSE（emitter 为 null，父类也不会发送）
  }

  @Override
  public void sendMessage(ChatMessageType type, String msgId, Object data) {
    // 非 SSE 场景不推送 TOOL_CALL 等事件
  }

  @Override
  public AnswerDTO stream(SseInvoker invoker, @Nullable String code, @Nullable String name, @Nullable String msgId) {
    return collectStream(invoker, msgId, code, name);
  }

  /**
   * 获取累积的文本回复内容（用于渠道推送与落库）
   *
   * @return 完整回复文本，无内容时返回空字符串
   */
  public String getCollectedText() {
    return textContent.toString();
  }

  /**
   * 获取累积的推理内容（可选）
   *
   * @return 推理内容，无时返回空字符串
   */
  public String getCollectedReasoning() {
    return reasoningContent.toString();
  }
}
