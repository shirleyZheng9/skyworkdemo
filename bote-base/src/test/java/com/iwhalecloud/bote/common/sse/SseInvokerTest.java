package com.iwhalecloud.bote.common.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

/**
 * {@link SseInvoker} 单元测试。
 *
 * <p>覆盖非废弃构造方法：invoker 非空校验、invoke 委托、finish 回调（有/无回调）、
 * errorCallback 读写、toString。废弃的双参构造方法依赖 ModelHttpClient/SseUtil 静态状态，不在此覆盖。</p>
 */
@SuppressWarnings("unchecked")
class SseInvokerTest {

  @Test
  void constructor_nullInvoker_throws() {
    assertThatThrownBy(() -> new SseInvoker((Consumer<Consumer<SseEvent>>) null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("invoker");
  }

  @Test
  void invoke_delegatesToInvoker() {
    Consumer<Consumer<SseEvent>> invoker = mock(Consumer.class);
    SseInvoker sseInvoker = new SseInvoker(invoker);
    Consumer<SseEvent> partialHandler = mock(Consumer.class);

    sseInvoker.invoke(partialHandler);

    verify(invoker).accept(partialHandler);
  }

  @Test
  void finish_withCallback_invokesCallback() {
    Consumer<Consumer<SseEvent>> invoker = mock(Consumer.class);
    SseInvoker sseInvoker = new SseInvoker(invoker);
    AnswerDTO answer = mock(AnswerDTO.class);
    Consumer<AnswerDTO> finishCallback = mock(Consumer.class);
    sseInvoker.setFinishCallback(finishCallback);

    sseInvoker.finish(answer);

    verify(finishCallback).accept(answer);
  }

  @Test
  void finish_withoutCallback_isNoOp() {
    SseInvoker sseInvoker = new SseInvoker(mock(Consumer.class));
    // 未设置 finishCallback，调用 finish 不应抛异常
    sseInvoker.finish(mock(AnswerDTO.class));
  }

  @Test
  void finish_nullAnswer_withCallback_passesNull() {
    SseInvoker sseInvoker = new SseInvoker(mock(Consumer.class));
    Consumer<AnswerDTO> finishCallback = mock(Consumer.class);
    sseInvoker.setFinishCallback(finishCallback);

    sseInvoker.finish(null);

    verify(finishCallback).accept(null);
  }

  @Test
  void finishCallback_canBeOverriddenAndCleared() {
    SseInvoker sseInvoker = new SseInvoker(mock(Consumer.class));
    Consumer<AnswerDTO> first = mock(Consumer.class);
    sseInvoker.setFinishCallback(first);
    // 覆盖为 null 后 finish 不再触发回调
    sseInvoker.setFinishCallback(null);
    sseInvoker.finish(mock(AnswerDTO.class));
    verifyNoInteractions(first);
  }

  @Test
  void errorCallback_getterAndSetter() {
    SseInvoker sseInvoker = new SseInvoker(mock(Consumer.class));
    assertThat(sseInvoker.getErrorCallback()).isNull();
    Consumer<Throwable> errorHandler = mock(Consumer.class);
    sseInvoker.setErrorCallback(errorHandler);
    assertThat(sseInvoker.getErrorCallback()).isSameAs(errorHandler);
  }

  @Test
  void toString_returnsConstant() {
    SseInvoker sseInvoker = new SseInvoker(mock(Consumer.class));
    assertThat(sseInvoker.toString()).isEqualTo("sse invoker");
  }
}
