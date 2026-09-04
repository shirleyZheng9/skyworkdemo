package com.iwhalecloud.bote.common.sse;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import okhttp3.sse.EventSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * SSE 调用器
 *
 * <p>用于 API 技能、工作流中的大模型节点的出参，以实现流式输出。</p>
 *
 * @author bianjp
 * @since 2024-12-26
 */
@JsonSerialize(using = ToStringSerializer.class)
public class SseInvoker {
  /** 调用器 */
  private final Consumer<Consumer<SseEvent>> invoker;
  /** 流式输出完成的回调 */
  @Setter
  @Nullable
  private Consumer<AnswerDTO> finishCallback;
  /** 流式输出失败时的回调，可选，用于在失败时执行节点配置的异常处理策略 */
  @Setter
  @Getter
  @Nullable
  private Consumer<Throwable> errorCallback;

  /**
   * @deprecated 废弃，应改用 {@link #SseInvoker(Consumer)}。暂时保留以兼容旧代码（江苏政企对接智宇使用了这个构造方法）
   */
  @Deprecated
  public SseInvoker(BiFunction<Consumer<SseEvent>, Consumer<BssException>, EventSource> invoker) {
    this.invoker = partialHandler -> {
      AtomicReference<BssException> exceptionHolder = new AtomicReference<>();
      CountDownLatch countDownLatch = new CountDownLatch(1);
      EventSource eventSource = invoker.apply(partialHandler, e -> {
        exceptionHolder.set(e);
        countDownLatch.countDown();
      });
      SseUtil.requestListener.accept(eventSource);

      try {
        boolean success = countDownLatch.await(ModelHttpClient.getClient().callTimeoutMillis(), TimeUnit.MILLISECONDS);
        if (!success) {
          throw new BssException("流式输出超时");
        }
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new BssException("流式输出中断: " + StringUtils.defaultString(e.getMessage()), e);
      }
      // 执行失败
      if (exceptionHolder.get() != null) {
        throw exceptionHolder.get();
      }
    };
  }

  public SseInvoker(Consumer<Consumer<SseEvent>> invoker) {
    Assert.notNull(invoker, "invoker cannot be null");
    this.invoker = invoker;
  }

  /**
   * 调用 SSE 接口
   *
   * @param partialHandler 片段处理器
   */
  public void invoke(Consumer<SseEvent> partialHandler) {
    invoker.accept(partialHandler);
  }

  /**
   * 结束 SSE 调用
   *
   * <p>有两个用途: 1. 获取回复内容; 2. 调整回复内容</p>
   *
   * @param answer 完整回复内容
   */
  public void finish(AnswerDTO answer) {
    if (finishCallback != null) {
      finishCallback.accept(answer);
    }
  }

  @Override
  public String toString() {
    return "sse invoker";
  }
}
