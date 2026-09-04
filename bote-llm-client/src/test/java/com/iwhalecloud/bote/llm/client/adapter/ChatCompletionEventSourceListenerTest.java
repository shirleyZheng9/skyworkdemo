package com.iwhalecloud.bote.llm.client.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ServerSentEvent;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import org.junit.jupiter.api.Test;

/**
 * {@link ChatCompletionEventSourceListener} 单元测试
 *
 * <p>覆盖 think 标签推理内容转换状态机、完成/失败回调、chatcmpl-error 忽略等。</p>
 * <p>processEvent 内部调用真实的 ModelHttpClient.convertResponse（纯逻辑，无需 mock）。</p>
 */
class ChatCompletionEventSourceListenerTest {

  private static final HttpUrl URL = HttpUrl.parse("http://localhost/test");

  // ==================== 辅助方法 ====================

  private static String deltaData(String content) {
    return JsonUtil.toJsonString(ChatCompletionResponse.ofDelta(new AssistantMessage(content), false));
  }

  private static String deltaDataWithReasoning(String content, String reasoningContent) {
    AssistantMessage delta = new AssistantMessage();
    delta.setContent(content);
    delta.setReasoningContent(reasoningContent);
    return JsonUtil.toJsonString(ChatCompletionResponse.ofDelta(delta, false));
  }

  private static String deltaDataWithObject(String object, String content) {
    AssistantMessage delta = new AssistantMessage(content);
    ChatCompletionResponse response = ChatCompletionResponse.ofDelta(delta, false);
    response.setObject(object);
    return JsonUtil.toJsonString(response);
  }

  private static String deltaDataWithId(String id, String content) {
    AssistantMessage delta = new AssistantMessage(content);
    ChatCompletionResponse response = ChatCompletionResponse.ofDelta(delta, false);
    response.setId(id);
    return JsonUtil.toJsonString(response);
  }

  private static ServerSentEvent sse(String data) {
    return new ServerSentEvent(null, null, data);
  }

  private static Response errorResponse(int code, String body) {
    return new Response.Builder()
        .request(new Request.Builder().url("http://localhost/test").build())
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("error")
        .body(ResponseBody.create(body, MediaType.parse("application/json")))
        .build();
  }

  // ==================== onEvent(ServerSentEvent) ====================

  @Test
  void onEvent_done_returnsWithoutPartial() {
    List<ChatCompletionResponse> partials = new ArrayList<>();
    List<BssException> completions = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(partials::add)
        .completionHandler(completions::add)
        .build();

    listener.onEvent(sse("[DONE]"));

    assertThat(partials).isEmpty();
    assertThat(completions).isEmpty();
  }

  @Test
  void onEvent_normalContent_dispatched() {
    List<ChatCompletionResponse> partials = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(partials::add)
        .completionHandler(e -> {})
        .build();

    listener.onEvent(sse(deltaData("hi")));

    assertThat(partials).hasSize(1);
    assertThat(partials.get(0).getDeltaContent()).isEqualTo("hi");
  }

  @Test
  void onEvent_chatcmplError_ignored() {
    List<ChatCompletionResponse> partials = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(partials::add)
        .completionHandler(e -> {})
        .build();

    listener.onEvent(sse(deltaDataWithId("chatcmpl-error-1", "error content")));

    assertThat(partials).isEmpty();
  }

  @Test
  void onEvent_invalidJson_throwsBssException() {
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(r -> {})
        .completionHandler(e -> {})
        .build();

    assertThatThrownBy(() -> listener.onEvent(sse("not json")))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("处理 SSE 响应失败");
  }

  // ==================== convertReasoning 状态机 ====================

  @Test
  void convertReasoning_thinkTags_convertedToReasoning() {
    List<ChatCompletionResponse> partials = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .convertReasoning(true)
        .partialHandler(partials::add)
        .completionHandler(e -> {})
        .build();

    // INITIAL -> <think> -> 不发送，状态变为 REASONING_STARTED
    listener.onEvent(sse(deltaData("<think>")));
    assertThat(partials).isEmpty();

    // REASONING_STARTED -> 思考内容 -> 发送，reasoningContent="思考内容"，content=null
    listener.onEvent(sse(deltaData("思考内容")));
    assertThat(partials).hasSize(1);
    assertThat(partials.get(0).getDeltaReasoningContent()).isEqualTo("思考内容");
    assertThat(partials.get(0).getDeltaContent()).isEmpty();

    // REASONING_STARTED -> </think> -> 不发送，状态变为 REASONING_ENDED
    listener.onEvent(sse(deltaData("</think>")));
    assertThat(partials).hasSize(1);

    // REASONING_ENDED -> 正文 -> 发送，content="正文"
    listener.onEvent(sse(deltaData("正文")));
    assertThat(partials).hasSize(2);
    assertThat(partials.get(1).getDeltaContent()).isEqualTo("正文");
  }

  @Test
  void convertReasoning_alreadyHasReasoningContent_stopsConversion() {
    List<ChatCompletionResponse> partials = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .convertReasoning(true)
        .partialHandler(partials::add)
        .completionHandler(e -> {})
        .build();

    listener.onEvent(sse(deltaDataWithReasoning(null, "reasoning text")));

    assertThat(partials).hasSize(1);
    assertThat(partials.get(0).getDeltaReasoningContent()).isEqualTo("reasoning text");
  }

