package com.iwhalecloud.bote.llm.client.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link LlmTraceUtil} 单元测试
 *
 * <p>覆盖 ThreadLocal traceId 的 get/set/clear 与线程隔离</p>
 */
class LlmTraceUtilTest {

  @BeforeEach
  void setUp() {
    LlmTraceUtil.clearTraceId();
  }

  @Test
  void getTraceId_defaultNull() {
    assertThat(LlmTraceUtil.getTraceId()).isNull();
  }

  @Test
  void setAndGet() {
    LlmTraceUtil.setTraceId("t1");
    assertThat(LlmTraceUtil.getTraceId()).isEqualTo("t1");
  }

  @Test
  void setNull_clears() {
    LlmTraceUtil.setTraceId("t1");
    assertThat(LlmTraceUtil.getTraceId()).isEqualTo("t1");

    LlmTraceUtil.setTraceId(null);
    assertThat(LlmTraceUtil.getTraceId()).isNull();
  }

  @Test
  void clearTraceId() {
    LlmTraceUtil.setTraceId("t1");
    assertThat(LlmTraceUtil.getTraceId()).isEqualTo("t1");

    LlmTraceUtil.clearTraceId();
    assertThat(LlmTraceUtil.getTraceId()).isNull();
  }

  @Test
  void threadIsolation() throws ExecutionException, InterruptedException {
    // 主线程设置 traceId
    LlmTraceUtil.setTraceId("main-trace");

    // 子线程应看不到主线程设置的 traceId（ThreadLocal 线程隔离）
    // 使用新线程而非线程池，避免池线程复用导致 ThreadLocal 污染
    String childTraceId = CompletableFuture.supplyAsync(() -> {
      String value = LlmTraceUtil.getTraceId();
      LlmTraceUtil.clearTraceId();
      return value;
    }).get();

    assertThat(childTraceId).isNull();
    // 主线程仍然有值
    assertThat(LlmTraceUtil.getTraceId()).isEqualTo("main-trace");
  }

  @Test
  void threadIsolation_childSetDoesNotAffectMain() throws ExecutionException, InterruptedException {
    LlmTraceUtil.setTraceId("main-trace");

    // 子线程设置自己的 traceId
    CompletableFuture.runAsync(() -> {
      try {
        LlmTraceUtil.setTraceId("child-trace");
      } finally {
        // 清理子线程 ThreadLocal，避免污染线程池中被后续测试复用
        LlmTraceUtil.clearTraceId();
      }
    }).get();

    // 主线程不受影响
    assertThat(LlmTraceUtil.getTraceId()).isEqualTo("main-trace");
  }
}
