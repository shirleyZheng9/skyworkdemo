package com.iwhalecloud.bote.doc.common.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * {@link ThreadContextRunner} 单元测试。
 *
 * <p>覆盖 runWithTenant/runWithoutTenant/runIgnoreTenant 的租户切换与 finally 恢复（含异常路径）、
 * runAsync 经 SpringUtil 获取线程池并委托 execute、私有构造器抛 IllegalStateException。</p>
 */
class ThreadContextRunnerTest {

  @AfterEach
  void clear() {
    TenantContextHolder.clear();
  }

  @Test
  void runWithTenant_setsTenantAndIgnoreFalse_thenRestores() {
    TenantContextHolder.setTenantId(1L);
    TenantContextHolder.setIgnore(true);

    AtomicReference<Long> seenTenant = new AtomicReference<>();
    String result = ThreadContextRunner.runWithTenant(2L, () -> {
      seenTenant.set(TenantContextHolder.getRequiredTenantId());
      assertThat(TenantContextHolder.isIgnore()).isFalse();
      return "ok";
    });

    assertThat(result).isEqualTo("ok");
    assertThat(seenTenant.get()).isEqualTo(2L);
    assertThat(TenantContextHolder.getTenantId()).isEqualTo(1L);
    assertThat(TenantContextHolder.isIgnore()).isTrue();
  }

  @Test
  void runWithTenant_restoresEvenWhenSupplierThrows() {
    TenantContextHolder.setTenantId(1L);
    assertThatThrownBy(() -> ThreadContextRunner.runWithTenant(2L, () -> {
        throw new RuntimeException("boom");
      }))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("boom");
    assertThat(TenantContextHolder.getTenantId()).isEqualTo(1L);
  }

  @Test
  void runWithoutTenant_setsIgnoreTrue_thenRestores() {
    TenantContextHolder.setTenantId(1L);
    TenantContextHolder.setIgnore(false);

    Boolean seenIgnore = ThreadContextRunner.runWithoutTenant(TenantContextHolder::isIgnore);

    assertThat(seenIgnore).isTrue();
    assertThat(TenantContextHolder.isIgnore()).isFalse();
    assertThat(TenantContextHolder.getTenantId()).isEqualTo(1L);
  }

  @Test
  void runIgnoreTenant_setsIgnoreTrue_thenRestores() {
    TenantContextHolder.setTenantId(1L);
    Boolean seenIgnore = ThreadContextRunner.runIgnoreTenant(TenantContextHolder::isIgnore);
    assertThat(seenIgnore).isTrue();
    assertThat(TenantContextHolder.isIgnore()).isFalse();
  }

  @Test
  void runAsync_delegatesToThreadPoolExecutor() {
    try (MockedStatic<SpringUtil> mocked = mockStatic(SpringUtil.class)) {
      ThreadPoolTaskExecutor executor = mock(ThreadPoolTaskExecutor.class);
      mocked.when(() -> SpringUtil.getBean(ThreadPoolTaskExecutor.class)).thenReturn(executor);

      Runnable task = () -> {
      };
      ThreadContextRunner.runAsync(task);

      verify(executor).execute(task);
    }
  }

  @Test
  void privateConstructor_throwsIllegalState() throws NoSuchMethodException {
    java.lang.reflect.Constructor<ThreadContextRunner> ctor =
      ThreadContextRunner.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    assertThatThrownBy(ctor::newInstance)
      .isInstanceOf(InvocationTargetException.class)
      .hasCauseInstanceOf(IllegalStateException.class);
  }
}
