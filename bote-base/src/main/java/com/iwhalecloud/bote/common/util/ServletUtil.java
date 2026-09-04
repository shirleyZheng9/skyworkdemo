package com.iwhalecloud.bote.common.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Servlet 工具类
 *
 * @author bianjp
 * @since 2024-08-21
 */
public final class ServletUtil {
  private ServletUtil() {
  }

  /**
   * 获取当前线程绑定的 HTTP 请求对象
   */
  @Nullable
  public static HttpServletRequest getRequest() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes instanceof ServletRequestAttributes) {
      return ((ServletRequestAttributes) requestAttributes).getRequest();
    }
    return null;
  }

  /**
   * 获取请求头的 Map 形式
   */
  public static Map<String, String> getHeadersAsMap(HttpServletRequest request) {
    Map<String, String> headerMap = new HashMap<>();
    Enumeration<String> enumeration = request.getHeaderNames();
    while (enumeration.hasMoreElements()) {
      String headerName = enumeration.nextElement();
      headerMap.put(headerName, request.getHeader(headerName));
    }
    return headerMap;
  }

  /**
   * 获取 URL 参数的 Map 形式
   */
  public static Map<String, String> getParametersAsMap() {
    HttpServletRequest request = getRequest();
    if (request == null) {
      return Collections.emptyMap();
    }
    Map<String, String> params = new HashMap<>();
    for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
      params.put(entry.getKey(), entry.getValue()[0]);
    }
    return params;
  }

  /**
   * 判断 HTTP 请求是否带有域名
   *
   * @return 是否带有域名
   */
  public static boolean isDomain() {
    HttpServletRequest request = getRequest();
    if (request == null) {
      return false;
    }
    String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    return !Pattern.matches(ipPattern, request.getServerName());
  }

}
