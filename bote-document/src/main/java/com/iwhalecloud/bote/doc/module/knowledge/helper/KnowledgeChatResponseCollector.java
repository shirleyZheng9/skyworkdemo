package com.iwhalecloud.bote.doc.module.knowledge.helper;

import com.iwhalecloud.bote.common.sse.event.KnowledgeChatLogSseEvent;
import com.iwhalecloud.bote.common.sse.event.QuestionsSseEvent;
import com.iwhalecloud.bote.common.sse.event.ReasoningSseEvent;
import com.iwhalecloud.bote.common.sse.event.ReferencesSseEvent;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.sse.event.TextSseEvent;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.lang.Nullable;

/**
 * 知识问答响应收集器
 *
 * @author bianjp
 * @since 2025-12-22
 */
public class KnowledgeChatResponseCollector implements Consumer<SseEvent> {
  /** 委托事件处理器 */
  @Nullable
  private final Consumer<SseEvent> delegate;
  /** 对话日志 ID */
  @Nullable
  private String chatLogId;
  /** 推理内容 */
  private final StringBuilder reasoning = new StringBuilder();
  /** 回复文本 */
  private final StringBuilder content = new StringBuilder();
  /** 参考文档列表 */
  @Nullable
  private List<ReferenceDocumentDTO> references;
  /** 相关问题列表。部分知识库类型支持返回相关问题，比如百应知识库 */
  @Nullable
  private List<String> questions;

  public KnowledgeChatResponseCollector(@Nullable Consumer<SseEvent> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void accept(SseEvent event) {
    switch (event) {
      case TextSseEvent textSseEvent -> content.append(textSseEvent.getText());
      case ReasoningSseEvent reasoningSseEvent -> reasoning.append(reasoningSseEvent.getText());
      case KnowledgeChatLogSseEvent chatLogSseEvent -> chatLogId = chatLogSseEvent.getChatLogId();
      case ReferencesSseEvent referencesSseEvent -> references = referencesSseEvent.getReferences();
      case QuestionsSseEvent questionsSseEvent -> questions = questionsSseEvent.getQuestions();
      default -> {
        // 忽略不需要关注的事件
      }
    }

    if (delegate != null) {
      delegate.accept(event);
    }
  }

  /**
   * 获取响应
   */
  public KnowledgeChatResponse getResponse() {
    return KnowledgeChatResponse.builder()
      .chatLogId(chatLogId)
      .reasoning(reasoning.isEmpty() ? null : reasoning.toString())
      .answer(content.toString())
      .references(references)
      .questions(questions)
      .build();
  }
}
