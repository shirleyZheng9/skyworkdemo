package com.iwhalecloud.bote.websocket;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 心跳任务
 *
 * <p>WebSocket 连接在长时间无交互的场景下，可能被网关或客户端超时断开。通过定时发送 ping 帧可尽量保持连接活跃。</p>
 *
 * @author bianjp
 * @since 2026-04-15
 */
@Component
@ConditionalOnBooleanProperty(name = "bote.websocket.heartbeat.enabled", matchIfMissing = true)
@SuppressWarnings("PMD.GuardLogStatement")
public class WebSocketHeartbeatTask {
  private static final Logger logger = LoggerFactory.getLogger(WebSocketHeartbeatTask.class);

  /** WebSocket 会话队列 */
  private final Queue<WebSocketSessionHolder> holders = new ConcurrentLinkedQueue<>();
  /** 超时时间(ms)，表示超过这个时间没有发送过心跳就需要发送 ping */
  private final long heartbeatTimeoutMills;

  public WebSocketHeartbeatTask(Environment environment) {
    long timeout = environment.getProperty("bote.websocket.heartbeat.timeout", Long.class, 20L);
    long interval = environment.getProperty("bote.websocket.heartbeat.interval", Long.class, 10L);
    Assert.isTrue(timeout >= 10L, "bote.websocket.heartbeat.timeout must be greater than or equal to 10 seconds");
    Assert.isTrue(interval >= 10L, "bote.websocket.heartbeat.interval must be greater than or equal to 10 seconds");
    this.heartbeatTimeoutMills = timeout * 1000;
  }

  /**
   * 添加 WebSocket 会话
   */
  public void addSession(WebSocketSession session) {
    holders.add(new WebSocketSessionHolder(session));
  }

  /**
   * 定时发送心跳包
   */
  @Scheduled(fixedDelayString = "${bote.websocket.heartbeat.interval:10}", initialDelay = 60, timeUnit = TimeUnit.SECONDS)
  public void heartbeat() {
    if (holders.isEmpty()) {
      return;
    }
    long now = System.currentTimeMillis();
    PingMessage pingMessage = new PingMessage();
    holders.removeIf(holder -> {
      WebSocketSession session = holder.session;
      // 已断开会话直接剔除
      if (!session.isOpen()) {
        return true;
      }
      // 超过心跳超时阈值才发送 ping，避免过于频繁
      if (now - holder.lastHeartbeatTime > heartbeatTimeoutMills) {
        try {
          session.sendMessage(pingMessage);
          holder.lastHeartbeatTime = now;
        }
        catch (Exception e) {
          logger.info("Failed to ping WebSocket client: sessionId={}, error={}", session.getId(), e.getMessage());
          return true;
        }
      }
      return false;
    });
  }

  /**
   * WebSocket 会话持有者
   */
  private static final class WebSocketSessionHolder {
    final WebSocketSession session;
    /** 上次心跳检测时间 */
    long lastHeartbeatTime = System.currentTimeMillis();

    WebSocketSessionHolder(WebSocketSession session) {
      this.session = Objects.requireNonNull(session);
    }
  }
}
