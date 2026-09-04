package com.iwhalecloud.bote.common.interceptor;

import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.IAuthProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * 用于 WebSocket 接口的登录状态拦截器
 *
 * @author bianjp
 * @since 2026-03-23
 */
@Component
@RequiredArgsConstructor
public class WebSocketSessionInterceptor implements HandshakeInterceptor {
  /** 属性名称: 登录信息 */
  public static final String ATTRIBUTE_LOGIN_INFO = "loginInfo";

  private final IAuthProvider authProvider;

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
    Assert.isTrue(request instanceof ServletServerHttpRequest, "仅支持 ServletServerHttpRequest");
    HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
    // 其次取 session
    LoginInfo loginInfo = authProvider.getLoginInfo(servletRequest);
    if (loginInfo == null) {
      response.setStatusCode(HttpStatus.UNAUTHORIZED);
      return false;
    }
    attributes.put(ATTRIBUTE_LOGIN_INFO, loginInfo);
    return true;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {
    // 无需处理
  }
}
