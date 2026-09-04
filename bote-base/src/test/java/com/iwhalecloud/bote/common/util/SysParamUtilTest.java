package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.dto.portal.LoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import okhttp3.Headers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * {@link SysParamUtil} 单元测试
 *
 * <p>覆盖 getParamValue / getParamValueAsString 的字面量、系统变量（${system.now/today/uuid/chatSessionId}）、
 * 会话变量（${session.userName/userId/sessionId/realName/attributes}）、Cookie 变量（${cookie}、${cookie.key}）、
 * ${...} 到 $. 前缀转换、未知前缀回退为字面量等分支。同时覆盖 buildHeaders 的 null 与参数替换逻辑，
 * 以及 convertParamToString 对 Date / Collection / Map / 普通对象的转换。</p>
 *
 * <p>Cookie 解析器通过 RequestContextHolder 设置 mock HttpServletRequest；
 * 会话解析器通过 SessionUtil ThreadLocal 设置 LoginInfo。</p>
 */
class SysParamUtilTest {

  @AfterEach
  void cleanUp() {
    RequestContextHolder.resetRequestAttributes();
    SessionUtil.clearThreadLocal();
    ChatContextUtil.clear();
  }

  // ==================== getParamValue ====================

  @Test
  void getParamValue_emptySpec_returnsNull() {
    assertThat(SysParamUtil.getParamValue(null)).isNull();
    assertThat(SysParamUtil.getParamValue("")).isNull();
  }

  @Test
  void getParamValue_literal_returnsLiteral() {
    assertThat(SysParamUtil.getParamValue("hello world")).isEqualTo("hello world");
  }

  @Test
  void getParamValue_dollarPrefixNoMatch_returnsSpec() {
    // $.unknown 前缀无匹配解析器，返回 spec 本身
    assertThat(SysParamUtil.getParamValue("$.unknown.foo")).isEqualTo("$.unknown.foo");
  }

  @Test
  void getParamValue_envVarFormat_convertedToDotPrefix() {
    // ${system.uuid} -> $.system.uuid -> 系统变量 uuid
    Object result = SysParamUtil.getParamValue("${system.uuid}");
    assertThat(result).isInstanceOf(String.class);
    assertThat((String) result).isNotEmpty();
  }

  @Test
  void getParamValue_systemNow_returnsDate() {
    Object result = SysParamUtil.getParamValue("$.system.now");
    assertThat(result).isInstanceOf(java.util.Date.class);
  }

  @Test
  void getParamValue_systemToday_returnsLocalDate() {
    Object result = SysParamUtil.getParamValue("$.system.today");
    assertThat(result).isInstanceOf(java.time.LocalDate.class);
  }

  @Test
  void getParamValue_systemUuid_returnsUuidString() {
    Object result = SysParamUtil.getParamValue("$.system.uuid");
    assertThat(result).isInstanceOf(String.class);
    // UUID 字符串格式
    assertThat((String) result).matches("[0-9a-f-]{36}");
  }

  @Test
  void getParamValue_systemChatSessionId_returnsContextValue() {
    ChatContextUtil.setChatSessionId("chat-sess-1");
    Object result = SysParamUtil.getParamValue("$.system.chatSessionId");
    assertThat(result).isEqualTo("chat-sess-1");
  }

  @Test
  void getParamValue_systemUnknownKey_returnsEmptyString() {
    Object result = SysParamUtil.getParamValue("$.system.unknown");
    assertThat(result).isEqualTo("");
  }

  @Test
  void getParamValue_systemPrefixOnly_returnsEmptyString() {
    // $.system -> removePrefix 返回 "" -> switch default -> ""
    Object result = SysParamUtil.getParamValue("$.system");
    assertThat(result).isEqualTo("");
  }

