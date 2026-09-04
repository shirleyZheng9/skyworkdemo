package com.iwhalecloud.bote.llm.client.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.llm.client.adapter.ChatCompletionEventSourceListener;
import com.iwhalecloud.bote.llm.client.config.ModelHttpClientProperties;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionError;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.EmbeddingResponse;
import com.iwhalecloud.bote.llm.client.dto.ServerSentEvent;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.LogUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.Getter;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.connection.RealCall;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSource.Factory;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.stream.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 调用大模型专用的 HTTP 客户端
 *
 * @author bianjp
 * @since 2024-08-01
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class ModelHttpClient {
  static final Logger logger = LoggerFactory.getLogger(ModelHttpClient.class);
  /** HTTP 客户端实例 */
  @Getter
  private static final OkHttpClient client = createOkHttpClient();
  /** SSE 工厂实例 */
  @Getter
  private static final Factory eventSourceFactory = EventSources.createFactory(client);
  /** 响应体中可能表示错误信息的属性名称 */
  private static final String[] ERROR_MSG_FIELD_NAMES = new String[]{
    // DocChain 调用失败时会返回 err
    "err",
    // gpt-proxy 有时用来返回网络错误
    "error",
    // RAG 平台会返回 message
    "message",
    // 用户消息列表为空时通义千问会返回 detail
    "detail",
  };

  private ModelHttpClient() {
  }

  /**
   * 调用会话补全接口
   *
   * @param url 接口地址
   * @param headers 请求头
   * @param data 请求头
   * @param requestListener 请求监听器，用于调用方存储 Call 对象以实现中断请求
   * @return 响应对象
   */
  public static ChatCompletionResponse post(HttpUrl url, @Nullable Headers headers, Object data, @Nullable Consumer<Object> requestListener) {
    return post(url, headers, data, requestListener, ChatCompletionResponse.class);
  }

  /**
   * 调用接口
   *
   * @param url 接口地址
   * @param headers 请求头
   * @param data 请求体
   * @param requestListener 请求监听器，用于调用方存储 Call 对象以实现中断请求
   * @param responseType 响应体类型
   * @return 响应对象
   */
  @SuppressWarnings({"PMD.AvoidRethrowingException", "unchecked"})
  public static <T> T post(HttpUrl url, @Nullable Headers headers, Object data, @Nullable Consumer<Object> requestListener, Class<T> responseType) {
    Request okHttpRequest = createRequest("POST", url, "application/json", headers, data);
    long startTime = System.currentTimeMillis();
    Call call = client.newCall(okHttpRequest);
    if (requestListener != null) {
      requestListener.accept(call);
    }
    logger.atTrace().setMessage("Request api start: url={}, method={}, headers={}, body={}")
      .addArgument(url)
      .addArgument(okHttpRequest.method())
      .addArgument(() -> okHttpRequest.headers().toMultimap())
      .addArgument(() -> JsonUtil.toJsonString(data))
      .log();
    try (Response response = call.execute()) {
      long spentTime = System.currentTimeMillis() - startTime;
      if (!response.isSuccessful()) {
        String responseBodyText = readResponseBody(response);
        String errorMsg = extractErrorMsg(responseBodyText);
        logger.error("Failed to request api: url={}, status={}, headers={}, response={}, request={}", url, response.code(), response.headers().toMultimap(), responseBodyText, JsonUtil.toJsonString(data));
        throw new BssException(errorMsg != null ? errorMsg : "调用接口失败: " + response.code());
      }
      ResponseBody body = response.body(); //NOPMD - suppressed CloseResource - 关闭 Response 时会自动关闭 body
      Assert.notNull(body, "调用接口失败，响应为空");
      MediaType contentType = body.contentType();
      Assert.notNull(contentType, "调用接口失败，响应类型为空");
      Assert.isTrue("json".equals(contentType.subtype()), () -> "调用接口失败，响应类型错误: " + contentType);

      try (InputStream inputStream = body.byteStream()) {
        JsonNode json = JsonUtil.readTree(inputStream);
        Assert.isTrue(!json.isMissingNode(), "调用接口失败，响应为空");
        T result = responseType == JsonNode.class ? (T) json : convertResponse(url, json, responseType);
        if (logger.isTraceEnabled()) {
          logResponse(url, data, result, spentTime);
        }
        return result;
      }
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      throw new BssException("调用接口失败: " + e.getMessage(), e);
    }
  }

  /**
   * 打印响应日志
   */
  private static <T> void logResponse(HttpUrl url, Object data, T result, long spentTime) {
    String actualModel;
    if (result instanceof ChatCompletionResponse) {
      actualModel = ((ChatCompletionResponse) result).getModel();
    }
    else if (result instanceof EmbeddingResponse) {
      actualModel = ((EmbeddingResponse) result).getModel();
    }
    else {
      actualModel = null;
    }
    logger.trace("Request api success: url={}, model={}, time={}ms, request={}, response={}",
      url,
      actualModel,
      spentTime,
      data instanceof RequestBody ? "" : LogUtil.limitJsonLength(data, 4096),
      LogUtil.limitJsonLength(result, 4096));
  }

  /**
   * 格式化请求体为字符串，用于日志记录
   */
  private static String formatDataForLogging(@Nullable Object data) {
    return data == null ? "" : JsonUtil.toJsonString(data);
  }

  /**
   * 调用 SSE 接口
   *
   * @param url 接口地址
   * @param data 请求体
   * @param eventSourceListener 响应事件监听器
   */
  public static EventSource postSse(HttpUrl url, Object data, EventSourceListener eventSourceListener) {
    return sse("POST", url, null, data, eventSourceListener);
  }

  /**
   * 调用 SSE 接口
   *
   * @param url 接口地址
   * @param headers 请求头
   * @param data 请求体
   * @param eventSourceListener 响应事件监听器
   */
  public static EventSource postSse(HttpUrl url, @Nullable Headers headers, Object data, EventSourceListener eventSourceListener) {
    return sse("POST", url, headers, data, eventSourceListener);
  }

  /**
   * 发送 SSE 请求
   *
   * @param method 请求方法
   * @param url 请求地址
   * @param headers 请求头
   * @param data 请求体
   * @param eventSourceListener 响应事件监听器
   */
  public static EventSource sse(String method, HttpUrl url, @Nullable Headers headers, @Nullable Object data, EventSourceListener eventSourceListener) {
    Request okHttpRequest = createRequest(method, url, "text/event-stream", headers, data);
    return eventSourceFactory.newEventSource(okHttpRequest, eventSourceListener);
  }

  /**
   * 调用大模型会话补全接口，阻塞等待请求结束，并收集完整响应
   *
   * @param url 请求地址
   * @param headers 请求头
   * @param data 请求体
   * @param requestListener 请求监听器，用于调用方存储 Call 对象以实现中断请求
   * @return 响应对象（合并流式输出结果）
   */
  public static ChatCompletionResponse chatCompletionsStreamAndCollect(HttpUrl url, @Nullable Headers headers, Object data,
                                                                       @Nullable Consumer<ChatCompletionResponse> eventHandler,
                                                                       @Nullable Consumer<? super Call> requestListener) {
    List<ChatCompletionResponse> partialResponses = new ArrayList<>();
    Consumer<ChatCompletionResponse> handler = eventHandler == null ? partialResponses::add : eventHandler.andThen(partialResponses::add);
    chatCompletionsStream(url, headers, data, handler, requestListener);
    return ModelResponseUtil.mergeStreamResponses(partialResponses);
  }

  /**
   * 调用大模型会话补全接口，阻塞等待请求结束
   *
   * @param url 请求地址
   * @param headers 请求头
   * @param data 请求体
   * @param eventHandler 事件处理器
   * @param requestListener 请求监听器，用于调用方存储 Call 对象以实现中断请求
   */
  public static void chatCompletionsStream(HttpUrl url, @Nullable Headers headers, Object data,
                                           Consumer<ChatCompletionResponse> eventHandler,
                                           @Nullable Consumer<? super Call> requestListener) {
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
      .url(url)
      .convertReasoning(true)
      .partialHandler(eventHandler)
      .build();
    sseBlocking("POST", url, headers, data, requestListener, listener::onEvent);
  }

  /**
   * 发送 SSE 请求，阻塞等待请求结束
   *
   * @param method 请求方法
   * @param url 请求地址
   * @param headers 请求头
   * @param data 请求体
   * @param requestListener 请求监听器，用于调用方存储 Call 对象以实现中断请求
   * @return SSE 事件列表
   */
  public static List<ServerSentEvent> sseBlocking(String method, HttpUrl url, @Nullable Headers headers, @Nullable Object data,
                                                  @Nullable Consumer<? super Call> requestListener) {
    List<ServerSentEvent> events = new ArrayList<>();
    sseBlocking(method, url, headers, data, requestListener, events::add);
    return events;
  }

  /**
   * 发送 SSE 请求，阻塞等待请求结束
   *
   * @param method 请求方法
   * @param url 请求地址
   * @param headers 请求头
   * @param data 请求体
   * @param requestListener 请求监听器，用于调用方存储 Call 对象以实现中断请求
   * @param eventHandler 事件处理器
   */
  public static void sseBlocking(String method, HttpUrl url, @Nullable Headers headers, @Nullable Object data,
                                 @Nullable Consumer<? super Call> requestListener,
                                 Consumer<ServerSentEvent> eventHandler) {
    Request okHttpRequest = createRequest(method, url, "text/event-stream", headers, data);
    logger.atTrace().setMessage("Request sse api start: url={}, method={}, headers={}, body={}")
      .addArgument(url)
      .addArgument(okHttpRequest.method())
      .addArgument(() -> okHttpRequest.headers().toMultimap())
      .addArgument(() -> formatDataForLogging(data))
      .log();
    Call call = client.newCall(okHttpRequest);
    if (requestListener != null) {
      requestListener.accept(call);
    }
    try (Response response = call.execute()) {
      // 参考 okhttp3.internal.sse.RealEventSource#processResponse
      if (!response.isSuccessful()) {
        String responseBodyText = readResponseBody(response);
        String errorMsg = extractErrorMsg(responseBodyText);
        logger.error("Failed to request sse api: url={}, status={}, headers={}, response={}, request={}", url, response.code(), response.headers(), responseBodyText, formatDataForLogging(data));
        throw new BssException("调用 SSE 接口失败: status=" + response.code() + ", error=" + StringUtils.defaultString(errorMsg));
      }
      ResponseBody body = response.body(); //NOPMD - suppressed CloseResource - 不需要关闭
      if (body == null) {
        logger.error("Failed to request sse api, empty body: url={}, status={}, headers={}", url, response.code(), response.headers());
        throw new BssException("调用 SSE 接口失败，响应为空: status=" + response.code());
      }
      MediaType mediaType = body.contentType();
      if (mediaType == null || !mediaType.toString().startsWith("text/event-stream")) {
        String responseBodyText = readResponseBody(response);
        String errorMsg = extractErrorMsg(responseBodyText);
        logger.error("Failed to request sse api, invalid content-type: url={}, content-type={}, body={}", url, mediaType, responseBodyText);
        throw new BssException("调用 SSE 接口失败，响应类型必须是 text/event-stream, 实际是: " + mediaType + (errorMsg == null ? "" : ", error=" + errorMsg));
      }
      if (call instanceof RealCall) {
        ((RealCall) call).timeoutEarlyExit();
      }
      ServerSentEventReader reader = new ServerSentEventReader(url, call, body.source());
      reader.read(eventHandler);
    }
    catch (BssException e) {
      cancelRequest(call);
      throw e;
    }
    catch (Exception e) {
      cancelRequest(call);
      logger.error("Failed to request sse api: url={}", url, e);
      throw new BssException("调用 SSE 接口失败: " + e.getMessage(), e);
    }
  }

  /**
   * 中断请求
   */
  private static void cancelRequest(Call call) {
    try {
      call.cancel();
    }
    catch (Exception e) {
      logger.error("Failed to cancel request: url={}", call.request().url(), e);
    }
  }

  /**
   * 构造请求对象
   */
  private static Request createRequest(String method, HttpUrl url, String accept, @Nullable Headers headers, @Nullable Object data) {
    Builder builder = new Builder().url(url);
    if (headers != null) {
      builder.headers(headers);
    }
    // 甘肃现场环境，不指定 Accept 时会返回 Content-Type: text/plain
    if (headers == null || headers.get("Accept") == null) {
      builder.header("Accept", accept);
    }
    method = method.toUpperCase();
    if ("GET".equals(method)) {
      builder.method(method, null);
    }
    else if (data instanceof RequestBody) {
      builder.method(method, (RequestBody) data);
    }
    else {
      Assert.notNull(data, "请求体不能为空");
      RequestBody requestBody = OkHttpUtil.jsonBody(data);
      builder.method(method, requestBody);
    }
    return builder.build();
  }

  /**
   * 读取响应体，忽略读取失败
   */
  @Nullable
  public static String readResponseBody(Response response) {
    try (ResponseBody body = response.body()) {
      if (body == null) {
        return null;
      }
      return body.string().trim();
    }
    catch (Exception e) {
      logger.warn("Failed to read response body: url={}", response.request().url(), e);
      return null;
    }
  }

  /**
   * 校验并转换响应对象
   *
   * <p>响应失败时需要抛异常。</p>
   */
  public static <T> T convertResponse(HttpUrl url, JsonNode json, Class<T> responseType) {
    // 检查 DocChain 接口错误
    checkDocChainApiError(json);
    // 浩鲸 MaaS 平台
    convertResponseFromMaas(url, json);
    // 标准协议
    JsonNode errorNode = json.path("error");
    if (errorNode.isObject()) {
      logger.error("Failed to request api: url={}, response={}", url, json);
      String errorMsg = extractErrorMsgFromError(errorNode);
      throw new BssException(StringUtils.defaultIfEmpty(errorMsg, "调用接口失败"));
    }
    else if (errorNode.isTextual()) {
      throw new BssException(StringUtils.defaultIfEmpty(errorNode.textValue(), "调用接口失败"));
    }
    // 浩鲸实验室可能通过 detail 返回错误信息
    JsonNode detail = json.get("detail");
    if (detail != null) {
      logger.error("Failed to request api: url={}, response={}", url, json);
      if (detail.isTextual()) {
        throw new BssException("调用接口失败: " + detail.textValue());
      }
      if (detail.isArray() && !detail.isEmpty()) {
        throw new BssException("调用接口失败: " + extractErrorMsgFromDetail(detail));
      }
    }
    // 成功
    return JsonUtil.convert(json, responseType);
  }

  /**
   * 检查 DocChain 接口错误
   */
  private static void checkDocChainApiError(JsonNode json) {
    // DocChain 对话接口
    if (json.hasNonNull("error_code")) {
      String errorCode = json.get("error_code").textValue();
      String errorMsg = json.path("err").textValue();
      throw new BssException(errorCode + ": " + errorMsg);
    }
    // DocChain 新版本通过 llmdoc_status=2 表示错误
    if ("2".equals(json.path("llmdoc_status").asText())) {
      String msg = json.path("choices").path(0).path("message").path("content").asText("");
      throw new BssException("知识问答失败: " + StringUtils.defaultString(msg));
    }
  }

  private static void convertResponseFromMaas(HttpUrl url, JsonNode json) {
    if (json.has("code")) {
      String code = json.path("code").asText();
      // 浩鲸 MaaS 平台在失败时会通过返回 code, msg 表示, code=10000 表示成功
      if (StringUtils.isEmpty(code) || "10000".equals(code)) {
        return;
      }
      String msg = json.has("msg") ? json.path("msg").textValue() : json.path("message").textValue();
      // 河南电信现场的大模型会返回 code=0, 但实际是成功的，msg 为空时当作成功以避免误判
      if (StringUtils.isNotEmpty(msg)) {
        logger.error("Failed to request api: url={}, code={}, response={}", url, code, json);
        throw new BssException("调用接口失败: " + msg);
      }
    }
  }

  /**
   * 从响应体提取错误信息
   */
  @Nullable
  public static String extractErrorMsg(@Nullable String responseBody) {
    if (StringUtils.isEmpty(responseBody) || !responseBody.startsWith("{")) {
      return null;
    }
    JsonNode json;
    try {
      json = JsonUtil.readTree(responseBody);
      // 提取文本错误信息
      for (String name : ERROR_MSG_FIELD_NAMES) {
        JsonNode node = json.get(name);
        if (node != null && node.isTextual()) {
          return node.textValue();
        }
      }

      // 从 error 对象提取错误信息
      JsonNode error = json.path("error");
      if (error.isObject()) {
        return extractErrorMsgFromError(error);
      }
      // 从 detail 数组提取错误信息
      JsonNode detail = json.path("detail");
      if (detail.isArray()) {
        return extractErrorMsgFromDetail(detail);
      }

      logger.warn("No error message found in response: {}", responseBody);
      return null;
    }
    catch (Exception e) {
      // 忽略异常
      logger.warn("Failed to extract error message from response: {}", responseBody, e);
      return null;
    }
  }

  /**
   * 从响应体的 detail 数组中提取错误信息
   */
  @Nullable
  private static String extractErrorMsgFromDetail(JsonNode detailNode) {
    JsonNode msg = detailNode.path(0).path("msg");
    JsonNode loc = detailNode.path(0).path("loc");
    if (msg.isTextual() && loc.isArray()) {
      String fieldPath = Streams.of(loc).map(i -> i.isInt() ? "[" + i.intValue() + "]" : i.textValue()).collect(Collectors.joining("."));
      return msg.textValue() + ": " + fieldPath;
    }
    return null;
  }

  /**
   * 从响应体的 error 对象中提取错误信息
   */
  @Nullable
  public static String extractErrorMsgFromError(JsonNode errorNode) {
    try {
      ChatCompletionError error = JsonUtil.convert(errorNode, ChatCompletionError.class);
      // 浩鲸 gpt-proxy 调用大模型报错时会包装一层，error.message 才是大模型返回的原始报错
      if ("api_forward_request_error".equals(error.getCode()) && Strings.CS.startsWith(error.getMessage(), "{") && error.getMessage().contains("\"message\":")) {
        // 忽略解析异常
        ChatCompletionError nestedError = JsonUtil.parseJson(error.getMessage(), ChatCompletionError.class);
        if (nestedError != null && StringUtils.isNoneEmpty(nestedError.getCode(), nestedError.getMessage())) {
          error = nestedError;
        }
      }
      String message;
      if ("invalid_parameter_error".equals(error.getCode()) || "invalid_function_parameters".equals(error.getCode())) {
        if (StringUtils.isEmpty(error.getParam())) {
          message = "调用接口参数错误: " + error.getMessage();
        }
        else {
          message = "调用接口参数错误: param=" + error.getParam() + ", error=" + error.getMessage();
        }
      }
      else {
        message = "调用接口失败: " + error.getMessage();
      }
      return message;
    }
    catch (Exception e) {
      // 忽略异常
    }
    return null;
  }

  /**
   * 构造 okhttp 客户端实例
   */
  private static OkHttpClient createOkHttpClient() {
    ModelHttpClientProperties properties = SpringUtil.getBean(ModelHttpClientProperties.class, ModelHttpClientProperties::new);

    // 调大连接数限制。大模型接口的 host 基本都是同一个，且耗时较长，默认的 maxRequestsPerHost=5 完全不够用，会导致请求阻塞等待
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.setMaxRequests(200);
    dispatcher.setMaxRequestsPerHost(200);

    // 配置超时时间、连接池
    OkHttpClient.Builder builder = new OkHttpClient.Builder()
      .callTimeout(properties.getCallTimeout(), TimeUnit.SECONDS)
      .connectTimeout(properties.getConnectTimeout(), TimeUnit.SECONDS)
      .readTimeout(properties.getReadTimeout(), TimeUnit.SECONDS)
      .writeTimeout(properties.getWriteTimeout(), TimeUnit.SECONDS)
      .dispatcher(dispatcher)
      .connectionPool(new ConnectionPool(properties.getMaxIdleConnections(), properties.getKeepAliveTimeout(), TimeUnit.SECONDS));

    // 关闭 SSL 校验
    if (properties.isUnsafeSsl()) {
      try {
        X509TrustManager trustManager = new DisableValidationTrustManager();
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{trustManager}, null);
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        builder.sslSocketFactory(sslSocketFactory, trustManager);
        builder.hostnameVerifier(new NoopHostnameVerifier());
      }
      catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }

    // 添加请求头
    builder.addInterceptor(chain -> {
      Builder requestBuilder = chain.request().newBuilder();
      requestBuilder.addHeader("User-Agent", "bote-service");
      return chain.proceed(requestBuilder.build());
    });

    return builder.build();
  }

  /**
   * 关闭校验的信任管理器
   */
  @SuppressFBWarnings("WEAK_TRUST_MANAGER")
  private static final class DisableValidationTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
      // 不做校验
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
      // 不做校验
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  }

  /**
   * 关闭校验的主机名校验器
   */
  @SuppressFBWarnings("WEAK_HOSTNAME_VERIFIER")
  private static final class NoopHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(final String s, final SSLSession sslSession) {
      return true;
    }
  }

  /**
   * 关闭应用时关闭 HTTP 客户端
   */
  @Component
  public static class ModelHttpClientShutdownHook implements ApplicationListener<ContextClosedEvent> {
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
      //noinspection resource
      client.dispatcher().executorService().shutdown();
      client.connectionPool().evictAll();
    }
  }

}
