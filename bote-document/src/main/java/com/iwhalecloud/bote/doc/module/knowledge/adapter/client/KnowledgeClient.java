package com.iwhalecloud.bote.doc.module.knowledge.adapter.client;

import com.iwhalecloud.bote.common.sse.event.ReferencesSseEvent;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.sse.event.TextSseEvent;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import okhttp3.sse.EventSource;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;

/**
 * 知识库客户端
 *
 * <p>用于适配不同知识库类型，只适配基本的召回、问答功能</p>
 *
 * <p>使用 Ordered 控制知识库类型列表的显示顺序</p>
 *
 * @author bianjp
 * @since 2025-05-13
 */
public interface KnowledgeClient extends Ordered {
  /**
   * 获取知识库类型
   *
   * @return 知识库类型
   */
  String getKnowledgeType();

  /**
   * 知识召回
   *
   * @param params 召回参数
   * @return 召回结果
   */
  KnowledgeRecallResponse recall(KnowledgeRecallParamDTO params);

  /**
   * 知识问答
   *
   * @param params 问答参数
   * @return 问答结果
   */
  KnowledgeChatResponse chat(KnowledgeChatParamsDTO params);

  /**
   * 知识问答，流式输出
   *
   * @param params 问答参数
   * @param partialHandler 片段处理器
   * @param completionHandler 完成回调，参数为异常对象（成功时为 null）
   * @return EventSource 实例，可用于中断请求
   */
  EventSource chatStream(KnowledgeChatParamsDTO params, Consumer<SseEvent> partialHandler, Consumer<BssException> completionHandler);

  /**
   * 知识问答，流式输出 - 同步阻塞模式
   *
   * <p>默认实现通过 CountDownLatch 阻塞当前线程，子类如果有更高效的方式可以覆盖此方法。</p>
   *
   * @param params 问答参数
   * @param eventHandler 事件处理器
   * @return 问答结果
   */
  default KnowledgeChatResponse chatStreamBlocking(KnowledgeChatParamsDTO params,
                                                   @Nullable Consumer<SseEvent> eventHandler,
                                                   @Nullable Consumer<Object> requestListener) {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    AtomicReference<BssException> exceptionHolder = new AtomicReference<>();
    StringBuilder content = new StringBuilder();
    AtomicReference<List<ReferenceDocumentDTO>> references = new AtomicReference<>();
    Consumer<SseEvent> finalHandler = event -> {
      if (event instanceof TextSseEvent textSseEvent) {
        content.append(textSseEvent.getText());
      }
      else if (event instanceof ReferencesSseEvent referencesSseEvent) {
        references.set(referencesSseEvent.getReferences());
      }
    };
    if (eventHandler != null) {
      finalHandler = finalHandler.andThen(eventHandler);
    }
    EventSource eventSource = chatStream(params, finalHandler, e -> {
      exceptionHolder.set(e);
      countDownLatch.countDown();
    });
    if (requestListener != null) {
      requestListener.accept(eventSource);
    }
    try {
      boolean success = countDownLatch.await(ModelHttpClient.getClient().callTimeoutMillis(), TimeUnit.MILLISECONDS);
      if (!success) {
        throw new BssException("调用知识问答接口超时");
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BssException("调用知识问答接口中断", e);
    }

    if (exceptionHolder.get() != null) {
      throw exceptionHolder.get();
    }

    KnowledgeChatResponse response = new KnowledgeChatResponse();
    response.setAnswer(content.toString());
    response.setReferences(references.get());
    return response;
  }
}
