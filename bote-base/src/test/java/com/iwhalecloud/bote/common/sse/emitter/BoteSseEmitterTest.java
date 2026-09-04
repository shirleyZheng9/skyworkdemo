package com.iwhalecloud.bote.common.sse.emitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpResponse;

/**
 * {@link BoteSseEmitter} 单元测试。
 *
 * <p>覆盖构造方法注入的 clientId/chatApi、closed 状态标志（complete/completeWithError 置位）、
 * 以及 extendResponse 写入 X-Accel-Buffering 头。send 相关逻辑依赖 Spring 的 handler 初始化，
 * 不在此覆盖。</p>
 */
class BoteSseEmitterTest {

  @Test
  void constructor_storesClientIdAndChatApiFlag() {
    BoteSseEmitter emitter = new BoteSseEmitter(60000L, "client-1", true);
    assertThat(emitter.getClientId()).isEqualTo("client-1");
    assertThat(emitter.isChatApi()).isTrue();
  }

  @Test
  void constructor_supportsNullClientIdAndNonChatApi() {
    BoteSseEmitter emitter = new BoteSseEmitter(60000L, null, false);
    assertThat(emitter.getClientId()).isNull();
    assertThat(emitter.isChatApi()).isFalse();
  }

  @Test
  void isClosed_initiallyFalse() {
    BoteSseEmitter emitter = new BoteSseEmitter(60000L, "c", false);
    assertThat(emitter.isClosed()).isFalse();
    assertThat(emitter.getLastSentTime()).isPositive();
  }

  @Test
  void complete_marksClosed() {
    BoteSseEmitter emitter = new BoteSseEmitter(60000L, "c", false);
    emitter.complete();
    assertThat(emitter.isClosed()).isTrue();
  }

  @Test
  void completeWithError_marksClosedEvenIfSuperThrows() {
    // super.completeWithError 在未初始化 handler 时可能抛异常，但 closed 标志在调用前已置位
    BoteSseEmitter emitter = new BoteSseEmitter(60000L, "c", false);
    try {
      emitter.completeWithError(new RuntimeException("boom"));
    } catch (Exception ignored) {
      // 忽略 Spring 内部因 handler 未初始化抛出的异常
    }
    assertThat(emitter.isClosed()).isTrue();
  }

  @Test
  void extendResponse_setsNoBufferingHeader() {
    BoteSseEmitter emitter = new BoteSseEmitter(60000L, "c", false);
    ServerHttpResponse response = mock(ServerHttpResponse.class);
    HttpHeaders headers = new HttpHeaders();
    when(response.getHeaders()).thenReturn(headers);

    emitter.extendResponse(response);

    assertThat(headers.getFirst("X-Accel-Buffering")).isEqualTo("no");
    assertThat(emitter.isClosed()).isFalse();
  }
}
