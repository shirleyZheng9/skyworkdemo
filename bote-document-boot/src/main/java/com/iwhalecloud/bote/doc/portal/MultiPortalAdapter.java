package com.iwhalecloud.bote.doc.portal;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.portal.adapter.NgportalAuthProvider;
import com.iwhalecloud.bote.portal.adapter.NonePortalAuthProvider;
import com.iwhalecloud.bote.portal.adapter.UportalAuthProvider;
import com.iwhalecloud.bote.portal.config.properties.NgportalProperties;
import com.iwhalecloud.bote.portal.config.properties.NonePortalProperties;
import com.iwhalecloud.bote.portal.config.properties.UportalProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author chen.linfa
 * @since 2025-11-19
 */
@Component
@RequiredArgsConstructor
public class MultiPortalAdapter {

  private final Environment environment;

  /**
   * 获取鉴权提供者实现
   *
   * @return 鉴权提供者实现
   */
  public IAuthProvider getAuthProvider() {
    String portalType = environment.getProperty("bote.portal.type");
    Assert.hasText(portalType, "门户对接类型不能为空");
    IAuthProvider provider = null;
    if (CommonConsts.PORTAL_TYPE_NONE.equals(portalType)) {
      NonePortalProperties properties = new NonePortalProperties();
      provider = new NonePortalAuthProvider(properties);
    }
    else if (CommonConsts.PORTAL_TYPE_UPORTAL.equals(portalType)) {
      UportalProperties properties = new UportalProperties();
      properties.setCookieName(environment.getProperty("bote.uportal.cookieName", "SESSION"));
      properties.setLoggedUrl(environment.getProperty("bote.uportal.loggedUrl"));
      provider = new UportalAuthProvider(properties);
    }
    else if (CommonConsts.PORTAL_TYPE_NGPORTAL.equals(portalType)) {
      NgportalProperties properties = new NgportalProperties();
      properties.setCookieName(environment.getProperty("bote.ngportal.cookieName", "SESSION"));
      properties.setLoggedUrl(environment.getProperty("bote.ngportal.loggedUrl"));
      provider = new NgportalAuthProvider(properties);
    }
    Assert.notNull(provider, "未知系统: " + portalType);
    return provider;
  }

}
