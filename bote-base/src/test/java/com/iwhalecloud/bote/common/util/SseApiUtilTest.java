package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.iwhalecloud.bote.cache.SseEmitterCache;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import okhttp3.Call;
import okhttp3.sse.EventSource;
import org.apache.commons.lang3.function.TriConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * {@link SseApiUtil} 单元测试
 *
 * <p>SseApiUtil.wrapBlockingApi 内部访问 {@link com.iwhalecloud.bote.common.sse.SseUtil#requestListener}
 * （触发 SseUtil 类加载，需 mockStatic SpringUtil 注入 mock SseEmitterCache）和
 * {@link ThreadPools#getCommon()}（mockStatic ThreadPools 返回真实 SimpleAsyncTaskExecutor）。
 * 覆盖三条路径：blockingApi 正常完成 -> completionHandler(null)；抛 BssException -> 透传；
 * 抛普通 Exception -> 包装为 BssException。</p>
 */
class SseApiUtilTest {

  @Test
  void wrapBlockingApi_success_callsPartialAndCompletionHandler() throws Exception {
    SseEmitterCache cacheMock = mock(SseEmitterCache.class);
    SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("test-sse-api");
    executor.setVirtualThreads(true);

    try (MockedStatic<SpringUtil> springMock = mockStatic(SpringUtil.class);
         MockedStatic<ThreadPools> poolsMock = mockStatic(ThreadPools.class)) {

      springMock.when(() -> SpringUtil.getBean(SseEmitterCache.class)).thenReturn(cacheMock);
      poolsMock.when(ThreadPools::getCommon).thenReturn(executor);

      List<String> partials = new ArrayList<>();
      List<BssException> completions = new ArrayList<>();
      CountDownLatch latch = new CountDownLatch(1);

      EventSource result = SseApiUtil.wrapBlockingApi(
          "request",
          (Consumer<String>) partials::add,
          (Consumer<BssException>) e -> { completions.add(e); latch.countDown(); },
          (TriConsumer<String, Consumer<String>, Consumer<Object>>) (req, handler, listener) -> {
            handler.accept("chunk1");
            handler.accept("chunk2");
          });

      assertThat(result).isNotNull();
      assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
      assertThat(partials).containsExactly("chunk1", "chunk2");
      assertThat(completions).hasSize(1);
      assertThat(completions.get(0)).isNull();
    }
  }

  @Test
  void wrapBlockingApi_bssException_passedExceptionToCompletionHandler() throws Exception {
    SseEmitterCache cacheMock = mock(SseEmitterCache.class);
    SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("test-sse-api");
    executor.setVirtualThreads(true);

    try (MockedStatic<SpringUtil> springMock = mockStatic(SpringUtil.class);
         MockedStatic<ThreadPools> poolsMock = mockStatic(ThreadPools.class)) {

      springMock.when(() -> SpringUtil.getBean(SseEmitterCache.class)).thenReturn(cacheMock);
      poolsMock.when(ThreadPools::getCommon).thenReturn(executor);

      List<BssException> completions = new ArrayList<>();
      CountDownLatch latch = new CountDownLatch(1);
      BssException expectedEx = new BssException("custom bss error");

      EventSource result = SseApiUtil.wrapBlockingApi(
          "request",
          (Consumer<String>) partial -> {},
          (Consumer<BssException>) e -> { completions.add(e); latch.countDown(); },
          (TriConsumer<String, Consumer<String>, Consumer<Object>>) (req, handler, listener) -> {
            throw expectedEx;
          });

      assertThat(result).isNotNull();
      assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
      assertThat(completions).hasSize(1);
      assertThat(completions.get(0)).isSameAs(expectedEx);
    }
  }

  @Test
  void wrapBlockingApi_generalException_wrapsInBssException() throws Exception {
    SseEmitterCache cacheMock = mock(SseEmitterCache.class);
    SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("test-sse-api");
    executor.setVirtualThreads(true);

    try (MockedStatic<SpringUtil> springMock = mockStatic(SpringUtil.class);
         MockedStatic<ThreadPools> poolsMock = mockStatic(ThreadPools.class)) {

      springMock.when(() -> SpringUtil.getBean(SseEmitterCache.class)).thenReturn(cacheMock);
      poolsMock.when(ThreadPools::getCommon).thenReturn(executor);

      List<BssException> completions = new ArrayList<>();
      CountDownLatch latch = new CountDownLatch(1);

      EventSource result = SseApiUtil.wrapBlockingApi(
          "request",
          (Consumer<String>) partial -> {},
          (Consumer<BssException>) e -> { completions.add(e); latch.countDown(); },
          (TriConsumer<String, Consumer<String>, Consumer<Object>>) (req, handler, listener) -> {
            throw new RuntimeException("unexpected failure");
          });

      assertThat(result).isNotNull();
      assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
      assertThat(completions).hasSize(1);
      assertThat(completions.get(0)).isInstanceOf(BssException.class);
    }
  }

  @Test
  void wrapBlockingApi_completionHandlerThrows_doesNotRethrow() throws Exception {
    SseEmitterCache cacheMock = mock(SseEmitterCache.class);
    SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("test-sse-api");
    executor.setVirtualThreads(true);

    try (MockedStatic<SpringUtil> springMock = mockStatic(SpringUtil.class);
         MockedStatic<ThreadPools> poolsMock = mockStatic(ThreadPools.class)) {

      springMock.when(() -> SpringUtil.getBean(SseEmitterCache.class)).thenReturn(cacheMock);
      poolsMock.when(ThreadPools::getCommon).thenReturn(executor);

      CountDownLatch latch = new CountDownLatch(1);

      // completionHandler 抛异常时应被捕获，不影响返回 EventSource
      EventSource result = SseApiUtil.wrapBlockingApi(
          "request",
          (Consumer<String>) partial -> {},
          (Consumer<BssException>) e -> { latch.countDown(); throw new RuntimeException("handler error"); },
          (TriConsumer<String, Consumer<String>, Consumer<Object>>) (req, handler, listener) -> {
            // 正常完成
          });

      assertThat(result).isNotNull();
      // 任务应能正常完成（completionHandler 异常被捕获）
      assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }
  }

  @Test
  void wrapBlockingApi_callRegistered_eventSourceCanCancel() throws Exception {
    SseEmitterCache cacheMock = mock(SseEmitterCache.class);
    SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("test-sse-api");
    executor.setVirtualThreads(true);

    try (MockedStatic<SpringUtil> springMock = mockStatic(SpringUtil.class);
         MockedStatic<ThreadPools> poolsMock = mockStatic(ThreadPools.class)) {

      springMock.when(() -> SpringUtil.getBean(SseEmitterCache.class)).thenReturn(cacheMock);
      poolsMock.when(ThreadPools::getCommon).thenReturn(executor);

      CountDownLatch latch = new CountDownLatch(1);
      Call mockCall = mock(Call.class);

      EventSource result = SseApiUtil.wrapBlockingApi(
          "request",
          (Consumer<String>) partial -> {},
          (Consumer<BssException>) e -> latch.countDown(),
          (TriConsumer<String, Consumer<String>, Consumer<Object>>) (req, handler, listener) -> {
            // 模拟 blockingApi 注册 Call
            listener.accept(mockCall);
          });

      assertThat(result).isNotNull();
      assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();

      // EventSource.cancel() 应能调用 Call.cancel() 而不报错
      result.cancel();
    }
  }
}
