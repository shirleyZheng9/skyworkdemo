package com.iwhalecloud.bote.doc.common.tenant;

import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.function.Supplier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 带租户环境切换的方法执行工具类， 用于带上上下文去执行某些方法
 *
 * @author Aiqing
 * @since 2023/12/18
 */
public final class ThreadContextRunner {

  private ThreadContextRunner() {
    throw new IllegalStateException();
  }

  /**
   * 带租户切换的方法执行
   *
   * @param tenantId 租户ID
   * @param supplier 执行方法
   * @param <T> 返回结果
   */
  public static <T> T runWithTenant(Long tenantId, Supplier<T> supplier) {
    Long originTenantId = TenantContextHolder.getTenantId();
    boolean originIgnore = TenantContextHolder.isIgnore();
    try {
      TenantContextHolder.setIgnore(false);
      TenantContextHolder.setTenantId(tenantId);
      return supplier.get();
    }
    finally {
      TenantContextHolder.setIgnore(originIgnore);
      TenantContextHolder.setTenantId(originTenantId);
    }
  }

  /**
   * 忽略租户，执行方法
   *
   * @param supplier 具体的逻辑方法
   * @param <T> 返回结果
   * @return 返回结果
   */
  public static <T> T runWithoutTenant(Supplier<T> supplier) {
    Long originTenantId = TenantContextHolder.getTenantId();
    boolean originIgnore = TenantContextHolder.isIgnore();
    try {
      TenantContextHolder.setIgnore(true);
      return supplier.get();
    }
    finally {
      TenantContextHolder.setIgnore(originIgnore);
      TenantContextHolder.setTenantId(originTenantId);
    }
  }

  /**
   * 提交线程池执行
   *
   * @param runnable 执行方法
   */
  public static void runAsync(Runnable runnable) {
    ThreadPoolTaskExecutor taskExecutor = SpringUtil.getBean(ThreadPoolTaskExecutor.class);
    taskExecutor.execute(runnable);
  }

  /**
   * 忽略租户方法执行
   *
   * @param supplier 执行方法
   * @param <T> 返回结果类型
   * @return 结果
   */
  public static <T> T runIgnoreTenant(Supplier<T> supplier) {
    Long originTenantId = TenantContextHolder.getTenantId();
    boolean originIgnore = TenantContextHolder.isIgnore();
    try {
      TenantContextHolder.setIgnore(true);
      return supplier.get();
    }
    finally {
      TenantContextHolder.setIgnore(originIgnore);
      TenantContextHolder.setTenantId(originTenantId);
    }
  }
}
