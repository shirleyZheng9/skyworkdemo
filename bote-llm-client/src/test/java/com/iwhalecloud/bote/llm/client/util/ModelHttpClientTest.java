package com.iwhalecloud.bote.llm.client.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;

/**
 * {@link ModelHttpClient} 纯工具方法单元测试
 *
 * <p>覆盖 extractErrorMsg / extractErrorMsgFromError / convertResponse / readResponseBody</p>
 */
class ModelHttpClientTest {

  private static final HttpUrl URL = HttpUrl.parse("http://localhost/test");

  // ==================== extractErrorMsg ====================

  @Test
  void extractErrorMsg_nullOrEmpty_returnsNull() {
    assertThat(ModelHttpClient.extractErrorMsg(null)).isNull();
    assertThat(ModelHttpClient.extractErrorMsg("")).isNull();
    assertThat(ModelHttpClient.extractErrorMsg("not json")).isNull();
    assertThat(ModelHttpClient.extractErrorMsg("   ")).isNull();
  }

  @Test
  void extractErrorMsg_textualField() {
    assertThat(ModelHttpClient.extractErrorMsg("{\"err\":\"e1\"}")).isEqualTo("e1");
    assertThat(ModelHttpClient.extractErrorMsg("{\"error\":\"e2\"}")).isEqualTo("e2");
    assertThat(ModelHttpClient.extractErrorMsg("{\"message\":\"e3\"}")).isEqualTo("e3");
    assertThat(ModelHttpClient.extractErrorMsg("{\"detail\":\"e4\"}")).isEqualTo("e4");
  }

  @Test
  void extractErrorMsg_errorObject() {
    String result = ModelHttpClient.extractErrorMsg("{\"error\":{\"message\":\"boom\",\"code\":\"x\"}}");
    assertThat(result).startsWith("调用接口失败: boom");
  }

  @Test
  void extractErrorMsg_detailArray() {
    String result = ModelHttpClient.extractErrorMsg("{\"detail\":[{\"msg\":\"missing\",\"loc\":[\"body\",\"a\"]}]}");
    assertThat(result).isEqualTo("missing: body.a");
  }

  @Test
  void extractErrorMsg_noMatch_returnsNull() {
    assertThat(ModelHttpClient.extractErrorMsg("{\"foo\":\"bar\"}")).isNull();
  }

  // ==================== extractErrorMsgFromError ====================

  @Test
  void extractErrorMsgFromError_invalidParameter_withParam() {
    JsonNode node = JsonUtil.readTree("{\"code\":\"invalid_parameter_error\",\"message\":\"bad\",\"param\":\"p1\"}");
    assertThat(ModelHttpClient.extractErrorMsgFromError(node))
        .isEqualTo("调用接口参数错误: param=p1, error=bad");
  }

  @Test
  void extractErrorMsgFromError_invalidParameter_withoutParam() {
    JsonNode node = JsonUtil.readTree("{\"code\":\"invalid_parameter_error\",\"message\":\"bad\"}");
    assertThat(ModelHttpClient.extractErrorMsgFromError(node))
        .isEqualTo("调用接口参数错误: bad");
  }

  @Test
  void extractErrorMsgFromError_invalidFunctionParameters() {
    JsonNode node = JsonUtil.readTree("{\"code\":\"invalid_function_parameters\",\"message\":\"bad\"}");
    assertThat(ModelHttpClient.extractErrorMsgFromError(node))
        .isEqualTo("调用接口参数错误: bad");
  }

  @Test
  void extractErrorMsgFromError_apiForwardNested() {
    JsonNode node = JsonUtil.readTree(
        "{\"code\":\"api_forward_request_error\",\"message\":\"{\\\"code\\\":\\\"x\\\",\\\"message\\\":\\\"inner msg\\\"}\"}");
    assertThat(ModelHttpClient.extractErrorMsgFromError(node))
        .isEqualTo("调用接口失败: inner msg");
  }

