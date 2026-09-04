package com.iwhalecloud.bote.controller.video;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.asr.RealtimeAsrRequestDTO;
import com.iwhalecloud.bote.dto.asr.RealtimeAsrResultDTO;
import com.iwhalecloud.bote.service.asr.realtime.RealtimeAsrHandler;
import com.iwhalecloud.bote.service.asr.realtime.RealtimeAsrFactory;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 实时语音识别 WebSocket 处理器
 * <p>
 * 消息格式：
 * <ul>
 *   <li>控制消息(JSON): {"action": "start", "provider": "takeAi"} 或 {"action": "stop"}</li>
 *   <li>音频数据(JSON): {"data": "base64编码的PCM数据", "start": 开始时间戳, "end": 结束时间戳}</li>
 * </ul>
 * </p>
 * <p>
 * 响应格式(JSON):
 * <ul>
 *   <li>识别结果: {"type": "result", "text": "识别文本", "isFinal": true/false}</li>
 *   <li>错误消息: {"type": "error", "message": "错误信息"}</li>
 *   <li>状态消息: {"type": "status", "status": "started"/"stopped"}</li>
 * </ul>
 * </p>
 *
 * @author qian.sisheng
 * @since 2026-01-09
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class RealtimeAsrWebSocketHandler extends TextWebSocketHandler {

  private static final Logger logger = LoggerFactory.getLogger(RealtimeAsrWebSocketHandler.class);

  private final RealtimeAsrFactory realtimeAsrFactory;

  /** 会话映射，key 为 WebSocket 会话 ID，value 为实时识别会话 */
  private final Map<String, SessionContext> sessionContextMap = new ConcurrentHashMap<>();

  public RealtimeAsrWebSocketHandler(RealtimeAsrFactory realtimeAsrFactory) {
    this.realtimeAsrFactory = realtimeAsrFactory;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void afterConnectionEstablished(WebSocketSession session) {
    logger.debug("WebSocket connection established: sessionId={}", session.getId());
    if (!realtimeAsrFactory.isProviderAvailable()) {
      handleError(session, "当前语音识别类型不支持实时识别");
      return;
    }
    // 初始化会话上下文
    SessionContext context = new SessionContext();
    sessionContextMap.put(session.getId(), context);
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
    String payload = message.getPayload();
    try {
      RealtimeAsrRequestDTO request = JsonUtil.parseJson(payload, RealtimeAsrRequestDTO.class);
      if (request == null) {
        handleError(session, "无效的 JSON 消息");
        return;
      }

      String action = request.getAction();

      if ("start".equals(action)) {
        handleStart(session);
      }
      else if ("stop".equals(action)) {
        handleStop(session);
      }
      else if (StringUtils.isNotEmpty(request.getData())) {
        // JSON 格式的音频数据
        handleAudioJson(session, request);
      }
      else {
        handleError(session, "未知的操作类型: " + action);
      }
    }
    catch (Exception e) {
      logger.error("Failed to handle text message: sessionId={}", session.getId(), e);
      handleError(session, "消息处理失败: " + e.getMessage());
    }
  }

  /**
   * 处理启动指令
   */
  private void handleStart(WebSocketSession session) {
    logger.debug("Handling start command: sessionId={}", session.getId());
    SessionContext context = sessionContextMap.get(session.getId());
    if (context == null) {
      logger.error("Session context not found: sessionId={}", session.getId());
      handleError(session, "会话上下文不存在");
      return;
    }
    if (context.realtimeAsrHandler != null) {
      logger.warn("Session already started: sessionId={}", session.getId());
      return;
    }
    try {
      String providerName = SystemParameter.VIDEO_ORC_TYPE.getValueFromDb();
      logger.debug("Creating realtime asr service: sessionId={}, provider={}", session.getId(), providerName);
      RealtimeAsrHandler realtimeAsrHandler = getRealtimeAsrHandler(session, providerName);
      context.realtimeAsrHandler = realtimeAsrHandler;
      realtimeAsrHandler.start();
      logger.debug("Realtime session started successfully: sessionId={}", session.getId());
      sendStatus(session, "started");
    }
    catch (Exception e) {
      logger.error("Failed to start realtime session: sessionId={}", session.getId(), e);
      handleError(session, "启动失败: " + e.getMessage());
    }
  }

  /**
   * 获取实时语音识别服务
   */
  private RealtimeAsrHandler getRealtimeAsrHandler(WebSocketSession session, String providerName) {
    // 创建实时语音识别服务
    return realtimeAsrFactory.getRealtimeAsrHandler(providerName, result -> {
      try {
        sendResult(session, result);
      }
      catch (IOException e) {
        logger.error("Failed to send recognition result: sessionId={}", session.getId(), e);
        closeSession(session);
      }
    }, error -> {
      logger.error("Realtime recognition error: sessionId={}", session.getId(), error);
      try {
        sendError(session, "识别错误: " + error.getMessage());
      }
      catch (IOException e) {
        logger.error("Failed to send error message: sessionId={}", session.getId(), e);
      }
      finally {
        closeSession(session);
      }
    });
  }

  /**
   * 处理停止指令
   */
  private void handleStop(WebSocketSession session) throws IOException {
    SessionContext context = sessionContextMap.get(session.getId());
    if (context == null || context.realtimeAsrHandler == null) {
      sendStatus(session, "stopped");
      return;
    }
    try {
      context.realtimeAsrHandler.stop();
      context.realtimeAsrHandler = null;
      sendStatus(session, "stopped");
      logger.debug("Realtime session stopped: sessionId={}", session.getId());
      session.close(CloseStatus.NORMAL);
    }
    catch (Exception e) {
      logger.error("Failed to stop realtime session: sessionId={}", session.getId(), e);
      handleError(session, "停止失败: " + e.getMessage());
    }
  }

  /**
   * 处理 JSON 格式的音频数据
   */
  private void handleAudioJson(WebSocketSession session, RealtimeAsrRequestDTO request) {
    SessionContext context = sessionContextMap.get(session.getId());
    if (context == null || context.realtimeAsrHandler == null) {
      handleError(session, "会话未启动，请先发送 start 指令");
      return;
    }

    String dataBase64 = request.getData();
    byte[] pcmData = Base64.getDecoder().decode(dataBase64);

    context.realtimeAsrHandler.sendAudio(pcmData, request.getStart(), request.getEnd());
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
    logger.debug("WebSocket connection closed: sessionId={}, status={}", session.getId(), status);
    SessionContext context = sessionContextMap.remove(session.getId());
    if (context != null && context.realtimeAsrHandler != null) {
      try {
        context.realtimeAsrHandler.close();
      }
      catch (Exception e) {
        logger.error("Failed to close realtime session: sessionId={}", session.getId(), e);
      }
    }
  }

  @Override
  public void handleTransportError(WebSocketSession session, @NonNull Throwable exception) {
    logger.error("WebSocket transport error: sessionId={}", session.getId(), exception);
    closeSession(session);
  }

  /**
   * 发送识别结果
   */
  private void sendResult(WebSocketSession session, RealtimeAsrResultDTO dto) throws IOException {
    if (!session.isOpen()) {
      return;
    }
    Map<String, Object> response = new HashMap<>();
    response.put("type", "result");
    response.put("text", dto.getText());
    response.put("finaled", dto.getFinaled());
    response.put("opration", StringUtils.isEmpty(dto.getOpration()) ? "replace" : dto.getOpration());
    session.sendMessage(new TextMessage(JsonUtil.toJsonString(response)));
  }

  /**
   * 发送错误消息
   */
  private void sendError(WebSocketSession session, String message) throws IOException {
    if (!session.isOpen()) {
      return;
    }
    Map<String, Object> response = new HashMap<>();
    response.put("type", "error");
    response.put("message", message);
    session.sendMessage(new TextMessage(JsonUtil.toJsonString(response)));
  }

  /**
   * 发送状态消息
   */
  private void sendStatus(WebSocketSession session, String status) throws IOException {
    if (!session.isOpen()) {
      return;
    }
    Map<String, Object> response = new HashMap<>();
    response.put("type", "status");
    response.put("status", status);
    session.sendMessage(new TextMessage(JsonUtil.toJsonString(response)));
  }

  /**
   * 处理致命错误并关闭连接
   */
  private void handleError(WebSocketSession session, String message) {
    try {
      sendError(session, message);
    }
    catch (IOException e) {
      logger.error("Failed to send error message: sessionId={}", session.getId(), e);
    }
    finally {
      closeSession(session);
    }
  }

  /**
   * 关闭 WebSocket 会话
   */
  private void closeSession(WebSocketSession session) {
    if (session != null && session.isOpen()) {
      try {
        session.close(CloseStatus.SERVER_ERROR);
      }
      catch (IOException e) {
        logger.error("Failed to close session: sessionId={}", session.getId(), e);
      }
    }
  }

  /**
   * 会话上下文
   */
  private static final class SessionContext {
    /** 实时识别会话 */
    RealtimeAsrHandler realtimeAsrHandler;
  }
}
