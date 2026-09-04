package com.iwhalecloud.bote.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import java.util.List;
import java.util.concurrent.Future;
import okhttp3.Call;
import okhttp3.WebSocket;
import okhttp3.sse.EventSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * {@link SseEmitterCache} 单元测试。
 *
 * <p>SseEmitterCache 自行持有三个 Guava 缓存（资源 / 取消标志 / 断开标志），不依赖 Spring 或 Redis，
 * 可直接构造测试。覆盖 addResource 的空值短路、isCancelled/isDisconnected/setDisconnected/invalidate、
 * refreshLocalCache 的 size 校验与资源释放开关（Future/EventSource/SseEmitter/Call/WebSocket/
 * AutoCloseable/未知类型），以及 refresh 委托。</p>
 */
class SseEmitterCacheTest {

  private SseEmitterCache cache;

  @BeforeEach
  void setUp() {
    cache = new SseEmitterCache();
  }

  @Test
  void getCacheName_returnsConstant() {
    assertThat(cache.getCacheName()).isEqualTo(CacheConsts.CACHE_NAME_SSE_EMITTER);
  }

  @Test
  void isDistributedCacheEnabled_returnsFalse() {
    assertThat(cache.isDistributedCacheEnabled()).isFalse();
  }

  // ==================== addResource ====================

  @Test
  void addResource_nullClientId_isNoop() {
    cache.addResource(null, new Object());
    // 无异常即视为通过
    assertThat(cache.isCancelled("c1")).isFalse();
  }

  @Test
  void addResource_nullResource_isNoop() {
    cache.addResource("c1", null);
    assertThat(cache.isCancelled("c1")).isFalse();
  }

  @Test
  void addResource_valid_pushesToHead() {
    cache.addResource("c1", "a");
    cache.addResource("c1", "b");
    // 倒序存储：后加入的在前；刷新时按迭代顺序释放
    cache.refresh(List.of("c1"));
    // 刷新后资源缓存被清除，且客户端被标记为取消
    assertThat(cache.isCancelled("c1")).isTrue();
  }

  // ==================== isCancelled / isDisconnected / setDisconnected ====================

  @Test
  void isCancelled_nullClientId_returnsFalse() {
    assertThat(cache.isCancelled(null)).isFalse();
  }

  @Test
  void isCancelled_absent_returnsFalse() {
    assertThat(cache.isCancelled("c1")).isFalse();
  }

  @Test
  void setDisconnected_valid_marksDisconnected() {
    assertThat(cache.isDisconnected("c1")).isFalse();
    cache.setDisconnected("c1");
    assertThat(cache.isDisconnected("c1")).isTrue();
  }

  @Test
  void setDisconnected_empty_isNoop() {
    cache.setDisconnected("");
    cache.setDisconnected(null);
    assertThat(cache.isDisconnected("")).isFalse();
    assertThat(cache.isDisconnected(null)).isFalse();
  }

  @Test
  void isDisconnected_nullClientId_returnsFalse() {
    assertThat(cache.isDisconnected(null)).isFalse();
  }

  // ==================== invalidate ====================

  @Test
  void invalidate_removesResources() {
    cache.addResource("c1", "a");
    cache.invalidate("c1");
    // 刷新时无资源应直接返回，不标记取消
    cache.refresh(List.of("c1"));
    assertThat(cache.isCancelled("c1")).isFalse();
  }

  // ==================== refresh / refreshLocalCache ====================

  @Test
  void refresh_sizeNotOne_isNoop() {
    cache.addResource("c1", "a");
    cache.refresh(List.of("c1", "c2"));
    // size != 1 直接返回，不释放、不标记取消
    assertThat(cache.isCancelled("c1")).isFalse();
  }

  @Test
  void refreshLocalCache_sizeNotOne_isNoop() {
    cache.addResource("c1", "a");
    cache.refreshLocalCache(List.of());
    assertThat(cache.isCancelled("c1")).isFalse();
  }

  @Test
  void refreshLocalCache_noResources_isNoop() {
    cache.refreshLocalCache(List.of("absent"));
    assertThat(cache.isCancelled("absent")).isFalse();
  }

  @Test
  void refresh_delegatesToRefreshLocalCache() {
    cache.addResource("c1", "a");
    cache.refresh(List.of("c1"));
    assertThat(cache.isCancelled("c1")).isTrue();
  }

  @Test
  void refresh_closesFutureResource() {
    Future<?> future = mock(Future.class);
    cache.addResource("c1", future);
    cache.refresh(List.of("c1"));
    verify(future).cancel(true);
  }

  @Test
  void refresh_closesEventSourceResource() {
    EventSource eventSource = mock(EventSource.class);
    cache.addResource("c1", eventSource);
    cache.refresh(List.of("c1"));
    verify(eventSource).cancel();
  }

  @Test
  void refresh_closesSseEmitterResource() {
    SseEmitter emitter = mock(SseEmitter.class);
    cache.addResource("c1", emitter);
    cache.refresh(List.of("c1"));
    verify(emitter).complete();
  }

  @Test
  void refresh_closesCallResource() {
    Call call = mock(Call.class);
    cache.addResource("c1", call);
    cache.refresh(List.of("c1"));
    verify(call).cancel();
  }

  @Test
  void refresh_closesWebSocketResource() {
    WebSocket webSocket = mock(WebSocket.class);
    cache.addResource("c1", webSocket);
    cache.refresh(List.of("c1"));
    verify(webSocket).cancel();
  }

  @Test
  void refresh_closesAutoCloseableResource() throws Exception {
    AutoCloseable closeable = mock(AutoCloseable.class);
    cache.addResource("c1", closeable);
    cache.refresh(List.of("c1"));
    verify(closeable).close();
  }

  @Test
  void refresh_unsupportedResourceType_doesNotThrow() {
    cache.addResource("c1", "unsupported-string");
    assertThatCode(() -> cache.refresh(List.of("c1"))).doesNotThrowAnyException();
    assertThat(cache.isCancelled("c1")).isTrue();
  }

  @Test
  void refresh_closeFailure_continuesAndMarksCancelled() {
    Future<?> failing = mock(Future.class);
    doThrow(new RuntimeException("boom")).when(failing).cancel(true);
    cache.addResource("c1", failing);
    cache.refresh(List.of("c1"));
    verify(failing).cancel(true);
    assertThat(cache.isCancelled("c1")).isTrue();
  }

  @Test
  void refresh_mixedResources_closesEach() {
    Future<?> future = mock(Future.class);
    SseEmitter emitter = mock(SseEmitter.class);
    cache.addResource("c1", future);
    cache.addResource("c1", emitter);
    cache.refresh(List.of("c1"));
    verify(future).cancel(true);
    verify(emitter).complete();
  }

  @Test
  void refresh_closeFailure_doesNotAffectSubsequentResources() {
    Future<?> failing = mock(Future.class);
    doThrow(new RuntimeException("boom")).when(failing).cancel(true);
    SseEmitter emitter = mock(SseEmitter.class);
    cache.addResource("c1", failing);
    cache.addResource("c1", emitter);
    cache.refresh(List.of("c1"));
    verify(emitter).complete();
  }
}
