package com.iwhalecloud.bote.portal.adapter;

import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.config.properties.NonePortalProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;

/**
 * none portal 鉴权提供者实现
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
public class NonePortalAuthProvider extends AbstractAuthProvider {
  private final NonePortalProperties properties;
  /** 固定的登录信息对象 */
  private final LoginInfo loginInfo;

  public NonePortalAuthProvider(NonePortalProperties properties) {
    this.properties = properties;
    this.loginInfo = LoginInfo.builder()
      .userId(-1L)
      .userName("none")
      .realName("none")
      .defaultTenantId(properties.getDefaultTenantId())
      .build();
  }

  @Override
  @Nullable
  public String getLoginUrl() {
    return null;
  }

  @Nullable
  @Override
  protected String getCookieName() {
    return null;
  }

  @Nullable
  @Override
  protected String getParamName() {
    return null;
  }

  @Override
  public String getSessionId(HttpServletRequest request) {
    return "none";
  }

  @Override
  public LoginInfo getLoginInfo(String sessionId) {
    return loginInfo;
  }

  @Override
  public LoginInfo getLoginInfo(HttpServletRequest request) {
    return loginInfo;
  }

  @Override
  public String getDefaultRole() {
    return properties.getDefaultRole();
  }

}
