package com.iwhalecloud.bote.common.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.cache.SseEmitterCache;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.emitter.BoteSseEmitter;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

/**
 * {@link SseUtil} 单元测试。
 *
 * <p>SseUtil 的静态字段在类加载时通过 {@code SpringUtil.getBean(SseEmitterCache.class)} 获取缓存，
 * 故在 @BeforeAll 中以 mockStatic(SpringUtil) 注入 mock 缓存并触发类加载，使 sseEmitterCache 字段持有该 mock。
 * 之后覆盖：clientId 的 ThreadLocal 读写、completeQuietly、sendText/sendJson 正常路径与各类异常分支
 * （断开抛 BssException / 其它异常抛 BssException / ERROR 与 [DONE] 忽略失败）、以及会话接口（chatApi）
 * 下 closed/disconnected 提前返回与发送失败时标记断开等分支。createSseEmitter 依赖线程池与异步执行，不在此覆盖。</p>
 */
class SseUtilTest {

  private static MockedStatic<SpringUtil> spring;
  private static SseEmitterCache cache;

  @BeforeAll
  static void setUp() {
    cache = mock(SseEmitterCache.class);
    spring = mockStatic(SpringUtil.class);
    spring.when(() -> SpringUtil.getBean(SseEmitterCache.class)).thenReturn(cache);
    // JsonUtil 静态初始化时通过 SpringUtil.getBean(ObjectMapper.class, Supplier) 获取 ObjectMapper，
    // 这里注入真实实例，使 sendJson 的字符串序列化路径可在测试中加载 JsonUtil
    spring.when(() -> SpringUtil.getBean(eq(ObjectMapper.class), any())).thenReturn(new ObjectMapper());
    // 触发 SseUtil 类加载，使其静态字段 sseEmitterCache 持有上面的 mock
    SseUtil.getClientId();
  }

  @AfterAll
  static void tearDown() {
    spring.close();
  }

  @AfterEach
  void clearClientId() {
    SseUtil.removeClientId();
  }

  @BeforeEach
  void resetCacheInvocations() {
    // cache 为类级共享 mock，会话接口分支测试会累积 setDisconnected 调用，
    // 每个用例前清空调用记录，保证 verify(never()) 仅校验当前用例
    clearInvocations(cache);
  }

  // ==================== clientId ThreadLocal ====================

  @Test
  void getClientId_initiallyNull() {
    assertThat(SseUtil.getClientId()).isNull();
  }

  @Test
  void setClientId_thenGet() {
    SseUtil.setClientId("client-1");
    assertThat(SseUtil.getClientId()).isEqualTo("client-1");
  }

  @Test
  void setClientId_null_clears() {
    SseUtil.setClientId("client-1");
    SseUtil.setClientId(null);
    assertThat(SseUtil.getClientId()).isNull();
  }

  @Test
  void removeClientId_clears() {
    SseUtil.setClientId("client-1");
    SseUtil.removeClientId();
    assertThat(SseUtil.getClientId()).isNull();
  }

  // ==================== completeQuietly ====================

  @Test
  void completeQuietly_callsComplete() {
    SseEmitter emitter = mock(SseEmitter.class);
    SseUtil.completeQuietly(emitter);
    verify(emitter).complete();
  }

  @Test
  void completeQuietly_swallowsException() {
    SseEmitter emitter = mock(SseEmitter.class);
    doThrow(new RuntimeException("boom")).when(emitter).complete();
    // 不应抛出异常
    SseUtil.completeQuietly(emitter);
    verify(emitter).complete();
  }

  // ==================== sendText / sendJson 正常路径 ====================

  @Test
  void sendText_withMsgType_sendsEvent() throws Exception {
    SseEmitter emitter = mock(SseEmitter.class);
    SseUtil.sendText(emitter, ChatMessageType.TEXT, "hi");
    verify(emitter).send(any(SseEventBuilder.class));
  }

  @Test
  void sendText_withNullEventName_sendsWithoutName() throws Exception {
    SseEmitter emitter = mock(SseEmitter.class);
    SseUtil.sendText(emitter, (String) null, "hi");
    verify(emitter).send(any(SseEventBuilder.class));
  }

  @Test
  void sendJson_withMsgType_sendsJsonEvent() throws Exception {
    SseEmitter emitter = mock(SseEmitter.class);
    SseUtil.sendJson(emitter, ChatMessageType.TEXT, new Object());
    verify(emitter).send(any(SseEventBuilder.class));
  }

  @Test
  void sendJson_withIdAndMsgType_sendsJsonEvent() throws Exception {
    SseEmitter emitter = mock(SseEmitter.class);
    SseUtil.sendJson(emitter, ChatMessageType.TEXT, "evt-1", new Object());
    verify(emitter).send(any(SseEventBuilder.class));
  }

  @Test
  void sendJson_stringData_isSerializedToJsonString() throws Exception {
    // 字符串类型数据会被 JsonUtil.toJsonString 转换，以保留换行符
    SseEmitter emitter = mock(SseEmitter.class);
    SseUtil.sendJson(emitter, ChatMessageType.TEXT, "plain");
    verify(emitter).send(any(SseEventBuilder.class));
  }

