package com.iwhalecloud.bote.mcp.client.transport;

import com.iwhalecloud.bote.common.util.SysParamUtil;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bote.llm.client.util.OkHttpUtil;
import com.iwhalecloud.bote.mcp.consts.McpConsts;
import com.iwhalecloud.bote.mcp.consts.McpErrorCodes;
import com.iwhalecloud.bote.mcp.consts.McpTransportStatus;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcMessage;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcResponse;
import com.iwhalecloud.bote.mcp.util.McpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.connection.RealCall;
import okhttp3.internal.sse.ServerSentEventReader;
import okhttp3.internal.sse.ServerSentEventReader.Callback;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

/**
 * MCP 的 streamable HTTP 传输协议
 *
 * @author bianjp
 * @since 2025-05-27
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class StreamableClientTransport implements McpClientTransport {
  private static final Logger logger = LoggerFactory.getLogger(StreamableClientTransport.class);
  /** 请求头: MCP 会话 ID */
  @SuppressWarnings("UastIncorrectHttpHeaderInspection")
  private static final String HEADER_MCP_SESSION_ID = "Mcp-Session-Id";
  /** 请求头: 上一个事件 ID */
  @SuppressWarnings("UastIncorrectHttpHeaderInspection")
  private static final String HEADER_LAST_EVENT_ID = "Last-Event-ID";
  /** 请求头: MCP 协议版本 */
  @SuppressWarnings("UastIncorrectHttpHeaderInspection")
  private static final String HEADER_PROTOCOL_VERSION = "MCP-Protocol-Version";
  /** Accept 请求头的值，需要同时支持 json 和 sse */
  private static final String DEFAULT_ACCEPT_HEADER = "application/json,text/event-stream";

  /** 客户端标识，用于日志中区分不同客户端 */
  private final String clientId;
  /** 接口地址 */
  private final HttpUrl url;
  /** 请求头 */
  private final Headers headers;
  /** 上一个事件 ID */
  private final AtomicReference<String> lastEventId = new AtomicReference<>();
  /** 会话 ID */
  private final AtomicReference<String> mcpSessionId = new AtomicReference<>();
  /** 连接状态 */
  private volatile McpTransportStatus status = McpTransportStatus.UNSET;
  /** 事件处理器 */
  private Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>> handler;
  /** SSE 连接，需要一直保持连接状态，用于接收服务器主动发送的通知、请求 */
  private EventSource sseEventSource;
  /** 服务器是否支持 SSE 连接。服务器不支持时标记下，避免不断重试 */
  private boolean supportsSseConnection = true;

  public StreamableClientTransport(String clientId, String url, @Nullable Headers headers) {
    this.clientId = clientId;
    this.url = HttpUrl.parse(url);
    this.headers = headers != null ? headers : Headers.of();
    Assert.notNull(this.url, () -> "MCP 服务地址不合法: " + url);
  }

  @Override
  public Mono<Void> connect(Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>> handler, Runnable disconnectionHandler) {
    this.handler = handler;
    this.status = McpTransportStatus.CONNECTED;
    return Mono.empty();
  }

  @Override
  public boolean isConnected() {
    return status == McpTransportStatus.CONNECTED;
  }

  @Override
  public Mono<Void> closeGracefully() {
    status = McpTransportStatus.CLOSED;
    closeSseConnection();
    deleteSession();
    return Mono.empty();
  }

  @Override
  public Mono<Void> sendMessage(JsonRpcMessage message) {
    // 已关闭时直接报错
    if (status == McpTransportStatus.CLOSED) {
      return Mono.error(new BssException("streamable 客户端已关闭，不能发送消息"));
    }
    return Mono.create((sink) -> {
      try {
        doSendMessage(message, sink);
      }
      catch (Exception e) {
        sink.error(e);
      }
    });
  }

  /**
   * 发送消息
   */
  private void doSendMessage(JsonRpcMessage message, MonoSink<Void> sink) throws IOException {
    String sessionId = mcpSessionId.get();
    String jsonText = JsonUtil.toJsonString(message);
    Request request = buildRequest(sessionId, lastEventId.get(), jsonText).build();
    logger.trace("Sending streamable message: client={}, sessionId={}, url={}, message={}", clientId, sessionId, url, jsonText);
    Call call = ModelHttpClient.getClient().newCall(request);
    try (Response response = call.execute()) {
      processSendMessageResponse(sink, call, response, sessionId, jsonText);
    }
  }

  /**
   * 处理发送消息的响应
   */
  private void processSendMessageResponse(MonoSink<Void> sink, Call call, Response response, @Nullable String sessionId, String jsonText) {
    int statusCode = response.code();
    // 服务器失效 session 时会返回 404，需要重新发送初始化请求获取新的 session
    if (sessionId != null && statusCode == 404) {
      logger.trace("Streamable session expired: client={}, sessionId={}", clientId, sessionId);
      mcpSessionId.set(null);
      lastEventId.set(null);
      closeSseConnection();
      status = McpTransportStatus.CONNECT_FAILED;
      sink.error(new BssException(McpErrorCodes.SESSION_EXPIRED, "MCP session 已失效，请重新连接"));
      return;
    }
    // 请求失败
    if (!response.isSuccessful()) {
      String error = StringUtils.defaultString(ModelHttpClient.readResponseBody(response));
      logger.error("Failed to send streamable message: client={}, message={}, status={}, body={}", clientId, jsonText, statusCode, error);
      sink.error(new BssException("发送 MCP streamable 消息失败: status=" + statusCode + ", error=" + error));
      return;
    }
    // 202 状态码没有响应体（比如发送初始化完成通知）
    if (statusCode == 202) {
      sink.success();
      return;
    }
    // 服务器可能会返回新的 session
    String newSessionId = response.header(HEADER_MCP_SESSION_ID);
    if (StringUtils.isNotEmpty(newSessionId) && !Objects.equals(newSessionId, sessionId)) {
      logger.trace("Streamable session changed: client={}, oldSessionId={}, sessionId={}", clientId, sessionId, newSessionId);
      sessionId = newSessionId;
      mcpSessionId.set(newSessionId);
      createSseConnection(newSessionId);
    }
    processSendMessageResponseBody(sink, call, response, sessionId);
  }

  /**
   * 处理发送消息的响应体
   */
  private void processSendMessageResponseBody(MonoSink<Void> sink, Call call, Response response, @Nullable String sessionId) {
    // 服务器可能返回 sse, 也可能返回 json, 根据响应类型判断
    String contentType = response.headers().get("Content-Type");
    // 对服务器的 ping 请求发送回复时，服务器可能会返回空响应
    if (contentType == null) {
      sink.success();
    }
    else if (contentType.startsWith("text/event-stream")) {
      handleSseResponse(sink, call, response, sessionId);
    }
    else if (contentType.startsWith("application/json")) {
      handleJsonResponse(sink, response, sessionId);
    }
    else {
      logger.warn("Unknown streamable response type: client={}, url={}, contentType={}, status={}, headers={}, body={}", clientId, url, contentType,
        response.code(), response.headers(), ModelHttpClient.readResponseBody(response));
      sink.error(new BssException("未知的 MCP streamable 响应类型错误: " + contentType));
    }
  }

  /**
   * 处理 JSON 响应
   */
  private void handleJsonResponse(MonoSink<Void> sink, Response response, @Nullable String sessionId) {
    String responseBodyText = ModelHttpClient.readResponseBody(response);
    logger.trace("Received streamable json response: client={}, session={}, body={}", clientId, sessionId, responseBodyText);
    if (StringUtils.isEmpty(responseBodyText)) {
      sink.error(new BssException("发送 MCP streamable 消息失败: 响应为空"));
    }
    else {
      JsonRpcResponse jsonRpcResponse = JsonUtil.parseJsonRequired(responseBodyText, JsonRpcResponse.class);
      handler.apply(Mono.just(jsonRpcResponse)).subscribe();
      sink.success();
    }
  }

  /**
   * 处理 SSE 响应
   */
  private void handleSseResponse(MonoSink<Void> sink, Call call, Response response, @Nullable String sessionId) {
    // 取消请求超时限制
    if (call instanceof RealCall) {
      ((RealCall) call).timeoutEarlyExit();
    }
    ResponseBody body = response.body(); //NOPMD - suppressed CloseResource - 不需要关闭
    if (body == null) {
      sink.error(new BssException("发送 MCP streamable 消息失败: 响应体为空"));
      return;
    }
    // 参考 okhttp3.internal.sse.RealEventSource#processResponse
    ServerSentEventReader reader = new ServerSentEventReader(body.source(), new Callback() {
      @Override
      @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
      public void onEvent(@Nullable String id, @Nullable String type, String data) {
        logger.trace("Received streamable event: client={}, session={}, id={}, type={}, data={}", clientId, sessionId, id, type, data);
        if (status == McpTransportStatus.CLOSED) {
          return;
        }
        if (id != null) {
          lastEventId.set(id);
        }
        try {
          if (StringUtils.isEmpty(type) || McpConsts.EVENT_TYPE_MESSAGE.equals(type)) {
            JsonRpcMessage message = McpUtil.deserializeMessage(data);
            handler.apply(Mono.just(message)).subscribe();
            sink.success();
          }
          else {
            logger.warn("Unknown streamable event: client={}, session={}, id={}, type={}, data={}", clientId, sessionId, id, type, data);
          }
        }
        catch (Exception e) {
          logger.error("Failed to process streamable event: client={}, session={}, id={}, type={}, data={}", clientId, sessionId, id, type, data, e);
          sink.error(e);
        }
      }

      @Override
      public void onRetryChange(long timeMs) {
        // 忽略 retry 消息
        // https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#retry
      }
    });

    try {
      //noinspection StatementWithEmptyBody
      while (reader.processNextEvent()) { //NOPMD - suppressed EmptyControlStatement - reader 会调用 callback 处理事件
        // 不需要处理，reader 会调用 callback 处理事件
      }
    }
    catch (Exception e) {
      logger.error("Streamable connection error: client={}, session={}, url={}", clientId, sessionId, url, e);
      sink.error(e);
    }
  }

  /**
   * 创建 SSE 连接，用于接收服务器主动发送的通知、请求
   *
   * <p>有些服务器不支持 SSE 连接，失败时忽略异常</p>
   *
   * @see <a href="https://modelcontextprotocol.io/specification/2025-06-18/basic/transports#listening-for-messages-from-the-server">Listening for Messages from the Server</a>
   */
  private void createSseConnection(String sessionId) {
    if (!supportsSseConnection || status != McpTransportStatus.CONNECTED) {
      return;
    }
    closeSseConnection();
    Request request = buildRequest(sessionId, null, null).get().build();
    EventSourceListener eventSourceListener = new EventSourceListener() {
      @Override
      public void onOpen(EventSource eventSource, Response response) {
        logger.debug("Streamable SSE connection opened: client={}, url={}, sessionId={}", clientId, url, sessionId);
      }

      @Override
      public void onClosed(EventSource eventSource) {
        logger.debug("Streamable SSE connection closed: client={}, sessionId={}", clientId, sessionId);
      }

      @Override
      @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
      public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
        logger.trace("Received streamable SSE event: client={}, session={}, id={}, type={}, data={}", clientId, sessionId, id, type, data);
        handleSseConnectionEvent(id, type, data, sessionId);
      }

      @Override
      @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
      public void onFailure(EventSource eventSource, @Nullable Throwable throwable, @Nullable Response response) {
        if (response != null) {
          // 按照协议，返回 405 表示服务器不支持 SSE
          if (response.code() == HttpStatus.METHOD_NOT_ALLOWED.value()) {
            supportsSseConnection = false;
            logger.trace("Streamable SSE connection not supported: client={}, url={}, sessionId={}", clientId, url, sessionId);
            return;
          }
          String body = ModelHttpClient.readResponseBody(response);
          logger.warn("Streamable SSE connection error: client={}, url={}, sessionId={}, status={}, body={}", clientId, url, sessionId, response.code(), body);
        }
        else if (throwable != null) {
          logger.warn("Streamable SSE connection error: client={}, url={}, sessionId={}", clientId, url, sessionId, throwable);
        }
        else {
          logger.warn("Streamable SSE connection error: client={}, url={}, sessionId={}", clientId, url, sessionId);
        }
      }
    };
    sseEventSource = McpUtil.getEventSourceFactory().newEventSource(request, eventSourceListener);
  }

  /**
   * 处理 SSE 长连接的事件
   */
  private void handleSseConnectionEvent(@Nullable String id, @Nullable String type, String data, String sessionId) {
    if (status == McpTransportStatus.CLOSED) {
      return;
    }
    if (StringUtils.isNotEmpty(type) && !McpConsts.EVENT_TYPE_MESSAGE.equals(type)) {
      logger.warn("Unknown streamable SSE event: client={}, session={}, id={}, type={}, data={}", clientId, sessionId, id, type, data);
      return;
    }
    try {
      JsonRpcMessage message = McpUtil.deserializeMessage(data);
      handler.apply(Mono.just(message)).subscribe();
    }
    catch (Exception e) {
      logger.error("Failed to process streamable SSE event: client={}, session={}, id={}, type={}, data={}", clientId, sessionId, id, type, data, e);
    }
  }

  /**
   * 关闭 SSE 连接
   */
  private void closeSseConnection() {
    if (sseEventSource != null) {
      sseEventSource.cancel();
    }
  }

  /**
   * 客户端关闭时，向服务器发送请求删除会话
   */
  private void deleteSession() {
    String sessionId = mcpSessionId.get();
    if (StringUtils.isEmpty(sessionId)) {
      return;
    }
    Request request = buildRequest(sessionId, null, null).delete().build();
    // 服务器不一定支持删除会话，失败时忽略异常
    // https://modelcontextprotocol.io/specification/2025-06-18/basic/transports#session-management
    logger.trace("Deleting streamable session: client={}, sessionId={}", clientId, sessionId);
    try (Response response = ModelHttpClient.getClient().newCall(request).execute()) {
      if (response.isSuccessful()) {
        logger.trace("Deleted streamable session: client={}, sessionId={}, status={}", clientId, sessionId, response.code());
      }
      else {
        String body = ModelHttpClient.readResponseBody(response);
        logger.debug("Failed to delete streamable session: client={}, sessionId={}, status={}, body={}", clientId, sessionId, response.code(), body);
      }
    }
    catch (Exception e) {
      logger.debug("Failed to delete streamable session: client={}, sessionId={}", clientId, sessionId, e);
    }
  }

  /**
   * 构造请求
   */
  private Request.Builder buildRequest(@Nullable String sessionId, @Nullable String eventId, @Nullable String body) {
    Request.Builder builder = new Request.Builder().url(url).headers(SysParamUtil.buildHeaders(headers));
    builder.header(HEADER_PROTOCOL_VERSION, McpConsts.PROTOCOL_VERSION_2025_03_26);
    if (sessionId != null) {
      builder.header(HEADER_MCP_SESSION_ID, sessionId);
    }
    if (eventId != null) {
      builder.header(HEADER_LAST_EVENT_ID, eventId);
    }
    if (body != null) {
      builder.header(HttpHeaders.ACCEPT, DEFAULT_ACCEPT_HEADER);
      builder.post(OkHttpUtil.jsonBody(body));
    }
    return builder;
  }
}
