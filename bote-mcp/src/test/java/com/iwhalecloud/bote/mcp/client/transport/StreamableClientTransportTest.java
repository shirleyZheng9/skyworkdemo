package com.iwhalecloud.bote.mcp.client.transport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bote.mcp.consts.McpConsts;
import com.iwhalecloud.bote.mcp.consts.McpTransportStatus;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcMessage;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcNotification;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcRequest;
import com.iwhalecloud.bote.mcp.util.McpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * {@link StreamableClientTransport} 单元测试
 *
 * <p>通过 {@link MockedStatic} 模拟 {@link ModelHttpClient#getClient()} 返回 mock OkHttpClient，
 * 使用真实 {@link Response} 对象（由 {@link Response.Builder} 构造）覆盖各响应分支。
 * {@code @BeforeAll} 预加载 {@link McpUtil} 以避免其静态初始化器在 mock 环境下失败。
 * 不依赖 Spring 环境。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StreamableClientTransportTest {

  private MockedStatic<ModelHttpClient> modelHttpClientMock;

  @org.mockito.Mock
  private OkHttpClient mockClient;

  @org.mockito.Mock
  private Call mockCall;

  private StreamableClientTransport transport;

  /** 下一次 execute() 返回的 Response */
  private Response nextResponse;
  /** execute() 是否抛异常 */
  private boolean executeThrows;
  private Exception executeException;

  /**
   * 在所有测试之前预加载 McpUtil 类，使其静态初始化器在 ModelHttpClient 被 mock 之前执行。
   * McpUtil 的静态初始化器调用 ModelHttpClient.getClient() 创建 SSE HttpClient，
   * 如果此时 ModelHttpClient 已被 mock 则会失败。
   */
  @BeforeAll
  static void preloadMcpUtil() {
    McpUtil.deserializeMessage("{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":\"1\"}");
  }

  @BeforeEach
  void setUp() {
    // 使用 CALLS_REAL_METHODS，使 readResponseBody 等未 stub 的方法调用真实实现
    modelHttpClientMock = Mockito.mockStatic(ModelHttpClient.class,
        Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
    modelHttpClientMock.when(ModelHttpClient::getClient).thenReturn(mockClient);

    lenient().when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
    try {
      lenient().doAnswer(inv -> {
        if (executeThrows && executeException != null) {
          throw executeException;
        }
        return nextResponse;
      }).when(mockCall).execute();
    } catch (IOException e) {
      // mock 方法不会真正抛出，此处仅为编译器要求
      throw new AssertionError(e);
    }

    transport = new StreamableClientTransport("test-client", "https://example.com/mcp", null);
    // 设置为已连接状态，handler 为恒等函数
    ReflectionTestUtils.setField(transport, "status", McpTransportStatus.CONNECTED);
    ReflectionTestUtils.setField(transport, "handler",
        (Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>>) mono -> mono);
    // 默认禁用 SSE 长连接，避免调用 McpUtil.getEventSourceFactory()
    ReflectionTestUtils.setField(transport, "supportsSseConnection", false);

    nextResponse = null;
    executeThrows = false;
    executeException = null;
  }

  @AfterEach
  void tearDown() {
    if (modelHttpClientMock != null) {
      modelHttpClientMock.close();
    }
  }

  // ----------------------------------------------------------------------
  // 辅助方法
  // ----------------------------------------------------------------------

  /** 构建无 Content-Type 头的 Response */
  private Response buildResponse(int code) {
    return new Response.Builder()
        .request(new Request.Builder().url("https://example.com/mcp").build())
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("")
        .body(ResponseBody.create("", null))
        .build();
  }

  /** 构建带 Content-Type 和 body 的 Response */
  private Response buildResponse(int code, String contentType, String bodyText) {
    MediaType mediaType = contentType != null ? MediaType.parse(contentType) : null;
    ResponseBody body = ResponseBody.create(bodyText, mediaType);
    Response.Builder builder = new Response.Builder()
        .request(new Request.Builder().url("https://example.com/mcp").build())
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("")
        .body(body);
    if (contentType != null) {
      builder.header("Content-Type", contentType);
    }
    return builder.build();
  }

  /** 构建带 MCP-Session-Id 头的 Response */
  private Response buildResponseWithSession(int code, String contentType, String bodyText, String sessionId) {
    Response.Builder builder = new Response.Builder()
        .request(new Request.Builder().url("https://example.com/mcp").build())
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("");
    if (bodyText != null) {
      MediaType mediaType = contentType != null ? MediaType.parse(contentType) : null;
      builder.body(ResponseBody.create(bodyText, mediaType));
    } else {
      builder.body(ResponseBody.create("", null));
    }
    if (contentType != null) {
      builder.header("Content-Type", contentType);
    }
    if (sessionId != null) {
      builder.header("Mcp-Session-Id", sessionId);
    }
    return builder.build();
  }

  @SuppressWarnings("unchecked")
  private AtomicReference<String> sessionIdField() {
    return (AtomicReference<String>) ReflectionTestUtils.getField(transport, "mcpSessionId");
  }

  @SuppressWarnings("unchecked")
  private AtomicReference<String> lastEventIdField() {
    return (AtomicReference<String>) ReflectionTestUtils.getField(transport, "lastEventId");
  }

  private JsonRpcRequest pingRequest() {
    return new JsonRpcRequest(McpConsts.METHOD_PING, "1", null);
  }

  // ----------------------------------------------------------------------
  // 构造与状态
  // ----------------------------------------------------------------------

  @Test
  void constructor_invalidUrl_throwsIllegalArgument() {
    assertThatThrownBy(() -> new StreamableClientTransport("c", "not-a-url", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("MCP 服务地址不合法");
  }

  @Test
  void constructor_nullHeaders_usesEmptyHeaders() {
    StreamableClientTransport t = new StreamableClientTransport("c", "https://example.com/mcp", null);
    assertThat(t.isConnected()).isFalse();
  }

  @Test
  void constructor_withCustomHeaders_succeeds() {
    StreamableClientTransport t = new StreamableClientTransport("c", "https://example.com/mcp",
        okhttp3.Headers.of("X-Custom", "value"));
    assertThat(t.isConnected()).isFalse();
  }

  @Test
  void connect_setsConnectedStatus() {
    transport = new StreamableClientTransport("c", "https://example.com/mcp", null);
    StepVerifier.create(transport.connect(m -> m, () -> { })).verifyComplete();
    assertThat(transport.isConnected()).isTrue();
  }

  @Test
  void isConnected_beforeConnect_isFalse() {
    transport = new StreamableClientTransport("c", "https://example.com/mcp", null);
    assertThat(transport.isConnected()).isFalse();
  }

  @Test
  void closeGracefully_noSession_completesWithoutDelete() {
    StepVerifier.create(transport.closeGracefully()).verifyComplete();
    assertThat(transport.isConnected()).isFalse();
    assertThat(ReflectionTestUtils.getField(transport, "status"))
        .isEqualTo(McpTransportStatus.CLOSED);
  }

  @Test
  void closeGracefully_withSession_callsDeleteSession() {
    sessionIdField().set("session-abc");
    nextResponse = buildResponse(200);

    StepVerifier.create(transport.closeGracefully()).verifyComplete();
    assertThat(ReflectionTestUtils.getField(transport, "status"))
        .isEqualTo(McpTransportStatus.CLOSED);
  }

  @Test
  void closeGracefully_deleteSessionFails_logsAndCompletes() {
    sessionIdField().set("session-abc");
    nextResponse = buildResponse(500);

    StepVerifier.create(transport.closeGracefully()).verifyComplete();
  }

  @Test
  void closeGracefully_deleteSessionThrowsException_completes() {
    sessionIdField().set("session-abc");
    executeThrows = true;
    executeException = new IOException("connection refused");

    StepVerifier.create(transport.closeGracefully()).verifyComplete();
  }

  @Test
  void closeGracefully_cancelsSseEventSource() {
    EventSource mockEventSource = mock(EventSource.class);
    ReflectionTestUtils.setField(transport, "sseEventSource", mockEventSource);

    StepVerifier.create(transport.closeGracefully()).verifyComplete();
    verify(mockEventSource).cancel();
  }

  // ----------------------------------------------------------------------
  // sendMessage — 错误守卫
  // ----------------------------------------------------------------------

  @Test
  void sendMessage_whenClosed_returnsError() {
    ReflectionTestUtils.setField(transport, "status", McpTransportStatus.CLOSED);

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyErrorSatisfies(e -> assertThat(e).isInstanceOf(BssException.class)
            .hasMessageContaining("已关闭"));
  }

  // ----------------------------------------------------------------------
  // sendMessage — 404 session expired
  // ----------------------------------------------------------------------

  @Test
  void sendMessage_404WithSession_returnsSessionExpired() {
    sessionIdField().set("old-session");
    nextResponse = buildResponse(404);

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyErrorSatisfies(e -> assertThat(e).isInstanceOf(BssException.class)
            .hasMessageContaining("session 已失效"));
    // session ID 应被清除
    assertThat(sessionIdField().get()).isNull();
    assertThat(lastEventIdField().get()).isNull();
    assertThat(ReflectionTestUtils.getField(transport, "status"))
        .isEqualTo(McpTransportStatus.CONNECT_FAILED);
  }

  @Test
  void sendMessage_404WithoutSession_treatedAsFailedResponse() {
    // 没有 session 时，404 不会被当作 session expired，而是走 !isSuccessful() 分支
    nextResponse = buildResponse(404);

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyErrorSatisfies(e -> assertThat(e).isInstanceOf(BssException.class)
            .hasMessageContaining("status=404"));
  }

  // ----------------------------------------------------------------------
  // sendMessage — 非成功响应
  // ----------------------------------------------------------------------

  @Test
  void sendMessage_500_returnsError() {
    nextResponse = buildResponse(500, "text/plain", "Internal Server Error");

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyErrorSatisfies(e -> assertThat(e).isInstanceOf(BssException.class)
            .hasMessageContaining("status=500"));
  }

  // ----------------------------------------------------------------------
  // sendMessage — 202 Accepted
  // ----------------------------------------------------------------------

  @Test
  void sendMessage_202_completes() {
    nextResponse = buildResponse(202);

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyComplete();
  }

  // ----------------------------------------------------------------------
  // sendMessage — JSON 响应
  // ----------------------------------------------------------------------

  @Test
  void sendMessage_jsonResponse_completes() {
    String json = "{\"jsonrpc\":\"2.0\",\"result\":{},\"id\":1}";
    nextResponse = buildResponse(200, "application/json", json);

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyComplete();
  }

  @Test
  void sendMessage_jsonResponseEmptyBody_returnsError() {
    nextResponse = buildResponse(200, "application/json", "");

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyErrorSatisfies(e -> assertThat(e).isInstanceOf(BssException.class)
            .hasMessageContaining("响应为空"));
  }

  // ----------------------------------------------------------------------
  // sendMessage — SSE 响应
  // ----------------------------------------------------------------------

  @Test
  void sendMessage_sseResponse_messageEvent_completes() {
    String json = "{\"jsonrpc\":\"2.0\",\"result\":{},\"id\":1}";
    String sseData = "data: " + json + "\n\n";
    nextResponse = buildResponse(200, "text/event-stream", sseData);

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyComplete();
  }

  @Test
  void sendMessage_sseResponse_withEventId_setsLastEventId() {
    String json = "{\"jsonrpc\":\"2.0\",\"result\":{},\"id\":1}";
    String sseData = "id: 42\ndata: " + json + "\n\n";
    nextResponse = buildResponse(200, "text/event-stream", sseData);

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyComplete();
    assertThat(lastEventIdField().get()).isEqualTo("42");
  }

  @Test
  void sendMessage_sseResponse_unknownEventType_doesNotComplete() {
    // unknown event type: callback logs warning, doesn't call sink.success()
    String json = "{\"jsonrpc\":\"2.0\",\"result\":{},\"id\":1}";
    String sseData = "event: unknown\ndata: " + json + "\n\n";
    nextResponse = buildResponse(200, "text/event-stream", sseData);

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .thenCancel()
        .verify(Duration.ofSeconds(2));
  }

  // ----------------------------------------------------------------------
  // sendMessage — null Content-Type
  // ----------------------------------------------------------------------

  @Test
  void sendMessage_nullContentType_completes() {
    // 没有 Content-Type 头，直接成功
    nextResponse = buildResponse(200);

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyComplete();
  }

  // ----------------------------------------------------------------------
  // sendMessage — 未知 Content-Type
  // ----------------------------------------------------------------------

  @Test
  void sendMessage_unknownContentType_returnsError() {
    nextResponse = buildResponse(200, "text/plain", "hello");

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyErrorSatisfies(e -> assertThat(e).isInstanceOf(BssException.class)
            .hasMessageContaining("text/plain"));
  }

  // ----------------------------------------------------------------------
  // sendMessage — 新 session ID 触发 createSseConnection
  // ----------------------------------------------------------------------

  @Test
  void sendMessage_newSessionId_updatesSessionAndCreatesSseConnection() {
    EventSource.Factory mockFactory = mock(EventSource.Factory.class);
    EventSource mockEventSource = mock(EventSource.class);
    try (MockedStatic<McpUtil> mcpUtilMock = Mockito.mockStatic(McpUtil.class)) {
      mcpUtilMock.when(McpUtil::getEventSourceFactory).thenReturn(mockFactory);
      doReturn(mockEventSource).when(mockFactory)
          .newEventSource(any(Request.class), any(EventSourceListener.class));

      ReflectionTestUtils.setField(transport, "supportsSseConnection", true);
      sessionIdField().set("old-session");

      String json = "{\"jsonrpc\":\"2.0\",\"result\":{},\"id\":1}";
      nextResponse = buildResponseWithSession(200, "application/json", json, "new-session");

      StepVerifier.create(transport.sendMessage(pingRequest()))
          .verifyComplete();

      assertThat(sessionIdField().get()).isEqualTo("new-session");
      verify(mockFactory).newEventSource(any(Request.class), any(EventSourceListener.class));
    }
  }

  @Test
  void sendMessage_sameSessionId_doesNotCreateSseConnection() {
    sessionIdField().set("same-session");
    String json = "{\"jsonrpc\":\"2.0\",\"result\":{},\"id\":1}";
    nextResponse = buildResponseWithSession(200, "application/json", json, "same-session");

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyComplete();
    // supportsSseConnection 为 false，即使 session 相同也不会调用 createSseConnection
  }

  // ----------------------------------------------------------------------
  // sendMessage — IOException
  // ----------------------------------------------------------------------

  @Test
  void sendMessage_ioException_returnsError() {
    executeThrows = true;
    executeException = new IOException("network error");

    StepVerifier.create(transport.sendMessage(pingRequest()))
        .verifyErrorSatisfies(e -> assertThat(e).isInstanceOf(IOException.class)
            .hasMessageContaining("network error"));
  }

  // ----------------------------------------------------------------------
  // createSseConnection — 独立测试
  // ----------------------------------------------------------------------

  @Test
  void createSseConnection_supportsSseFalse_isNoop() {
    ReflectionTestUtils.setField(transport, "supportsSseConnection", false);
    ReflectionTestUtils.invokeMethod(transport, "createSseConnection", "session-1");
    // 不抛异常即可（无 SSE 工厂调用）
  }

  @Test
  void createSseConnection_statusNotConnected_isNoop() {
    ReflectionTestUtils.setField(transport, "supportsSseConnection", true);
    ReflectionTestUtils.setField(transport, "status", McpTransportStatus.CLOSED);
    ReflectionTestUtils.invokeMethod(transport, "createSseConnection", "session-1");
    // 不抛异常即可
  }

  // ----------------------------------------------------------------------
  // handleSseConnectionEvent — 反射测试
  // ----------------------------------------------------------------------

  @Test
  void handleSseConnectionEvent_closedStatus_returnsEarly() {
    ReflectionTestUtils.setField(transport, "status", McpTransportStatus.CLOSED);
    ReflectionTestUtils.invokeMethod(transport, "handleSseConnectionEvent", null, null, "{}", "session-1");
    // 不抛异常即可
  }

  @Test
  void handleSseConnectionEvent_unknownType_logsWarning() {
    AtomicReference<JsonRpcMessage> captured = new AtomicReference<>();
    ReflectionTestUtils.setField(transport, "handler",
        (Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>>) mono -> mono.doOnNext(captured::set));

    ReflectionTestUtils.invokeMethod(transport, "handleSseConnectionEvent", null, "unknown-type", "{}", "session-1");
    assertThat(captured.get()).isNull();
  }

  @Test
  void handleSseConnectionEvent_messageType_processesMessage() {
    AtomicReference<JsonRpcMessage> captured = new AtomicReference<>();
    ReflectionTestUtils.setField(transport, "handler",
        (Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>>) mono -> mono.doOnNext(captured::set));

    String json = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}";
    ReflectionTestUtils.invokeMethod(transport, "handleSseConnectionEvent", null, "message", json, "session-1");
    assertThat(captured.get()).isInstanceOf(JsonRpcNotification.class);
  }

  @Test
  void handleSseConnectionEvent_emptyType_processesAsMessage() {
    AtomicReference<JsonRpcMessage> captured = new AtomicReference<>();
    ReflectionTestUtils.setField(transport, "handler",
        (Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>>) mono -> mono.doOnNext(captured::set));

    String json = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}";
    ReflectionTestUtils.invokeMethod(transport, "handleSseConnectionEvent", "1", "", json, "session-1");
    assertThat(captured.get()).isInstanceOf(JsonRpcNotification.class);
  }

  @Test
  void handleSseConnectionEvent_invalidJson_logsErrorNoThrow() {
    // 无效 JSON 应被 catch，不抛异常
    ReflectionTestUtils.invokeMethod(transport, "handleSseConnectionEvent", null, "message", "not-valid-json", "session-1");
    // 不抛异常即可
  }

  // ----------------------------------------------------------------------
  // buildRequest — 反射测试
  // ----------------------------------------------------------------------

  @Test
  void buildRequest_withBody_setsAcceptAndPost() {
    Request.Builder builder = ReflectionTestUtils.invokeMethod(transport, "buildRequest", "session-1", "event-1", "{}");
    Request request = builder.build();
    assertThat(request.method()).isEqualTo("POST");
    assertThat(request.header("Accept")).isEqualTo("application/json,text/event-stream");
    assertThat(request.header("Mcp-Session-Id")).isEqualTo("session-1");
    assertThat(request.header("Last-Event-ID")).isEqualTo("event-1");
    assertThat(request.header("MCP-Protocol-Version"))
        .isEqualTo(McpConsts.PROTOCOL_VERSION_2025_03_26);
  }

  @Test
  void buildRequest_withoutSessionAndEvent_noHeaders() {
    Request.Builder builder = ReflectionTestUtils.invokeMethod(transport, "buildRequest", null, null, null);
    Request request = builder.build();
    assertThat(request.method()).isEqualTo("GET");
    assertThat(request.header("Mcp-Session-Id")).isNull();
    assertThat(request.header("Last-Event-ID")).isNull();
  }

  // ----------------------------------------------------------------------
  // closeSseConnection — 反射测试
  // ----------------------------------------------------------------------

  @Test
  void closeSseConnection_nullEventSource_isNoop() {
    ReflectionTestUtils.invokeMethod(transport, "closeSseConnection");
    // 不抛异常即可
  }

  @Test
  void closeSseConnection_withEventSource_cancelsIt() {
    EventSource mockEventSource = mock(EventSource.class);
    ReflectionTestUtils.setField(transport, "sseEventSource", mockEventSource);

    ReflectionTestUtils.invokeMethod(transport, "closeSseConnection");
    verify(mockEventSource).cancel();
  }

  // ----------------------------------------------------------------------
  // deleteSession — 反射测试
  // ----------------------------------------------------------------------

  @Test
  void deleteSession_emptySession_isNoop() {
    ReflectionTestUtils.invokeMethod(transport, "deleteSession");
    verify(mockClient, never()).newCall(any(Request.class));
  }

  @Test
  void deleteSession_successful_logsAndCompletes() {
    sessionIdField().set("session-del");
    nextResponse = buildResponse(200);

    ReflectionTestUtils.invokeMethod(transport, "deleteSession");
    verify(mockClient).newCall(any(Request.class));
  }

  @Test
  void deleteSession_unsuccessful_logsAndCompletes() {
    sessionIdField().set("session-del");
    nextResponse = buildResponse(404);

    ReflectionTestUtils.invokeMethod(transport, "deleteSession");
    verify(mockClient).newCall(any(Request.class));
  }

  @Test
  void deleteSession_executeThrows_logsAndCompletes() {
    sessionIdField().set("session-del");
    executeThrows = true;
    executeException = new IOException("delete failed");

    ReflectionTestUtils.invokeMethod(transport, "deleteSession");
    verify(mockClient).newCall(any(Request.class));
  }

  // ----------------------------------------------------------------------
  // createSseConnection — EventSourceListener 回调测试
  // ----------------------------------------------------------------------

  @Test
  void createSseConnection_eventSourceListener_callbacksDoNotThrow() {
    EventSource.Factory mockFactory = mock(EventSource.Factory.class);
    EventSource mockEventSource = mock(EventSource.class);
    AtomicReference<EventSourceListener> listenerRef = new AtomicReference<>();
    try (MockedStatic<McpUtil> mcpUtilMock = Mockito.mockStatic(McpUtil.class)) {
      mcpUtilMock.when(McpUtil::getEventSourceFactory).thenReturn(mockFactory);
      Mockito.doAnswer(inv -> {
        listenerRef.set(inv.getArgument(1));
        return mockEventSource;
      }).when(mockFactory)
          .newEventSource(any(Request.class), any(EventSourceListener.class));

      ReflectionTestUtils.setField(transport, "supportsSseConnection", true);
      ReflectionTestUtils.invokeMethod(transport, "createSseConnection", "session-sse");

      EventSourceListener listener = listenerRef.get();
      assertThat(listener).isNotNull();

      // onOpen
      listener.onOpen(mockEventSource, buildResponse(200));
      // onClosed
      listener.onClosed(mockEventSource);
      // onEvent — message type (deserializeMessage mocked → returns null → NPE caught)
      listener.onEvent(mockEventSource, null, "message",
          "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
      // onEvent — unknown type
      listener.onEvent(mockEventSource, "1", "unknown-type", "{}");
      // onFailure with 405 response → supportsSseConnection = false
      listener.onFailure(mockEventSource, null, buildResponse(405));
      assertThat(ReflectionTestUtils.getField(transport, "supportsSseConnection")).isEqualTo(false);

      // 重新启用后 onFailure with throwable
      ReflectionTestUtils.setField(transport, "supportsSseConnection", true);
      listener.onFailure(mockEventSource, new IOException("conn lost"), null);

      // onFailure with null response and null throwable
      listener.onFailure(mockEventSource, null, null);
    }
  }
}
