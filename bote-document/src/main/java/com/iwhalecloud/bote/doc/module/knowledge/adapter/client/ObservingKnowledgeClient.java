package com.iwhalecloud.bote.doc.module.knowledge.adapter.client;

import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.function.Consumer;
import okhttp3.sse.EventSource;
import org.springframework.lang.Nullable;

/**
 * 带观察者埋点的知识库客户端包装
 *
 * @author cursor
 * @since 2026-07-23
 */
public final class ObservingKnowledgeClient implements KnowledgeClient {
  private final KnowledgeClient delegate;
  private final KnowledgeCallObserver observer;

  public ObservingKnowledgeClient(KnowledgeClient delegate, KnowledgeCallObserver observer) {
    this.delegate = delegate;
    this.observer = observer;
  }

  @Override
  public int getOrder() {
    return delegate.getOrder();
  }

  @Override
  public String getKnowledgeType() {
    return delegate.getKnowledgeType();
  }

  @Override
  public KnowledgeRecallResponse recall(KnowledgeRecallParamDTO params) {
    long startEpochMs = System.currentTimeMillis();
    KnowledgeRecallResponse response = null;
    Throwable error = null;
    try {
      response = delegate.recall(params);
      return response;
    }
    catch (Throwable e) {
      error = e;
      throw e;
    }
    finally {
      safeObserveRecall(params, response, startEpochMs, error);
    }
  }

  @Override
  public KnowledgeChatResponse chat(KnowledgeChatParamsDTO params) {
    long startEpochMs = System.currentTimeMillis();
    KnowledgeChatResponse response = null;
    Throwable error = null;
    try {
      response = delegate.chat(params);
      return response;
    }
    catch (Throwable e) {
      error = e;
      throw e;
    }
    finally {
      safeObserveChat(params, response, startEpochMs, error);
    }
  }

  @Override
  public EventSource chatStream(KnowledgeChatParamsDTO params, Consumer<SseEvent> partialHandler, Consumer<BssException> completionHandler) {
    long startEpochMs = System.currentTimeMillis();
    return delegate.chatStream(params, partialHandler, e -> {
      safeObserveChat(params, null, startEpochMs, e);
      completionHandler.accept(e);
    });
  }

  @Override
  public KnowledgeChatResponse chatStreamBlocking(KnowledgeChatParamsDTO params,
                                                  @Nullable Consumer<SseEvent> eventHandler,
                                                  @Nullable Consumer<Object> requestListener) {
    long startEpochMs = System.currentTimeMillis();
    KnowledgeChatResponse response = null;
    Throwable error = null;
    try {
      response = delegate.chatStreamBlocking(params, eventHandler, requestListener);
      return response;
    }
    catch (Throwable e) {
      error = e;
      throw e;
    }
    finally {
      safeObserveChat(params, response, startEpochMs, error);
    }
  }

  private void safeObserveRecall(KnowledgeRecallParamDTO params, @Nullable KnowledgeRecallResponse response,
                                 long startEpochMs, @Nullable Throwable error) {
    try {
      observer.onRecall(delegate.getKnowledgeType(), params, response, startEpochMs, System.currentTimeMillis(), error);
    }
    catch (Exception ignored) {
      // 埋点失败不影响主流程
    }
  }

  private void safeObserveChat(KnowledgeChatParamsDTO params, @Nullable KnowledgeChatResponse response,
                               long startEpochMs, @Nullable Throwable error) {
    try {
      observer.onChat(delegate.getKnowledgeType(), params, response, startEpochMs, System.currentTimeMillis(), error);
    }
    catch (Exception ignored) {
      // 埋点失败不影响主流程
    }
  }
}
