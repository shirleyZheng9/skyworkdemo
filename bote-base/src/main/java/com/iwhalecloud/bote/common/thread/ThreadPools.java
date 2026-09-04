package com.iwhalecloud.bote.common.thread;

import com.google.common.base.CaseFormat;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.config.properties.ThreadPoolProperties;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * 线程池工具类
 *
 * @author Admin
 * @author bianjp
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class ThreadPools {
  private static final Logger logger = LoggerFactory.getLogger(ThreadPools.class);

  /** 线程池名称: SSE */
  public static final String SSE = "sse";
  /** 线程池名称: 会话消息保存 */
  public static final String MSG_SAVER = "msgSaver";
  /** 线程池名称: 数据同步 */
  public static final String DATASYNC = "datasync";
  /** 线程池名称: 文档处理 */
  public static final String DOCUMENT = "document";
  /** 线程池名称: 编排引擎 */
  public static final String ORCHESTRATION = "orchestration";
  /** 线程池名称: MCP */
  public static final String MCP = "mcp";
  /** 线程池名称: A2A */
  public static final String A2A = "a2a";
  /** 线程池名称: 发布 */
  public static final String PUBLISH = "publish";
  /** 线程池名称: 评测 */
  public static final String EVAL = "eval";
  /** 线程池名称: 公共，用于干杂活 */
  public static final String COMMON = "common";

  /** 线程池映射。key 为线程池名称 */
  private static final Map<String, SimpleAsyncTaskExecutor> threadPoolMap = new ConcurrentHashMap<>();

  private ThreadPools() {
  }

  /**
   * 获取线程池
   *
   * @param threadPoolName 线程池名称
   * @return 线程池
   */
  public static AsyncTaskExecutor get(String threadPoolName) {
    return threadPoolMap.computeIfAbsent(threadPoolName, ThreadPools::createThreadPool);
  }

  /**
   * 获取公共线程池
   */
  public static AsyncTaskExecutor getCommon() {
    return get(COMMON);
  }
  /**
   * 获取资源发布线程池
   */
  public static AsyncTaskExecutor getPublish() {
    return get(PUBLISH);
  }
  /**
   * 获取评测线程池
   */
  public static AsyncTaskExecutor getEval() {
    return get(EVAL);
  }

  /**
   * 获取 SSE 线程池
   */
  public static AsyncTaskExecutor getSse() {
    return get(SSE);
  }

  /**
   * 获取会话消息保存线程池
   */
  public static AsyncTaskExecutor getMsgSaver() {
    return get(MSG_SAVER);
  }

  /**
   * 获取数据同步线程池
   */
  public static AsyncTaskExecutor getDatasync() {
    return get(DATASYNC);
  }

  /**
   * 获取文档处理线程池
   */
  public static AsyncTaskExecutor getDocument() {
    return get(DOCUMENT);
  }

  /**
   * 获取编排引擎线程池
   */
  public static AsyncTaskExecutor getOrchestration() {
    return get(ORCHESTRATION);
  }

  /**
   * 获取 MCP 线程池
   */
  public static AsyncTaskExecutor getMcp() {
    return get(MCP);
  }

  /**
   * 获取 A2A 线程池
   */
  public static AsyncTaskExecutor getA2a() {
    return get(A2A);
  }

  /**
   * 创建线程池
   */
  private static SimpleAsyncTaskExecutor createThreadPool(String threadPoolName) {
    ThreadPoolProperties properties = new ThreadPoolProperties();
    // 驼峰格式转换为连字符，否则 Binder 会报错
    String normalizedName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, threadPoolName);
    Binder.get(SpringUtil.getEnvironment()).bind("thread.pool." + normalizedName, Bindable.ofInstance(properties));
    String threadNamePrefix = StringUtils.isNotEmpty(properties.getThreadNamePrefix()) ? properties.getThreadNamePrefix() : threadPoolName + "-";
    SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor(threadNamePrefix);
    executor.setVirtualThreads(true);
    executor.setCancelRemainingTasksOnClose(true);
    executor.setRejectTasksWhenLimitReached(true);
    executor.setTaskTerminationTimeout(properties.getAwaitTerminationPeriod().toMillis());
    executor.setConcurrencyLimit(properties.getConcurrencyLimit());

    if (properties.isInheritContext()) {
      BoteTaskDecorator taskDecorator = SpringUtil.getBeanOptional(BoteTaskDecorator.class);
      if (taskDecorator != null) {
        executor.setTaskDecorator(taskDecorator);
      }
    }

    return executor;
  }

  /**
   * 批量执行任务，并等待任务执行结束或失败
   *
   * <p>任一任务执行失败时，都会取消尚未执行完成的任务。</p>
   *
   * @param tasks 任务列表
   */
  public static void invokeTasks(AsyncTaskExecutor executor, List<Runnable> tasks) {
    List<Future<?>> futures = new ArrayList<>(tasks.size());
    for (Runnable task : tasks) {
      futures.add(executor.submit(task));
    }
    waitTasksComplete(futures);
  }

  /**
   * 等待任务执行完成（成功或报错）
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  private static void waitTasksComplete(List<Future<?>> futures) {
    // 未执行完成的任务
    List<Future<?>> pendingTasks = new LinkedList<>(futures);
    try {
      // 轮询，直到所有任务执行完成，或者执行失败
      while (!pendingTasks.isEmpty()) {
        Iterator<Future<?>> iterator = pendingTasks.iterator();
        while (iterator.hasNext()) {
          Future<?> future = iterator.next();
          try {
            // 限制等待时间，这样才能及早发现执行失败的任务（比如第一个任务可能要耗时 1s 完成，但第二个任务可能在 100ms 时已经报错了）
            future.get(50, TimeUnit.MILLISECONDS);
            iterator.remove();
          }
          catch (TimeoutException e) {
            // 忽略超时异常，继续检查下一个任务
          }
          catch (ExecutionException e) {
            if (e.getCause() instanceof BssException) {
              throw (BssException) e.getCause();
            }
            throw new BssException("执行失败: " + ExpUtil.getMsg(e.getCause()), e.getCause());
          }
          catch (InterruptedException e) {
            throw new BssException("执行中断: " + ExpUtil.getMsg(e), e);
          }
          catch (Exception e) {
            throw new BssException("执行失败: " + ExpUtil.getMsg(e), e);
          }
        }
      }
    }
    finally {
      // 执行失败时取消尚未结束的任务
      if (!pendingTasks.isEmpty()) {
        cancelPendingTasks(pendingTasks);
      }
    }
  }

  /**
   * 取消未结束的任务
   */
  private static void cancelPendingTasks(List<Future<?>> futures) {
    for (Future<?> future : futures) {
      if (!future.isDone()) {
        try {
          logger.trace("Canceling pending task: {}", future);
          if (future.cancel(true)) {
            logger.trace("Canceled pending task: {}", future);
          }
          else {
            logger.trace("Failed to cancel pending task: {}", future);
          }
        }
        catch (Exception e) {
          logger.warn("Failed to cancel task", e);
        }
      }
    }
  }

  /**
   * 关闭线程池
   */
  public static void close() {
    logger.debug("Shutdown thread pools");
    for (Entry<String, SimpleAsyncTaskExecutor> entry : threadPoolMap.entrySet()) {
      try {
        entry.getValue().close();
      }
      catch (Exception e) {
        logger.warn("Failed to shutdown thread pool: {}", entry.getKey(), e);
      }
    }
  }

  /**
   * 应用关闭时关闭线程池
   */
  @Component(ThreadPoolShutdownHook.BEAN_NAME)
  public static class ThreadPoolShutdownHook implements DisposableBean {
    /** bean 名称 */
    public static final String BEAN_NAME = "threadPoolShutdownHook";

    @Override
    public void destroy() {
      close();
    }
  }
}
