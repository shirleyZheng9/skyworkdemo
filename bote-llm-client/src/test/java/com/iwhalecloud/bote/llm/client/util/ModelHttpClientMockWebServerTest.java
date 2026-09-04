package com.iwhalecloud.bote.llm.client.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ServerSentEvent;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link ModelHttpClient} 的 MockWebServer 测试
 *
 * <p>使用进程内 MockWebServer 覆盖 post/sseBlocking 的真实 HTTP 分支（成功/失败/空 body/非 json/event-stream）。
 * 不连外部网络，符合纯单测约束。</p>
 */
class ModelHttpClientMockWebServerTest {

  private MockWebServer server;
  private HttpUrl url;

  @BeforeEach
  void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
    url = server.url("/v1/chat/completions");
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
  }

  /** 构造合法的 ChatCompletionResponse JSON 响应体 */
  private static String chatCompletionResponseJson(String content) {
    return JsonUtil.toJsonString(ChatCompletionResponse.ofMessage(new AssistantMessage(content)));
  }

  /** 构造简单的请求体数据 */
  private static Object requestData() {
    return ChatCompletionRequest.builder().addUserMessage("hi").build();
  }

  // ==================== post ====================

  @Test
  void post_success_returnsResponse() throws Exception {
    server.enqueue(new MockResponse()
        .setBody(chatCompletionResponseJson("hello from model"))
        .setHeader("Content-Type", "application/json"));

    ChatCompletionResponse response = ModelHttpClient.post(url, null, requestData(), null);

    assertThat(response).isNotNull();
    assertThat(response.getMessageContent()).isEqualTo("hello from model");

    RecordedRequest recorded = server.takeRequest();
    assertThat(recorded.getMethod()).isEqualTo("POST");
    assertThat(recorded.getPath()).isEqualTo("/v1/chat/completions");
    assertThat(recorded.getBody().readUtf8()).contains("hi");
    assertThat(recorded.getHeader("User-Agent")).isEqualTo("bote-service");
  }

  @Test
  void post_httpError_throwsBssException() {
    server.enqueue(new MockResponse()
        .setResponseCode(500)
        .setBody("{\"error\":\"boom\"}")
        .setHeader("Content-Type", "application/json"));

    assertThatThrownBy(() -> ModelHttpClient.post(url, null, requestData(), null))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("boom");
  }

  @Test
  void post_httpError_noErrorMsg_usesStatus() {
    server.enqueue(new MockResponse()
        .setResponseCode(404)
        .setBody("plain text")
        .setHeader("Content-Type", "text/plain"));

    assertThatThrownBy(() -> ModelHttpClient.post(url, null, requestData(), null))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("调用接口失败: 404");
  }

  /**
   * 200 + Content-Type: application/json + 空 body。
   * OkHttp 4.x 对空响应也返回非 null body，但 Jackson readTree 对空输入返回 MissingNode，
   * 触发 {@code Assert.isTrue(!json.isMissingNode(), "调用接口失败，响应为空")}。
   */
  @Test
  void post_emptyBody_throws() {
    server.enqueue(new MockResponse()
        .setBody("")
        .setHeader("Content-Type", "application/json"));

    assertThatThrownBy(() -> ModelHttpClient.post(url, null, requestData(), null))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("响应为空");
  }

  @Test
  void post_nonJsonContentType_throws() {
    server.enqueue(new MockResponse()
        .setBody("plain text")
        .setHeader("Content-Type", "text/plain"));

    assertThatThrownBy(() -> ModelHttpClient.post(url, null, requestData(), null))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("响应类型错误");
  }

  @Test
  void post_requestListener_invoked() {
    server.enqueue(new MockResponse()
        .setBody(chatCompletionResponseJson("ok"))
        .setHeader("Content-Type", "application/json"));

    List<Object> calls = new ArrayList<>();
    ChatCompletionResponse response = ModelHttpClient.post(url, null, requestData(), calls::add);

    assertThat(response).isNotNull();
    assertThat(response.getMessageContent()).isEqualTo("ok");
    assertThat(calls).hasSize(1);
    assertThat(calls.get(0)).isInstanceOf(Call.class);
  }

  @Test
  void post_returnsJsonNode_whenResponseTypeJsonNode() {
    server.enqueue(new MockResponse()
        .setBody("{\"key\":\"value\"}")
        .setHeader("Content-Type", "application/json"));

    JsonNode node = ModelHttpClient.post(url, null, requestData(), null, JsonNode.class);

    assertThat(node).isNotNull();
    assertThat(node.path("key").asText()).isEqualTo("value");
  }

  // ==================== sseBlocking ====================

  @Test
  void sseBlocking_success_collectsEvents() {
    server.enqueue(new MockResponse()
        .setHeader("Content-Type", "text/event-stream")
        .setBody("data: e1\n\ndata: e2\n\n"));

    List<ServerSentEvent> events = ModelHttpClient.sseBlocking("POST", url, null, requestData(), null);

    assertThat(events).hasSize(2);
    assertThat(events.get(0).data()).isEqualTo("e1");
    assertThat(events.get(1).data()).isEqualTo("e2");
  }

  @Test
  void sseBlocking_httpError_throws() {
    server.enqueue(new MockResponse()
        .setResponseCode(500)
        .setBody("{\"error\":\"boom\"}")
        .setHeader("Content-Type", "application/json"));

    assertThatThrownBy(() -> ModelHttpClient.sseBlocking("POST", url, null, requestData(), null))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("调用 SSE 接口失败");
  }

  /**
   * 200 + text/event-stream + 空 body：OkHttp 4.x body 永不为 null，
   * 空流时 ServerSentEventReader.readUtf8Line() 立即返回 null，无事件产生。
   * （{@code if (body == null)} 分支在 OkHttp 4.x 下不可达，改为验证空流行为。）
   */
  @Test
  void sseBlocking_emptyStream_returnsNoEvents() {
    server.enqueue(new MockResponse()
        .setBody("")
        .setHeader("Content-Type", "text/event-stream"));

    List<ServerSentEvent> events = ModelHttpClient.sseBlocking("POST", url, null, requestData(), null);

    assertThat(events).isEmpty();
  }

  @Test
  void sseBlocking_nonEventStream_throws() {
    server.enqueue(new MockResponse()
        .setBody("{\"error\":\"boom\"}")
        .setHeader("Content-Type", "application/json"));

    assertThatThrownBy(() -> ModelHttpClient.sseBlocking("POST", url, null, requestData(), null))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("响应类型必须是 text/event-stream");
  }

  @Test
  void sseBlocking_eventHandler_receivesEvents() {
    server.enqueue(new MockResponse()
        .setHeader("Content-Type", "text/event-stream")
        .setBody("data: event1\n\ndata: event2\n\n"));

    List<ServerSentEvent> events = new ArrayList<>();
    ModelHttpClient.sseBlocking("POST", url, null, requestData(), null, events::add);

    assertThat(events).hasSize(2);
    assertThat(events.get(0).data()).isEqualTo("event1");
    assertThat(events.get(1).data()).isEqualTo("event2");
  }
}
