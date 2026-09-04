package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.dto.plugin.PluginExecutionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 插件上下文工具类
 *
 * @author qian.sisheng
 * @since 2025-07-19
 */
public abstract class PluginContextUtil {

  /** 插件上下文栈线程变量 */
  private static final ThreadLocal<Deque<PluginExecutionContext>> contextStackThreadLocal =
    ThreadLocal.withInitial(ArrayDeque::new);

  /**
   * 设置上下文到线程变量
   */
  public static void setContext(PluginExecutionContext context) {
    Deque<PluginExecutionContext> stack = contextStackThreadLocal.get();
    stack.addLast(context);
  }

  /**
   * 清除上下文对象
   */
  public static void removeContext() {
    Deque<PluginExecutionContext> stack = contextStackThreadLocal.get();
    stack.removeLast();
    if (stack.isEmpty()) {
      contextStackThreadLocal.remove();
    }
  }

  /**
   * 获取上下文对象
   */
  public static PluginExecutionContext getContext() {
    PluginExecutionContext context = contextStackThreadLocal.get().peekLast();
    if (context == null) {
      throw new BssException("插件执行异常，缺少上下文对象信息");
    }
    return context;
  }
}
