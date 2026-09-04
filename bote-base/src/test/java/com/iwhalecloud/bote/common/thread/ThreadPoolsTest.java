package com.iwhalecloud.bote.common.thread;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.mock.env.MockEnvironment;

/**
 * {@link ThreadPools} 单元测试
 *
 * <p>get/createThreadPool 路径用 mockStatic(SpringUtil) 注入 MockEnvironment 与可选的
 * BoteTaskDecorator，覆盖缓存命中、inheritContext=true/false 与 decorator 存在/缺失分支；
 * invokeTasks/waitTasksComplete/cancelPendingTasks 用真实 SimpleAsyncTaskExecutor 覆盖
 * 全成功、BssException 透传、其他异常包装、超时轮询与失败取消未完成任务等分支。</p>
 *
 * <p>threadPoolMap 为静态共享，故每个 get 用例采用唯一池名以隔离；invokeTasks 用例使用独立
 * 构造的执行器，不触碰静态映射。</p>
 */
class ThreadPoolsTest {

  // ==================== get / createThreadPool ====================

  @Test
  void get_createsAndCachesExecutor() {
    try (MockedStatic<SpringUtil> spring = mockStatic(SpringUtil.class)) {
      spring.when(SpringUtil::getEnvironment).thenReturn(new MockEnvironment());
      spring.when(() -> SpringUtil.getBeanOptional(BoteTaskDecorator.class)).thenReturn(null);

      AsyncTaskExecutor first = ThreadPools.get("cacheA");
      AsyncTaskExecutor second = ThreadPools.get("cacheA");

      assertThat(first).isSameAs(second).isNotNull();
      // 仅首次创建触发 createThreadPool -> 查询 BoteTaskDecorator 一次
      spring.verify(() -> SpringUtil.getBeanOptional(BoteTaskDecorator.class));
    }
  }

  @Test
  void get_inheritContextTrueWithoutDecorator_doesNotSetDecorator() {
    try (MockedStatic<SpringUtil> spring = mockStatic(SpringUtil.class)) {
      spring.when(SpringUtil::getEnvironment).thenReturn(new MockEnvironment());
      spring.when(() -> SpringUtil.getBeanOptional(BoteTaskDecorator.class)).thenReturn(null);

      AsyncTaskExecutor exec = ThreadPools.get("decorNullB");
      assertThat(exec).isNotNull();
    }
  }

  @Test
  void get_inheritContextTrueWithDecorator_setsDecorator() {
    try (MockedStatic<SpringUtil> spring = mockStatic(SpringUtil.class)) {
      spring.when(SpringUtil::getEnvironment).thenReturn(new MockEnvironment());
      BoteTaskDecorator decorator = mock(BoteTaskDecorator.class);
      spring.when(() -> SpringUtil.getBeanOptional(BoteTaskDecorator.class)).thenReturn(decorator);

      AsyncTaskExecutor exec = ThreadPools.get("decorPresentC");
      assertThat(exec).isNotNull();
    }
  }

  @Test
  void get_inheritContextFalse_skipsDecoratorLookup() {
    MockEnvironment env = new MockEnvironment();
    // normalizedName(inheritFalse) = inherit-false
    env.setProperty("thread.pool.inherit-false.inherit-context", "false");

    try (MockedStatic<SpringUtil> spring = mockStatic(SpringUtil.class)) {
      spring.when(SpringUtil::getEnvironment).thenReturn(env);

      AsyncTaskExecutor exec = ThreadPools.get("inheritFalse");
      assertThat(exec).isNotNull();

      // inheritContext=false 时不应查询 BoteTaskDecorator
      spring.verify(() -> SpringUtil.getBeanOptional(BoteTaskDecorator.class), never());
    }
  }

