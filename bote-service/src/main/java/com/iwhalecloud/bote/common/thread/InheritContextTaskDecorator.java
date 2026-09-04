package com.iwhalecloud.bote.common.thread;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * 可继承上下文的任务封装器
 *
 * @author bianjp
 * @since 2024-11-06
 */
@Component
public class InheritContextTaskDecorator implements BoteTaskDecorator {

  @Override
  @NonNull
  public Runnable decorate(@NonNull Runnable runnable) {
    return new InheritContextRunnable(runnable);
  }

  /**
   * 可继承上下文的 Runnable
   */
  private static class InheritContextRunnable implements Runnable {
    /** 线程本地变量收集器 */
    private final ThreadLocalCollector collector = new ThreadLocalCollector();
    /** 原始任务 */
    private final Runnable runnable;

    public InheritContextRunnable(Runnable runnable) {
      this.runnable = runnable;
    }

    @Override
    public void run() {
      collector.run(runnable);
    }
  }
}
