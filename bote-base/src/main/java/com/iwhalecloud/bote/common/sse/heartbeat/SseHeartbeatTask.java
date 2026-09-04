package com.iwhalecloud.bote.common.sse.heartbeat;

import com.iwhalecloud.bote.cache.SseEmitterCache;
import com.iwhalecloud.bote.common.sse.emitter.BoteSseEmitter;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 心跳任务
 *
 * <p>使用 Server-Sent-Event 时，如果服务器端长时间未发送消息，可能会触发网关、客户端超时限制导致连接断开（比如 Nginx 的 proxy_read_timeout），通过定时发送心跳包避免。</p>
 *
 * @author bianjp
 * @since 2025-11-13
 */
@Component
@ConditionalOnBooleanProperty(name = "bote.sse.heartbeat.enabled", matchIfMissing = true)
public class SseHeartbeatTask {
  private static final Logger logger = LoggerFactory.getLogger(SseHeartbeatTask.class);

  /** SSE 触发器列表 */
  private final Queue<BoteSseEmitter> emitters = new ConcurrentLinkedQueue<>();
  /** 心跳包 */
  private final Set<DataWithMediaType> heartbeatPacket = SseEmitter.event().comment("ping").build();
  /** SSE 缓存 */
  private final SseEmitterCache sseEmitterCache;
  /** 超时时间(ms)，表示超过这个时间没有交互就需要发送心跳包 */
  private final long heartbeatTimeoutMills;

  public SseHeartbeatTask(SseEmitterCache sseEmitterCache, Environment environment) {
    this.sseEmitterCache = sseEmitterCache;
    long timeout = environment.getProperty("bote.sse.heartbeat.timeout", Long.class, 20L);
    long interval = environment.getProperty("bote.sse.heartbeat.interval", Long.class, 10L);
    Assert.isTrue(timeout >= 10L, "bote.sse.heartbeat.timeout must be greater than or equal to 10 seconds");
    Assert.isTrue(interval >= 10L, "bote.sse.heartbeat.interval must be greater than or equal to 10 seconds");
    this.heartbeatTimeoutMills = timeout * 1000;
  }

  /**
   * 添加 SSE 触发器
   */
  public void addEmitter(BoteSseEmitter emitter) {
    emitters.add(emitter);
  }

  /**
   * 定时发送心跳包
   */
  @Scheduled(fixedDelayString = "${bote.sse.heartbeat.interval:10}", initialDelay = 60, timeUnit = TimeUnit.SECONDS)
  @SuppressWarnings("PMD.GuardLogStatement")
  public void heartbeat() {
    if (emitters.isEmpty()) {
      return;
    }
    long now = System.currentTimeMillis();
    emitters.removeIf(emitter -> {
      // 已关闭的直接删除
      if (emitter.isClosed()) {
        return true;
      }
      // 只有超过心跳间隔没有发送消息时才需要发送心跳
      if (now - emitter.getLastSentTime() > heartbeatTimeoutMills) {
        try {
          emitter.send(heartbeatPacket);
        }
        catch (Exception e) {
          logger.info("Failed to ping SSE client: clientId={}, error={}", emitter.getClientId(), e.getMessage());
          // 会话接口特殊处理断开情况
          if (emitter.isChatApi()) {
            sseEmitterCache.setDisconnected(emitter.getClientId());
          }
          // 发送失败时删除
          return true;
        }
      }
      return false;
    });
  }
}
