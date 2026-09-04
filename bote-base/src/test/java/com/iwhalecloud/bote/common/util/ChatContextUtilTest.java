package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * {@link ChatContextUtil} 单元测试
 *
 * <p>覆盖 ThreadLocal 会话标识的 set/get/clear 以及 null 处理分支。</p>
 */
class ChatContextUtilTest {

  @AfterEach
  void cleanUp() {
    ChatContextUtil.clear();
  }

  @Test
  void getChatSessionId_noValueSet_returnsNull() {
    assertThat(ChatContextUtil.getChatSessionId()).isNull();
  }

  @Test
  void setChatSessionId_nonNull_thenGetReturnsValue() {
    ChatContextUtil.setChatSessionId("session-123");
    assertThat(ChatContextUtil.getChatSessionId()).isEqualTo("session-123");
  }

  @Test
  void setChatSessionId_null_removesValue() {
    ChatContextUtil.setChatSessionId("session-123");
    ChatContextUtil.setChatSessionId(null);
    assertThat(ChatContextUtil.getChatSessionId()).isNull();
  }

  @Test
  void clear_removesValue() {
    ChatContextUtil.setChatSessionId("session-456");
    ChatContextUtil.clear();
    assertThat(ChatContextUtil.getChatSessionId()).isNull();
  }

  @Test
  void setChatSessionId_overwrite_replacesValue() {
    ChatContextUtil.setChatSessionId("old");
    ChatContextUtil.setChatSessionId("new");
    assertThat(ChatContextUtil.getChatSessionId()).isEqualTo("new");
  }
}
