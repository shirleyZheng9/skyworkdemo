package com.iwhalecloud.bote.websocket.context;

import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.chat.ChatWebSocketEvent;
import com.iwhalecloud.bote.dto.chat.ChatWebSocketEvent.ToolCallResponse;
import com.iwhalecloud.bote.dto.chat.ChatWebSocketEventType;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 会话上下文
 *
 * @author bianjp
 * @since 2026-03-23
 */
@Getter
@Setter
@SuppressWarnings("PMD.GuardLogStatement")
public class WebSocketChatContext {
  private static final Logger logger = LoggerFactory.getLogger(WebSocketChatContext.class);

  /** WebSocket 会话 */
  private final WebSocketSession session;
  /** 登录信息 */
  private final LoginInfo loginInfo;
  /** 是否正在处理会话请求 */
  private volatile boolean processing;
  /** 正在处理的会话任务 */
  @Nullable
  private Future<?> processFuture;
  /** 正在执行的工具调用任务，key 为任务 ID */
  private Map<String, CompletableFuture<ToolCallResponse>> toolCallTasks = new ConcurrentHashMap<>();

  public WebSocketChatContext(WebSocketSession session, LoginInfo loginInfo) {
    this.session = session;
    this.loginInfo = loginInfo;
  }

  /**
   * 调用工具
   */
  public String invokeTool(String toolName, @Nullable Object parameters) {
    if (!session.isOpen()) {
      throw new BssException("客户端已断开连接");
    }
    String id = UUID.randomUUID().toString();
    CompletableFuture<ToolCallResponse> future = new CompletableFuture<>();
    toolCallTasks.put(id, future);
    try {
      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("toolName", toolName);
      payload.put("parameters", parameters);
      ChatWebSocketEvent webSocketEvent = new ChatWebSocketEvent(id, ChatWebSocketEventType.TOOL_REQUEST.getCode(), payload);
      session.sendMessage(new TextMessage(JsonUtil.toJsonString(webSocketEvent)));
    }
    catch (Exception e) {
      logger.error("Failed to send tool call request: id={}, toolName={}", id, toolName, e);
      future.completeExceptionally(e);
      return "Error: 发送工具调用请求失败: " + ExpUtil.getMsg(e);
    }

    ToolCallResponse response;
    try {
      response = future.get(5, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      logger.error("Execute tool call interrupted: id={}, toolName={}", id, toolName, e);
      return "Error: 执行中断";
    }
    catch (ExecutionException e) {
      logger.error("Execute tool call failed: id={}, toolName={}", id, toolName, e);
      return "Error: 执行失败: " + ExpUtil.getMsg(e.getCause());
    }
    catch (TimeoutException e) {
      logger.error("Execute tool call timeout: id={}, toolName={}", id, toolName, e);
      return "Error: 执行超时";
    }

    if (Boolean.TRUE.equals(response.success())) {
      return response.result();
    }
    throw new ToolExecutionException("Error: " + StringUtils.defaultString(response.error()));
  }

  /**
   * 完成工具调用
   */
  public void completeToolCall(String id, ToolCallResponse response) {
    CompletableFuture<ToolCallResponse> future = toolCallTasks.remove(id);
    if (future != null) {
      future.complete(response);
    }
    else {
      logger.warn("Tool call not found: id={}", id);
    }
  }

  /**
   * 发送文本消息
   */
  public void sendMessage(String event, Object data) {
    if (!session.isOpen()) {
      throw new BssException("客户端已断开连接");
    }
    try {
      ChatWebSocketEvent webSocketEvent = new ChatWebSocketEvent(null, event, data);
      session.sendMessage(new TextMessage(JsonUtil.toJsonString(webSocketEvent)));
    }
    catch (IOException e) {
      logger.warn("Failed to send WebSocket message: sessionId={}, event={}", session.getId(), event, e);
      throw new BssException("发送 WebSocket 消息失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 发送二进制消息
   */
  public void sendBinaryMessage(BinaryMessage message) {
    if (!session.isOpen()) {
      throw new BssException("客户端已断开连接");
    }
    try {
      session.sendMessage(message);
    }
    catch (IOException e) {
      logger.warn("Failed to send WebSocket binary message: sessionId={}", session.getId(), e);
      throw new BssException("发送 WebSocket 二进制消息失败: " + ExpUtil.getMsg(e), e);
    }
  }
}
