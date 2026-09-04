package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * {@link ServletUtil} 单元测试
 *
 * <p>覆盖 getRequest（无上下文返回 null、有上下文返回 request）、getHeadersAsMap、
 * getParametersAsMap（无请求返回空 Map、有请求返回参数）、isDomain（无请求返回 false、
 * IP 地址返回 false、域名返回 true）。通过 RequestContextHolder 设置 mock 请求。</p>
 */
class ServletUtilTest {

  @AfterEach
  void cleanUp() {
    RequestContextHolder.resetRequestAttributes();
  }

  // ==================== getRequest ====================

  @Test
  void getRequest_noContext_returnsNull() {
    assertThat(ServletUtil.getRequest()).isNull();
  }

  @Test
  void getRequest_withContext_returnsRequest() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

    assertThat(ServletUtil.getRequest()).isSameAs(mockRequest);
  }

  // ==================== getHeadersAsMap ====================

  @Test
  void getHeadersAsMap_returnsAllHeaders() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeaderNames()).thenReturn(Collections.enumeration(java.util.List.of("Accept", "Content-Type")));
    when(request.getHeader("Accept")).thenReturn("application/json");
    when(request.getHeader("Content-Type")).thenReturn("text/plain");

    Map<String, String> headers = ServletUtil.getHeadersAsMap(request);

    assertThat(headers)
        .containsEntry("Accept", "application/json")
        .containsEntry("Content-Type", "text/plain")
        .hasSize(2);
  }

  @Test
  void getHeadersAsMap_noHeaders_returnsEmptyMap() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

    Map<String, String> headers = ServletUtil.getHeadersAsMap(request);

    assertThat(headers).isEmpty();
  }

  // ==================== getParametersAsMap ====================

  @Test
  void getParametersAsMap_noRequest_returnsEmptyMap() {
    assertThat(ServletUtil.getParametersAsMap()).isEmpty();
  }

  @Test
  void getParametersAsMap_withRequest_returnsParameters() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

    when(mockRequest.getParameterMap()).thenReturn(Map.of(
        "key1", new String[]{"value1"},
        "key2", new String[]{"value2"}));

    Map<String, String> params = ServletUtil.getParametersAsMap();

    assertThat(params)
        .containsEntry("key1", "value1")
        .containsEntry("key2", "value2");
  }

  // ==================== isDomain ====================

  @Test
  void isDomain_noRequest_returnsFalse() {
    assertThat(ServletUtil.isDomain()).isFalse();
  }

  @Test
  void isDomain_ipAddress_returnsFalse() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    when(mockRequest.getServerName()).thenReturn("192.168.1.1");

    assertThat(ServletUtil.isDomain()).isFalse();
  }

  @Test
  void isDomain_localhost_returnsFalse() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    when(mockRequest.getServerName()).thenReturn("127.0.0.1");

    assertThat(ServletUtil.isDomain()).isFalse();
  }

  @Test
  void isDomain_domainName_returnsTrue() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    when(mockRequest.getServerName()).thenReturn("example.com");

    assertThat(ServletUtil.isDomain()).isTrue();
  }

  @Test
  void isDomain_emptyServerName_returnsTrue() {
    // 空字符串不匹配 IP 正则
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    when(mockRequest.getServerName()).thenReturn("");

    assertThat(ServletUtil.isDomain()).isTrue();
  }
}
