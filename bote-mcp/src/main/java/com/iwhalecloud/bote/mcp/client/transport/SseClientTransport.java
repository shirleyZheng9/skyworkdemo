package com.iwhalecloud.bote.mcp.client.transport;

import com.iwhalecloud.bote.common.util.SysParamUtil;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bote.llm.client.util.OkHttpUtil;
import com.iwhalecloud.bote.mcp.consts.McpConsts;
import com.iwhalecloud.bote.mcp.consts.McpTransportStatus;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcMessage;
import com.iwhalecloud.bote.mcp.util.McpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.EOFException;
import java.net.SocketTimeoutException;
import java.util.function.Function;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;

/**
 * MCP 的 sse 传输协议
 *
 * @author bianjp
 * @since 2025-05-22
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class SseClientTransport implements McpClientTransport {
  private static final Logger logger = LoggerFactory.getLogger(SseClientTransport.class);

  /** 客户端标识，用于日志中区分不同客户端 */
  private final String clientId;
  /** SSE 接口地址 */
  private final HttpUrl url;
  /** SSE 端点 */
  private final String endpoint;
  /** 请求头 */
  private final Headers headers;
  /** 发送消息请求地址 */
  private HttpUrl messageEndpoint;
  /** 连接状态 */
  private volatile McpTransportStatus status = McpTransportStatus.UNSET;
  /** SSE 连接，需要一直保持连接状态，用于接收服务器响应 */
  private EventSource connectEventSource;

  public SseClientTransport(String clientId, String url, @Nullable String endpoint, @Nullable Headers headers) {
    this.clientId = clientId;
    this.url = HttpUrl.parse(url);
    this.endpoint = endpoint;
    this.headers = headers != null ? headers : Headers.of();
    Assert.notNull(this.url, () -> "SSE 地址不合法: " + url);
  }

  @Override
  public Mono<Void> connect(Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>> handler, Runnable disconnectionHandler) {
    return Mono.create(sink -> {
      if (status == McpTransportStatus.CLOSED) {
        sink.error(new BssException("已关闭，请勿连接"));
        return;
      }
      status = McpTransportStatus.CONNECTING;
      Request request = new Request.Builder().get().url(url).headers(SysParamUtil.buildHeaders(headers)).header("Accept", "text/event-stream").build();
      EventSourceListener eventSourceListener = new EventSourceListener() {
        @Override
        public void onOpen(EventSource eventSource, Response response) {
          logger.debug("SSE connection opened: client={}, url={}", clientId, url);
        }

        @Override
        public void onClosed(EventSource eventSource) {
          logger.debug("SSE connection closed: client={}, url={}", clientId, url);
          // 如果状态为连接中、已连接，则设置为连接失败，以便能自动重连
          if (status == McpTransportStatus.CONNECTING || status == McpTransportStatus.CONNECTED) {
            status = McpTransportStatus.CONNECT_FAILED;
          }
        }

        @Override
        @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
        public void onFailure(EventSource eventSource, @Nullable Throwable throwable, @Nullable Response response) {
          handleSseFailure(throwable, response, disconnectionHandler, sink);
        }

        @Override
        @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
        public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
          handleSseEvent(id, type, data, sink, handler);
        }
      };
      logger.debug("Creating SSE connection: client={}, url={}", clientId, url);
      connectEventSource = McpUtil.getEventSourceFactory().newEventSource(request, eventSourceListener);
    });
  }

  /**
   * 处理 SSE 事件
   */
  private void handleSseEvent(@Nullable String id, @Nullable String type, String data, MonoSink<Void> sink, Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>> handler) {
    logger.trace("Received SSE event: client={}, id={}, type={}, data={}", clientId, id, type, data);
    if (status == McpTransportStatus.CLOSED) {
      return;
    }
    try {
      if (McpConsts.EVENT_TYPE_ENDPOINT.equals(type)) {
        messageEndpoint = resolveMessageEndpoint(data);
        if (messageEndpoint == null) {
          logger.error("Invalid message endpoint: client={}, url={}, messageEndpoint={}", clientId, url, data);
          sink.error(new BssException("message endpoint 不合法: " + data));
        }
        else {
          logger.trace("Received SSE message endpoint: client={}, data={}, endpoint={}", clientId, data, messageEndpoint);
          status = McpTransportStatus.CONNECTED;
          sink.success();
        }
      }
      else if (StringUtils.isEmpty(type) || McpConsts.EVENT_TYPE_MESSAGE.equals(type)) {
        JsonRpcMessage message = McpUtil.deserializeMessage(data);
        handler.apply(Mono.just(message)).subscribe();
      }
      else {
        logger.warn("Unknown SSE event: client={}, id={}, type={}, data={}", clientId, id, type, data);
      }
    }
    catch (Exception e) {
      logger.error("Failed to process SSE event: client={}, id={}, type={}, data={}", clientId, id, type, data, e);
      sink.error(e);
    }
  }

  /**
   * 处理 SSE 请求异常
   */
  private void handleSseFailure(@Nullable Throwable throwable, @Nullable Response response, Runnable disconnectionHandler, MonoSink<Void> sink) {
    if (status == McpTransportStatus.CLOSED) {
      return;
    }
    // 成功连接后，对于部分异常自动重连。异常种类根据经验不断完善
    if (status == McpTransportStatus.CONNECTED && (throwable instanceof SocketTimeoutException || throwable instanceof EOFException)) {
      status = McpTransportStatus.CONNECT_FAILED;
      logger.error("SSE connection error, will try reconnecting: client={}, url={}", clientId, url, throwable);
      disconnectionHandler.run();
      return;
    }
    status = McpTransportStatus.CONNECT_FAILED;
    if (throwable != null) {
      logger.error("SSE connection error: client={}, url={}", clientId, url, throwable);
      sink.error(throwable);
    }
    else if (response != null) {
      String error = StringUtils.defaultString(ModelHttpClient.readResponseBody(response));
      logger.error("SSE connection error: client={}, url={}, status={}, body={}", clientId, url, response.code(), error);
      sink.error(new BssException("SSE 请求失败: status=" + response.code() + ", error=" + error));
    }
    else {
      logger.error("SSE connection error: client={}, url={}", clientId, url);
      sink.error(new BssException("SSE 请求失败"));
    }
  }

  @Override
  public boolean isConnected() {
    return status == McpTransportStatus.CONNECTED;
  }

  @Override
  public Mono<Void> sendMessage(JsonRpcMessage message) {
    // 正在关闭时直接报错
    if (status == McpTransportStatus.CLOSED) {
      return Mono.error(new BssException("SSE 客户端已关闭，不能发送消息"));
    }
    try {
      if (messageEndpoint == null) {
        return Mono.error(new BssException("没有可用的 message endpoint"));
      }
      return doSendMessage(message);
    }
    catch (Exception e) {
      return Mono.error(new BssException("发送 MCP SSE 消息失败: " + e.getMessage(), e));
    }
  }

  /**
   * 发送消息
   */
  private Mono<Void> doSendMessage(JsonRpcMessage message) {
    String jsonText = JsonUtil.toJsonString(message);
    Request request = new Request.Builder()
      .url(messageEndpoint)
      .headers(SysParamUtil.buildHeaders(headers))
      .post(OkHttpUtil.jsonBody(jsonText))
      .build();

    return Mono.<Void>fromRunnable(() -> {
      logger.trace("Sending SSE message: client={}, url={}, message={}", clientId, messageEndpoint, jsonText);
      // 通过普通的 HTTP 请求发送消息，服务器只会返回 202 Accepted，不会返回消息的处理结果，处理结果会通过 SSE 连接返回
      try (Response response = ModelHttpClient.getClient().newCall(request).execute()) {
        int statusCode = response.code();
        if (statusCode != 200 && statusCode != 201 && statusCode != 202 && statusCode != 206) {
          String error = StringUtils.defaultString(ModelHttpClient.readResponseBody(response));
          logger.error("Failed to send SSE message: client={}, url={}, message={}, status={}, body={}", clientId, request.url(), jsonText, statusCode, error);
          throw new BssException("发送 MCP SSE 消息失败: status=" + statusCode + ", error=" + error);
        }
      }
      catch (BssException e) {
        logger.error("Failed to send SSE message: client={}, url={}, message={}, error={}", clientId, request.url(), jsonText, e.getMessage());
        throw e;
      }
      catch (Exception e) {
        logger.error("Failed to send SSE message: client={}, url={}, message={}", clientId, request.url(), jsonText, e);
        throw new BssException("发送 MCP SSE 消息失败: " + e.getMessage(), e);
      }
    }).subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<Void> closeGracefully() {
    status = McpTransportStatus.CLOSED;
    return Mono.fromRunnable(() -> {
      if (connectEventSource != null) {
        connectEventSource.cancel();
      }
    });
  }

  /**
   * 解析消息端点 URL
   * <p>SSE 端点没有固定的生成规范，不适合硬编码处理。允许用户指定，有以下几种案例 <p/>
   * <ul>
   *   <li>url=https://mcp.bochaai.com/sse  endpoint=/ </li>
   *   <li>url=https://mcp.api-inference.modelscope.net/59693c17262f40/sse  endpoint=/ </li>
   *   <li>url=http://10.43.62.40:19100/mcpServer/fj/kdywxt/sse  endpoint=mcpServer/fj/kdywxt </li>
   * </ul>
   */
  @Nullable
  private HttpUrl resolveMessageEndpoint(String data) {
    if (StringUtils.isEmpty(endpoint)) {
      return url.resolve(data);
    }

    // 处理 endpoint：去除首尾斜杠，去除末尾的 /sse
    String normalizedEndpoint = StringUtils.stripEnd(endpoint, "/");
    if (normalizedEndpoint.endsWith("/sse")) {
      normalizedEndpoint = normalizedEndpoint.substring(0, normalizedEndpoint.length() - 4);
    }
    normalizedEndpoint = StringUtils.stripEnd(normalizedEndpoint, "/");

    // 处理 data：分离路径和查询参数
    String normalizedData = StringUtils.stripStart(data, "/");
    String pathPart = normalizedData;
    String queryPart = null;

    // 如果 data 包含 ?，将其分离为路径和查询参数
    int queryIndex = normalizedData.indexOf('?');
    if (queryIndex >= 0) {
      pathPart = normalizedData.substring(0, queryIndex);
      queryPart = normalizedData.substring(queryIndex + 1);
    }

    // 构建路径：使用 encodedPath 设置已编码的路径
    HttpUrl.Builder builder = url.newBuilder();
    if (StringUtils.isNotEmpty(normalizedEndpoint)) {
      builder.encodedPath("/" + normalizedEndpoint + "/" + pathPart);
    }
    else {
      builder.encodedPath("/" + pathPart);
    }

    // 设置查询参数
    if (StringUtils.isNotEmpty(queryPart)) {
      builder.encodedQuery(queryPart);
    }

    return builder.build();
  }
}
