package com.iwhalecloud.bote.llm.wrapper;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.util.ModelResponseUtil;
import com.iwhalecloud.bote.llm.interceptor.LlmClientInterceptor;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import okhttp3.sse.EventSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 大语言模型客户端封装器
 *
 * @author bianjp
 * @since 2025-11-10
 */
@SuppressWarnings("ClassCanBeRecord")
@RequiredArgsConstructor
public class LlmClientWrapper implements LlmClient {
  /** 流式增量校验字符阈值 */
  private static final int PARTIAL_CHECK_CHAR_THRESHOLD = SystemParameter.PARTIAL_CHECK_CHAR_THRESHOLD.getRequiredIntegerValueFromDb();

  /** 拦截器 */
  private static final LlmClientInterceptor interceptor = SpringUtil.getBeanOptional(LlmClientInterceptor.class);

  /** 代理客户端 */
  private final LlmClient delegate;

  /**
   * 包装大语言模型客户端
   */
  public static LlmClient wrap(LlmClient client) {
    // modelConfigInfo 为空表示是在模型管理页面调试模型，不需要拦截
    return interceptor != null && client.getModelConfigInfo() != null ? new LlmClientWrapper(client) : client;
  }

  @Override
  @Nullable
  public ModelConfigInfoDTO getModelConfigInfo() {
    return delegate.getModelConfigInfo();
  }

  @Override
  public String defaultModel() {
    return delegate.defaultModel();
  }

  @Override
  public int contextLength() {
    return delegate.contextLength();
  }

  @Override
  public boolean supportsVision() {
    return delegate.supportsVision();
  }

  @Override
  public boolean supportsFunctionCall() {
    return delegate.supportsFunctionCall();
  }

  @Override
  public boolean supportsStreamingFunctionCall() {
    return delegate.supportsStreamingFunctionCall();
  }

  @Override
  public ChatCompletionResponse chatCompletion(ChatCompletionRequest request, @Nullable Consumer<Object> requestListener) {
    interceptor.beforeInvoke(request);
    ChatCompletionResponse response = delegate.chatCompletion(request, requestListener);
    invokeOnSuccessInterceptor(request, response);
    return response;
  }

  @Override
  public EventSource chatCompletionStream(ChatCompletionRequest request, Consumer<ChatCompletionResponse> partialHandler, Consumer<BssException> completionHandler) {
    // 收集所有流式响应
    List<ChatCompletionResponse> partialResponses = new ArrayList<>();
    ModelConfigInfoDTO modelConfig = getModelConfigRequired();
    StringBuilder outputContent = new StringBuilder();
    StringBuilder reasoningContent = new StringBuilder();
    int[] lastCheckedLength = {0};
    AtomicBoolean blocked = new AtomicBoolean(false);
    AtomicBoolean completionSent = new AtomicBoolean(false);
    Consumer<ChatCompletionResponse> finalPartialHandler = res -> {
      if (blocked.get()) {
        return;
      }
      appendDeltaContent(res, outputContent, reasoningContent);
      if (shouldCheckPartial(outputContent, reasoningContent, lastCheckedLength[0])) {
        try {
          interceptor.onPartial(modelConfig, request, buildPartialResponse(outputContent, reasoningContent));
          lastCheckedLength[0] = outputContent.length() + reasoningContent.length();
        }
        catch (Exception e) {
          blocked.set(true);
          if (completionSent.compareAndSet(false, true)) {
            completionHandler.accept(toBssException(e));
          }
          return;
        }
      }
      partialResponses.add(res);
      partialHandler.accept(res);
    };
    Consumer<BssException> finalCompletionHandler = e -> {
      if (!completionSent.compareAndSet(false, true)) {
        return;
      }
      if (blocked.get()) {
        return;
      }
      if (e == null) {
        invokeOnSuccessInterceptor(request, partialResponses);
      }
      completionHandler.accept(e);
    };
    interceptor.beforeInvoke(request);
    return delegate.chatCompletionStream(request, finalPartialHandler, finalCompletionHandler);
  }

