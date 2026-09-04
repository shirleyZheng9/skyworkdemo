package com.iwhalecloud.bote.llm.client.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ServerSentEvent;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.function.Consumer;
import lombok.Builder;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 会话补全请求的 SSE 事件处理器
 *
 * @author bianjp
 * @since 2024-08-06
 */
@SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
@SuppressWarnings("PMD.GuardLogStatement")
public class ChatCompletionEventSourceListener extends EventSourceListener {
  private static final Logger logger = LoggerFactory.getLogger(ChatCompletionEventSourceListener.class);

  /** 请求地址 */
  private final HttpUrl url;
  /** 是否允许长度超出限制，不允许时会报错，允许时调用方需要自行检测并做相应处理 */
  private final boolean allowLengthLimit;
  /** 是否转换思考内容（将 content 中 {@code <think>} 标签内的内容转为 reasoning_content） */
  private boolean convertReasoning;
  /** 是否已发送思考内容 */
  private boolean sentReasoning = false;
  /** 片段处理器 */
  private final Consumer<ChatCompletionResponse> partialHandler;
  /** 完成回调（成功或失败时都会调用，失败时参数为异常对象，成功时为 null） */
  private final Consumer<BssException> completionHandler;
  /** 是否已结束（调用过完成回调或异常处理器才算结束） */
  private volatile boolean finished = false;
  /** 思考内容返回状态 */
  private ReasoningState reasoningState = ReasoningState.INITIAL;

  @Builder
  protected ChatCompletionEventSourceListener(HttpUrl url, boolean allowLengthLimit, @Nullable Boolean convertReasoning, Consumer<ChatCompletionResponse> partialHandler, Consumer<BssException> completionHandler) {
    this.url = url;
    this.allowLengthLimit = allowLengthLimit;
    // 默认开启思考内容转换
    this.convertReasoning = convertReasoning == null || convertReasoning;
    this.partialHandler = partialHandler;
    this.completionHandler = completionHandler;
  }

  @Override
  public void onOpen(EventSource eventSource, Response response) {
    logger.trace("SSE connection opened: url={}, status={}", url, response.code());
  }

  @Override
  public void onClosed(EventSource eventSource) {
    // MaaS 平台有时会直接关闭连接，导致 completionCallback, errorHandler 都未触发
    if (!finished) {
      completionHandler.accept(new BssException("SSE 意外关闭连接"));
    }
  }