  @Test
  void getParamValue_systemCookie_withRequest_returnsCookieHeader() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getHeader(HttpHeaders.COOKIE)).thenReturn("k1=v1; k2=v2");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

    Object result = SysParamUtil.getParamValue("$.system.cookie");
    assertThat(result).isEqualTo("k1=v1; k2=v2");
  }

  @Test
  void getParamValue_systemCookie_noRequest_returnsNull() {
    Object result = SysParamUtil.getParamValue("$.system.cookie");
    assertThat(result).isNull();
  }

  // ==================== 会话变量 ====================

  @Test
  void getParamValue_sessionUserName_returnsValue() {
    LoginInfo info = LoginInfo.builder().userName("alice").userId(42L).realName("Alice").build();
    SessionUtil.setLoginInfo(info);

    assertThat(SysParamUtil.getParamValue("$.session.userName")).isEqualTo("alice");
    assertThat(SysParamUtil.getParamValue("$.session.userId")).isEqualTo(42L);
    assertThat(SysParamUtil.getParamValue("$.session.realName")).isEqualTo("Alice");
  }

  @Test
  void getParamValue_sessionNoLoginInfo_returnsNull() {
    assertThat(SysParamUtil.getParamValue("$.session.userName")).isNull();
  }

  @Test
  void getParamValue_sessionUnknownKey_returnsEmptyString() {
    SessionUtil.setLoginInfo(LoginInfo.builder().userName("alice").build());
    assertThat(SysParamUtil.getParamValue("$.session.unknown")).isEqualTo("");
  }

  @Test
  void getParamValue_sessionAttributes_returnsMap() {
    Map<String, Object> attrs = Map.of("dept", "engineering");
    LoginInfo info = LoginInfo.builder().userName("alice").attributes(attrs).build();
    SessionUtil.setLoginInfo(info);

    Object result = SysParamUtil.getParamValue("$.session.attributes");
    assertThat(result).isSameAs(attrs);
  }

  // ==================== Cookie 变量 ====================

  @Test
  void getParamValue_cookieNoKey_returnsEntireCookieHeader() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getHeader(HttpHeaders.COOKIE)).thenReturn("k1=v1; k2=v2");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

    Object result = SysParamUtil.getParamValue("$.cookie");
    assertThat(result).isEqualTo("k1=v1; k2=v2");
  }

  @Test
  void getParamValue_cookieSpecificKey_returnsValue() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getHeader(HttpHeaders.COOKIE)).thenReturn("k1=v1; k2=v2");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

    assertThat(SysParamUtil.getParamValue("$.cookie.k1")).isEqualTo("v1");
    assertThat(SysParamUtil.getParamValue("$.cookie.k2")).isEqualTo("v2");
  }

  @Test
  void getParamValue_cookieKeyNotFound_returnsNull() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getHeader(HttpHeaders.COOKIE)).thenReturn("k1=v1");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

    assertThat(SysParamUtil.getParamValue("$.cookie.nonexistent")).isNull();
  }

  @Test
  void getParamValue_cookieNoRequest_returnsNull() {
    assertThat(SysParamUtil.getParamValue("$.cookie")).isNull();
    assertThat(SysParamUtil.getParamValue("$.cookie.key")).isNull();
  }

  @Test
  void getParamValue_cookieEmptyHeader_returnsNullForKey() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getHeader(HttpHeaders.COOKIE)).thenReturn("");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

    assertThat(SysParamUtil.getParamValue("$.cookie.key")).isNull();
  }

  // ==================== getParamValueAsString ====================

  @Test
  void getParamValueAsString_emptySpec_returnsEmpty() {
    assertThat(SysParamUtil.getParamValueAsString(null)).isEmpty();
    assertThat(SysParamUtil.getParamValueAsString("")).isEmpty();
  }

  @Test
  void getParamValueAsString_literal_returnsLiteral() {
    assertThat(SysParamUtil.getParamValueAsString("hello")).isEqualTo("hello");
  }

  @Test
  void getParamValueAsString_systemNow_returnsFormattedDate() {
    String result = SysParamUtil.getParamValueAsString("$.system.now");
    assertThat(result).isNotEmpty();
    // 日期格式应为 yyyy-MM-dd HH:mm:ss 或类似格式
    assertThat(result).contains("-");
  }

  @Test
  void getParamValueAsString_systemUuid_returnsUuidString() {
    String result = SysParamUtil.getParamValueAsString("$.system.uuid");
    assertThat(result).matches("[0-9a-f-]{36}");
  }

  @Test
  void getParamValueAsString_sessionAttributes_returnsJson() {
    Map<String, Object> attrs = Map.of("dept", "engineering");
    LoginInfo info = LoginInfo.builder().attributes(attrs).build();
    SessionUtil.setLoginInfo(info);

    String result = SysParamUtil.getParamValueAsString("$.session.attributes");
    assertThat(result).contains("dept").contains("engineering");
  }

  @Test
  void getParamValueAsString_sessionUserId_returnsString() {
    SessionUtil.setLoginInfo(LoginInfo.builder().userId(42L).build());
    assertThat(SysParamUtil.getParamValueAsString("$.session.userId")).isEqualTo("42");
  }

  @Test
  void getParamValueAsString_unknownPrefix_returnsSpec() {
    assertThat(SysParamUtil.getParamValueAsString("$.unknown.foo")).isEqualTo("$.unknown.foo");
  }

  // ==================== buildHeaders ====================

  @Test
  void buildHeaders_null_returnsEmpty() {
    Headers result = SysParamUtil.buildHeaders(null);
    assertThat(result.size()).isEqualTo(0);
  }

  @Test
  void buildHeaders_literalValues_preserved() {
    Headers input = Headers.of("X-Foo", "bar", "X-Baz", "qux");
    Headers result = SysParamUtil.buildHeaders(input);

    assertThat(result.get("X-Foo")).isEqualTo("bar");
    assertThat(result.get("X-Baz")).isEqualTo("qux");
  }

  @Test
  void buildHeaders_paramValues_replaced() {
    Headers input = Headers.of("X-Trace-Id", "${system.uuid}");
    Headers result = SysParamUtil.buildHeaders(input);

    assertThat(result.get("X-Trace-Id")).matches("[0-9a-f-]{36}");
  }

  @Test
  void buildHeaders_emptyHeaders_returnsEmpty() {
    Headers result = SysParamUtil.buildHeaders(Headers.of());
    assertThat(result.size()).isEqualTo(0);
  }
}
