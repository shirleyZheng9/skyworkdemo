package com.iwhalecloud.bote.doc.common.mybatis.toolkit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iwhalecloud.bote.doc.common.mybatis.exception.MybatisPlusException;
import org.junit.jupiter.api.Test;

/**
 * {@link ExceptionUtils} 单元测试。
 *
 * <p>覆盖 mpe 三个重载（消息+cause+参数 / 消息+参数 / cause）的格式化与包装、
 * throwMpe 条件成立抛异常与不成立放行。</p>
 */
class ExceptionUtilsTest {

  @Test
  void mpe_withMessageCauseAndParams_formatsAndWraps() {
    Throwable cause = new RuntimeException("root");
    MybatisPlusException ex = ExceptionUtils.mpe("hi %s", cause, "world");
    assertThat(ex.getMessage()).isEqualTo("hi world");
    assertThat(ex.getCause()).isSameAs(cause);
  }

  @Test
  void mpe_withMessageAndParams_formatsWithoutCause() {
    MybatisPlusException ex = ExceptionUtils.mpe("val=%d", 42);
    assertThat(ex.getMessage()).isEqualTo("val=42");
    assertThat(ex.getCause()).isNull();
  }

  @Test
  void mpe_withThrowable_wraps() {
    Throwable cause = new IllegalStateException("bad");
    MybatisPlusException ex = ExceptionUtils.mpe(cause);
    assertThat(ex.getCause()).isSameAs(cause);
  }

  @Test
  void throwMpe_true_throwsFormatted() {
    assertThatThrownBy(() -> ExceptionUtils.throwMpe(true, "err %s", "x"))
      .isInstanceOf(MybatisPlusException.class)
      .hasMessage("err x");
  }

  @Test
  void throwMpe_false_doesNotThrow() {
    ExceptionUtils.throwMpe(false, "should not throw %s", "x");
  }
}
