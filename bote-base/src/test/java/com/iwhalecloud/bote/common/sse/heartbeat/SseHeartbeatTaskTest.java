package com.iwhalecloud.bote.common.sse.heartbeat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.iwhalecloud.bote.cache.SseEmitterCache;
import com.iwhalecloud.bote.common.sse.emitter.BoteSseEmitter;
import java.io.IOException;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/**
 * {@link SseHeartbeatTask} 单元测试。
 *
 * <p>覆盖构造方法参数校验（timeout/interval &gt;= 10s）、心跳超时阈值计算、addEmitter，
 * 以及 heartbeat 的各分支：空队列直接返回、已关闭的 emitter 直接移除、近期活跃的不发心跳、
 * 超时且发送成功的保留、超时且发送失败的（会话接口标记断开并移除 / 非会话接口仅移除）。</p>
 *
 * <p>通过 spy(BoteSseEmitter) 控制 send(Set) 的成败，避免依赖 Spring 的 handler 初始化；
 * 通过反射将 lastSentTime 置为过期值以触发心跳发送。</p>
 */
class SseHeartbeatTaskTest {

  private SseEmitterCache sseEmitterCache;
  private SseHeartbeatTask task;

  @BeforeEach
  void setUp() {
    sseEmitterCache = mock(SseEmitterCache.class);
    task = newTask(20L, 10L);
  }

  private SseHeartbeatTask newTask(long timeoutSeconds, long intervalSeconds) {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("bote.sse.heartbeat.timeout", String.valueOf(timeoutSeconds));
    env.setProperty("bote.sse.heartbeat.interval", String.valueOf(intervalSeconds));
    return new SseHeartbeatTask(sseEmitterCache, env);
  }

  private long heartbeatTimeoutMillis(SseHeartbeatTask t) throws Exception {
    Field f = SseHeartbeatTask.class.getDeclaredField("heartbeatTimeoutMills");
    f.setAccessible(true);
    return f.getLong(t);
  }

  private void makeStale(BoteSseEmitter emitter) throws Exception {
    Field f = BoteSseEmitter.class.getDeclaredField("lastSentTime");
    f.setAccessible(true);
    f.setLong(emitter, System.currentTimeMillis() - 60_000L);
  }

  // ==================== 构造方法 ====================

  @Test
  void constructor_valid_setsHeartbeatTimeout() throws Exception {
    assertThat(heartbeatTimeoutMillis(task)).isEqualTo(20_000L);
  }

  @Test
  void constructor_timeoutTooSmall_throws() {
    assertThatThrownBy(() -> newTask(5L, 10L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("timeout");
  }

  @Test
  void constructor_intervalTooSmall_throws() {
    assertThatThrownBy(() -> newTask(20L, 5L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("interval");
  }

  @Test
  void constructor_usesDefaults_whenPropertiesAbsent() throws Exception {
    // 未设置属性时使用代码默认值 20s/10s，校验通过
    SseHeartbeatTask defaultTask = new SseHeartbeatTask(sseEmitterCache, new MockEnvironment());
    assertThat(heartbeatTimeoutMillis(defaultTask)).isEqualTo(20_000L);
  }

  // ==================== heartbeat ====================

  @Test
  void heartbeat_emptyQueue_returnsEarly() {
    task.heartbeat();
    verifyNoInteractions(sseEmitterCache);
  }

  @Test
  void heartbeat_closedEmitter_removedWithoutPing() throws Exception {
    BoteSseEmitter emitter = spy(new BoteSseEmitter(60_000L, "c1", true));
    emitter.complete();
    task.addEmitter(emitter);

    task.heartbeat();

    verify(emitter, never()).send(anySet());
    verify(sseEmitterCache, never()).setDisconnected(any());
  }

  @Test
  void heartbeat_recentEmitter_notPinged() throws Exception {
    BoteSseEmitter emitter = spy(new BoteSseEmitter(60_000L, "c1", true));
    task.addEmitter(emitter);

    task.heartbeat();

    // 新建 emitter 的 lastSentTime 为当前时间，未超过心跳阈值，不应发送心跳
    verify(emitter, never()).send(anySet());
  }

  @Test
  void heartbeat_staleEmitter_pingedAndKept() throws Exception {
    BoteSseEmitter emitter = spy(new BoteSseEmitter(60_000L, "c1", true));
    doNothing().when(emitter).send(anySet());
    makeStale(emitter);
    task.addEmitter(emitter);

    task.heartbeat();

    verify(emitter).send(anySet());
    // 发送成功不应标记断开
    verify(sseEmitterCache, never()).setDisconnected(any());
  }

  @Test
  void heartbeat_staleChatApi_sendFails_marksDisconnected() throws Exception {
    BoteSseEmitter emitter = spy(new BoteSseEmitter(60_000L, "c1", true));
    doThrow(new IOException("broken pipe")).when(emitter).send(anySet());
    makeStale(emitter);
    task.addEmitter(emitter);

    task.heartbeat();

    verify(emitter).send(anySet());
    verify(sseEmitterCache).setDisconnected("c1");
  }

  @Test
  void heartbeat_staleNonChatApi_sendFails_removedWithoutDisconnect() throws Exception {
    BoteSseEmitter emitter = spy(new BoteSseEmitter(60_000L, "c2", false));
    doThrow(new IOException("broken pipe")).when(emitter).send(anySet());
    makeStale(emitter);
    task.addEmitter(emitter);

    task.heartbeat();

    verify(emitter).send(anySet());
    verify(sseEmitterCache, never()).setDisconnected(any());
  }

  @Test
  void addEmitter_addsToQueueSoHeartbeatCanPing() throws Exception {
    BoteSseEmitter emitter = spy(new BoteSseEmitter(60_000L, "c1", true));
    doNothing().when(emitter).send(anySet());
    makeStale(emitter);

    // 未 add 前 heartbeat 不应触及该 emitter
    task.heartbeat();
    verify(emitter, never()).send(anySet());

    task.addEmitter(emitter);
    task.heartbeat();
    verify(emitter).send(anySet());
  }
}
