package com.iwhalecloud.bote.mcp.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.mcp.client.transport.McpClientTransport;
import com.iwhalecloud.bote.mcp.client.transport.SseClientTransport;
import com.iwhalecloud.bote.mcp.consts.LoggingLevel;
import com.iwhalecloud.bote.mcp.consts.McpClientStatus;
import com.iwhalecloud.bote.mcp.consts.McpConsts;
import com.iwhalecloud.bote.mcp.consts.McpErrorCodes;
import com.iwhalecloud.bote.mcp.dto.McpTool;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcError;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcMessage;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcNotification;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcRequest;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcResponse;
import com.iwhalecloud.bote.mcp.dto.request.CallToolRequest;
import com.iwhalecloud.bote.mcp.dto.request.InitializeRequest;
import com.iwhalecloud.bote.mcp.dto.request.PaginatedRequest;
import com.iwhalecloud.bote.mcp.dto.request.SetLevelRequest;
import com.iwhalecloud.bote.mcp.dto.response.CallToolResult;
import com.iwhalecloud.bote.mcp.dto.response.InitializeResult;
import com.iwhalecloud.bote.mcp.dto.response.ListToolsResult;
import com.iwhalecloud.bote.mcp.dto.response.LoggingMessageNotification;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

/**
 * MCP 异步客户端
 *
 * @author bianjp
 * @since 2025-05-22
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings("PMD.GuardLogStatement")
public class McpClient implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(McpClient.class);

  /** 客户端名称（用于区分不同 MCP 服务） */
  @Getter
  private final String name;
  /** 日志中的客户端标识 */
  private final String clientId;
  /** 请求 ID 前缀，隔离不同会话 */
  private final String sessionPrefix;
  /** 请求计数器，用于生成唯一的请求 ID */
  private final AtomicLong requestCounter = new AtomicLong(0);
  /** 初始化超时时间 */
  private final Duration initializationTimeout;
  /** 请求超时时间 */
  private final Duration requestTimeout;
  /** 客户端功能 */
  private final McpClientFeatures features;
  /** 传输协议 */
  private final McpClientTransport transport;
  /** 待处理的响应，key 为请求 ID */
  private final Map<Object, MonoSink<JsonRpcResponse>> pendingResponses = new ConcurrentHashMap<>();
  /** 客户端状态 */
  private volatile McpClientStatus status = McpClientStatus.UNSET;
  /** 初始化锁 */
  private final Object initializationLock = new Object();
  /** 初始化结果 */
  private InitializeResult initializeResult;
  /** 工具列表缓存 */
  private final Supplier<List<McpTool>> toolsCache = Suppliers.memoizeWithExpiration(this::listAllTools, Duration.ofMinutes(5));

  /**
   * 构造 MCP 客户端
   *
   * @param name 客户端名称
   */
  public static McpClientBuilder builder(String name) {
    return new McpClientBuilder(name);
  }

  /**
   * 获取初始化结果
   */
  public InitializeResult getInitializeResult() {
    Assert.notNull(initializeResult, "MCP 客户端尚未初始化");
    return initializeResult;
  }

  /**
   * 关闭
   */
  @Override
  public void close() {
    logger.debug("Closing mcp client: {}", clientId);
    status = McpClientStatus.CLOSED;
    try {
      transport.close();
    }
    catch (Exception e) {
      logger.warn("Failed to close mcp client: {}", clientId, e);
    }
  }

  /**
   * 初始化
   */
  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  public void initialize() {
    synchronized (initializationLock) {
      // 关闭后不能再初始化
      if (status == McpClientStatus.CLOSED) {
        throw new BssException("MCP 客户端已关闭");
      }
      // 已初始化时直接返回
      if (isHealthy()) {
        return;
      }
      logger.debug("Initializing mcp client: {}", clientId);
      status = McpClientStatus.INITIALIZING;
      try {
        transport.connect(mono -> mono.doOnNext(this::handleMessage), this::handleDisconnected)
          // 发送初始化请求
          .then(Mono.defer(() -> {
            String latestVersion = transport instanceof SseClientTransport ? McpConsts.PROTOCOL_VERSION_2024_11_05 : McpConsts.PROTOCOL_VERSIONS.get(McpConsts.PROTOCOL_VERSIONS.size() - 1);
            InitializeRequest initializeRequest = new InitializeRequest(latestVersion, features.getClientCapabilities(), features.getClientInfo());
            return sendRequest(McpConsts.METHOD_INITIALIZE, initializeRequest, InitializeResult.class);
          }))
          // 发送初始化完成通知
          .flatMap(result -> {
            if (result == null) {
              return Mono.error(new BssException("MCP 客户端初始化失败"));
            }
            if (!McpConsts.PROTOCOL_VERSIONS.contains(result.getProtocolVersion())) {
              return Mono.error(new BssException("客户端不支持服务器返回的协议版本: " + result.getProtocolVersion()));
            }
            logger.debug("Received initialize response: client={}, protocol={}, capabilities={}, info={}, instructions={}", clientId,
              result.getProtocolVersion(), result.getCapabilities(), result.getServerInfo(), result.getInstructions());
            this.initializeResult = result;
            return transport.sendMessage(new JsonRpcNotification(McpConsts.METHOD_NOTIFICATION_INITIALIZED));
          })
          .timeout(initializationTimeout)
          .block();
        status = McpClientStatus.INITIALIZED;
      }
      catch (Throwable e) {
        status = McpClientStatus.INITIALIZE_FAILED;
        Throwable cause = Exceptions.unwrap(e);
        if (cause instanceof BssException) {
          logger.warn("Failed to initialize mcp client: client={}, error={}", clientId, e.getMessage());
          throw (BssException) cause;
        }
        if (cause instanceof TimeoutException) {
          logger.warn("Failed to initialize mcp client, timeout: client={}", clientId);
          throw new BssException("MCP 客户端初始化超时", e);
        }
        logger.warn("Failed to initialize mcp client: client={}", clientId, e);
        throw new BssException("MCP 客户端初始化失败: " + ExpUtil.getMsg(e), e);
      }
    }
  }

  /**
   * 处理传输协议异常断开，实现自动重连
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void handleDisconnected() {
    // 只处理初始化成功后的异常断开，不处理初始化过程中的断开事件
    if (status != McpClientStatus.INITIALIZED) {
      logger.warn("Ignore transport disconnected: client={}, status={}", clientId, status);
      return;
    }
    logger.debug("Transport disconnected, try reconnecting: client={}", clientId);
    ThreadPools.getMcp().submit(this::initialize);
  }

  /**
   * 处理服务器发给客户端的消息
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void handleMessage(JsonRpcMessage message) {
    // 服务器返回的回复
    if (message instanceof JsonRpcResponse) {
      JsonRpcResponse response = (JsonRpcResponse) message;
      logger.trace("Received mcp response: client={}, response={}", clientId, response);
      Object requestId = response.getId();
      if (requestId == null) {
        logger.warn("Unexpected mcp response, missing id: client={}, response={}", clientId, response);
        return;
      }
      MonoSink<JsonRpcResponse> sink = pendingResponses.remove(requestId);
      if (sink == null) {
        logger.warn("Unexpected mcp response id: client={}, response={}", clientId, response);
      }
      else {
        sink.success(response);
      }
    }
    // 服务器发送的请求
    else if (message instanceof JsonRpcRequest) {
      JsonRpcRequest request = (JsonRpcRequest) message;
      // 收到 ping 请求后需要发送一个空响应
      if (McpConsts.METHOD_PING.equals(((JsonRpcRequest) message).getMethod())) {
        logger.trace("Received mcp ping request: client={}, request={}", clientId, request);
        transport.sendMessage(new JsonRpcResponse(request.getId(), Collections.emptyMap())).subscribe();
        return;
      }
      // 不支持其它请求，直接发送错误响应
      logger.warn("Received mcp request: client={}, request={}", clientId, request);
      transport.sendMessage(new JsonRpcResponse(request.getId(), buildMethodNotFoundError(request.getMethod()))).subscribe();
    }
    // 服务器发送的通知
    else if (message instanceof JsonRpcNotification) {
      JsonRpcNotification notification = (JsonRpcNotification) message;
      logger.trace("Received mcp notification: client={}, notification={}", clientId, notification);
      handleServerNotification(notification)
        .doOnError(e -> logger.error("Failed to process mcp notification: client={}, notification={}", clientId, notification, e))
        .subscribe();
    }
    else {
      logger.warn("Received unknown mcp message type: client={}, message={}", clientId, message);
    }
  }

  /**
   * 构造找不到方法的错误
   */
  private JsonRpcError buildMethodNotFoundError(String method) {
    if (method.equals(McpConsts.METHOD_ROOTS_LIST)) {
      return new JsonRpcError(McpErrorCodes.METHOD_NOT_FOUND, "Roots not supported", ImmutableMap.of("reason", "Client does not have roots capability"));
    }
    return new JsonRpcError(McpErrorCodes.METHOD_NOT_FOUND, "Method not found: " + method);
  }

  /**
   * 发送请求
   *
   * @param <T> 响应类型
   * @param method 方法名称
   * @param requestParams 请求参数
   * @param responseType 响应类型
   */
  private <T> Mono<T> sendRequest(String method, @Nullable Object requestParams, Class<T> responseType) {
    String requestId = sessionPrefix + "-" + requestCounter.getAndIncrement();
    JsonRpcRequest request = new JsonRpcRequest(method, requestId, requestParams);
    return Mono.deferContextual(ctx -> Mono.<JsonRpcResponse>create(sink -> {
        // 等待初始化完成。初始化请求会在初始化过程中执行，不能检查
        if (!McpConsts.METHOD_INITIALIZE.equals(method)) {
          waitInitialized();
        }
        logger.trace("Send mcp request: client={}, method={}", clientId, method);
        pendingResponses.put(requestId, sink);
        transport.sendMessage(request)
          .contextWrite(ctx)
          .onErrorResume(e -> {
            // streamable 协议的 session 失效时，自动重新初始化再发送消息
            if (e instanceof BssException && ((BssException) e).getFailCode().equals(McpErrorCodes.SESSION_EXPIRED)) {
              logger.debug("Session expired, try reinitializing: client={}", clientId);
              try {
                waitInitialized();
              }
              catch (Exception e2) {
                // 忽略初始化失败，返回原始异常
                return Mono.error(e);
              }
              return transport.sendMessage(request).contextWrite(ctx);
            }
            return Mono.error(e);
          })
          .subscribe(
            v -> {
            },
            e -> {
              pendingResponses.remove(requestId);
              sink.error(e);
            });
      }))
      .timeout(requestTimeout)
      .handle((response, sink) -> {
        JsonRpcError error = response.getError();
        if (error != null) {
          logger.error("Failed to process mcp request: client={}, request={}, error={}", clientId, request, error);
          sink.error(new BssException("处理 MCP 请求失败: code=" + error.getCode() + ", message=" + error.getMessage()));
        }
        else {
          if (responseType == Void.class) {
            sink.complete();
          }
          else if (responseType.isInstance(response.getResult())) {
            sink.next(responseType.cast(response.getResult()));
          }
          else {
            sink.next(JsonUtil.convert(response.getResult(), responseType));
          }
        }
      });
  }

  /**
   * 发送 ping 请求
   */
  public void ping() {
    sendRequest(McpConsts.METHOD_PING, null, Object.class).block();
  }

  /**
   * 调用工具
   */
  public CallToolResult callTool(CallToolRequest callToolRequest) {
    // 过滤掉值为 null 的参数，某些 MCP 服务在非必填参数传了 null 时会报错，比如博查 bocha_web_search 的 freshness 参数
    Map<String, Object> arguments = callToolRequest.getArguments();
    if (MapUtils.isNotEmpty(arguments)) {
      arguments = JsonUtil.getCompactObjectMapper().convertValue(arguments, new TypeReference<Map<String, Object>>() {
      });
      callToolRequest.setArguments(arguments);
    }
    long startEpochMs = System.currentTimeMillis();
    CallToolResult callToolResult = null;
    Throwable error = null;
    try {
      callToolResult = sendRequest(McpConsts.METHOD_TOOLS_CALL, callToolRequest, CallToolResult.class).block();
      Assert.notNull(callToolResult, "调用 MCP 工具失败");
      return callToolResult;
    }
    catch (Exception e) {
      Throwable cause = Exceptions.unwrap(e);
      error = cause;
      if (cause instanceof BssException) {
        throw (BssException) cause;
      }
      if (cause instanceof TimeoutException) {
        logger.error("Failed to call MCP tool, timeout: client={}, tool={}", clientId, callToolRequest.getName());
        throw new BssException("调用 MCP 工具 " + callToolRequest.getName() + " 超时", e);
      }
      else {
        logger.error("Failed to call MCP tool: client={}, tool={}", clientId, callToolRequest.getName(), cause);
        throw new BssException("调用 MCP 工具 " + callToolRequest.getName() + " 失败: " + ExpUtil.getMsg(cause), e);
      }
    }
    finally {
      notifyToolCallObserver(callToolRequest, callToolResult, startEpochMs, error);
    }
  }

  private void notifyToolCallObserver(CallToolRequest request, @Nullable CallToolResult result, long startEpochMs, @Nullable Throwable error) {
    try {
      McpToolCallObserver observer = SpringUtil.getBeanOptional(McpToolCallObserver.class);
      if (observer != null) {
        observer.onToolCall(name, request, result, startEpochMs, System.currentTimeMillis(), error);
      }
    }
    catch (Exception e) {
      logger.debug("MCP tool call observer failed: {}", e.getMessage());
    }
  }

  /**
   * 获取服务器提供的所有工具列表，使用缓存
   */
  public List<McpTool> listAllToolsWithCache() {
    return toolsCache.get();
  }

  /**
   * 获取服务器提供的所有工具列表
   */
  public List<McpTool> listAllTools() {
    // TODO 可能需要递归调用才能确保找到所有分页的数据？
    return ListUtils.emptyIfNull(listTools(null).getTools());
  }

  /**
   * 分页获取服务器提供的工具列表
   *
   * @param cursor 分页游标
   */
  public ListToolsResult listTools(@Nullable String cursor) {
    ListToolsResult listToolsResult = listToolAsync(cursor).block();
    Assert.notNull(listToolsResult, "获取 MCP 工具列表失败");
    return listToolsResult;
  }

  /**
   * 异步获取服务器提供的工具列表
   */
  private Mono<ListToolsResult> listToolAsync(@Nullable String cursor) {
    return sendRequest(McpConsts.METHOD_TOOLS_LIST, new PaginatedRequest(cursor), ListToolsResult.class);
  }

  /**
   * 处理服务器发送的通知
   */
  private Mono<Void> handleServerNotification(JsonRpcNotification notification) {
    switch (notification.getMethod()) {
      case McpConsts.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED:
        return handleToolsChangeNotification();
      case McpConsts.METHOD_NOTIFICATION_MESSAGE:
        return handleLoggingNotification(notification.getParams());
      default:
        return Mono.empty();
    }
  }

  /**
   * 处理服务器发送的工具列表变化通知
   */
  private Mono<Void> handleToolsChangeNotification() {
    if (CollectionUtils.isEmpty(features.getToolsChangeConsumers())) {
      return Mono.empty();
    }
    return listToolAsync(null)
      .flatMap(listToolsResult -> Flux.fromIterable(features.getToolsChangeConsumers())
        .flatMap(consumer -> consumer.apply(listToolsResult.getTools()))
        .onErrorResume(error -> {
          logger.error("Error handling tools list change notification: client={}", clientId, error);
          return Mono.empty();
        })
        .then());
  }

  /**
   * 处理服务器发送的日志通知
   */
  private Mono<Void> handleLoggingNotification(@Nullable Object params) {
    if (params == null || CollectionUtils.isEmpty(features.getLoggingConsumers())) {
      return Mono.empty();
    }
    LoggingMessageNotification notification = JsonUtil.convert(params, LoggingMessageNotification.class);
    return Flux.fromIterable(features.getLoggingConsumers())
      .flatMap(consumer -> consumer.apply(notification))
      .then();
  }

  /**
   * 设置日志级别
   */
  public void setLoggingLevel(LoggingLevel loggingLevel) {
    SetLevelRequest params = new SetLevelRequest(loggingLevel);
    sendRequest(McpConsts.METHOD_LOGGING_SET_LEVEL, params, Object.class).block();
  }

  /**
   * 检查客户端是否健康
   */
  public boolean isHealthy() {
    return status == McpClientStatus.INITIALIZED && transport.isConnected();
  }

  /**
   * 等待初始化完成，如果已经断开则自动重连
   */
  public void waitInitialized() {
    if (status == McpClientStatus.CLOSED) {
      throw new BssException("MCP 客户端已关闭");
    }
    if (!isHealthy()) {
      initialize();
    }
  }

}
