package com.iwhalecloud.bote.llm.client;

import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bote.llm.client.util.ModelResponseUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import okhttp3.sse.EventSource;
import org.springframework.lang.Nullable;

/**
 * 大语言模型客户端
 *
 * <p>用于适配不同的大模型，以 OpenAI 的接口协议为准，其它大模型如有不同在内部实现中适配为 OpenAI 的接口协议。</p>
 *
 * @author bianjp
 * @since 2024-12-23
 */
public interface LlmClient extends ModelClient {

  /**
   * 获取模型配置信息
   *
   * @return 模型配置信息
   */
  @Nullable
  ModelConfigInfoDTO getModelConfigInfo();

  /**
   * 获取上下文长度
   *
   * @return 上下文长度。非正数表示不限制
   */
  int contextLength();

  /**
   * 是否支持视觉
   *
   * @return 是否支持视觉
   */
  boolean supportsVision();

  /**
   * 是否支持函数调用
   *
   * @return 是否支持函数调用
   */
  default boolean supportsFunctionCall() {
    return false;
  }

  /**
   * 是否支持流式函数调用
   *
   * @return 是否支持流式函数调用
   */
  default boolean supportsStreamingFunctionCall() {
    return false;
  }

  /**
   * 会话补全
   *
   * @param request 会话补全请求
   * @return 会话补全响应
   */
  default ChatCompletionResponse chatCompletion(ChatCompletionRequest request) {
    return chatCompletion(request, null);
  }

  /**
   * 会话补全
   *
   * @param request 会话补全请求
   * @param requestListener 请求监听器，用于调用方存储 Call 对象以实现中断请求
   * @return 会话补全响应
   */
  ChatCompletionResponse chatCompletion(ChatCompletionRequest request, @Nullable Consumer<Object> requestListener);

  /**
   * 流式输出的会话补全
   *
   * <p>不支持工具调用。部分大模型的流式输出不兼容工具调用，比如通义千问。</p>
   *
   * @param request 会话补全请求
   * @param partialHandler 片段处理器
   * @param completionHandler 完成回调（成功或失败时都会调用，失败时参数为异常对象，成功时为 null）
   */
  EventSource chatCompletionStream(ChatCompletionRequest request, Consumer<ChatCompletionResponse> partialHandler, Consumer<BssException> completionHandler);

  /**
   * 流式输出的会话补全 - 同步阻塞模式，收集完整响应
   *
   * <p>默认实现通过 CountDownLatch 阻塞当前线程，子类如果有更高效的方式可以覆盖此方法。</p>
   *
   * @param request 会话补全请求
   * @param requestListener 请求监听器，用于调用方存储 Call 对象以实现中断请求
   * @return 非流式响应（汇总流式响应的所有内容）
   */
  default ChatCompletionResponse chatCompletionStreamBlockingAndCollect(ChatCompletionRequest request,
                                                                        @Nullable Consumer<ChatCompletionResponse> eventHandler,
                                                                        @Nullable Consumer<Object> requestListener) {
    List<ChatCompletionResponse> partialResponses = new ArrayList<>();
    Consumer<ChatCompletionResponse> handler = eventHandler == null ? partialResponses::add : eventHandler.andThen(partialResponses::add);
    chatCompletionStreamBlocking(request, handler, requestListener);
    ChatCompletionResponse response = ModelResponseUtil.mergeStreamResponses(partialResponses);
    ModelResponseUtil.fixParallelToolCalls(response);
    return response;
  }

  /**
   * 流式输出的会话补全 - 同步阻塞模式
   *
   * <p>默认实现通过 CountDownLatch 阻塞当前线程，子类如果有更高效的方式可以覆盖此方法。</p>
   *
   * @param request 会话补全请求
   * @param eventHandler 事件处理器
   * @param requestListener 请求监听器，用于调用方存储 Call 对象以实现中断请求
   */
  default void chatCompletionStreamBlocking(ChatCompletionRequest request, Consumer<ChatCompletionResponse> eventHandler,
                                            @Nullable Consumer<Object> requestListener) {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    AtomicReference<BssException> exceptionHolder = new AtomicReference<>();
    EventSource eventSource = chatCompletionStream(request, eventHandler, e -> {
      exceptionHolder.set(e);
      countDownLatch.countDown();
    });
    if (requestListener != null) {
      requestListener.accept(eventSource);
    }
    try {
      boolean success = countDownLatch.await(ModelHttpClient.getClient().callTimeoutMillis(), TimeUnit.MILLISECONDS);
      if (!success) {
        throw new BssException("调用大模型接口超时");
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BssException("调用大模型接口中断", e);
    }

    if (exceptionHolder.get() != null) {
      throw exceptionHolder.get();
    }
  }

}
