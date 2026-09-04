package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.llm.client.util.OkHttpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import okhttp3.Call;
import okhttp3.sse.EventSource;
import org.apache.commons.lang3.function.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSE API 辅助类
 *
 * @author bianjp
 * @since 2025-12-22
 */
public final class SseApiUtil {
  private static final Logger logger = LoggerFactory.getLogger(SseApiUtil.class);

  private SseApiUtil() {
  }

  /**
   * 将同步阻塞 API 封装为异步 API
   *
   * @param request 请求参数
   * @param partialHandler 片段处理器
   * @param completionHandler 完成回调
   * @param blockingApi 同步阻塞 API
   * @return EventSource 实例，可用于中断请求
   */
  public static <T, E> EventSource wrapBlockingApi(T request,
                                                   Consumer<E> partialHandler,
                                                   Consumer<BssException> completionHandler,
                                                   TriConsumer<T, Consumer<E>, Consumer<Object>> blockingApi) {
    AtomicReference<Call> callHolder = new AtomicReference<>();
    Consumer<Object> requestListener = SseUtil.requestListener.andThen(obj -> {
      if (obj instanceof Call) {
        callHolder.set((Call) obj);
      }
    });
    Future<?> future = ThreadPools.getCommon().submit(() -> {
      try {
        blockingApi.accept(request, partialHandler, requestListener);
        try {
          completionHandler.accept(null);
        }
        catch (Exception e) {
          // 捕获异常，避免再次触发 completionHandler
          logger.error("Failed to invoke completion handler", e);
        }
      }
      catch (BssException e) {
        completionHandler.accept(e);
      }
      catch (Exception e) {
        completionHandler.accept(new BssException(e));
      }
    });
    return OkHttpUtil.toEventSource(callHolder, future);
  }

}
