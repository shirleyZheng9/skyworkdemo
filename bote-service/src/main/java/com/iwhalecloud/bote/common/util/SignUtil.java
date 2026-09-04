package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.portal.config.properties.DefaultPortalProperties;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import java.util.Objects;

/**
 * 签名相关工具
 *
 * @author zhangJun
 * @since 2022/8/12
 **/
public final class SignUtil {

  private SignUtil() {

  }

  /**
   * 获取 cookieName 参数名
   */
  public static String getCookieName() {
    return SpringUtil.getBean(DefaultPortalProperties.class).getCookieName();
  }

  /**
   * 获取用户ID参数名
   *
   * @return 用户ID参数名
   */
  public static String getUserIdCookieName() {
    if (StringUtils.isNotEmpty(getCookieName())) {
      return getCookieName() + "-" + BaseConsts.COOKIE_NAME_USER_ID;
    }
    return BaseConsts.COOKIE_NAME_USER_ID;
  }

  public static String getUserId() {
    ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    String userId = "";
    if (servletRequestAttributes != null) {
      Cookie userIdCookie = WebUtils.getCookie(servletRequestAttributes.getRequest(), getUserIdCookieName());
      userId = Objects.nonNull(userIdCookie) ? userIdCookie.getValue() : "";
    }
    return userId;
  }
}
