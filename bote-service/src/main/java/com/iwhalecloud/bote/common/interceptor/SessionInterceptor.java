package com.iwhalecloud.bote.common.interceptor;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.ApiAuthCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.ExternalPortalUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.base.SimpleApiAuthDTO;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.llm.client.util.LlmTraceUtil;
import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.portal.MultiPortalAdapter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

/**
 * 登录状态拦截器
 *
 * @author bianjp
 * @since 2021-08-03
 */
@Component
@RequiredArgsConstructor
public class SessionInterceptor implements AsyncHandlerInterceptor {
  /** 鉴权失败的响应内容 */
  private static final String FAILED_RESPONSE_TEXT = "{\"resultCode\": \"401\", \"resultMsg\": \"%s\"}";

  private final IAuthProvider authProvider;
  private final ApiAuthCache apiAuthCache;
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

    LoginInfo loginInfo;
    Long tenantId;

    // 优先取 API 鉴权
    SimpleApiAuthDTO apiAuth = apiAuthCache.getApiAuth(request);
    // 多门户CODE,目前百应系统会传入
    String systemCode = request.getHeader(BaseConsts.HEADER_SYSTEM_CODE);
    if (apiAuth != null) {
      if (apiAuth.isExpired()) {
        sendAuthFailed(response, "密钥已失效");
        return false;
      }
      // 基于密钥归属的租户、用户，构造登录信息
      loginInfo = apiAuth.toLoginInfo();
      tenantId = apiAuth.getTenantId();
    }
    else if (StringUtils.isNotEmpty(systemCode)) {
      IAuthProvider systemAuthProvider = multiPortalAdapter.getAuthProvider(systemCode);
      loginInfo = systemAuthProvider.getLoginInfo(request);
      if (loginInfo == null) {
        sendAuthFailed(response, "未登录");
        return false;
      }
      // authProvider 中可能做了缓存，修改前拷贝一份以避免影响缓存
      loginInfo = loginInfo.toBuilder().build();
      loginInfo.setSystemCode(systemCode);
      // 同步用户表(新增或更新用户信息)
      multiPortalAdapter.syncUser(loginInfo, systemCode, systemAuthProvider.getDefaultRole());
      // 优先取参数和head中的信息
      tenantId = TenantIdUtil.getTenantIdFromRequest(request);
      if (tenantId == null) {
        tenantId = loginInfo.getDefaultTenantId();
      }
    }
    else {
      // 其次取 session
      loginInfo = authProvider.getLoginInfo(request);
      if (loginInfo == null) {
        String sessionId = authProvider.getSessionId(request);
        String casRedirectUrl = ExternalPortalUtil.getCasRedirectUrl(sessionId, "login");
        // 是否需要重定向
        if (StringUtils.isNotEmpty(casRedirectUrl)) {
          response.setHeader(HttpHeaders.LINK, casRedirectUrl);
        }
        sendAuthFailed(response, "未登录");
        return false;
      }
      tenantId = TenantIdUtil.getTenantIdFromRequest(request);
    }

    // 设置线程变量
    SessionUtil.setSessionId(loginInfo.getToken());
    SessionUtil.setLoginInfo(loginInfo);
    TenantIdUtil.setTenantId(tenantId);
    return true;
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
   * 判断是否需要跳过鉴权
   */
  private boolean shouldSkip(HandlerMethod handlerMethod) {
    // Controller 方法或类上有注解时跳过
    return handlerMethod.hasMethodAnnotation((Class<? extends Annotation>) IgnoreSession.class) ||
      AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), IgnoreSession.class);
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
   * 清理线程本地变量
   */
  private void clearThreadLocals() {
    SessionUtil.clearThreadLocal();
    TenantIdUtil.clearThreadLocal();
    LlmTraceUtil.clearTraceId();
  }
}