  // ==================== send 异常分支（非会话接口：clientId 为空） ====================

  @Test
  void send_disconnectedViaBssException_throwsBssException() throws Exception {
    SseEmitter emitter = mock(SseEmitter.class);
    doThrow(new BssException("客户端已断开")).when(emitter).send(any(SseEventBuilder.class));

    assertThatThrownBy(() -> SseUtil.sendText(emitter, ChatMessageType.TEXT, "hi"))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("客户端已断开");
  }

  @Test
  void send_disconnectedViaIllegalState_throwsBssException() throws Exception {
    SseEmitter emitter = mock(SseEmitter.class);
    doThrow(new IllegalStateException("ResponseBodyEmitter has already completed, foo"))
        .when(emitter).send(any(SseEventBuilder.class));

    assertThatThrownBy(() -> SseUtil.sendText(emitter, ChatMessageType.TEXT, "hi"))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("客户端已断开");
  }

  @Test
  void send_otherException_throwsBssException() throws Exception {
    SseEmitter emitter = mock(SseEmitter.class);
    doThrow(new RuntimeException("boom")).when(emitter).send(any(SseEventBuilder.class));

    assertThatThrownBy(() -> SseUtil.sendText(emitter, ChatMessageType.TEXT, "hi"))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("发送 SSE 消息失败");
  }

  @Test
  void send_errorEvent_ignoresFailure() throws Exception {
    SseEmitter emitter = mock(SseEmitter.class);
    doThrow(new RuntimeException("boom")).when(emitter).send(any(SseEventBuilder.class));

    // 错误事件发送失败时仅记录日志，不抛异常
    SseUtil.sendJson(emitter, ChatMessageType.ERROR, "err");
    verify(emitter).send(any(SseEventBuilder.class));
  }

  @Test
  void send_doneText_ignoresFailure() throws Exception {
    SseEmitter emitter = mock(SseEmitter.class);
    doThrow(new RuntimeException("boom")).when(emitter).send(any(SseEventBuilder.class));

    // [DONE] 文本发送失败时仅记录日志，不抛异常
    SseUtil.sendText(emitter, ChatMessageType.TEXT, "[DONE]");
    verify(emitter).send(any(SseEventBuilder.class));
  }

  // ==================== 会话接口（chatApi）分支 ====================

  @Test
  void send_chatApiClosed_returnsEarlyWithoutSending() throws Exception {
    BoteSseEmitter emitter = mock(BoteSseEmitter.class);
    when(emitter.isChatApi()).thenReturn(true);
    when(emitter.getClientId()).thenReturn("c1");
    when(emitter.isClosed()).thenReturn(true);

    SseUtil.sendText(emitter, ChatMessageType.TEXT, "hi");

    verify(emitter, never()).send(any(SseEventBuilder.class));
    verify(cache, never()).setDisconnected(any());
  }

  @Test
  void send_chatApiDisconnected_returnsEarlyWithoutSending() throws Exception {
    BoteSseEmitter emitter = mock(BoteSseEmitter.class);
    when(emitter.isChatApi()).thenReturn(true);
    when(emitter.getClientId()).thenReturn("c1");
    when(emitter.isClosed()).thenReturn(false);
    when(cache.isDisconnected("c1")).thenReturn(true);

    SseUtil.sendText(emitter, ChatMessageType.TEXT, "hi");

    verify(emitter, never()).send(any(SseEventBuilder.class));
  }

  @Test
  void send_chatApiNormal_sendsWithoutMarkingDisconnected() throws Exception {
    BoteSseEmitter emitter = mock(BoteSseEmitter.class);
    when(emitter.isChatApi()).thenReturn(true);
    when(emitter.getClientId()).thenReturn("c1");
    when(emitter.isClosed()).thenReturn(false);
    when(cache.isDisconnected("c1")).thenReturn(false);

    SseUtil.sendText(emitter, ChatMessageType.TEXT, "hi");

    verify(emitter).send(any(SseEventBuilder.class));
    verify(cache, never()).setDisconnected(any());
  }

  @Test
  void send_chatApiSendFailsDisconnected_marksDisconnectedWithoutThrowing() throws Exception {
    BoteSseEmitter emitter = mock(BoteSseEmitter.class);
    when(emitter.isChatApi()).thenReturn(true);
    when(emitter.getClientId()).thenReturn("c1");
    when(emitter.isClosed()).thenReturn(false);
    when(cache.isDisconnected("c1")).thenReturn(false);
    doThrow(new BssException("客户端已断开")).when(emitter).send(any(SseEventBuilder.class));

    // 会话接口客户端断开时静默标记，不中断流程
    SseUtil.sendText(emitter, ChatMessageType.TEXT, "hi");

    verify(emitter).send(any(SseEventBuilder.class));
    verify(cache).setDisconnected("c1");
  }
}