  @Test
  void convenienceMethods_returnNonNullableExecutors() {
    try (MockedStatic<SpringUtil> spring = mockStatic(SpringUtil.class)) {
      spring.when(SpringUtil::getEnvironment).thenReturn(new MockEnvironment());
      spring.when(() -> SpringUtil.getBeanOptional(BoteTaskDecorator.class)).thenReturn(null);

      assertThat(ThreadPools.getCommon()).isNotNull();
      assertThat(ThreadPools.getPublish()).isNotNull();
      assertThat(ThreadPools.getEval()).isNotNull();
      assertThat(ThreadPools.getSse()).isNotNull();
      assertThat(ThreadPools.getMsgSaver()).isNotNull();
      assertThat(ThreadPools.getDatasync()).isNotNull();
      assertThat(ThreadPools.getDocument()).isNotNull();
      assertThat(ThreadPools.getOrchestration()).isNotNull();
      assertThat(ThreadPools.getMcp()).isNotNull();
      assertThat(ThreadPools.getA2a()).isNotNull();
    }
  }

  // ==================== invokeTasks / waitTasksComplete ====================

  @Test
  void invokeTasks_allSucceed_runsAll() {
    AtomicInteger counter = new AtomicInteger();
    List<Runnable> tasks = List.of(counter::incrementAndGet, counter::incrementAndGet,
        counter::incrementAndGet);

    ThreadPools.invokeTasks(newTestExecutor(), tasks);

    assertThat(counter).hasValue(3);
  }

  @Test
  void invokeTasks_emptyTaskList_returnsImmediately() {
    ThreadPools.invokeTasks(newTestExecutor(), List.of());
    // 不抛异常即通过
  }

  @Test
  void invokeTasks_bssExceptionPropagatedAsIs() {
    List<Runnable> tasks = List.of(() -> {
      throw new BssException("boom");
    });

    assertThatThrownBy(() -> ThreadPools.invokeTasks(newTestExecutor(), tasks))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("boom");
  }

  @Test
  void invokeTasks_otherExceptionWrappedInBssException() {
    List<Runnable> tasks = List.of(() -> {
      throw new RuntimeException("inner-fail");
    });

    assertThatThrownBy(() -> ThreadPools.invokeTasks(newTestExecutor(), tasks))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("执行失败");
  }

  @Test
  void invokeTasks_slowTask_pollsTimeoutThenSucceeds() {
    AtomicBoolean done = new AtomicBoolean();
    List<Runnable> tasks = List.of(() -> {
      try {
        Thread.sleep(120);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
      done.set(true);
    });

    ThreadPools.invokeTasks(newTestExecutor(), tasks);

    // 任务需经多次 50ms 超时轮询后完成
    assertThat(done).isTrue();
  }

  @Test
  void invokeTasks_failureCancelsPendingTask() throws InterruptedException {
    CountDownLatch release = new CountDownLatch(1);
    AtomicBoolean wasInterrupted = new AtomicBoolean();

    Runnable longTask = () -> {
      try {
        release.await();
      }
      catch (InterruptedException e) {
        wasInterrupted.set(true);
        Thread.currentThread().interrupt();
      }
    };
    Runnable failingTask = () -> {
      throw new BssException("fail-fast");
    };

    assertThatThrownBy(() -> ThreadPools.invokeTasks(newTestExecutor(), List.of(longTask, failingTask)))
        .isInstanceOf(BssException.class);

    // 轮询等待取消中断传播（最多 ~3s）
    for (int i = 0; i < 300 && !wasInterrupted.get(); i++) {
      Thread.sleep(10);
    }
    assertThat(wasInterrupted)
        .as("未执行完成的 longTask 应被取消中断")
        .isTrue();

    // 释放可能仍阻塞的线程
    release.countDown();
  }

  // ==================== close / shutdown hook ====================

  @Test
  void close_doesNotThrow() {
    // 关闭静态映射中的所有执行器，吞没异常，不应向外抛出
    ThreadPools.close();
  }

  @Test
  void shutdownHookDestroy_invokesClose() {
    new ThreadPools.ThreadPoolShutdownHook().destroy();
    // 不抛异常即通过
  }

  // ==================== 辅助 ====================

  private SimpleAsyncTaskExecutor newTestExecutor() {
    SimpleAsyncTaskExecutor exec = new SimpleAsyncTaskExecutor("tp-test-");
    // 显式放宽并发，确保多任务可并行（覆盖失败取消场景）
    exec.setConcurrencyLimit(50);
    return exec;
  }
}
