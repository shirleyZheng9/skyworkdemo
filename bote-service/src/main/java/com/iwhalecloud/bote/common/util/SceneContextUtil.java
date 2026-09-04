package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 场景编排引擎上下文工具类
 *
 * @author bianjp
 * @since 2024-08-29
 */
public abstract class SceneContextUtil {
  /** 上下文栈线程变量 */
  private static final ThreadLocal<Deque<SceneOrchestrationContext>> contextStackThreadLocal = ThreadLocal.withInitial(ArrayDeque::new);

  /**
   * 随机生成新的上下文 ID
   */
  public static String newContextId() {
    return UUID.randomUUID().toString();
  }

  /**
   * 设置上下文到线程变量
   */
  public static void setContext(SceneOrchestrationContext context) {
    Deque<SceneOrchestrationContext> stack = contextStackThreadLocal.get();
    Assert.isTrue(stack.size() < 1000, "工作流上下文深度超过 1000，请检查是否存在死循环");
    stack.addLast(context);
  }

  /**
   * 清除上下文对象
   */
  public static void removeContext() {
    Deque<SceneOrchestrationContext> stack = contextStackThreadLocal.get();
    stack.removeLast();
    if (stack.isEmpty()) {
      contextStackThreadLocal.remove();
    }
  }

  /**
   * 获取上下文对象
   */
  public static SceneOrchestrationContext getContext() {
    SceneOrchestrationContext context = contextStackThreadLocal.get().peekLast();
    if (context == null) {
      throw new BssException("工作流执行异常，缺少上下文对象信息");
    }
    return context;
  }

  /**
   * 获取栈底的上下文对象
   */
  public static SceneOrchestrationContext getRootContext() {
    SceneOrchestrationContext context = contextStackThreadLocal.get().peekFirst();
    if (context == null) {
      throw new BssException("工作流执行异常，缺少上下文对象信息");
    }
    return context;
  }

  /**
   * 获取可选的上下文对象
   */
  @Nullable
  public static SceneOrchestrationContext getOptionalContext() {
    return contextStackThreadLocal.get().peekLast();
  }

  /**
   * 是否存在上下文对象
   */
  public static boolean hasContext() {
    return !contextStackThreadLocal.get().isEmpty();
  }

}
