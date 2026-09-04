package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * {@link ExpUtil} 单元测试
 *
 * <p>覆盖 hasCause 的 cause 链遍历（直接匹配、嵌套 cause、自引用防护、不匹配）
 * 以及 getMsg 的 null/空消息/根因消息分支。</p>
 */
class ExpUtilTest {

  @Test
  void hasCause_directInstance_returnsTrue() {
    IOException ex = new IOException("io error");
    assertThat(ExpUtil.hasCause(ex, IOException.class)).isTrue();
  }

  @Test
  void hasCause_nestedCause_returnsTrue() {
    IllegalStateException root = new IllegalStateException("root");
    RuntimeException wrapper = new RuntimeException("wrapper", root);
    assertThat(ExpUtil.hasCause(wrapper, IllegalStateException.class)).isTrue();
  }

  @Test
  void hasCause_deepNestedCause_returnsTrue() {
    IllegalArgumentException deepest = new IllegalArgumentException("deepest");
    Exception mid = new Exception("mid", deepest);
    Exception top = new Exception("top", mid);
    assertThat(ExpUtil.hasCause(top, IllegalArgumentException.class)).isTrue();
  }

  @Test
  void hasCause_notInChain_returnsFalse() {
    RuntimeException ex = new RuntimeException("only me");
    assertThat(ExpUtil.hasCause(ex, IllegalStateException.class)).isFalse();
  }

  @Test
  void hasCause_noCauseInChain_returnsFalse() {
    RuntimeException ex = new RuntimeException("only me");
    assertThat(ExpUtil.hasCause(ex, NullPointerException.class)).isFalse();
  }

  @Test
  void getMsg_nullThrowable_returnsEmptyString() {
    assertThat(ExpUtil.getMsg(null)).isEmpty();
  }

  @Test
  void getMsg_nonNullMessage_returnsMessage() {
    Exception ex = new RuntimeException("something went wrong");
    assertThat(ExpUtil.getMsg(ex)).isEqualTo("something went wrong");
  }

  @Test
  void getMsg_emptyMessage_returnsRootCauseMessage() {
    NullPointerException root = new NullPointerException("root cause msg");
    Exception ex = new Exception("", root);
    // ExceptionUtils.getRootCauseMessage 返回 "NullPointerException: root cause msg"
    assertThat(ExpUtil.getMsg(ex)).contains("root cause msg");
  }

  @Test
  void getMsg_nullMessage_returnsRootCauseMessage() {
    NullPointerException root = new NullPointerException("npe msg");
    Exception ex = new Exception(null, root);
    // ExceptionUtils.getRootCauseMessage returns "NullPointerException: npe msg"
    assertThat(ExpUtil.getMsg(ex)).contains("npe msg");
  }
}
