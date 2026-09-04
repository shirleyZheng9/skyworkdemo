package com.iwhalecloud.bote.llm.client.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import org.springframework.lang.Nullable;

/**
 * OkHttp 工具类
 *
 * @author bianjp
 * @since 2025-06-02
 */
public final class OkHttpUtil {
  private OkHttpUtil() {
  }

  /** 媒体类型: json */
  private static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json");

  /**
   * 构造 JSON 请求体
   *
   * @param json JSON 字符串
   * @return 请求体对象
   */
  public static RequestBody jsonBody(String json) {
    // 不要使用 RequestBody.create(String) 方法，否则会给 Content-Type 添加不必要的 charset=utf-8, 可能导致部分服务器端报错
    return RequestBody.create(json.getBytes(StandardCharsets.UTF_8), MEDIA_TYPE_JSON);
  }

  /**
   * 构造 JSON 请求体
   *
   * @param data 请求体数据
   * @return 请求体对象
   */
  public static RequestBody jsonBody(Object data) {
    byte[] bytes;
    try {
      bytes = JsonUtil.getObjectMapper().writeValueAsBytes(data);
    }
    catch (JsonProcessingException e) {
      throw new IllegalArgumentException("序列化 JSON 失败", e);
    }
    return RequestBody.create(bytes, MEDIA_TYPE_JSON);
  }

  /**
   * 将 Call 封装成 EventSource 对象
   */
  public static EventSource toEventSource(AtomicReference<Call> callHolder, @Nullable Future<?> future) {
    return new EventSourceWrapper(callHolder, future);
  }

  /**
   * EventSource 封装器，用于中断请求
   */
  private record EventSourceWrapper(AtomicReference<Call> callHolder, @Nullable Future<?> future) implements EventSource {
    @Override
    public Request request() {
      Call call = callHolder.get();
      if (call == null) {
        throw new IllegalArgumentException("请求尚未发起");
      }
      return call.request();
    }

    @Override
    public void cancel() {
      Call call = callHolder.get();
      if (call != null) {
        call.cancel();
      }
      if (future != null) {
        future.cancel(true);
      }
    }
  }
}
