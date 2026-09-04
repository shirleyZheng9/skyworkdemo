package com.iwhalecloud.bote.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * {@link BeyondSseEvent} 单元测试
 *
 * <p>覆盖 of(code) 的命中/空/未命中三分支与 getCode。</p>
 */
class BeyondSseEventTest {

  @Test
  void of_knownCode_returnsEvent() {
    assertThat(BeyondSseEvent.of("moduleStatus")).isSameAs(BeyondSseEvent.MODULE_STATUS);
    assertThat(BeyondSseEvent.of("answerDelta")).isSameAs(BeyondSseEvent.ANSWER_DELTA);
    assertThat(BeyondSseEvent.of("appStreamResponse")).isSameAs(BeyondSseEvent.END);
  }

  @Test
  void of_emptyCode_returnsNull() {
    assertThat(BeyondSseEvent.of(null)).isNull();
    assertThat(BeyondSseEvent.of("")).isNull();
    assertThat(BeyondSseEvent.of("  ")).isNull();
  }

  @Test
  void of_unknownCode_returnsNull() {
    assertThat(BeyondSseEvent.of("no-such-code")).isNull();
  }

  @Test
  void getCode_returnsCode() {
    assertThat(BeyondSseEvent.REASONING_START.getCode()).isEqualTo("reasoningLogStart");
    assertThat(BeyondSseEvent.ERROR.getCode()).isEqualTo("error");
  }
}
