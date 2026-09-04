package com.iwhalecloud.bote.portal.adapter;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.GroovyUtil;
import com.iwhalecloud.bote.common.util.ServletUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.config.properties.SsoPortalProperties;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * sso portal 鉴权提供者实现
 *
 * @author chen.linfa
 * @since 2025-02-26
 */
public class SsoPortalAuthProvider extends AbstractCachingAuthProvider<SsoPortalProperties> {
  public SsoPortalAuthProvider(SsoPortalProperties properties) {
    super(properties);
  }

  @Override
  protected String getCookieName() {
    return "";
  }

  @Override
  protected String getParamName() {
    return StringUtils.defaultIfEmpty(properties.getParamName(), "jwt");
  }

  @Override
  @Nullable
  protected LoginInfo loadLoginInfo(String sessionId) {
    LoginInfo loginInfo;
    // 自定义脚本方式
    if (StringUtils.isNotEmpty(properties.getScript())) {
      loginInfo = loadLoginInfoByScript();
    }
    // JWT 方式
    else {
      loginInfo = loadLoginInfoByJwt(sessionId);
    }
    if (loginInfo == null) {
      return null;
    }
    if (StringUtils.isEmpty(loginInfo.getUserName())) {
      return null;
    }
    // 构造登录信息
    if (StringUtils.isEmpty(loginInfo.getRealName())) {
      loginInfo.setRealName(loginInfo.getUserName());
    }
    loginInfo.setDefaultTenantId(properties.getDefaultTenantId());
    if (BaseConsts.SYSTEM_TYPE_BEYOND.equals(loginInfo.getSystemType())) {
      Map<String, String> parameters = ServletUtil.getParametersAsMap();
      if (parameters.containsKey("tenantId")) {
        loginInfo.setDefaultTenantId(Long.valueOf(MapUtils.getString(parameters, "tenantId")));
      }
    }
    boolean autoCreateTenant = properties.getAutoCreateTenant();
    Map<String, Object> attributes = MapUtils.isNotEmpty(loginInfo.getAttributes()) ? new LinkedHashMap<>(loginInfo.getAttributes()) : new LinkedHashMap<>();
    loginInfo.setAttributes(attributes);
    attributes.put("autoCreateTenant", autoCreateTenant);
    return loginInfo;
  }

  /**
   * 根据脚本加载登录信息
   */
  @Nullable
  private LoginInfo loadLoginInfoByScript() {
    Map<String, Object> params = buildScriptParams();
    Object result;
    try {
      result = GroovyUtil.invoke(properties.getScript(), params);
      if (result == null) {
        return null;
      }
    }
    catch (BssException e) {
      logger.error("Failed to load login info by sso script: params={}", params, e);
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to load login info by sso script: params={}", params, e);
      throw new BssException("单点登录失败: " + ExpUtil.getMsg(e), e);
    }

    try {
      return JsonUtil.convert(result, LoginInfo.class);
    }
    catch (IllegalArgumentException e) {
      logger.error("Failed to load login info by sso script, convert failed: result={}", result, e);
      throw new BssException("单点登录失败: 登录信息不合法", e);
    }
  }

  /**
   * 构造脚本参数
   */
  private Map<String, Object> buildScriptParams() {
    HttpServletRequest request = ServletUtil.getRequest();
    if (request == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> params = new HashMap<>();
    // 添加 URL 参数
    for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
      params.put(entry.getKey(), entry.getValue()[0]);
    }
    // 添加请求头
    params.put("headers", ServletUtil.getHeadersAsMap(request));
    return params;
  }

  /**
   * 根据 JWT token 加载登录信息
   */
  private LoginInfo loadLoginInfoByJwt(String sessionId) {
    Claims claims;
    try {
      claims = Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor(properties.getSecretKey().getBytes(StandardCharsets.UTF_8)))
        .build()
        .parseSignedClaims(sessionId)
        .getPayload();
    }
    catch (UnsupportedJwtException | MalformedJwtException e) {
      logger.error("Failed to load login info by sso jwt, token invalid: token={}", sessionId, e);
      throw new BssException("单点登录失败: token 不合法", e);
    }
    catch (ExpiredJwtException e) {
      logger.error("Failed to load login info by sso jwt, token expired: token={}", sessionId, e);
      throw new BssException("单点登录失败: token 已失效", e);
    }
    catch (SignatureException e) {
      logger.error("Failed to load login info by sso jwt, signature invalid: token={}", sessionId, e);
      throw new BssException("单点登录失败: token 签名错误", e);
    }
    catch (Exception e) {
      logger.error("Failed to load login info by sso jwt: token={}", sessionId, e);
      throw new BssException("单点登录失败: " + ExpUtil.getMsg(e), e);
    }

    try {
      return JsonUtil.convert(claims, LoginInfo.class);
    }
    catch (IllegalArgumentException e) {
      logger.error("Failed to load login info by sso jwt, convert failed: token={}", sessionId, e);
      throw new BssException("单点登录失败: token 中的登录信息不合法", e);
    }
  }
}
