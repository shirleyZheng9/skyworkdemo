package com.iwhalecloud.bote.filter;

import com.iwhalecloud.bss.litchi.util.IPUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;


/**
 * 拦截器：拦截处理请求body
 *
 * @author zhangJun
 * @since 2022/3/23
 **/
public class SignFilter implements Filter {

  /** IP响应头 */
  private static final String X_IP = "X-IP";

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    if (!(servletRequest instanceof HttpServletRequest)) {
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    printRequestIp(request, servletResponse);

    if (needWrapperReq(request)) {
      filterChain.doFilter(new BodyReadWrapper((HttpServletRequest) servletRequest), servletResponse);
    }
    else {
      filterChain.doFilter(servletRequest, servletResponse);
    }
  }

  private boolean needWrapperReq(HttpServletRequest request) {
    // 1.拦截所有contentType 带有application/json的post
    if (HttpMethod.POST.matches(request.getMethod()) &&
      Strings.CI.startsWith(request.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
      return true;
    }
    // 2.拦截所有get 方法
    return HttpMethod.GET.matches(request.getMethod());
  }

  /**
   * 响应头中设置 X-IP
   */
  private void printRequestIp(HttpServletRequest request, ServletResponse servletResponse) {
    if (StringUtils.isNotEmpty(request.getHeader(X_IP)) && servletResponse instanceof HttpServletResponse) {
      HttpServletResponse response = (HttpServletResponse) servletResponse;
      response.setHeader(X_IP, IPUtil.getClientIP(request));
    }
  }

}
