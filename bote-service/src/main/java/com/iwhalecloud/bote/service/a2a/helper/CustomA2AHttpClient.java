package com.iwhalecloud.bote.service.a2a.helper;

import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bote.llm.client.util.OkHttpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.a2a.client.http.A2AHttpClient;
import io.a2a.client.http.A2AHttpResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import lombok.ToString;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 自定义 A2AHttpClient
 *
 * <p>不使用 {@link io.a2a.client.http.JdkA2AHttpClient}, 以便控制请求超时时间、错误日志，
 * 同时绕开 io.a2a.client.http.JdkA2AHttpClient.JdkBuilder#asyncRequest 未在请求异常时调用 subscriber.onError 导致请求一直卡住的 bug</p>
 *
 * @author bianjp
 * @since 2025-09-19
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class CustomA2AHttpClient implements A2AHttpClient {
  private static final Logger logger = LoggerFactory.getLogger(CustomA2AHttpClient.class);

  @Override
  public GetBuilder createGet() {
    return new OkHttpGetBuilder();
  }

  @Override
  public PostBuilder createPost() {
    return new OkHttpPostBuilder();
  }

  @Override
  public DeleteBuilder createDelete() {
    return new OkHttpDeleteBuilder();
  }

  /**
   * OkHttp 请求构造器
   */
  private abstract static class OkHttpBuilder<T extends Builder<T>> implements Builder<T> {
    /** 请求地址 */
    protected HttpUrl url;
    /** 请求头 */
    protected final Headers.Builder headers = new Headers.Builder();

    @SuppressWarnings("unchecked")
    @Override
    public T url(String url) {
      this.url = HttpUrl.parse(url);
      Assert.notNull(this.url, () -> "请求地址不合法: " + url);
      return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addHeader(String name, String value) {
      headers.set(name, value);
      return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addHeaders(Map<String, String> headers) {
      if (MapUtils.isNotEmpty(headers)) {
        for (Entry<String, String> entry : headers.entrySet()) {
          this.headers.set(entry.getKey(), entry.getValue());
        }
      }
      return (T) this;
    }

    /**
     * 构造请求
     */
    protected final Request createRequest(HttpMethod method, @Nullable String body, boolean isSse) {
      logger.trace("A2A request start: method={}, sse={}, url={}, body={}", method, isSse, url, body);
      Request.Builder builder = new Request.Builder().url(url).headers(headers.build());
      if (isSse) {
        builder.header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE);
      }
      RequestBody requestBody = body != null ? OkHttpUtil.jsonBody(body) : null;
      return builder.method(method.name(), requestBody).build();
    }

    /**
     * 异步发起 SSE 请求
     */
    protected final CompletableFuture<Void> asyncSse(Request request, Consumer<String> messageConsumer, Consumer<Throwable> errorConsumer, Runnable completeRunnable) {
      AtomicReference<EventSource> eventSourceHolder = new AtomicReference<>();
      CompletableFuture<Void> future = new CompletableFuture<>();
      // 异常时关闭连接
      future.whenComplete((v, throwable) -> {
        // A2A 客户端在收到 final 状态的事件时会主动 cancel 请求，需要忽略这种异常，避免调用 EventSource#cancel, 否则可能会触发 EventSourceListener#onFailure
        // io.a2a.client.transport.jsonrpc.sse.SSEEventListener#handleMessage
        if (throwable == null || throwable instanceof CancellationException) {
          completeRunnable.run();
        }
        else {
          logger.error("SSE request failed: url={}", request.url(), throwable);
          EventSource eventSource = eventSourceHolder.get();
          if (eventSource != null) {
            eventSource.cancel();
          }
        }
      });
      try {
        EventSource eventSource = ModelHttpClient.getEventSourceFactory().newEventSource(request, new SseEventSourceListener(url, future, messageConsumer, errorConsumer));
        eventSourceHolder.set(eventSource);
      }
      catch (Exception e) {
        errorConsumer.accept(e);
        future.completeExceptionally(e);
      }
      return future;
    }
  }

  /**
   * SSE 事件监听器
   */
  @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
  private static final class SseEventSourceListener extends EventSourceListener {
    private final HttpUrl url;
    private final CompletableFuture<Void> future;
    private final Consumer<String> messageConsumer;
    private final Consumer<Throwable> errorConsumer;

    public SseEventSourceListener(HttpUrl url, CompletableFuture<Void> future, Consumer<String> messageConsumer, Consumer<Throwable> errorConsumer) {
      this.url = url;
      this.future = future;
      this.messageConsumer = messageConsumer;
      this.errorConsumer = errorConsumer;
    }

    @Override
    @SuppressWarnings("PMD.GuardLogStatement")
    public void onOpen(EventSource eventSource, Response response) {
      logger.trace("SSE connection opened: url={}, status={}", url, response.code());
    }

    @Override
    public void onClosed(EventSource eventSource) {
      logger.trace("SSE connection closed: url={}", url);
      future.complete(null);
    }

    @Override
    public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
      logger.trace("Received SSE event: {}", data);
      messageConsumer.accept(data);
    }

    @Override
    public void onFailure(EventSource eventSource, @Nullable Throwable throwable, @Nullable Response response) {
      Integer responseCode = response != null ? response.code() : null;
      String responseBodyText = response != null ? ModelHttpClient.readResponseBody(response) : null;
      String errorMsg = StringUtils.defaultString(ModelHttpClient.extractErrorMsg(responseBodyText));
      if (errorMsg.isEmpty() && throwable != null) {
        errorMsg = ExpUtil.getMsg(throwable);
      }

      logger.error("Failed to process SSE request: url={}, status={}, response={}", url, responseCode, responseBodyText, throwable);

      BssException exception;
      if (throwable instanceof BssException) {
        exception = (BssException) throwable;
      }
      else if (throwable != null) {
        exception = new BssException("调用 SSE 接口失败: status=" + responseCode + ", error=" + errorMsg, throwable);
      }
      else {
        exception = new BssException("调用 SSE 接口失败: status=" + responseCode + ", error=" + errorMsg);
      }
      errorConsumer.accept(exception);
      future.completeExceptionally(exception);
    }
  }

  /**
   * OkHttp GET 请求构造器
   */
  private static final class OkHttpGetBuilder extends OkHttpBuilder<GetBuilder> implements GetBuilder {
    @Override
    public A2AHttpResponse get() throws IOException {
      Request request = createRequest(HttpMethod.GET, null, false);
      Call call = ModelHttpClient.getClient().newCall(request);
      try (Response response = call.execute()) {
        return new OkHttpA2AHttpResponse(response);
      }
    }

    @Override
    public CompletableFuture<Void> getAsyncSSE(Consumer<String> messageConsumer, Consumer<Throwable> errorConsumer, Runnable completeRunnable) {
      Request request = createRequest(HttpMethod.GET, null, true);
      return asyncSse(request, messageConsumer, errorConsumer, completeRunnable);
    }
  }

  /**
   * OkHttp POST 请求构造器
   */
  private static final class OkHttpPostBuilder extends OkHttpBuilder<PostBuilder> implements PostBuilder {
    /** 请求体 */
    private String body;

    @Override
    public PostBuilder body(String body) {
      this.body = body;
      return this;
    }

    @Override
    public A2AHttpResponse post() throws IOException {
      Request request = createRequest(HttpMethod.POST, body, false);
      Call call = ModelHttpClient.getClient().newCall(request);
      try (Response response = call.execute()) {
        return new OkHttpA2AHttpResponse(response);
      }
    }

    @Override
    public CompletableFuture<Void> postAsyncSSE(Consumer<String> messageConsumer, Consumer<Throwable> errorConsumer, Runnable completeRunnable) {
      Request request = createRequest(HttpMethod.POST, body, true);
      return asyncSse(request, messageConsumer, errorConsumer, completeRunnable);
    }
  }

  /**
   * OkHttp DELETE 请求构造器
   */
  private static final class OkHttpDeleteBuilder extends OkHttpBuilder<DeleteBuilder> implements DeleteBuilder {
    @Override
    public A2AHttpResponse delete() throws IOException {
      Request request = createRequest(HttpMethod.DELETE, null, false);
      Call call = ModelHttpClient.getClient().newCall(request);
      try (Response response = call.execute()) {
        return new OkHttpA2AHttpResponse(response);
      }
    }
  }

  /**
   * 基于 OkHttp 的响应对象
   */
  @ToString
  private static final class OkHttpA2AHttpResponse implements A2AHttpResponse {
    /** 状态码 */
    private final int status;
    /** 是否成功 */
    private final boolean success;
    /** 响应体 */
    private final String body;

    private OkHttpA2AHttpResponse(Response response) {
      this.status = response.code();
      this.success = response.isSuccessful();
      this.body = StringUtils.defaultString(ModelHttpClient.readResponseBody(response));
    }

    @Override
    public int status() {
      return status;
    }

    @Override
    public boolean success() {
      return success;
    }

    @Override
    public String body() {
      return body;
    }
  }
}
