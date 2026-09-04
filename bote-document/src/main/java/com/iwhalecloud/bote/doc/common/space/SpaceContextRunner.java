package com.iwhalecloud.bote.doc.common.space;

import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.function.Supplier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 带企业空间环境切换的方法执行工具类， 用于带上上下文去执行某些方法
 *
 * @author Aiqing
 * @since 2025/10/24
 */
public final class SpaceContextRunner {

  private SpaceContextRunner() {
    throw new IllegalStateException();
  }

  /**
   * 带企业空间切换的方法执行
   *
   * @param spaceId 企业空间ID
   * @param supplier 执行方法
   * @param <T> 返回结果
   */
  public static <T> T runWithSpace(Long spaceId, Supplier<T> supplier) {
    Long originSpaceId = SpaceContextHolder.getSpaceId();
    boolean originIgnore = SpaceContextHolder.isIgnore();
    try {
      SpaceContextHolder.setIgnore(false);
      SpaceContextHolder.setSpaceId(spaceId);
      return supplier.get();
    }
    finally {
      SpaceContextHolder.setIgnore(originIgnore);
      SpaceContextHolder.setSpaceId(originSpaceId);
    }
  }

  /**
   * 忽略企业空间，执行方法
   *
   * @param supplier 具体的逻辑方法
   * @param <T> 返回结果
   * @return 返回结果
   */
  public static <T> T runWithoutSpace(Supplier<T> supplier) {
    Long originSpaceId = SpaceContextHolder.getSpaceId();
    boolean originIgnore = SpaceContextHolder.isIgnore();
    try {
      SpaceContextHolder.setIgnore(true);
      return supplier.get();
    }
    finally {
      SpaceContextHolder.setIgnore(originIgnore);
      SpaceContextHolder.setSpaceId(originSpaceId);
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

}
