package com.iwhalecloud.bote.mcp.client.transport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.iwhalecloud.bote.mcp.consts.McpConsts;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcMessage;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcNotification;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.EOFException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.test.StepVerifier;

import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bote.mcp.consts.McpTransportStatus;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcRequest;
import com.iwhalecloud.bote.mcp.util.McpUtil;
import java.io.IOException;
import java.lang.reflect.Field;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.MockedStatic;

/**
 * {@link SseClientTransport} 单元测试
 *
 * <p>覆盖构造校验、{@code resolveMessageEndpoint} 全部分支、{@code handleSseEvent}/
 * {@code handleSseFailure} 事件处理、{@code isConnected}、{@code closeGracefully}、
 * {@code sendMessage} 错误守卫与 {@code connect} 已关闭分支。不发起真实网络请求。</p>
 */
class SseClientTransportTest {

  private static OkHttpClient originalHttpClient;

  @BeforeAll
  static void preloadMcpUtil() {
    McpUtil.deserializeMessage("{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":\"1\"}");
    try {
      Field f = ModelHttpClient.class.getDeclaredField("client");
      f.setAccessible(true);
      originalHttpClient = (OkHttpClient) f.get(null);
    } catch (Exception ignored) { }
  }

  @AfterAll
  static void restoreHttpClientAfterAll() {
    if (originalHttpClient != null) {
      try {
        Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        uf.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) uf.get(null);
        Field clientField = ModelHttpClient.class.getDeclaredField("client");
        long offset = unsafe.staticFieldOffset(clientField);
        Object base = unsafe.staticFieldBase(clientField);
        unsafe.putObject(base, offset, originalHttpClient);
      } catch (Exception ignored) { }
    }
  }

  private void setMockHttpClient(OkHttpClient mockClient) throws Exception {
    Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    uf.setAccessible(true);
    sun.misc.Unsafe unsafe = (sun.misc.Unsafe) uf.get(null);
    Field clientField = ModelHttpClient.class.getDeclaredField("client");
    long offset = unsafe.staticFieldOffset(clientField);
    Object base = unsafe.staticFieldBase(clientField);
    unsafe.putObject(base, offset, mockClient);
  }

  private void restoreHttpClient() throws Exception {
    Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    uf.setAccessible(true);
    sun.misc.Unsafe unsafe = (sun.misc.Unsafe) uf.get(null);
    Field clientField = ModelHttpClient.class.getDeclaredField("client");
    long offset = unsafe.staticFieldOffset(clientField);
    Object base = unsafe.staticFieldBase(clientField);
    unsafe.putObject(base, offset, originalHttpClient);
  }

  private Response buildOkResponse(int code) {
    return new Response.Builder()
      .request(new Request.Builder().url("https://example.com").build())
      .protocol(Protocol.HTTP_1_1)
      .code(code)
      .message("")
      .body(ResponseBody.create("", null))
      .build();
  }

  private Response buildOkResponse(int code, String bodyText) {
    return new Response.Builder()
      .request(new Request.Builder().url("https://example.com").build())
      .protocol(Protocol.HTTP_1_1)
      .code(code)
      .message("")
      .body(ResponseBody.create(bodyText, MediaType.parse("text/plain")))
      .build();
  }

  private SseClientTransport newTransport(String url, String endpoint) {
    return new SseClientTransport("client@id1", url, endpoint, null);
  }

  // ----------------------------------------------------------------------
  // 构造与状态
  // ----------------------------------------------------------------------

  @Test
  void constructor_invalidUrl_throwsAssertion() {
    assertThatThrownBy(() -> new SseClientTransport("c", "not-a-url", null, null))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void constructor_nullHeaders_usesEmptyHeaders() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    // 不抛异常即说明构造成功
    assertThat(t.isConnected()).isFalse();
  }

  @Test
  void isConnected_beforeConnect_isFalse() {
    assertThat(newTransport("https://example.com/sse", null).isConnected()).isFalse();
  }

  @Test
  void closeGracefully_setsClosedAndCancelsEventSource() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    StepVerifier.create(t.closeGracefully()).verifyComplete();
    assertThat(t.isConnected()).isFalse();
    assertThat(ReflectionTestUtils.getField(t, "status"))
      .isEqualTo(com.iwhalecloud.bote.mcp.consts.McpTransportStatus.CLOSED);
  }

  // ----------------------------------------------------------------------
  // resolveMessageEndpoint（私有方法，反射）
  // ----------------------------------------------------------------------

  @Test
  void resolveMessageEndpoint_emptyEndpoint_resolvesRelative() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    HttpUrl result = ReflectionTestUtils.invokeMethod(t, "resolveMessageEndpoint", "/messages");
    assertThat(result).isNotNull();
    assertThat(result.toString()).isEqualTo("https://example.com/messages");
  }

  @Test
  void resolveMessageEndpoint_emptyEndpoint_withQuery() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    HttpUrl result = ReflectionTestUtils.invokeMethod(t, "resolveMessageEndpoint", "/messages?token=abc");
    assertThat(result).isNotNull();
    assertThat(result.encodedPath()).isEqualTo("/messages");
    assertThat(result.queryParameter("token")).isEqualTo("abc");
  }

  @Test
  void resolveMessageEndpoint_slashEndpoint_buildsRootPath() {
    SseClientTransport t = newTransport("https://mcp.bochaai.com/sse", "/");
    HttpUrl result = ReflectionTestUtils.invokeMethod(t, "resolveMessageEndpoint", "/messages");
    assertThat(result).isNotNull();
    assertThat(result.encodedPath()).isEqualTo("/messages");
  }

  @Test
  void resolveMessageEndpoint_pathEndpoint_withQuery() {
    SseClientTransport t = newTransport("http://10.43.62.40:19100/mcpServer/fj/kdywxt/sse", "mcpServer/fj/kdywxt");
    HttpUrl result = ReflectionTestUtils.invokeMethod(t, "resolveMessageEndpoint", "/messages?sessionId=xxx");
    assertThat(result).isNotNull();
    assertThat(result.encodedPath()).isEqualTo("/mcpServer/fj/kdywxt/messages");
    assertThat(result.queryParameter("sessionId")).isEqualTo("xxx");
  }

  @Test
  void resolveMessageEndpoint_endpointEndingWithSse_stripsSseSuffix() {
    SseClientTransport t = newTransport("https://example.com/api/sse", "api/sse");
    HttpUrl result = ReflectionTestUtils.invokeMethod(t, "resolveMessageEndpoint", "/messages");
    assertThat(result).isNotNull();
    assertThat(result.encodedPath()).isEqualTo("/api/messages");
  }

  @Test
  void resolveMessageEndpoint_emptyEndpointAndDataWithoutSlash_resolves() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    HttpUrl result = ReflectionTestUtils.invokeMethod(t, "resolveMessageEndpoint", "messages");
    assertThat(result).isNotNull();
    assertThat(result.encodedPath()).isEqualTo("/messages");
  }

  // ----------------------------------------------------------------------
  // handleSseEvent（私有方法，反射）
  // ----------------------------------------------------------------------

  @Test
  void handleSseEvent_whenClosed_returnsEarly() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    ReflectionTestUtils.setField(t, "status", com.iwhalecloud.bote.mcp.consts.McpTransportStatus.CLOSED);
    MonoSink<Void> sink = Mockito.mock(MonoSink.class);

    ReflectionTestUtils.invokeMethod(t, "handleSseEvent", "1", McpConsts.EVENT_TYPE_ENDPOINT, "/messages", sink, (Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>>) m -> m);

    Mockito.verifyNoInteractions(sink);
  }

  @Test
  void handleSseEvent_endpointEvent_setsConnectedAndSuccess() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    MonoSink<Void> sink = Mockito.mock(MonoSink.class);

    ReflectionTestUtils.invokeMethod(t, "handleSseEvent", "1", McpConsts.EVENT_TYPE_ENDPOINT, "/messages", sink, (Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>>) m -> m);

    assertThat(t.isConnected()).isTrue();
    verify(sink).success();
    assertThat(ReflectionTestUtils.getField(t, "messageEndpoint")).isNotNull();
  }

  @Test
  void handleSseEvent_messageEvent_invokesHandler() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    AtomicReference<JsonRpcMessage> captured = new AtomicReference<>();
    Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>> handler = m -> m.doOnNext(captured::set);
    MonoSink<Void> sink = Mockito.mock(MonoSink.class);

    String json = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}";
    ReflectionTestUtils.invokeMethod(t, "handleSseEvent", "1", McpConsts.EVENT_TYPE_MESSAGE, json, sink, handler);

    assertThat(captured.get()).isInstanceOf(JsonRpcNotification.class);
  }

  @Test
  void handleSseEvent_emptyType_treatedAsMessage() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    AtomicReference<JsonRpcMessage> captured = new AtomicReference<>();
    Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>> handler = m -> m.doOnNext(captured::set);
    MonoSink<Void> sink = Mockito.mock(MonoSink.class);

    String json = "{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":1}";
    ReflectionTestUtils.invokeMethod(t, "handleSseEvent", "1", "", json, sink, handler);

    assertThat(captured.get()).isNotNull();
  }

  @Test
  void handleSseEvent_unknownType_logsWarn() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    MonoSink<Void> sink = Mockito.mock(MonoSink.class);

    ReflectionTestUtils.invokeMethod(t, "handleSseEvent", "1", "unknown-type", "data", sink, (Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>>) m -> m);

    verify(sink, never()).success();
    verify(sink, never()).error(any());
  }

  @Test
  void handleSseEvent_invalidJson_callsSinkError() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    MonoSink<Void> sink = Mockito.mock(MonoSink.class);

    ReflectionTestUtils.invokeMethod(t, "handleSseEvent", "1", McpConsts.EVENT_TYPE_MESSAGE, "not-json", sink, (Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>>) m -> m);

    verify(sink).error(any(Throwable.class));
  }

  // ----------------------------------------------------------------------
  // handleSseFailure（私有方法，反射）
  // ----------------------------------------------------------------------

  @Test
  void handleSseFailure_whenClosed_returnsEarly() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    ReflectionTestUtils.setField(t, "status", com.iwhalecloud.bote.mcp.consts.McpTransportStatus.CLOSED);
    MonoSink<Void> sink = Mockito.mock(MonoSink.class);
    Runnable disconnectHandler = Mockito.mock(Runnable.class);

    ReflectionTestUtils.invokeMethod(t, "handleSseFailure", new SocketTimeoutException(), null, disconnectHandler, sink);

    Mockito.verifyNoInteractions(sink, disconnectHandler);
  }

  @Test
  void handleSseFailure_connectedSocketTimeout_triggersReconnect() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    ReflectionTestUtils.setField(t, "status", com.iwhalecloud.bote.mcp.consts.McpTransportStatus.CONNECTED);
    MonoSink<Void> sink = Mockito.mock(MonoSink.class);
    Runnable disconnectHandler = Mockito.mock(Runnable.class);

    ReflectionTestUtils.invokeMethod(t, "handleSseFailure", new SocketTimeoutException(), null, disconnectHandler, sink);

    verify(disconnectHandler).run();
    assertThat(ReflectionTestUtils.getField(t, "status"))
      .isEqualTo(com.iwhalecloud.bote.mcp.consts.McpTransportStatus.CONNECT_FAILED);
    verify(sink, never()).error(any());
  }

  @Test
  void handleSseFailure_connectedEof_triggersReconnect() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    ReflectionTestUtils.setField(t, "status", com.iwhalecloud.bote.mcp.consts.McpTransportStatus.CONNECTED);
    MonoSink<Void> sink = Mockito.mock(MonoSink.class);
    Runnable disconnectHandler = Mockito.mock(Runnable.class);

    ReflectionTestUtils.invokeMethod(t, "handleSseFailure", new EOFException(), null, disconnectHandler, sink);

    verify(disconnectHandler).run();
  }

  @Test
  void handleSseFailure_throwable_propagatesToSink() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    ReflectionTestUtils.setField(t, "status", com.iwhalecloud.bote.mcp.consts.McpTransportStatus.CONNECTING);
    MonoSink<Void> sink = Mockito.mock(MonoSink.class);
    Runnable disconnectHandler = Mockito.mock(Runnable.class);
    Throwable error = new RuntimeException("network down");

    ReflectionTestUtils.invokeMethod(t, "handleSseFailure", error, null, disconnectHandler, sink);

    verify(sink).error(error);
    assertThat(ReflectionTestUtils.getField(t, "status"))
      .isEqualTo(com.iwhalecloud.bote.mcp.consts.McpTransportStatus.CONNECT_FAILED);
  }

  @Test
  void handleSseFailure_responseOnly_callsSinkErrorWithBssException() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    ReflectionTestUtils.setField(t, "status", com.iwhalecloud.bote.mcp.consts.McpTransportStatus.CONNECTING);
    MonoSink<Void> sink = Mockito.mock(MonoSink.class);
    Runnable disconnectHandler = Mockito.mock(Runnable.class);
    Response response = Mockito.mock(Response.class);
    lenient().when(response.code()).thenReturn(500);
    lenient().when(response.body()).thenReturn(null);

    ReflectionTestUtils.invokeMethod(t, "handleSseFailure", null, response, disconnectHandler, sink);

    org.mockito.ArgumentCaptor<Throwable> captor = org.mockito.ArgumentCaptor.forClass(Throwable.class);
    verify(sink).error(captor.capture());
    assertThat(captor.getValue()).isInstanceOf(BssException.class);
  }

  @Test
  void handleSseFailure_neitherThrowableNorResponse_callsSinkError() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    ReflectionTestUtils.setField(t, "status", com.iwhalecloud.bote.mcp.consts.McpTransportStatus.CONNECTING);
    MonoSink<Void> sink = Mockito.mock(MonoSink.class);
    Runnable disconnectHandler = Mockito.mock(Runnable.class);

    ReflectionTestUtils.invokeMethod(t, "handleSseFailure", null, null, disconnectHandler, sink);

    org.mockito.ArgumentCaptor<Throwable> captor = org.mockito.ArgumentCaptor.forClass(Throwable.class);
    verify(sink).error(captor.capture());
    assertThat(captor.getValue()).isInstanceOf(BssException.class);
  }

  // ----------------------------------------------------------------------
  // sendMessage 守卫
  // ----------------------------------------------------------------------

  @Test
  void sendMessage_whenClosed_returnsError() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    t.closeGracefully().block();

    StepVerifier.create(t.sendMessage(new JsonRpcNotification(McpConsts.METHOD_NOTIFICATION_INITIALIZED)))
      .verifyErrorSatisfies(e -> assertThat(e).isInstanceOf(BssException.class)
        .hasMessageContaining("SSE 客户端已关闭"));
  }

  @Test
  void sendMessage_whenNoEndpoint_returnsError() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    // status=UNSET, messageEndpoint=null

    StepVerifier.create(t.sendMessage(new JsonRpcNotification(McpConsts.METHOD_NOTIFICATION_INITIALIZED)))
      .verifyErrorSatisfies(e -> assertThat(e).isInstanceOf(BssException.class)
        .hasMessageContaining("没有可用的 message endpoint"));
  }

  // ----------------------------------------------------------------------
  // connect 已关闭分支
  // ----------------------------------------------------------------------

  @Test
  void connect_whenClosed_returnsError() {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    t.closeGracefully().block();

    StepVerifier.create(t.connect(m -> m, () -> { }))
      .verifyErrorSatisfies(e -> assertThat(e).isInstanceOf(BssException.class)
        .hasMessageContaining("已关闭"));
  }

  // ----------------------------------------------------------------------
  // connect() — EventSourceListener 回调（匿名内部类覆盖）
  // ----------------------------------------------------------------------

  @Test
  void connect_eventSourceListener_allCallbacks() {
    EventSource.Factory mockFactory = mock(EventSource.Factory.class);
    EventSource mockEs = mock(EventSource.class);
    AtomicReference<EventSourceListener> listenerRef = new AtomicReference<>();

    try (MockedStatic<McpUtil> mcpUtilMock = Mockito.mockStatic(McpUtil.class,
        Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))) {
      mcpUtilMock.when(McpUtil::getEventSourceFactory).thenReturn(mockFactory);
      Mockito.doAnswer(inv -> {
        listenerRef.set(inv.getArgument(1));
        return mockEs;
      }).when(mockFactory).newEventSource(any(okhttp3.Request.class), any(EventSourceListener.class));

      // --- transport t1: onOpen + onEvent(endpoint) + onEvent(message) + onEvent(unknown) + onClosed ---
      SseClientTransport t1 = newTransport("https://example.com/sse", null);
      t1.connect(m -> m, () -> { }).subscribe();
      EventSourceListener l1 = listenerRef.get();
      assertThat(l1).isNotNull();

      l1.onOpen(mockEs, null);
      assertThat(ReflectionTestUtils.getField(t1, "status")).isEqualTo(McpTransportStatus.CONNECTING);

      l1.onEvent(mockEs, "1", McpConsts.EVENT_TYPE_ENDPOINT, "/messages");
      assertThat(t1.isConnected()).isTrue();

      l1.onEvent(mockEs, "2", McpConsts.EVENT_TYPE_MESSAGE,
        "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");

      l1.onEvent(mockEs, "3", "unknown-type", "data");

      l1.onClosed(mockEs);
      assertThat(ReflectionTestUtils.getField(t1, "status")).isEqualTo(McpTransportStatus.CONNECT_FAILED);
      listenerRef.set(null);

      // --- transport t2: onFailure(throwable) when CONNECTING ---
      SseClientTransport t2 = newTransport("https://example.com/sse", null);
      t2.connect(m -> m, () -> { }).subscribe();
      EventSourceListener l2 = listenerRef.get();
      l2.onFailure(mockEs, new RuntimeException("net error"), null);
      assertThat(ReflectionTestUtils.getField(t2, "status")).isEqualTo(McpTransportStatus.CONNECT_FAILED);
      listenerRef.set(null);

      // --- transport t3: onFailure(response only) when CONNECTING ---
      SseClientTransport t3 = newTransport("https://example.com/sse", null);
      t3.connect(m -> m, () -> { }).subscribe();
      EventSourceListener l3 = listenerRef.get();
      l3.onFailure(mockEs, null, buildOkResponse(500, "error body"));
      assertThat(ReflectionTestUtils.getField(t3, "status")).isEqualTo(McpTransportStatus.CONNECT_FAILED);
      listenerRef.set(null);

      // --- transport t4: onFailure(null, null) when CONNECTING ---
      SseClientTransport t4 = newTransport("https://example.com/sse", null);
      t4.connect(m -> m, () -> { }).subscribe();
      EventSourceListener l4 = listenerRef.get();
      l4.onFailure(mockEs, null, null);
      assertThat(ReflectionTestUtils.getField(t4, "status")).isEqualTo(McpTransportStatus.CONNECT_FAILED);
      listenerRef.set(null);

      // --- transport t5: onFailure(SocketTimeout) when CONNECTED → reconnect ---
      Runnable disconnectHandler = mock(Runnable.class);
      SseClientTransport t5 = newTransport("https://example.com/sse", null);
      t5.connect(m -> m, disconnectHandler).subscribe();
      ReflectionTestUtils.setField(t5, "status", McpTransportStatus.CONNECTED);
      EventSourceListener l5 = listenerRef.get();
      l5.onFailure(mockEs, new SocketTimeoutException(), null);
      verify(disconnectHandler).run();
      assertThat(ReflectionTestUtils.getField(t5, "status")).isEqualTo(McpTransportStatus.CONNECT_FAILED);
      listenerRef.set(null);

      // --- transport t6: onClosed when CONNECTING → CONNECT_FAILED ---
      SseClientTransport t6 = newTransport("https://example.com/sse", null);
      t6.connect(m -> m, () -> { }).subscribe();
      EventSourceListener l6 = listenerRef.get();
      l6.onClosed(mockEs);
      assertThat(ReflectionTestUtils.getField(t6, "status")).isEqualTo(McpTransportStatus.CONNECT_FAILED);
      listenerRef.set(null);

      // --- transport t7: onFailure(EOFException) when CONNECTED → reconnect ---
      Runnable disconnectHandler2 = mock(Runnable.class);
      SseClientTransport t7 = newTransport("https://example.com/sse", null);
      t7.connect(m -> m, disconnectHandler2).subscribe();
      ReflectionTestUtils.setField(t7, "status", McpTransportStatus.CONNECTED);
      EventSourceListener l7 = listenerRef.get();
      l7.onFailure(mockEs, new EOFException(), null);
      verify(disconnectHandler2).run();
      listenerRef.set(null);

      // --- transport t8: handleSseEvent invalid JSON → sink.error ---
      SseClientTransport t8 = newTransport("https://example.com/sse", null);
      t8.connect(m -> m, () -> { }).subscribe();
      EventSourceListener l8 = listenerRef.get();
      l8.onEvent(mockEs, "9", McpConsts.EVENT_TYPE_MESSAGE, "not-valid-json");
      listenerRef.set(null);

      // --- transport t9: handleSseEvent when CLOSED → early return ---
      SseClientTransport t9 = newTransport("https://example.com/sse", null);
      t9.connect(m -> m, () -> { }).subscribe();
      EventSourceListener l9 = listenerRef.get();
      ReflectionTestUtils.setField(t9, "status", McpTransportStatus.CLOSED);
      l9.onEvent(mockEs, "10", McpConsts.EVENT_TYPE_ENDPOINT, "/messages");
      // status should still be CLOSED
      assertThat(ReflectionTestUtils.getField(t9, "status")).isEqualTo(McpTransportStatus.CLOSED);
      listenerRef.set(null);

      // --- transport t10: handleSseFailure when CLOSED → early return ---
      SseClientTransport t10 = newTransport("https://example.com/sse", null);
      t10.connect(m -> m, () -> { }).subscribe();
      EventSourceListener l10 = listenerRef.get();
      ReflectionTestUtils.setField(t10, "status", McpTransportStatus.CLOSED);
      l10.onFailure(mockEs, new RuntimeException("err"), null);
      // status should still be CLOSED
      assertThat(ReflectionTestUtils.getField(t10, "status")).isEqualTo(McpTransportStatus.CLOSED);
    }
  }

  // ----------------------------------------------------------------------
  // doSendMessage — 通过 sendMessage 调用，mock ModelHttpClient.client 字段
  // ----------------------------------------------------------------------

  @Test
  void sendMessage_success_completes() throws Exception {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    ReflectionTestUtils.setField(t, "messageEndpoint", HttpUrl.parse("https://example.com/messages"));
    ReflectionTestUtils.setField(t, "status", McpTransportStatus.CONNECTED);

    OkHttpClient mockClient = mock(OkHttpClient.class);
    Call mockCall = mock(Call.class);
    lenient().when(mockClient.newCall(any(okhttp3.Request.class))).thenReturn(mockCall);
    try {
      lenient().when(mockCall.execute()).thenReturn(buildOkResponse(200));
    } catch (IOException e) { throw new AssertionError(e); }
    setMockHttpClient(mockClient);

    try {
      StepVerifier.create(t.sendMessage(new JsonRpcRequest("ping", "1", null)))
        .verifyComplete();
    } finally {
      restoreHttpClient();
    }
  }

  @Test
  void sendMessage_errorStatus_returnsBssException() throws Exception {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    ReflectionTestUtils.setField(t, "messageEndpoint", HttpUrl.parse("https://example.com/messages"));
    ReflectionTestUtils.setField(t, "status", McpTransportStatus.CONNECTED);

    OkHttpClient mockClient = mock(OkHttpClient.class);
    Call mockCall = mock(Call.class);
    lenient().when(mockClient.newCall(any(okhttp3.Request.class))).thenReturn(mockCall);
    try {
      lenient().when(mockCall.execute()).thenReturn(buildOkResponse(500, "Internal Server Error"));
    } catch (IOException e) { throw new AssertionError(e); }
    setMockHttpClient(mockClient);

    try {
      StepVerifier.create(t.sendMessage(new JsonRpcRequest("ping", "1", null)))
        .verifyErrorSatisfies(e -> assertThat(e).isInstanceOf(BssException.class)
          .hasMessageContaining("status=500"));
    } finally {
      restoreHttpClient();
    }
  }

  @Test
  void sendMessage_ioException_returnsBssException() throws Exception {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    ReflectionTestUtils.setField(t, "messageEndpoint", HttpUrl.parse("https://example.com/messages"));
    ReflectionTestUtils.setField(t, "status", McpTransportStatus.CONNECTED);

    OkHttpClient mockClient = mock(OkHttpClient.class);
    Call mockCall = mock(Call.class);
    lenient().when(mockClient.newCall(any(okhttp3.Request.class))).thenReturn(mockCall);
    try {
      lenient().doThrow(new IOException("network error")).when(mockCall).execute();
    } catch (IOException e) { throw new AssertionError(e); }
    setMockHttpClient(mockClient);

    try {
      StepVerifier.create(t.sendMessage(new JsonRpcRequest("ping", "1", null)))
        .verifyErrorSatisfies(e -> assertThat(e).isInstanceOf(BssException.class)
          .hasMessageContaining("network error"));
    } finally {
      restoreHttpClient();
    }
  }

  @Test
  void sendMessage_accepted202_completes() throws Exception {
    SseClientTransport t = newTransport("https://example.com/sse", null);
    ReflectionTestUtils.setField(t, "messageEndpoint", HttpUrl.parse("https://example.com/messages"));
    ReflectionTestUtils.setField(t, "status", McpTransportStatus.CONNECTED);

    OkHttpClient mockClient = mock(OkHttpClient.class);
    Call mockCall = mock(Call.class);
    lenient().when(mockClient.newCall(any(okhttp3.Request.class))).thenReturn(mockCall);
    try {
      lenient().when(mockCall.execute()).thenReturn(buildOkResponse(202));
    } catch (IOException e) { throw new AssertionError(e); }
    setMockHttpClient(mockClient);

    try {
      StepVerifier.create(t.sendMessage(new JsonRpcRequest("ping", "1", null)))
        .verifyComplete();
    } finally {
      restoreHttpClient();
    }
  }
}
