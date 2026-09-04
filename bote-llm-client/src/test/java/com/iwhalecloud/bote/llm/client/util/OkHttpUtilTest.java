package com.iwhalecloud.bote.llm.client.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okio.Buffer;
import org.junit.jupiter.api.Test;

/**
 * {@link OkHttpUtil} 单元测试
 *
 * <p>覆盖 jsonBody 构造与 toEventSource 封装的 request/cancel 行为</p>
 */
class OkHttpUtilTest {

  // ==================== jsonBody(String) ====================

  @Test
  void jsonBody_string_contentTypeJson() throws Exception {
    String json = "{\"a\":1}";
    RequestBody body = OkHttpUtil.jsonBody(json);

    assertThat(body.contentType()).hasToString("application/json");
    // 验证写入的字节内容与原串一致
    Buffer buffer = new Buffer();
    body.writeTo(buffer);
    assertThat(buffer.readUtf8()).isEqualTo(json);
  }

  // ==================== jsonBody(Object) ====================

  @Test
  void jsonBody_object_serializes() throws Exception {
    Map<String, Integer> data = Map.of("a", 1);
    RequestBody body = OkHttpUtil.jsonBody(data);

    assertThat(body.contentType()).hasToString("application/json");
    Buffer buffer = new Buffer();
    body.writeTo(buffer);
    assertThat(buffer.readUtf8()).isEqualTo("{\"a\":1}");
  }

  @Test
  void jsonBody_object_serializeThrows_illegalArgument() {
    // 自引用数组，Jackson 序列化时无限递归抛 JsonProcessingException
    Object[] arr = new Object[1];
    arr[0] = arr;

    assertThatThrownBy(() -> OkHttpUtil.jsonBody(arr))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("序列化 JSON 失败");
  }

  // ==================== toEventSource - request ====================

  @Test
  void toEventSource_request_beforeStart_throws() {
    AtomicReference<Call> callHolder = new AtomicReference<>();
    EventSource eventSource = OkHttpUtil.toEventSource(callHolder, null);

    assertThatThrownBy(() -> eventSource.request())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("请求尚未发起");
  }

  @Test
  void toEventSource_request_returnsCallRequest() {
    Request request = new Request.Builder().url("http://localhost/test").build();
    Call call = mock(Call.class);
    when(call.request()).thenReturn(request);

    AtomicReference<Call> callHolder = new AtomicReference<>();
    callHolder.set(call);
    EventSource eventSource = OkHttpUtil.toEventSource(callHolder, null);

    assertThat(eventSource.request()).isSameAs(request);
  }

  // ==================== toEventSource - cancel ====================

  @Test
  @SuppressWarnings("unchecked")
  void toEventSource_cancel_cancelsCallAndFuture() {
    Call call = mock(Call.class);
    Future<?> future = mock(Future.class);

    AtomicReference<Call> callHolder = new AtomicReference<>();
    callHolder.set(call);
    EventSource eventSource = OkHttpUtil.toEventSource(callHolder, future);

    eventSource.cancel();

    verify(call).cancel();
    verify(future).cancel(true);
  }

  @Test
  @SuppressWarnings("unchecked")
  void toEventSource_cancel_nullFuture_onlyCancelsCall() {
    Call call = mock(Call.class);

    AtomicReference<Call> callHolder = new AtomicReference<>();
    callHolder.set(call);
    EventSource eventSource = OkHttpUtil.toEventSource(callHolder, null);

    // 不应抛 NPE
    eventSource.cancel();

    verify(call).cancel();
  }

  @Test
  void toEventSource_cancel_nullCall_noException() {
    AtomicReference<Call> callHolder = new AtomicReference<>();
    EventSource eventSource = OkHttpUtil.toEventSource(callHolder, null);

    // call=null + future=null，cancel 不应抛异常
    eventSource.cancel();
  }
}
