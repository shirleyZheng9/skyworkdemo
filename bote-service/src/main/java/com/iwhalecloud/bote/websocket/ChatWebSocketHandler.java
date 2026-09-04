package com.iwhalecloud.bote.websocket;

import com.iwhalecloud.bote.agent.agents.GeneralAgent;
import com.iwhalecloud.bote.agent.event.handlers.WebSocketAgentEventHandler;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.interceptor.WebSocketSessionInterceptor;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.ChatContextUtil;
import com.iwhalecloud.bote.common.util.CustomizedSensitiveWordUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SensitiveWordUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestMessageDTO;
import com.iwhalecloud.bote.dto.chat.ChatWebSocketEvent;
import com.iwhalecloud.bote.dto.chat.ChatWebSocketEvent.ToolCallResponse;
import com.iwhalecloud.bote.dto.chat.ChatWebSocketEventType;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.llm.client.util.LlmTraceUtil;
import com.iwhalecloud.bote.observability.LangfuseTracingService;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.websocket.context.WebSocketChatContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 智能体对话 WebSocket 处理器
 *
 * @author bianjp
 * @since 2026-03-23
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public final class ChatWebSocketHandler extends TextWebSocketHandler {
  private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

  private final IRefreshCacheService refreshCacheService;
  private final WebSocketHeartbeatTask webSocketHeartbeatTask;
  private final LangfuseTracingService langfuseTracingService;

  /** 会话映射，key 为 WebSocket 会话 ID，value 为会话上下文 */
  private final Map<String, WebSocketChatContext> sessionContextMap = new ConcurrentHashMap<>();

  public ChatWebSocketHandler(IRefreshCacheService refreshCacheService,
                              ObjectProvider<WebSocketHeartbeatTask> webSocketHeartbeatTaskProvider,
                              LangfuseTracingService langfuseTracingService) {
    this.refreshCacheService = refreshCacheService;
    this.webSocketHeartbeatTask = webSocketHeartbeatTaskProvider.getIfAvailable();
    this.langfuseTracingService = langfuseTracingService;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    logger.debug("Chat WebSocket connection established: sessionId={}", session.getId());
    LoginInfo loginInfo = (LoginInfo) session.getAttributes().get(WebSocketSessionInterceptor.ATTRIBUTE_LOGIN_INFO);
    if (loginInfo == null) {
      throw new BssException("未登录");
    }
    // 包装会话，确保线程安全
    WebSocketSession decoratedSession = new ConcurrentWebSocketSessionDecorator(session, 10000, 524288);
    WebSocketChatContext context = new WebSocketChatContext(decoratedSession, loginInfo);
    sessionContextMap.put(session.getId(), context);
    if (webSocketHeartbeatTask != null) {
      webSocketHeartbeatTask.addSession(decoratedSession);
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    logger.debug("Chat WebSocket connection closed: sessionId={}, status={}", session.getId(), status);
    sessionContextMap.remove(session.getId());
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    logger.error("Chat WebSocket transport error: sessionId={}", session.getId(), exception);
    if (session.isOpen()) {
      try {
        session.close(CloseStatus.SERVER_ERROR);
      }
      catch (Exception e) {
        logger.error("Failed to close session: sessionId={}", session.getId(), e);
      }
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    WebSocketChatContext context = sessionContextMap.get(session.getId());
    if (context == null) {
      logger.error("WebSocket context not found for session: sessionId={}", session.getId());
      sendError(session, null, "会话上下文不存在");
      return;
    }
    session = context.getSession();

    // 解析消息
    String payload = message.getPayload();
    ChatWebSocketEvent event;
    try {
      event = JsonUtil.getObjectMapper().readValue(payload, ChatWebSocketEvent.class);
    }
    catch (Exception e) {
      logger.error("Invalid JSON message: sessionId={}, payload={}", session.getId(), payload, e);
      sendError(session, null, "消息解析失败");
      return;
    }
    ChatWebSocketEventType eventType = ChatWebSocketEventType.of(event.type());
    if (eventType == null) {
      logger.error("Invalid event type: sessionId={}, payload={}", session.getId(), payload);
      sendError(session, event.id(), "无效的事件类型: " + event.type());
      return;
    }

    if (eventType == ChatWebSocketEventType.CHAT_REQUEST) {
      handleChatRequest(context, session, event);
    }
    else if (eventType == ChatWebSocketEventType.CHAT_CANCEL) {
      handleChatCancel(context, session, event);
    }
    else if (eventType == ChatWebSocketEventType.TOOL_RESPONSE) {
      handleToolCallResponse(context, session, event);
    }
    else if (eventType == ChatWebSocketEventType.ERROR) {
      logger.warn("Received error event: sessionId={}, payload={}", session.getId(), payload);
    }
    else {
      logger.error("Invalid client event type: sessionId={}, error={}", session.getId(), event.payload());
      sendError(session, event.id(), "不支持的客户端事件类型: " + event.type());
    }
  }

  /**
   * 处理对话请求
   */
  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  private void handleChatRequest(WebSocketChatContext context, WebSocketSession session, ChatWebSocketEvent event) {
    // 如果当前正在处理请求，拒绝新请求
    if (context.isProcessing()) {
      sendError(session, event.id(), "当前有正在处理的请求，请等待完成，或手动取消");
      return;
    }

    context.setProcessing(true);
    try {
      // 解析请求
      ChatRequestDTO request = JsonUtil.convert(event.payload(), ChatRequestDTO.class);
      // 验证请求
      validateRequest(request);

      // 异步执行，避免阻塞 WebSocket 处理线程
      context.setProcessFuture(ThreadPools.getSse().submit(() -> {
        // 此处不需要清理线程本地变量，InheritContextTaskDecorator 会做清理
        SessionUtil.setLoginInfo(context.getLoginInfo());
        ChatContextUtil.setChatSessionId(request.getSessionId() + "");
        String traceId = langfuseTracingService.beginAgentTurn(
          request.getTraceId(), "chat", request.getMessage(), request.getSessionId());
        request.setTraceId(traceId);
        ChatContext chatContext = new ChatContext(request, null, context.getLoginInfo().getUserId());
        WebSocketAgentEventHandler eventHandler = new WebSocketAgentEventHandler(context);
        try {
          GeneralAgent generalAgent = new GeneralAgent(chatContext, context, eventHandler);
          generalAgent.execute();
        }
        catch (Exception e) {
          logger.error("Failed to handle chat message: sessionId={}", session.getId(), e);
          sendError(session, event.id(), "消息处理失败: " + ExpUtil.getMsg(e));
        }
        finally {
          try {
            eventHandler.complete();
          }
          catch (Exception e2) {
            logger.warn("Failed to send done message: sessionId={}", session.getId(), e2);
          }
          langfuseTracingService.endAgentTurn(null);
          LlmTraceUtil.clearTraceId();
          context.setProcessing(false);
          chatContext.complete();
        }
      }));
    }
    catch (IllegalArgumentException e) {
      context.setProcessing(false);
      sendError(session, event.id(), e.getMessage());
    }
    catch (Exception e) {
      context.setProcessing(false);
      logger.error("Failed to handle chat message: sessionId={}", session.getId(), e);
      sendError(session, event.id(), "消息处理失败: " + e.getMessage());
    }
    catch (Throwable e) {
      context.setProcessing(false);
      logger.error("Failed to handle chat message: sessionId={}", session.getId(), e);
      throw e;
    }
  }

  /**
   * 处理取消对话
   */
  private void handleChatCancel(WebSocketChatContext context, WebSocketSession session, ChatWebSocketEvent event) {
    if (event.payload() instanceof Map<?, ?> payload) {
      String clientId = Objects.toString(payload.get("clientId"), null);
      if (StringUtils.isNotBlank(clientId)) {
        refreshCacheService.refresh(CacheConsts.CACHE_NAME_SSE_EMITTER, clientId);
        Future<?> future = context.getProcessFuture();
        if (future != null) {
          future.cancel(true);
          context.setProcessing(false);
        }
        return;
      }
    }
    logger.warn("Invalid cancel event, missing clientId: sessionId={}, event={}", session.getId(), event);
  }

  /**
   * 处理工具调用响应
   */
  private void handleToolCallResponse(WebSocketChatContext context, WebSocketSession session, ChatWebSocketEvent event) {
    ToolCallResponse response;
    try {
      response = JsonUtil.getObjectMapper().convertValue(event.payload(), ToolCallResponse.class);
    }
    catch (Exception e) {
      logger.error("Invalid tool call response event: sessionId={}, event={}", session.getId(), event, e);
      sendError(session, event.id(), "非法的工具调用响应事件: " + e.getMessage());
      return;
    }

    if (StringUtils.isEmpty(event.id())) {
      logger.error("Invalid tool call response event, missing id: sessionId={}, event={}", session.getId(), event);
      sendError(session, event.id(), "工具调用响应事件的 id 不能为空");
    }

    context.completeToolCall(event.id(), response);
  }

  /**
   * 校验请求对象
   */
  private void validateRequest(ChatRequestDTO request) {
    Assert.notNull(request.getSessionId(), "sessionId 不能为空");
    Assert.hasLength(request.getOs(), "os 不能为空");
    Assert.hasLength(request.getWorkDir(), "workDir 不能为空");
    ChatRequestMessageDTO message = request.getMessage();
    Assert.notNull(message, "message 不能为空");
    Assert.isTrue(StringUtils.isNotBlank(message.getContent()) || CollectionUtils.isNotEmpty(message.getFileIds()), "消息内容或文件不能同时为空");

    Assert.isTrue(!SensitiveWordUtil.isSensitive(message.getContent()), "消息包含敏感词");
    CustomizedSensitiveWordUtil.checkSensitive(message.getContent(), request.getTenantId(), BaseConsts.SECURITY_TYPE_USER_INPUT);
  }

  /**
   * 发送错误消息
   */
  private void sendError(WebSocketSession session, @Nullable String id, String message) {
    if (!session.isOpen()) {
      return;
    }
    try {
      ChatWebSocketEvent event = new ChatWebSocketEvent(id, ChatWebSocketEventType.ERROR.getCode(), Map.of("message", message));
      session.sendMessage(new TextMessage(JsonUtil.toJsonString(event)));
    }
    catch (Exception e) {
      logger.warn("Failed to send error message: sessionId={}, message={}", session.getId(), message, e);
    }
  }

}