  /**
   * 处理 SSE 事件
   */
  public void onEvent(ServerSentEvent event) {
    String data = event.data();
    if ("[DONE]".equals(data)) {
      return;
    }
    try {
      processEvent(data);
    }
    catch (BssException e) {
      logger.warn("Failed to process event: event={}, error={}", data, e.getFailMsg());
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to process event: {}", data, e);
      throw new BssException("处理 SSE 响应失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
    logger.trace("Received SSE event: {}", data);
    if ("[DONE]".equals(data) || "READY".equals(data)) {
      invokeCompletionHandler(null);
      return;
    }

    try {
      processEvent(data);
    }
    catch (BssException e) {
      logger.warn("Failed to process event: event={}, error={}", data, e.getFailMsg());
      cancelRequest(eventSource);
      invokeCompletionHandler(e);
    }
    catch (Exception e) {
      logger.error("Failed to process event: {}", data, e);
      cancelRequest(eventSource);
      invokeCompletionHandler(new BssException("处理 SSE 响应失败: " + e.getMessage(), e));
    }
  }

  /**
   * 处理 SSE 事件
   */
  private void processEvent(String data) {
    JsonNode json = JsonUtil.readTree(data);
    ChatCompletionResponse response = ModelHttpClient.convertResponse(url, json, ChatCompletionResponse.class);
    // gpt-proxy 可能会同时以多种方式返回报错，忽略 id=chatcmpl-error-xxx 的消息，以避免把错误当作正常回复。后面还会有 {"error": xxx} 消息可以识别错误
    if (Strings.CS.startsWith(response.getId(), "chatcmpl-error")) {
      logger.warn("Ignore chatcmpl-error event: {}", data);
      return;
    }
    // 校验响应成功
    response.assertSuccess(allowLengthLimit);
    // 转换思考内容
    AssistantMessage delta = response.getDelta();
    if (convertReasoning && delta != null && !delta.isEmpty()) {
      boolean sendMessage = convertReasoningContent(response, delta);
      if (sendMessage) {
        partialHandler.accept(response);
      }
    }
    else {
      // ModelProxyController 对外提供服务时，处理逻辑要和原始大模型一致，
      // 即使消息为空也要发送，缺少包含 finish_reason 的空消息会导致 DocChain 不能正确返回参考文档
      partialHandler.accept(response);
    }
  }

  /**
   * 转换思考内容
   *
   * @return 是否发送消息。对于 ChatCompletion
   */
  private boolean convertReasoningContent(ChatCompletionResponse response, AssistantMessage delta) {
    // 如果以 reasoning_content 返回了思考内容，不需要转换
    if (StringUtils.isNotEmpty(delta.getReasoningContent())) {
      convertReasoning = false;
      return true;
    }
    // 是否需要发送消息。思考内容的开始、结束标记不需要发送
    boolean sendMessage = true;
    switch (reasoningState) {
      case INITIAL:
        sendMessage = handleInitialState(response, delta);
        break;
      case REASONING_STARTED:
        sendMessage = handleReasoningStartedState(delta);
        break;
      case REASONING_ENDED:
        // 忽略思考内容和正文之间的空白字符
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
   * 转换思考内容，处理初始状态
   *
   * @return 是否要发送消息
   */
  private boolean handleInitialState(ChatCompletionResponse response, AssistantMessage delta) {
    // 如果只有空白字符，忽略消息。有现场使用的 Qwen/QwQ-32B 会在 <think> 标签前返回一个换行符，要避免影响转换思考内容
    if (StringUtils.isBlank(delta.getContent())) {
      return false;
    }
    // DocChain 会在思考内容前返回一条特殊的参考文档消息，不能影响转换思考内容
    if ("llmdoc.completion.chunk".equals(response.getObject())) {
      return true;
    }
    // 思考内容开始
    if ("<think>".equals(delta.getContent())) {
      reasoningState = ReasoningState.REASONING_STARTED;
      return false;
    }
    // 没有思考内容，停止转换
    reasoningState = ReasoningState.CONTENT_STARTED;
    convertReasoning = false;
    return true;
  }

  /**
   * 转换思考内容，处理思考内容已开始状态
   *
   * @return 是否要发送消息
   */
  private boolean handleReasoningStartedState(AssistantMessage delta) {
    // 思考结束
    if ("</think>".equals(delta.getContent())) {
      reasoningState = ReasoningState.REASONING_ENDED;
      return false;
    }
    String reasoningContent;
    if (sentReasoning) {
      reasoningContent = delta.getContent();
    }
    else {
      // 删除思考内容开头的空白字符
      reasoningContent = StringUtils.stripStart(delta.getContent(), null);
      if (StringUtils.isEmpty(reasoningContent)) {
        return false;
      }
      sentReasoning = true;
    }
    delta.setReasoningContent(reasoningContent);
    delta.setContent(null);
    return true;
  }

  /**
   * 取消请求
   */
  private void cancelRequest(EventSource eventSource) {
    try {
      eventSource.cancel();
    }
    catch (Exception e2) {
      logger.error("Failed to cancel event source", e2);
    }
  }

  @Override
  public void onFailure(EventSource eventSource, @Nullable Throwable throwable, @Nullable Response response) {
    // onEvent 中遇到异常会调用 eventSource.cancel, 导致这里触发 Socket closed 异常，可以忽略
    if (finished) {
      logger.warn("Failed to process SSE request: url={}, error={}", url, getMsg(throwable));
      return;
    }
    Integer responseCode = null;
    String responseBodyText = null;
    String errorMsg = null;
    if (response != null) {
      responseCode = response.code();
      responseBodyText = ModelHttpClient.readResponseBody(response);
      errorMsg = ModelHttpClient.extractErrorMsg(responseBodyText);
    }

    logger.error("Failed to process SSE request: url={}, status={}, response={}", url, responseCode, responseBodyText, throwable);

    BssException exception;
    if (throwable instanceof BssException) {
      exception = (BssException) throwable;
    }
    else if (throwable != null) {
      errorMsg = StringUtils.isNotEmpty(errorMsg) ? errorMsg : "调用 SSE 接口失败: status=" + responseCode + ", error=" + throwable.getMessage();
      exception = new BssException(errorMsg, throwable);
    }
    else {
      errorMsg = StringUtils.isNotEmpty(errorMsg) ? errorMsg : "调用 SSE 接口失败: status=" + responseCode;
      exception = new BssException(errorMsg);
    }

    invokeCompletionHandler(exception);
  }

  /**
   * 调用完成回调
   */
  private void invokeCompletionHandler(@Nullable BssException e) {
    // 避免重复调用
    if (finished) {
      return;
    }
    finished = true;
    try {
      completionHandler.accept(e);
    }
    catch (Exception e2) {
      logger.error("Failed to execute completion handler", e2);
    }
  }

  private String getMsg(@Nullable Throwable throwable) {
    if (throwable == null) {
      return "";
    }
    String msg = throwable.getMessage();
    if (StringUtils.isEmpty(msg)) {
      msg = ExceptionUtils.getRootCauseMessage(throwable);
    }
    return msg;
  }

  /**
   * 思考内容返回状态
   */
  private enum ReasoningState {
    /** 初始状态，尚未收到任何内容 */
    INITIAL,
    /** 思考已开始 */
    REASONING_STARTED,
    /** 思考已结束，正文尚未开始 */
    REASONING_ENDED,
    /** 正文已开始 */
    CONTENT_STARTED
  }
}
