package com.iwhalecloud.bote.service.portal.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.portal.config.properties.DefaultPortalProperties;
import com.cmsr.tiangong.cache.CacheFactory;
import com.cmsr.tiangong.cache.inf.ICacheClient;
import com.cmsr.tiangong.util.JsonUtil;
import com.cmsr.tiangong.util.SpringUtil;
import com.iwhalecloud.bote.cache.ApiAuthCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.common.util.BoteAuthUtil;
import com.iwhalecloud.bote.common.util.SignUtil;
import com.iwhalecloud.bote.dto.base.SimpleApiAuthDTO;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.mapper.base.ApiAuthManageMapper;
import com.iwhalecloud.bote.service.portal.ISingleLoginService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
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

  private final ApiAuthManageMapper apiAuthManageMapper;

  public SingleLoginServiceImpl(DefaultPortalProperties portalProperties, ApiAuthCache apiAuthCache, CacheFactory cacheFactory,
                                ApiAuthManageMapper apiAuthManageMapper) {
    this.portalProperties = portalProperties;
    this.apiAuthCache = apiAuthCache;
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_LOGIN);
    this.apiAuthManageMapper = apiAuthManageMapper;
  }

  @Override
  @SuppressFBWarnings("XSS_SERVLET")
  public void single(String accessToken, String redirect, Map<String, String> queryParams,
                     HttpServletRequest request, HttpServletResponse response) throws IOException {
    Assert.hasText(accessToken, "accessToken 参数不能为空");
    // 1.解密 accessToken，获取用户信息以及加密在其中的发布记录ID
    TokenInfo tokenInfo = decryptToken(accessToken);
    if (tokenInfo.loginInfo() == null) {
      sendErrorResponse(response, tokenInfo.errorMsg());
      return;
    }
    // 2.校验发布链接是否有效：以 accessToken 中加密的发布记录ID为准，链接参数被删除或篡改都不会绕过校验
    if (!checkPublishLink(tokenInfo, response)) {
      return;
    }
    LoginInfo loginInfo = tokenInfo.loginInfo();

    // 3.设置登录信息到 session
    String sessionId = BoteAuthUtil.setInfoAndRebuildSession(loginInfo, request, response);
    loginInfo.setToken(sessionId);

    // 4.保存登录信息到缓存
    cacheClient.opsForValue().set(sessionId, JsonUtil.toJsonStringCompact(loginInfo), portalProperties.getTimeout());

    // 5.设置 cookie
    setCookies(request, response, loginInfo);

    // 6.构建重定向URL查询参数：将 accessToken 和 redirect 以外的参数添加到URL查询参数中
    List<String> queryList = new ArrayList<>();
    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
      if (!"accessToken".equals(entry.getKey()) && !"redirect".equals(entry.getKey()) && !"publishId".equals(entry.getKey())) {
        queryList.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
      }
    }
    // 将发布记录ID透出给页面（供页面会话隔离使用）：以 accessToken 中解密的发布记录ID为准，历史链接回退使用链接参数
    String publishIdParam = tokenInfo.publishId() == null ? queryParams.get("publishId") : tokenInfo.publishId().toString();
    if (StringUtils.isNotEmpty(publishIdParam)) {
      queryList.add("publishId=" + URLEncoder.encode(publishIdParam, StandardCharsets.UTF_8));
    }

    // 7.重定向到指定页面
    response.sendRedirect(queryList.isEmpty() ? redirect : redirect + "?" + String.join("&", queryList));
  }

  /**
   * 校验发布链接是否有效
   * <p>发布记录ID加密在 accessToken 中，优先按其校验：发布记录不存在（已删除）时直接返回错误提示；</p>
   * <p>历史链接的 accessToken 中未包含发布记录ID，无法定位具体发布记录，按其发布密钥兜底校验：
   * 若该密钥对应的发布记录已全部删除，则视为链接已删除</p>
   *
   * @param tokenInfo 解密后的 token 信息
   * @param response 响应
   * @return 校验通过返回 true，否则返回 false 并已输出错误提示
   */
  private boolean checkPublishLink(TokenInfo tokenInfo, HttpServletResponse response) throws IOException {
    boolean deleted;
    if (tokenInfo.publishId() != null) {
      deleted = apiAuthManageMapper.getAppPublish(tokenInfo.publishId()) == null;
    }
    else {
      List<String> statusList = apiAuthManageMapper.selectAppPublishStatusByToken(tokenInfo.apiKey());
      deleted = !statusList.isEmpty() && statusList.stream().noneMatch(BaseConsts.STATUS_CD_VALID::equals);
    }
    if (deleted) {
      sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "404", "该链接已被删除，请联系管理员重新获取");
      return false;
    }
    return true;
  }

  /**
   * 解密 token 并获取登录信息
   */
  private TokenInfo decryptToken(String token) {
    String encryptStr = base64UrlDecode(token);
    String json = AesUtil.aesDecrypt(encryptStr, SystemParameter.ENCRYPTION_AES.getValueFromDb());
    if (StringUtils.isEmpty(json)) {
      return TokenInfo.fail("解密失败，非法 token 信息【" + token + "】");
    }
    Map<String, Object> map = JsonUtil.parseJson(json, new TypeReference<>() {
    });
    String apiKey = MapUtils.getString(map, "key");
    if (StringUtils.isEmpty(apiKey)) {
      return TokenInfo.fail("解密内容缺少 key，非法 token 信息【" + token + "】");
    }
    Long publishId = parsePublishId(map);
    SimpleApiAuthDTO apiAuth = apiAuthCache.get(apiKey);
    if (apiAuth == null) {
      return TokenInfo.fail("获取用户信息失败，非法 token 信息【" + token + "】");
    }
    if (apiAuth.isExpired()) {
      return TokenInfo.fail("用户密钥已过期，非法 token 信息【" + token + "】");
    }
    return new TokenInfo(apiAuth.toLoginInfo(), apiKey, publishId, null);
  }

  /**
   * 从解密内容中解析发布记录ID，历史链接中不存在该字段
   */
  @Nullable
  private static Long parsePublishId(Map<String, Object> map) {
    Object value = map.get("publishId");
    if (value == null || !NumberUtils.isCreatable(value.toString())) {
      return null;
    }
    return Long.valueOf(value.toString());
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
    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "401", message);
  }

  /**
   * 发送错误响应
   *
   * @param status HTTP 状态码
   * @param resultCode 业务结果码
   * @param message 错误信息
   */
  private void sendErrorResponse(HttpServletResponse response, int status, String resultCode, String message) throws IOException {
    response.setStatus(status);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("resultCode", resultCode);
    errorResponse.put("resultMsg", message);
    response.getWriter().print(JsonUtil.toJsonString(errorResponse));
  }

  /**
   * accessToken 解密结果
   *
   * @param loginInfo 登录信息，为空表示解密失败
   * @param apiKey 用户密钥（发布密钥）
   * @param publishId 发布记录ID，历史链接可能为空
   * @param errorMsg 解密失败时的错误信息
   */
  private record TokenInfo(LoginInfo loginInfo, String apiKey, Long publishId, String errorMsg) {

    /**
     * 构造解密失败结果
     */
    private static TokenInfo fail(String errorMsg) {
      return new TokenInfo(null, null, null, errorMsg);
    }
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
