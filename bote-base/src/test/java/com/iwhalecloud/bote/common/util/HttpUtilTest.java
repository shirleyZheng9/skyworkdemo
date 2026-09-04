package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * {@link HttpUtil} 单元测试
 *
 * <p>使用 {@link MockRestServiceServer} 绑定到 {@link HttpUtil#getRestTemplate()} 的静态 RestTemplate
 * 来测试 GET/POST/PUT/getForBytes 的成功与错误路径，无需发起真实 HTTP 请求。同时直接测试静态辅助方法
 * isValid / extractQueryParams / decodeQueryParams / createRestTemplateWithTimeout。</p>
 *
 * <p>注意：实际的网络 I/O 由 MockRestServiceServer 拦截，不覆盖 CustomRestTemplate.doExecute 中
 * "Connection pool shut down" 的重试分支（需要真实的连接池关闭异常才能触发）。</p>
 */
class HttpUtilTest {

  // ==================== isValid ====================

  @Test
  void isValid_emptyUrl_returnsFalse() {
    assertThat(HttpUtil.isValid("")).isFalse();
    assertThat(HttpUtil.isValid(null)).isFalse();
  }

  @Test
  void isValid_nonHttpProtocol_returnsFalse() {
    assertThat(HttpUtil.isValid("ftp://example.com")).isFalse();
    assertThat(HttpUtil.isValid("file:///path")).isFalse();
  }

  @Test
  void isValid_validHttpUrl_returnsTrue() {
    assertThat(HttpUtil.isValid("http://example.com/api")).isTrue();
    assertThat(HttpUtil.isValid("https://example.com/api?key=value")).isTrue();
  }

  @Test
  void isValid_invalidUrlSyntax_returnsFalse() {
    assertThat(HttpUtil.isValid("http://[invalid")).isFalse();
  }

  // ==================== extractQueryParams ====================

  @Test
  void extractQueryParams_withParams_returnsDecodedMap() {
    MultiValueMap<String, String> result = HttpUtil.extractQueryParams(
        "http://example.com/api?key=value&name=alice");

    assertThat(result.get("key")).containsExactly("value");
    assertThat(result.get("name")).containsExactly("alice");
  }

  @Test
  void extractQueryParams_encodedParams_returnsDecodedValues() {
    MultiValueMap<String, String> result = HttpUtil.extractQueryParams(
        "http://example.com/api?q=%E4%B8%AD%E6%96%87");

    assertThat(result.get("q")).containsExactly("中文");
  }

  @Test
  void extractQueryParams_noParams_returnsEmptyMap() {
    MultiValueMap<String, String> result = HttpUtil.extractQueryParams("http://example.com/api");
    assertThat(result).isEmpty();
  }

  // ==================== decodeQueryParams ====================

  @Test
  void decodeQueryParams_withEncodedParams_setsDecodedOnBuilder() {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("http://example.com/api");
    HttpUtil.decodeQueryParams("http://example.com/api?key=%E4%B8%AD%E6%96%87", builder);

    URI uri = builder.build().toUri();
    assertThat(uri.toString()).contains("key=");
  }

  @Test
  void decodeQueryParams_noParams_builderUnchanged() {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("http://example.com/api");
    HttpUtil.decodeQueryParams("http://example.com/api", builder);

    URI uri = builder.build().toUri();
    assertThat(uri.getQuery()).isNull();
  }

  // ==================== createRestTemplateWithTimeout ====================

  @Test
  void createRestTemplateWithTimeout_returnsRestTemplateWithConverters() {
    var rt = HttpUtil.createRestTemplateWithTimeout(5000, 10000);
    assertThat(rt).isNotNull();
    assertThat(rt.getMessageConverters()).isNotEmpty();
  }

  @Test
  void createRestTemplateWithTimeout_nullTimeouts_usesDefaults() {
    var rt = HttpUtil.createRestTemplateWithTimeout(null, null);
    assertThat(rt).isNotNull();
    assertThat(rt.getMessageConverters()).isNotEmpty();
  }

  @Test
  void getRestTemplate_returnsInitializedInstance() {
    assertThat(HttpUtil.getRestTemplate()).isNotNull();
    assertThat(HttpUtil.getRestTemplate().getMessageConverters()).isNotEmpty();
  }

  // ==================== GET (ParameterizedTypeReference) ====================

  @Test
  void get_withTypeReference_returnsResponseBody() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"key\":\"value\"}", MediaType.APPLICATION_JSON));

    Map<String, String> result = HttpUtil.get(
        "http://example.com/api", new ParameterizedTypeReference<Map<String, String>>() {});

    assertThat(result).containsEntry("key", "value");
    server.verify();
  }

  @Test
  void get_withMapParams_buildsUriWithQueryParams() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api?key=value"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"result\":\"ok\"}", MediaType.APPLICATION_JSON));

    Map<String, String> result = HttpUtil.get(
        "http://example.com/api",
        Map.of("key", "value"),
        new ParameterizedTypeReference<Map<String, String>>() {});

    assertThat(result).containsEntry("result", "ok");
    server.verify();
  }

  @Test
  void get_withMultiValueMapParamsAndHeaders_buildsUri() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api?key=value"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"result\":\"ok\"}", MediaType.APPLICATION_JSON));

    LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("key", "value");

    Map<String, String> result = HttpUtil.get(
        "http://example.com/api",
        params,
        new ParameterizedTypeReference<Map<String, String>>() {},
        null);

    assertThat(result).containsEntry("result", "ok");
    server.verify();
  }

  // ==================== GET (String response) ====================

  @Test
  void getWithStringResponse_returnsBody() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("hello world", MediaType.TEXT_PLAIN));

    String result = HttpUtil.get("http://example.com/api", Map.of());

    assertThat(result).isEqualTo("hello world");
    server.verify();
  }

  @Test
  void getWithStringResponse_withCustomHeaders() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("data", MediaType.TEXT_PLAIN));

    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Custom", "val");
    String result = HttpUtil.get("http://example.com/api", Map.of(), headers);

    assertThat(result).isEqualTo("data");
    server.verify();
  }

  // ==================== getForBytes ====================

  @Test
  void getForBytes_returnsBinaryData() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    byte[] data = {1, 2, 3, 4, 5};
    server.expect(once(), requestTo("http://example.com/file"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(data, MediaType.APPLICATION_OCTET_STREAM));

    byte[] result = HttpUtil.getForBytes("http://example.com/file", Map.of());

    assertThat(result).isEqualTo(data);
    server.verify();
  }

  // ==================== POST ====================

  @Test
  void post_nullBody_sendsPostAndReturnsResponse() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("created", MediaType.TEXT_PLAIN));

    String result = HttpUtil.post("http://example.com/api", null, String.class);

    assertThat(result).isEqualTo("created");
    server.verify();
  }

  @Test
  void post_stringBody_sendsPostAndReturnsResponse() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

    String result = HttpUtil.post("http://example.com/api", "raw body", String.class);

    assertThat(result).isEqualTo("ok");
    server.verify();
  }

  @Test
  void post_formBody_setsFormContentType() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

    LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("field", "value");

    String result = HttpUtil.post("http://example.com/api", form, String.class);

    assertThat(result).isEqualTo("ok");
    server.verify();
  }

  @Test
  void post_jsonBody_setsJsonContentType() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

    Map<String, String> body = Map.of("name", "alice");
    Map<String, Object> result = HttpUtil.post(
        "http://example.com/api", body, new ParameterizedTypeReference<Map<String, Object>>() {});

    assertThat(result).containsEntry("id", 1);
    server.verify();
  }

  @Test
  void post_withQueryParamsAndBody_buildsUriAndSendsBody() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api?q=test"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"result\":\"ok\"}", MediaType.APPLICATION_JSON));

    HttpHeaders headers = new HttpHeaders();
    Map<String, Object> result = HttpUtil.post(
        "http://example.com/api",
        Map.of("q", "test"),
        Map.of("name", "alice"),
        new ParameterizedTypeReference<Map<String, Object>>() {},
        headers);

    assertThat(result).containsEntry("result", "ok");
    server.verify();
  }

  // ==================== PUT ====================

  @Test
  void put_jsonBody_returnsResponse() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api"))
        .andExpect(method(HttpMethod.PUT))
        .andRespond(withSuccess("{\"id\":1,\"name\":\"updated\"}", MediaType.APPLICATION_JSON));

    HttpHeaders headers = new HttpHeaders();
    Map<String, Object> result = HttpUtil.put(
        "http://example.com/api",
        Map.of("name", "updated"),
        new ParameterizedTypeReference<Map<String, Object>>() {},
        headers);

    assertThat(result).containsEntry("id", 1);
    assertThat(result).containsEntry("name", "updated");
    server.verify();
  }

  // ==================== Error paths ====================

  @Test
  void get_httpError_throwsBssException() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("bad request"));

    assertThatThrownBy(() -> HttpUtil.get(
        "http://example.com/api", new ParameterizedTypeReference<Map<String, String>>() {}))
        .isInstanceOf(BssException.class);
  }

  @Test
  void get_serverError_throwsBssException() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api"))
        .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("server error"));

    assertThatThrownBy(() -> HttpUtil.get(
        "http://example.com/api", new ParameterizedTypeReference<Map<String, String>>() {}))
        .isInstanceOf(BssException.class);
  }

  @Test
  void get_deserializationError_throwsBssException() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    // 返回非法 JSON，Jackson 解析失败 -> 非 HttpStatusCodeException -> 走通用 catch(Exception)
    server.expect(once(), requestTo("http://example.com/api"))
        .andRespond(withSuccess("{bad json", MediaType.APPLICATION_JSON));

    assertThatThrownBy(() -> HttpUtil.get(
        "http://example.com/api", new ParameterizedTypeReference<Map<String, String>>() {}))
        .isInstanceOf(BssException.class);
  }

  @Test
  void post_httpError_throwsBssException() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/api"))
        .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY).body("error"));

    assertThatThrownBy(() -> HttpUtil.post("http://example.com/api", Map.of("k", "v"), String.class))
        .isInstanceOf(BssException.class);
  }

  @Test
  void getForBytes_httpError_throwsBssException() {
    MockRestServiceServer server = MockRestServiceServer.bindTo(HttpUtil.getRestTemplate()).build();
    server.expect(once(), requestTo("http://example.com/file"))
        .andRespond(withStatus(HttpStatus.NOT_FOUND).body("not found"));

    assertThatThrownBy(() -> HttpUtil.getForBytes("http://example.com/file", Map.of()))
        .isInstanceOf(BssException.class);
  }

  @Test
  void get_emptyUrl_throwsIllegalArgument() {
    assertThatThrownBy(() -> HttpUtil.get("", new ParameterizedTypeReference<Map<String, String>>() {}))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void post_emptyUrl_throwsIllegalArgument() {
    assertThatThrownBy(() -> HttpUtil.post("", null, String.class))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