  @Test
  void convertReasoning_blankBeforeThink_ignored() {
    List<ChatCompletionResponse> partials = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .convertReasoning(true)
        .partialHandler(partials::add)
        .completionHandler(e -> {})
        .build();

    // 空白 content -> INITIAL 空白分支 -> 不发送
    listener.onEvent(sse(deltaData(" ")));
    assertThat(partials).isEmpty();

    // <think> -> REASONING_STARTED -> 不发送
    listener.onEvent(sse(deltaData("<think>")));
    assertThat(partials).isEmpty();
  }

  @Test
  void convertReasoning_llmdocChunkObject_passedThrough() {
    List<ChatCompletionResponse> partials = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .convertReasoning(true)
        .partialHandler(partials::add)
        .completionHandler(e -> {})
        .build();

    listener.onEvent(sse(deltaDataWithObject("llmdoc.completion.chunk", "doc content")));

    assertThat(partials).hasSize(1);
    assertThat(partials.get(0).getDeltaContent()).isEqualTo("doc content");
  }

  @Test
  void convertReasoning_disabled_passesThrough() {
    List<ChatCompletionResponse> partials = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .convertReasoning(false)
        .partialHandler(partials::add)
        .completionHandler(e -> {})
        .build();

    listener.onEvent(sse(deltaData("<think>")));

    assertThat(partials).hasSize(1);
    assertThat(partials.get(0).getDeltaContent()).isEqualTo("<think>");
  }

  // ==================== onEvent(EventSource, id, type, data) ====================

  @Test
  void onEventEventSource_done_invokesCompletionNull() {
    List<BssException> completions = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(r -> {})
        .completionHandler(completions::add)
        .build();

    listener.onEvent(mock(EventSource.class), null, null, "[DONE]");

    assertThat(completions).hasSize(1);
    assertThat(completions.get(0)).isNull();
  }

  @Test
  void onEventEventSource_ready_invokesCompletionNull() {
    List<BssException> completions = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(r -> {})
        .completionHandler(completions::add)
        .build();

    listener.onEvent(mock(EventSource.class), null, null, "READY");

    assertThat(completions).hasSize(1);
    assertThat(completions.get(0)).isNull();
  }

  @Test
  void onEventEventSource_error_invokesCompletionWithException() {
    List<BssException> completions = new ArrayList<>();
    EventSource es = mock(EventSource.class);
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(r -> {})
        .completionHandler(completions::add)
        .build();

    listener.onEvent(es, null, null, "not json");

    assertThat(completions).hasSize(1);
    assertThat(completions.get(0)).isNotNull();
    assertThat(completions.get(0).getMessage()).contains("处理 SSE 响应失败");
    verify(es).cancel();
  }

  // ==================== onClosed ====================

  @Test
  void onClosed_unfinished_invokesCompletion() {
    List<BssException> completions = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(r -> {})
        .completionHandler(completions::add)
        .build();

    listener.onClosed(mock(EventSource.class));

    assertThat(completions).hasSize(1);
    assertThat(completions.get(0).getMessage()).contains("意外关闭连接");
  }

  @Test
  void onClosed_afterFinished_noDoubleInvoke() {
    List<BssException> completions = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(r -> {})
        .completionHandler(completions::add)
        .build();

    // 先触发一次 completion（[DONE]）
    listener.onEvent(mock(EventSource.class), null, null, "[DONE]");
    assertThat(completions).hasSize(1);

    // 再 onClosed -> 不再调用
    listener.onClosed(mock(EventSource.class));
    assertThat(completions).hasSize(1);
  }

  // ==================== onFailure ====================

  @Test
  void onFailure_withBssException_propagated() {
    List<BssException> completions = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(r -> {})
        .completionHandler(completions::add)
        .build();

    BssException expected = new BssException("custom error");
    listener.onFailure(mock(EventSource.class), expected, null);

    assertThat(completions).hasSize(1);
    assertThat(completions.get(0)).isSameAs(expected);
  }

  @Test
  void onFailure_withResponse_extractsError() {
    List<BssException> completions = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(r -> {})
        .completionHandler(completions::add)
        .build();

    Response response = errorResponse(400, "{\"error\":\"boom\"}");
    listener.onFailure(mock(EventSource.class), null, response);

    assertThat(completions).hasSize(1);
    assertThat(completions.get(0).getMessage()).contains("boom");
  }

  @Test
  void onFailure_afterFinished_returns() {
    List<BssException> completions = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(r -> {})
        .completionHandler(completions::add)
        .build();

    // 先触发 completion
    listener.onEvent(mock(EventSource.class), null, null, "[DONE]");
    assertThat(completions).hasSize(1);

    // 再 onFailure -> 不调用
    listener.onFailure(mock(EventSource.class), new BssException("x"), null);
    assertThat(completions).hasSize(1);
  }

  // ==================== invokeCompletionHandler 异常处理 ====================

  @Test
  void invokeCompletionHandler_throwingHandler_swallowed() {
    List<BssException> completions = new ArrayList<>();
    ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
        .url(URL)
        .partialHandler(r -> {})
        .completionHandler(e -> {
          completions.add(e);
          throw new RuntimeException("handler boom");
        })
        .build();

    // completionHandler 抛异常 -> 不传播，finished 仍为 true
    listener.onEvent(mock(EventSource.class), null, null, "[DONE]");
    assertThat(completions).hasSize(1);

    // 再次触发 onClosed -> finished=true -> 不再调用
    listener.onClosed(mock(EventSource.class));
    assertThat(completions).hasSize(1);
  }
}
