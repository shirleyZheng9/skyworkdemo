package com.iwhalecloud.bote.doc.interceptor;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.doc.portal.MultiPortalAdapter;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.llm.client.util.LlmTraceUtil;
import com.iwhalecloud.bote.portal.IAuthProvider;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

/**
 * 登录状态拦截器
 *
 * @author chen.linfa
 * @since 2025-11-20
 */
@Component
@RequiredArgsConstructor
public class SessionInterceptor implements AsyncHandlerInterceptor {

  /** 鉴权失败的响应内容 */
  private static final String FAILED_RESPONSE_TEXT = "{\"resultCode\": \"401\", \"resultMsg\": \"%s\"}";

  private final MultiPortalAdapter multiPortalAdapter;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
    // 避免处理静态资源请求
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }
    // 防止有残留的线程本地变量时影响当前线程
    clearThreadLocals();
    // 是否忽略鉴权
    if (shouldSkip((HandlerMethod) handler)) {
      return true;
    }
    IAuthProvider provider = multiPortalAdapter.getAuthProvider();
    LoginInfo loginInfo = provider.getLoginInfo(request);
    if (loginInfo == null) {
      sendAuthFailed(response, "未登录");
      return false;
    }
    // 设置线程变量
    SessionUtil.setSessionId(loginInfo.getToken());
    SessionUtil.setLoginInfo(loginInfo);
    TenantIdUtil.setTenantId(0L);
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
    clearThreadLocals();
  }

  @Override
  public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) {
    // SSE 请求会用到两个 HTTP 处理线程，第一个线程不会触发 afterCompletion, 因此需要在 afterConcurrentHandlingStarted 中清理线程本地变量
    clearThreadLocals();
  }

  /**
   * 判断是否需要跳过鉴权
   */
  private boolean shouldSkip(HandlerMethod handlerMethod) {
    // Controller 方法或类上有注解时跳过
    return handlerMethod.hasMethodAnnotation((Class<? extends Annotation>) IgnoreSession.class) || AnnotatedElementUtils.hasAnnotation(
      handlerMethod.getBeanType(), IgnoreSession.class);
  }

  /**
   * 发送鉴权失败响应
   */
  @SuppressFBWarnings("XSS_SERVLET")
  private void sendAuthFailed(HttpServletResponse response, String msg) throws IOException {
    response.setStatus(401);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().print(String.format(FAILED_RESPONSE_TEXT, msg));
  }

  /**
   * 清理线程本地变量
   */
  private void clearThreadLocals() {
    SessionUtil.clearThreadLocal();
    TenantIdUtil.clearThreadLocal();
    LlmTraceUtil.clearTraceId();
  }
}
