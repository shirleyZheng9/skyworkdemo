package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;

/**
 * {@link DocChainApiUtil} 单元测试
 *
 * <p>覆盖 extractErrorMsg 的各分支：非 JSON 响应体、err 字段提取、detail[0].msg 提取、
 * 无匹配字段回退、非法 JSON 解析异常回退。使用真实的 HttpClientErrorException 避免对
 * toString() 的 mock。</p>
 */
class DocChainApiUtilTest {

  private static HttpClientErrorException createException() {
    return HttpClientErrorException.create(
        HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);
  }

  @Test
  void extractErrorMsg_nonJsonBody_returnsExceptionToString() {
    HttpClientErrorException ex = createException();
    String result = DocChainApiUtil.extractErrorMsg(ex, "plain text error");
    assertThat(result).isEqualTo(ex.toString());
  }

  @Test
  void extractErrorMsg_jsonNotEndsWithBrace_returnsExceptionToString() {
    HttpClientErrorException ex = createException();
    String result = DocChainApiUtil.extractErrorMsg(ex, "{incomplete");
    assertThat(result).isEqualTo(ex.toString());
  }

  @Test
  void extractErrorMsg_jsonWithErrField_returnsErrText() {
    HttpClientErrorException ex = createException();
    String body = "{\"err\":\"topic not found\"}";
    String result = DocChainApiUtil.extractErrorMsg(ex, body);
    assertThat(result).isEqualTo("topic not found");
  }

  @Test
  void extractErrorMsg_jsonWithDetailMsg_returnsDetailMsg() {
    HttpClientErrorException ex = createException();
    String body = "{\"detail\":[{\"msg\":\"field is required\"}]}";
    String result = DocChainApiUtil.extractErrorMsg(ex, body);
    assertThat(result).isEqualTo("field is required");
  }

  @Test
  void extractErrorMsg_jsonWithErrNotTextual_fallsBackToDetail() {
    HttpClientErrorException ex = createException();
    String body = "{\"err\":123,\"detail\":[{\"msg\":\"validation error\"}]}";
    String result = DocChainApiUtil.extractErrorMsg(ex, body);
    assertThat(result).isEqualTo("validation error");
  }

  @Test
  void extractErrorMsg_jsonWithNoMatchingFields_returnsExceptionToString() {
    HttpClientErrorException ex = createException();
    String body = "{\"other\":\"some info\"}";
    String result = DocChainApiUtil.extractErrorMsg(ex, body);
    assertThat(result).isEqualTo(ex.toString());
  }

  @Test
  void extractErrorMsg_invalidJson_returnsExceptionToString() {
    HttpClientErrorException ex = createException();
    // 以 { 开头、} 结尾但不是合法 JSON
    String body = "{invalid json}";
    String result = DocChainApiUtil.extractErrorMsg(ex, body);
    assertThat(result).isEqualTo(ex.toString());
  }

  @Test
  void extractErrorMsg_errFieldNull_fallsBackToDetail() {
    HttpClientErrorException ex = createException();
    String body = "{\"err\":null,\"detail\":[{\"msg\":\"missing param\"}]}";
    String result = DocChainApiUtil.extractErrorMsg(ex, body);
    assertThat(result).isEqualTo("missing param");
  }
}
