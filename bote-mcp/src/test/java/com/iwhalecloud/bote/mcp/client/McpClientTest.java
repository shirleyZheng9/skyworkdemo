package com.iwhalecloud.bote.mcp.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.iwhalecloud.bote.mcp.client.transport.McpClientTransport;
import com.iwhalecloud.bote.mcp.consts.McpClientStatus;
import com.iwhalecloud.bote.mcp.consts.McpConsts;
import com.iwhalecloud.bote.mcp.consts.McpErrorCodes;
import com.iwhalecloud.bote.mcp.dto.ClientCapabilities;
import com.iwhalecloud.bote.mcp.dto.Implementation;
import com.iwhalecloud.bote.mcp.dto.McpTool;
import com.iwhalecloud.bote.mcp.dto.ServerCapabilities;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcError;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcMessage;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcNotification;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcRequest;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcResponse;
import com.iwhalecloud.bote.mcp.dto.request.CallToolRequest;
import com.iwhalecloud.bote.mcp.dto.response.CallToolResult;
import com.iwhalecloud.bote.mcp.dto.response.InitializeResult;
import com.iwhalecloud.bote.mcp.dto.response.ListToolsResult;
import com.iwhalecloud.bote.mcp.dto.response.LoggingMessageNotification;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

/**
 * {@link McpClient} 单元测试
 *
 * <p>使用 Mockito 模拟 {@link McpClientTransport}，在 {@code sendMessage} 范围内自动构造响应并回送，
 * 从而覆盖初始化、ping、工具调用、工具列表、日志级别、关闭、健康检查、消息处理等全部主流程与异常分支。
 * 不依赖 Spring 环境。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class McpClientTest {

  @Mock
  private McpClientTransport transport;

  /** connect 时捕获的处理器，用于回送响应 */
  private final AtomicReference<Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>>> handlerRef = new AtomicReference<>();

  /** 响应构造器：根据请求方法返回响应。可被单个测试覆盖以模拟错误/超时等。 */
  private Function<JsonRpcRequest, JsonRpcResponse> responder = req -> {
    Object result = switch (req.getMethod()) {
      case McpConsts.METHOD_INITIALIZE -> buildInitResult(McpConsts.PROTOCOL_VERSION_2025_06_18);
      case McpConsts.METHOD_TOOLS_LIST -> new ListToolsResult();
      case McpConsts.METHOD_TOOLS_CALL -> new CallToolResult();
      default -> Collections.emptyMap();
    };
    return new JsonRpcResponse(req.getId(), result);
  };

  /** 是否在 sendMessage 时回送响应（默认 true；超时场景设为 false） */
  private boolean feedResponse = true;

  @BeforeEach
  void setUpTransport() {
    handlerRef.set(null);
    feedResponse = true;
    responder = req -> {
      Object result = switch (req.getMethod()) {
        case McpConsts.METHOD_INITIALIZE -> buildInitResult(McpConsts.PROTOCOL_VERSION_2025_06_18);
        case McpConsts.METHOD_TOOLS_LIST -> new ListToolsResult();
        case McpConsts.METHOD_TOOLS_CALL -> new CallToolResult();
        default -> Collections.emptyMap();
      };
      return new JsonRpcResponse(req.getId(), result);
    };

    lenient().when(transport.connect(any(), any())).thenAnswer(inv -> {
      handlerRef.set(inv.getArgument(0));
      return Mono.empty();
    });
    lenient().when(transport.sendMessage(any())).thenAnswer(inv -> {
      JsonRpcMessage msg = inv.getArgument(0);
      if (feedResponse && msg instanceof JsonRpcRequest req) {
        JsonRpcResponse resp = responder.apply(req);
        Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>> h = handlerRef.get();
        if (h != null) {
          h.apply(Mono.just(resp)).subscribe();
        }
      }
      return Mono.empty();
    });
    lenient().when(transport.isConnected()).thenReturn(true);
  }

  private McpClient newClient(Duration initTimeout, Duration reqTimeout) {
    McpClientFeatures features = new McpClientFeatures(
      new Implementation("test", "1.0.0"), ClientCapabilities.EMPTY, Collections.emptyList(), Collections.emptyList());
    McpClient client = new McpClient("test", "test@sess0001", "sess0001", initTimeout, reqTimeout, features, transport);
    return client;
  }

  private McpClient newInitializedClient() {
    McpClient client = newClient(Duration.ofSeconds(5), Duration.ofSeconds(5));
    client.initialize();
    return client;
  }

  private InitializeResult buildInitResult(String protocolVersion) {
    InitializeResult result = new InitializeResult();
    result.setProtocolVersion(protocolVersion);
    result.setServerInfo(new Implementation("server", "1.0.0"));
    result.setCapabilities(new ServerCapabilities());
    return result;
  }

  // ----------------------------------------------------------------------
  // initialize
  // ----------------------------------------------------------------------

  @Test
  void initialize_success_setsStatusAndResult() {
    McpClient client = newClient(Duration.ofSeconds(5), Duration.ofSeconds(5));

    client.initialize();

    assertThat(ReflectionTestUtils.getField(client, "status")).isEqualTo(McpClientStatus.INITIALIZED);
    assertThat(client.getInitializeResult().getProtocolVersion()).isEqualTo(McpConsts.PROTOCOL_VERSION_2025_06_18);
    assertThat(client.isHealthy()).isTrue();
    // connect 应被调用一次
    verify(transport, times(1)).connect(any(), any());
  }

  @Test
  void initialize_whenAlreadyInitialized_isNoop() {
    McpClient client = newInitializedClient();
    int before = org.mockito.Mockito.mockingDetails(transport).getInvocations().size();

    client.initialize();

    // 不应再次 connect
    verify(transport, times(1)).connect(any(), any());
  }

  @Test
  void initialize_whenClosed_throwsBssException() {
    McpClient client = newInitializedClient();
    ReflectionTestUtils.setField(client, "status", McpClientStatus.CLOSED);

    assertThatThrownBy(client::initialize)
      .isInstanceOf(BssException.class)
      .hasMessageContaining("MCP 客户端已关闭");
    assertThat(ReflectionTestUtils.getField(client, "status")).isEqualTo(McpClientStatus.CLOSED);
  }

  @Test
  void initialize_versionNotSupported_throwsBssException() {
    responder = req -> new JsonRpcResponse(req.getId(), buildInitResult("9999-99-99"));

    McpClient client = newClient(Duration.ofSeconds(5), Duration.ofSeconds(5));

    assertThatThrownBy(client::initialize)
      .isInstanceOf(BssException.class)
      .hasMessageContaining("客户端不支持服务器返回的协议版本");
    assertThat(ReflectionTestUtils.getField(client, "status")).isEqualTo(McpClientStatus.INITIALIZE_FAILED);
  }

  @Test
  void initialize_nullResult_throwsBssException() {
    responder = req -> new JsonRpcResponse(req.getId(), null);

    McpClient client = newClient(Duration.ofSeconds(5), Duration.ofSeconds(5));

    assertThatThrownBy(client::initialize)
      .isInstanceOf(BssException.class)
      .hasMessageContaining("MCP 客户端初始化失败");
  }

  @Test
  void initialize_connectError_throwsBssException() {
    org.mockito.Mockito.reset(transport);
    lenient().when(transport.connect(any(), any())).thenReturn(Mono.error(new RuntimeException("conn refused")));

    McpClient client = newClient(Duration.ofSeconds(5), Duration.ofSeconds(5));

    assertThatThrownBy(client::initialize)
      .isInstanceOf(BssException.class);
    assertThat(ReflectionTestUtils.getField(client, "status")).isEqualTo(McpClientStatus.INITIALIZE_FAILED);
  }

  @Test
  void initialize_timeout_throwsBssException() {
    // connect 完成但不回送任何响应，使初始化请求超时
    org.mockito.Mockito.reset(transport);
    lenient().when(transport.connect(any(), any())).thenAnswer(inv -> {
      handlerRef.set(inv.getArgument(0));
      return Mono.empty();
    });
    lenient().when(transport.sendMessage(any())).thenReturn(Mono.empty());
    lenient().when(transport.isConnected()).thenReturn(true);

    McpClient client = newClient(Duration.ofMillis(100), Duration.ofSeconds(5));

    assertThatThrownBy(client::initialize)
      .isInstanceOf(BssException.class)
      .hasMessageContaining("超时");
  }

  // ----------------------------------------------------------------------
  // ping
  // ----------------------------------------------------------------------

  @Test
  void ping_success() {
    McpClient client = newInitializedClient();
    client.ping();
    verify(transport, atLeastOnce()).sendMessage(any());
  }

  // ----------------------------------------------------------------------
  // callTool
  // ----------------------------------------------------------------------

  @Test
  void callTool_success_returnsResult() {
    McpClient client = newInitializedClient();
    CallToolResult expectedResult = new CallToolResult();
    responder = req -> {
      CallToolResult r = new CallToolResult();
      r.setIsError(false);
      return new JsonRpcResponse(req.getId(), r);
    };

    CallToolRequest request = new CallToolRequest("search", Map.of("q", "hello"));
    CallToolResult result = client.callTool(request);

    assertThat(result).isNotNull();
  }

  @Test
  void callTool_emptyArguments_sendsRequest() {
    McpClient client = newInitializedClient();
    CallToolRequest request = new CallToolRequest("tool1", Collections.emptyMap());
    CallToolResult result = client.callTool(request);
    assertThat(result).isNotNull();
  }

  @Test
  void callTool_errorResponse_throwsBssException() {
    McpClient client = newInitializedClient();
    responder = req -> new JsonRpcResponse(req.getId(),
      new JsonRpcError(McpErrorCodes.INTERNAL_ERROR, "boom"));

    assertThatThrownBy(() -> client.callTool(new CallToolRequest("tool1", null)))
      .isInstanceOf(BssException.class)
      .hasMessageContaining("处理 MCP 请求失败");
  }

  @Test
  void callTool_timeout_throwsBssException() {
    McpClient client = newClient(Duration.ofSeconds(5), Duration.ofMillis(50));
    client.initialize();
    feedResponse = false; // 不回送响应 → 超时

    assertThatThrownBy(() -> client.callTool(new CallToolRequest("slowTool", null)))
      .isInstanceOf(BssException.class)
      .hasMessageContaining("超时");
  }

  // ----------------------------------------------------------------------
  // listTools / listAllTools / cache
  // ----------------------------------------------------------------------

  @Test
  void listTools_success() {
    McpClient client = newInitializedClient();
    ListToolsResult expected = new ListToolsResult();
    McpTool tool = new McpTool();
    tool.setName("t1");
    expected.setTools(List.of(tool));
    responder = req -> new JsonRpcResponse(req.getId(), expected);

    ListToolsResult result = client.listTools(null);
    assertThat(result.getTools()).hasSize(1);
    assertThat(result.getTools().get(0).getName()).isEqualTo("t1");
  }

  @Test
  void listAllTools_returnsEmptyWhenNull() {
    McpClient client = newInitializedClient();
    // 默认 responder 返回空 ListToolsResult（tools 为 null）
    List<McpTool> tools = client.listAllTools();
    assertThat(tools).isEmpty();
  }

  @Test
  void listAllToolsWithCache_returnsCachedResult() {
    McpClient client = newInitializedClient();
    List<McpTool> first = client.listAllToolsWithCache();
    List<McpTool> second = client.listAllToolsWithCache();
    assertThat(first).isSameAs(second);
  }

  // ----------------------------------------------------------------------
  // setLoggingLevel
  // ----------------------------------------------------------------------

  @Test
  void setLoggingLevel_sendsRequest() {
    McpClient client = newInitializedClient();
    client.setLoggingLevel(com.iwhalecloud.bote.mcp.consts.LoggingLevel.INFO);
    verify(transport, atLeastOnce()).sendMessage(any());
  }

  // ----------------------------------------------------------------------
  // close / isHealthy / waitInitialized / getInitializeResult
  // ----------------------------------------------------------------------

  @Test
  void close_setsStatusAndCallsTransportClose() {
    McpClient client = newInitializedClient();
    client.close();
    assertThat(ReflectionTestUtils.getField(client, "status")).isEqualTo(McpClientStatus.CLOSED);
    verify(transport, atLeastOnce()).close();
  }

  @Test
  void close_whenTransportThrowsException_isSwallowed() {
    McpClient client = newInitializedClient();
    org.mockito.Mockito.doThrow(new RuntimeException("close error")).when(transport).close();
    // 不应抛出异常
    client.close();
    assertThat(ReflectionTestUtils.getField(client, "status")).isEqualTo(McpClientStatus.CLOSED);
  }

  @Test
  void isHealthy_beforeInit_isFalse() {
    McpClient client = newClient(Duration.ofSeconds(5), Duration.ofSeconds(5));
    assertThat(client.isHealthy()).isFalse();
  }

  @Test
  void isHealthy_whenTransportDisconnected_isFalse() {
    McpClient client = newInitializedClient();
    lenient().when(transport.isConnected()).thenReturn(false);
    assertThat(client.isHealthy()).isFalse();
  }

  @Test
  void waitInitialized_whenClosed_throwsBssException() {
    McpClient client = newInitializedClient();
    ReflectionTestUtils.setField(client, "status", McpClientStatus.CLOSED);
    assertThatThrownBy(client::waitInitialized)
      .isInstanceOf(BssException.class)
      .hasMessageContaining("MCP 客户端已关闭");
  }

  @Test
  void waitInitialized_whenNotHealthy_triggersInitialize() {
    McpClient client = newInitializedClient();
    // 模拟断开后重新初始化
    lenient().when(transport.isConnected()).thenReturn(false);
    client.waitInitialized();
    verify(transport, atLeastOnce()).connect(any(), any());
  }

  @Test
  void getInitializeResult_beforeInit_throws() {
    McpClient client = newClient(Duration.ofSeconds(5), Duration.ofSeconds(5));
    assertThatThrownBy(client::getInitializeResult)
      .isInstanceOf(IllegalArgumentException.class);
  }

  // ----------------------------------------------------------------------
  // handleMessage（私有方法，通过反射调用）
  // ----------------------------------------------------------------------

  @Test
  void handleMessage_responseWithNullId_logsWarn() {
    McpClient client = newInitializedClient();
    JsonRpcResponse response = new JsonRpcResponse();
    response.setId(null);
    ReflectionTestUtils.invokeMethod(client, "handleMessage", (Object) response);
    // 无 pending sink，不抛异常即通过
  }

  @Test
  void handleMessage_responseWithUnknownId_logsWarn() {
    McpClient client = newInitializedClient();
    JsonRpcResponse response = new JsonRpcResponse("unknown-id", Collections.emptyMap());
    ReflectionTestUtils.invokeMethod(client, "handleMessage", (Object) response);
  }

  @Test
  void handleMessage_pingRequest_repliesWithEmptyResponse() {
    McpClient client = newInitializedClient();
    JsonRpcRequest ping = new JsonRpcRequest(McpConsts.METHOD_PING, "ping-1", null);

    ReflectionTestUtils.invokeMethod(client, "handleMessage", (Object) ping);

    // 应发送一个空响应
    org.mockito.ArgumentCaptor<JsonRpcMessage> captor = org.mockito.ArgumentCaptor.forClass(JsonRpcMessage.class);
    verify(transport, atLeastOnce()).sendMessage(captor.capture());
    assertThat(captor.getAllValues()).anySatisfy(m -> {
      assertThat(m).isInstanceOf(JsonRpcResponse.class);
      assertThat(((JsonRpcResponse) m).getId()).isEqualTo("ping-1");
    });
  }

  @Test
  void handleMessage_unknownRequest_repliesWithMethodNotFoundError() {
    McpClient client = newInitializedClient();
    JsonRpcRequest request = new JsonRpcRequest("some/unknown", "req-1", null);

    ReflectionTestUtils.invokeMethod(client, "handleMessage", (Object) request);

    org.mockito.ArgumentCaptor<JsonRpcMessage> captor = org.mockito.ArgumentCaptor.forClass(JsonRpcMessage.class);
    verify(transport, atLeastOnce()).sendMessage(captor.capture());
    assertThat(captor.getAllValues()).anySatisfy(m -> {
      assertThat(m).isInstanceOf(JsonRpcResponse.class);
      JsonRpcResponse resp = (JsonRpcResponse) m;
      assertThat(resp.getError()).isNotNull();
      assertThat(resp.getError().getCode()).isEqualTo(McpErrorCodes.METHOD_NOT_FOUND);
    });
  }

  @Test
  void handleMessage_rootsListRequest_repliesWithMethodNotFoundErrorAndData() {
    McpClient client = newInitializedClient();
    JsonRpcRequest request = new JsonRpcRequest(McpConsts.METHOD_ROOTS_LIST, "req-2", null);

    ReflectionTestUtils.invokeMethod(client, "handleMessage", (Object) request);

    org.mockito.ArgumentCaptor<JsonRpcMessage> captor = org.mockito.ArgumentCaptor.forClass(JsonRpcMessage.class);
    verify(transport, atLeastOnce()).sendMessage(captor.capture());
    assertThat(captor.getAllValues()).anySatisfy(m -> {
      assertThat(m).isInstanceOf(JsonRpcResponse.class);
      JsonRpcResponse resp = (JsonRpcResponse) m;
      assertThat(resp.getError()).isNotNull();
      assertThat(resp.getError().getData()).isNotNull();
    });
  }

  @Test
  void handleMessage_toolsListChangedNotification_emptyConsumers_isNoop() {
    McpClient client = newInitializedClient();
    JsonRpcNotification notif = new JsonRpcNotification(McpConsts.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED);

    ReflectionTestUtils.invokeMethod(client, "handleMessage", (Object) notif);
    // 无消费者时不应抛异常
  }

  @Test
  void handleMessage_messageNotification_emptyConsumers_isNoop() {
    McpClient client = newInitializedClient();
    JsonRpcNotification notif = new JsonRpcNotification(McpConsts.METHOD_NOTIFICATION_MESSAGE,
      Map.of("level", "info", "data", "hello"));

    ReflectionTestUtils.invokeMethod(client, "handleMessage", (Object) notif);
  }

  @Test
  void handleMessage_unknownMessageType_logsWarn() {
    McpClient client = newInitializedClient();
    JsonRpcMessage unknown = new JsonRpcMessage() {
      @Override
      public String getJsonrpc() {
        return "2.0";
      }
    };
    ReflectionTestUtils.invokeMethod(client, "handleMessage", (Object) unknown);
  }

  @Test
  void handleMessage_toolsListChanged_withConsumer_invokesConsumer() {
    // 构造带消费者的客户端
    AtomicReference<List<McpTool>> captured = new AtomicReference<>();
    McpClientFeatures features = new McpClientFeatures(
      new Implementation("test", "1.0.0"), ClientCapabilities.EMPTY,
      List.of(tools -> { captured.set(tools); return Mono.empty(); }),
      Collections.emptyList());
    McpClient client = new McpClient("test", "test@sess0002", "sess0002",
      Duration.ofSeconds(5), Duration.ofSeconds(5), features, transport);
    client.initialize();

    ListToolsResult listResult = new ListToolsResult();
    McpTool tool = new McpTool();
    tool.setName("captured-tool");
    listResult.setTools(List.of(tool));
    responder = req -> new JsonRpcResponse(req.getId(), listResult);

    JsonRpcNotification notif = new JsonRpcNotification(McpConsts.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED);
    ReflectionTestUtils.invokeMethod(client, "handleMessage", (Object) notif);

    // 消费者在响应回送后同步触发；短暂等待以兼容调度差异
    waitFor(() -> captured.get() != null, 2000);
    assertThat(captured.get()).hasSize(1);
  }

  /** 简单轮询等待条件成立，最长 millis 毫秒 */
  private static void waitFor(java.util.function.BooleanSupplier condition, long millis) {
    long deadline = System.currentTimeMillis() + millis;
    while (System.currentTimeMillis() < deadline) {
      if (condition.getAsBoolean()) {
        return;
      }
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  @Test
  void handleMessage_messageNotification_withConsumer_invokesConsumer() {
    AtomicReference<LoggingMessageNotification> captured = new AtomicReference<>();
    McpClientFeatures features = new McpClientFeatures(
      new Implementation("test", "1.0.0"), ClientCapabilities.EMPTY, Collections.emptyList(),
      List.of(notif -> { captured.set(notif); return Mono.empty(); }));
    McpClient client = new McpClient("test", "test@sess0003", "sess0003",
      Duration.ofSeconds(5), Duration.ofSeconds(5), features, transport);
    client.initialize();

    JsonRpcNotification notif = new JsonRpcNotification(McpConsts.METHOD_NOTIFICATION_MESSAGE,
      Map.of("level", "info", "data", "hello"));
    ReflectionTestUtils.invokeMethod(client, "handleMessage", (Object) notif);

    waitFor(() -> captured.get() != null, 2000);
    assertThat(captured.get().getData()).isEqualTo("hello");
  }
}