  @Override
  public ChatCompletionResponse chatCompletionStreamBlockingAndCollect(ChatCompletionRequest request,
                                                                       @Nullable Consumer<ChatCompletionResponse> eventHandler,
                                                                       @Nullable Consumer<Object> requestListener) {
    interceptor.beforeInvoke(request);
    ModelConfigInfoDTO modelConfig = getModelConfigRequired();
    StringBuilder outputContent = new StringBuilder();
    StringBuilder reasoningContent = new StringBuilder();
    int[] lastCheckedLength = {0};
    Consumer<ChatCompletionResponse> finalEventHandler = event -> {
      appendDeltaContent(event, outputContent, reasoningContent);
      if (shouldCheckPartial(outputContent, reasoningContent, lastCheckedLength[0])) {
        interceptor.onPartial(modelConfig, request, buildPartialResponse(outputContent, reasoningContent));
        lastCheckedLength[0] = outputContent.length() + reasoningContent.length();
      }
      if (eventHandler != null) {
        eventHandler.accept(event);
      }
    };
    ChatCompletionResponse response = delegate.chatCompletionStreamBlockingAndCollect(request, finalEventHandler, requestListener);
    invokeOnSuccessInterceptor(request, response);
    return response;
  }

  @Override
  public void chatCompletionStreamBlocking(ChatCompletionRequest request, Consumer<ChatCompletionResponse> eventHandler,
                                           @Nullable Consumer<Object> requestListener) {
    interceptor.beforeInvoke(request);
    ModelConfigInfoDTO modelConfig = getModelConfigRequired();
    List<ChatCompletionResponse> partialResponses = new ArrayList<>();
    StringBuilder outputContent = new StringBuilder();
    StringBuilder reasoningContent = new StringBuilder();
    int[] lastCheckedLength = {0};
    Consumer<ChatCompletionResponse> finalEventHandler = event -> {
      appendDeltaContent(event, outputContent, reasoningContent);
      if (shouldCheckPartial(outputContent, reasoningContent, lastCheckedLength[0])) {
        interceptor.onPartial(modelConfig, request, buildPartialResponse(outputContent, reasoningContent));
        lastCheckedLength[0] = outputContent.length() + reasoningContent.length();
      }
      eventHandler.accept(event);
      partialResponses.add(event);
    };
    delegate.chatCompletionStreamBlocking(request, finalEventHandler, requestListener);
    invokeOnSuccessInterceptor(request, partialResponses);
  }

  /**
   * 调用成功拦截器
   */
  private void invokeOnSuccessInterceptor(ChatCompletionRequest request, ChatCompletionResponse response) {
    interceptor.onSuccess(getModelConfigRequired(), request, response);
  }

  /**
   * 调用成功拦截器(流式输出)
   */
  private void invokeOnSuccessInterceptor(ChatCompletionRequest request, List<ChatCompletionResponse> partialResponses) {
    // 合并流式响应
    ChatCompletionResponse response = ModelResponseUtil.mergeStreamResponses(partialResponses);
    interceptor.onSuccess(getModelConfigRequired(), request, response);
  }

  private ModelConfigInfoDTO getModelConfigRequired() {
    ModelConfigInfoDTO modelConfig = delegate.getModelConfigInfo();
    assert modelConfig != null;
    return modelConfig;
  }

  private void appendDeltaContent(ChatCompletionResponse response, StringBuilder outputContent, StringBuilder reasoningContent) {
    String deltaContent = response.getDeltaContent();
    if (StringUtils.isNotEmpty(deltaContent)) {
      outputContent.append(deltaContent);
    }
    String deltaReasoningContent = response.getDeltaReasoningContent();
    if (StringUtils.isNotEmpty(deltaReasoningContent)) {
      reasoningContent.append(deltaReasoningContent);
    }
  }

  private ChatCompletionResponse buildPartialResponse(StringBuilder outputContent, StringBuilder reasoningContent) {
    AssistantMessage message = new AssistantMessage(outputContent.toString());
    if (!reasoningContent.isEmpty()) {
      message.setReasoningContent(reasoningContent.toString());
    }
    return ChatCompletionResponse.ofMessage(message);
  }

  private BssException toBssException(Exception e) {
    return e instanceof BssException ? (BssException) e : new BssException(e.getMessage(), e);
  }

  private boolean shouldCheckPartial(StringBuilder outputContent, StringBuilder reasoningContent, int lastCheckedLength) {
    int currentLength = outputContent.length() + reasoningContent.length();
    return currentLength - lastCheckedLength >= PARTIAL_CHECK_CHAR_THRESHOLD;
  }
}
