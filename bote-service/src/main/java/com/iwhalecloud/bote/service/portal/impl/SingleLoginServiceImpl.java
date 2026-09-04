package com.iwhalecloud.bote.service.portal.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.portal.config.properties.DefaultPortalProperties;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bote.cache.ApiAuthCache;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.common.util.BoteAuthUtil;
import com.iwhalecloud.bote.common.util.SignUtil;
import com.iwhalecloud.bote.dto.base.SimpleApiAuthDTO;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.service.portal.ISingleLoginService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单点登录访问博特指定页面
 *
 * @author wangtingyun
 * @since 2026-02-07
 */
@Service
public class SingleLoginServiceImpl implements ISingleLoginService {

  private final DefaultPortalProperties portalProperties;

  private final ICacheClient cacheClient;

  private final ApiAuthCache apiAuthCache;

  public SingleLoginServiceImpl(DefaultPortalProperties portalProperties, ApiAuthCache apiAuthCache, CacheFactory cacheFactory) {
    this.portalProperties = portalProperties;
    this.apiAuthCache = apiAuthCache;
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_LOGIN);
  }

  @Override
  @SuppressFBWarnings("XSS_SERVLET")
  public void single(String accessToken, String redirect, Map<String, String> queryParams,
                     HttpServletRequest request, HttpServletResponse response) throws IOException {
    Assert.hasText(accessToken, "accessToken 参数不能为空");
    // 1.解密 accessToken 获取用户信息
    Pair<LoginInfo, String> pair = decryptToken(accessToken);
    if (pair.getLeft() == null) {
      sendErrorResponse(response, pair.getRight());
      return;
    }
    LoginInfo loginInfo = pair.getLeft();

    // 2.设置登录信息到 session
    String sessionId = BoteAuthUtil.setInfoAndRebuildSession(loginInfo, request, response);
    loginInfo.setToken(sessionId);

    // 3.保存登录信息到缓存
    cacheClient.opsForValue().set(sessionId, JsonUtil.toJsonStringCompact(loginInfo), portalProperties.getTimeout());

    // 4.设置 cookie
    setCookies(request, response, loginInfo);

    // 5.构建重定向URL查询参数：将 accessToken 和 redirect 以外的参数添加到URL查询参数中
    List<String> queryList = new ArrayList<>();
    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
      if (!"accessToken".equals(entry.getKey()) && !"redirect".equals(entry.getKey())) {
        queryList.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
      }
    }

    // 6.重定向到指定页面
    response.sendRedirect(queryList.isEmpty() ? redirect : redirect + "?" + String.join("&", queryList));
  }

  /**
   * 解密 token 并获取登录信息
   */
  private Pair<LoginInfo, String> decryptToken(String token) {
    String encryptStr = base64UrlDecode(token);
    String json = AesUtil.aesDecrypt(encryptStr, SystemParameter.ENCRYPTION_AES.getValueFromDb());
    if (StringUtils.isEmpty(json)) {
      return Pair.of(null, "解密失败，非法 token 信息【" + token + "】");
    }
    Map<String, Object> map = JsonUtil.parseJson(json, new TypeReference<>() {
    });
    String apiKey = MapUtils.getString(map, "key");
    if (StringUtils.isEmpty(apiKey)) {
      return Pair.of(null, "解密内容缺少 key，非法 token 信息【" + token + "】");
    }
    SimpleApiAuthDTO apiAuth = apiAuthCache.get(apiKey);
    if (apiAuth != null) {
      if (apiAuth.isExpired()) {
        return Pair.of(null, "用户密钥已过期，非法 token 信息【" + token + "】");
      }
      return Pair.of(apiAuth.toLoginInfo(), null);
    }
    return Pair.of(null, "获取用户信息失败，非法 token 信息【" + token + "】");
  }

  /**
   * 设置 cookie
   */
  @SuppressFBWarnings({ "HTTPONLY_COOKIE", "INSECURE_COOKIE" })
  private void setCookies(HttpServletRequest request, HttpServletResponse response, LoginInfo loginInfo) {
    String basePath = request.getParameter("basePath");
    if (StringUtils.isEmpty(basePath)) {
      basePath = "/";
    }

    // 设置用户ID cookie（用于签名）
    Cookie userIdCookie = new Cookie(SignUtil.getUserIdCookieName(), String.valueOf(loginInfo.getUserId()));
    userIdCookie.setPath(basePath);
    userIdCookie.setHttpOnly(false);
    userIdCookie.setSecure(SpringUtil.getProperty("app.security.cookieSecure", Boolean.class, false));
    response.addCookie(userIdCookie);
  }

  /**
   * 发送错误响应
   */
  private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("resultCode", "401");
    errorResponse.put("resultMsg", message);
    response.getWriter().print(JsonUtil.toJsonString(errorResponse));
  }

  /**
   * Base64URL 解码
   */
  private String base64UrlDecode(String base64UrlStr) {
    if (StringUtils.isEmpty(base64UrlStr)) {
      return base64UrlStr;
    }
    // 还原特殊字符：- -> +, _ -> /
    String base64Str = base64UrlStr.replace('-', '+').replace('_', '/');
    // 添加填充符 =（Base64 编码长度必须是 4 的倍数）
    int paddingLength = (4 - base64Str.length() % 4) % 4;
    for (int i = 0; i < paddingLength; i++) {
      base64Str += "=";
    }
    return base64Str;
  }
}
