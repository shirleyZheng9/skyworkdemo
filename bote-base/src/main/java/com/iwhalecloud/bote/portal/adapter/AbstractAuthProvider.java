package com.iwhalecloud.bote.portal.adapter;

import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.IAuthProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.util.WebUtils;

/**
 * 鉴权提供者抽象类
 *
 * @author bianjp
 * @since 2024-10-28
 */
public abstract class AbstractAuthProvider implements IAuthProvider {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * 获取 session cookie 名称
   *
   * @return cookie 名称
   */
  @Nullable
  protected abstract String getCookieName();

  /**
   * 获取 session URL 参数 名称
   *
   * @return session URL 参数名称
   */
  @Nullable
  protected abstract String getParamName();

  @Nullable
  @Override
  public String getSessionId(HttpServletRequest request) {
    String paramName = getParamName();
    if (StringUtils.isNotEmpty(paramName)) {
      // 优先取 URL 参数
      String value = request.getParameter(paramName);
      if (StringUtils.isNotEmpty(value)) {
        return value;
      }
      // 取 Header 参数,百应系统传入的token是使用 kebab-case (连字符)格式
      value = request.getHeader(paramName);
      if (StringUtils.isNotEmpty(value)) {
        return value;
      }
    }

    // 其次取 cookie
    String cookieName = getCookieName();
    if (StringUtils.isNotEmpty(cookieName)) {
      Cookie cookie = WebUtils.getCookie(request, cookieName);
      return cookie == null ? null : cookie.getValue();
    }
    return null;
  }

  @Nullable
  @Override
  public LoginInfo getLoginInfo(HttpServletRequest request) {
    String sessionId = getSessionId(request);
    if (sessionId == null) {
      return null;
    }
    return getLoginInfo(sessionId);
  }

  @Nullable
  @Override
  public String getDefaultRole() {
    return null;
  }

  @Nullable
  @Override
  public String getRedirectUrl(HttpServletRequest request) {
    return getLoginUrl();
  }

  @Override
  public void saveExtSessionMapping(LoginInfo loginInfo) {
    // CAS 才需处理
  }
}