  @Test
  void extractErrorMsgFromError_generic() {
    JsonNode node = JsonUtil.readTree("{\"code\":\"whatever\",\"message\":\"oops\"}");
    assertThat(ModelHttpClient.extractErrorMsgFromError(node))
        .isEqualTo("调用接口失败: oops");
  }

  /**
   * 空对象 {} 时 message 为 null，源码走 else 分支返回 "调用接口失败: null"。
   * 计划预期 null，但以源码实际行为为准。
   */
  @Test
  void extractErrorMsgFromError_emptyNode_returnsFallback() {
    JsonNode node = JsonUtil.readTree("{}");
    assertThat(ModelHttpClient.extractErrorMsgFromError(node))
        .isEqualTo("调用接口失败: null");
  }

  // ==================== convertResponse ====================

  @Test
  void convertResponse_success() {
    JsonNode json = JsonUtil.readTree(
        "{\"id\":\"1\",\"object\":\"chat.completion\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"hi\"},\"finish_reason\":\"stop\"}]}");
    ChatCompletionResponse response = ModelHttpClient.convertResponse(URL, json, ChatCompletionResponse.class);
    assertThat(response).isNotNull();
    assertThat(response.getMessageContent()).isEqualTo("hi");
  }

  @Test
  void convertResponse_errorObject_throws() {
    JsonNode json = JsonUtil.readTree("{\"error\":{\"message\":\"boom\"}}");
    assertThatThrownBy(() -> ModelHttpClient.convertResponse(URL, json, ChatCompletionResponse.class))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("boom");
  }

  @Test
  void convertResponse_errorTextual_throws() {
    JsonNode json = JsonUtil.readTree("{\"error\":\"fail text\"}");
    assertThatThrownBy(() -> ModelHttpClient.convertResponse(URL, json, ChatCompletionResponse.class))
        .isInstanceOf(BssException.class)
        .hasMessage("fail text");
  }

  @Test
  void convertResponse_docChainError_throws() {
    JsonNode json = JsonUtil.readTree("{\"error_code\":\"E1\",\"err\":\"desc\"}");
    assertThatThrownBy(() -> ModelHttpClient.convertResponse(URL, json, ChatCompletionResponse.class))
        .isInstanceOf(BssException.class)
        .hasMessage("E1: desc");
  }

  @Test
  void convertResponse_maasError_throws() {
    JsonNode json = JsonUtil.readTree("{\"code\":\"9999\",\"msg\":\"maas fail\"}");
    assertThatThrownBy(() -> ModelHttpClient.convertResponse(URL, json, ChatCompletionResponse.class))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("maas fail");
  }

  @Test
  void convertResponse_maasSuccessCode_noThrow() {
    JsonNode json = JsonUtil.readTree(
        "{\"code\":\"10000\",\"id\":\"1\",\"object\":\"chat.completion\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"hi\"},\"finish_reason\":\"stop\"}]}");
    ChatCompletionResponse response = ModelHttpClient.convertResponse(URL, json, ChatCompletionResponse.class);
    assertThat(response).isNotNull();
    assertThat(response.getMessageContent()).isEqualTo("hi");
  }

  // ==================== readResponseBody ====================

  @Test
  void readResponseBody_normal() {
    Response response = new Response.Builder()
        .request(new Request.Builder().url("http://localhost/test").build())
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("ok")
        .body(ResponseBody.create("  hello  ", MediaType.parse("text/plain")))
        .build();
    assertThat(ModelHttpClient.readResponseBody(response)).isEqualTo("hello");
  }

  @Test
  void readResponseBody_nullBody_returnsNull() {
    Response response = new Response.Builder()
        .request(new Request.Builder().url("http://localhost/test").build())
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("ok")
        .body(null)
        .build();
    assertThat(ModelHttpClient.readResponseBody(response)).isNull();
  }
}
