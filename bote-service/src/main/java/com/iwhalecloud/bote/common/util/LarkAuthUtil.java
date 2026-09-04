package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.cache.LarkUserAccessTokenCache;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.lark.LarkSateDTO;
import com.iwhalecloud.bote.dto.plugin.lark.LarkUserAccessTokenDTO;
import com.iwhalecloud.bote.dto.plugin.lark.LarkUserAuthInfoDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 飞书授权工具类
 *
 * @author qian.sisheng
 * @since 2025-08-21
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class LarkAuthUtil {

  private static final Logger logger = LoggerFactory.getLogger(LarkAuthUtil.class);

  private static final LarkUserAccessTokenCache larkUserAccessTokenCache = SpringUtil.getBean(LarkUserAccessTokenCache.class);

  /** 飞书获取用户访问令牌接口 */
  private static final String LARK_GET_TOKEN_URL = "https://open.feishu.cn/open-apis/authen/v2/oauth/token";
  /** 飞书获取用户信息接口 */
  private static final String LARK_GET_USER_INFO_URL = "https://open.feishu.cn/open-apis/authen/v1/user_info";
  /** 飞书用户授权接口 */
  private static final String LARK_USER_AUTH_URL = "https://open.feishu.cn/open-apis/authen/v1/authorize";
  /** 博特网关地址 */
  private static final String BOTE_API_URL = SystemParameter.BOTE_API_URL.getValueFromEnv();
  /** 飞书授权范围 */
  private static final List<String> LARK_SCOPES = Arrays.asList("offline_access", "bitable:app", "wiki:wiki:readonly", "search:docs:read",
    "drive:drive.metadata:readonly");
  /** 博特前端网关地址 */
  private static final String BOTE_FRONT_URL = SystemParameter.BOTE_WEB_URL.getValueFromEnv();

  private LarkAuthUtil() {
  }

  /**
   * 获取当前用户的有效访问令牌
   *
   * @return 访问令牌，如果无效或不存在则返回null
   */
  @Nullable
  public static String getCurrentUserAccessToken(String appId, String appSecret) {
    String accessTokenJson = larkUserAccessTokenCache.get(SessionUtil.getLoginInfo().getUserId());
    if (StringUtils.isBlank(accessTokenJson)) {
      return null;
    }
    try {
      LarkUserAccessTokenDTO accessToken = JsonUtil.parseJson(accessTokenJson, LarkUserAccessTokenDTO.class);
      if (accessToken == null) {
        return null;
      }
      // 检查accessToken是否过期
      if (accessToken.isAccessTokenExpired()) {
        // 检查refreshToken是否过期
        if (accessToken.isRefreshTokenExpired()) {
          return null;
        }
        // 刷新token
        return refreshUserAccessToken(appId, appSecret, accessToken.getRefreshToken());
      }
      return accessToken.getAccessToken();
    }
    catch (Exception e) {
      logger.error("Failed to get userAccessToken", e);
      throw new BssException("获取飞书用户访问令牌失败", e);
    }
  }

  /**
   * 构建授权状态参数
   *
   * @param appId 应用ID
   * @param appSecret 应用密钥
   * @param redirectUrl 回调地址
   * @return 加密的状态参数
   */
  private static String buildState(String appId, String appSecret, String redirectUrl, String pluginName) {
    LarkSateDTO larkSate = new LarkSateDTO();
    larkSate.setAppId(appId);
    larkSate.setAppSecret(appSecret);
    larkSate.setUserId(SessionUtil.getLoginInfo().getUserId());
    larkSate.setRedirectUrl(redirectUrl);
    larkSate.setPluginName(pluginName);
    String state = AesUtil.aesEncrypt(JsonUtil.toJsonString(larkSate), SystemParameter.ENCRYPTION_AES.getValueFromDb());
    Assert.notNull(state, "加密授权状态失败");
    return state;
  }

  /**
   * 刷新用户访问令牌
   *
   * @param appId 飞书应用ID
   * @param appSecret 飞书应用密钥
   * @param refreshToken 刷新令牌
   * @return 新的访问令牌
   */
  @Nullable
  private static String refreshUserAccessToken(String appId, String appSecret, String refreshToken) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("grant_type", "refresh_token");
    requestBody.put("client_id", appId);
    requestBody.put("client_secret", appSecret);
    requestBody.put("refresh_token", refreshToken);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    try {
      LarkUserAccessTokenDTO response = HttpUtil.post(LARK_GET_TOKEN_URL, requestBody, new ParameterizedTypeReference<LarkUserAccessTokenDTO>() {
      }, headers);
      if (response == null) {
        throw new BssException("刷新飞书用户访问令牌失败：响应为空");
      }
      // 用户授权 365 天后，必须通过用户重新授权的方式来获取，此时会抛出错误码 20037
      if (response.getCode() == 20037) {
        return null;
      }
      if (response.getCode() != 0) {
        String msg = response.getErrorDescription();
        logger.error("Failed to request lark refreshToken API, code:{}, msg:{}", response.getCode(), msg);
        throw new BssException("刷新飞书用户访问令牌失败：" + msg);
      }
      String accessToken = response.getAccessToken();
      if (StringUtils.isEmpty(accessToken)) {
        throw new BssException("刷新飞书用户访问令牌失败：访问令牌为空");
      }
      // 保存新的令牌信息
      larkUserAccessTokenCache.save(SessionUtil.getLoginInfo().getUserId(), JsonUtil.toJsonString(response));
      return accessToken;
    }
    catch (Exception e) {
      logger.error("Failed to refresh userAccessToken: ", e);
      throw new BssException("刷新飞书用户访问令牌失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 创建需要授权的响应
   *
   * @param appId 应用ID
   * @param appSecret 应用密钥
   * @return 授权响应
   */
  public static LarkResultDTO<Map<String, Object>> createAuthUrl(String appId, String appSecret, String pluginName) {
    if (StringUtils.isEmpty(BOTE_API_URL)) {
      throw new BssException("博特后端网关地址未配置");
    }
    if (StringUtils.isEmpty(BOTE_FRONT_URL)) {
      throw new BssException("博特前端网关地址未配置");
    }
    // 构建飞书回调API
    String redirectUrl = StringUtils.stripEnd(BOTE_API_URL, "/") + "/bote/lark/callback";
    Map<String, Object> data = new HashMap<>();
    String state = buildState(appId, appSecret, redirectUrl, pluginName);
    data.put("needAuth", true);
    data.put("authUrl", generateAuthUrl(appId, redirectUrl, state));
    LarkResultDTO<Map<String, Object>> result = new LarkResultDTO<>();
    result.setCode("401");
    result.setData(data);
    result.setMsg("飞书多维表格 (open.feishu.cn) 需要授权使用，请打开授权链接进行授权");
    return result;
  }

  /**
   * 生成飞书授权 URL
   *
   * @param appId 应用ID
   * @param redirectUrl 回调地址
   * @param state 状态参数
   * @return 授权URL
   */
  private static String generateAuthUrl(String appId, String redirectUrl, String state) {
    if (StringUtils.isEmpty(appId)) {
      throw new BssException("应用ID不能为空");
    }
    if (StringUtils.isEmpty(redirectUrl)) {
      throw new BssException("回调地址不能为空");
    }
    return LARK_USER_AUTH_URL + "?response_type=code"
      + "&client_id=" + appId
      + "&redirect_uri=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8)
      + "&scope=" + URLEncoder.encode(String.join(" ", LARK_SCOPES), StandardCharsets.UTF_8)
      + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
  }

  /**
   * 保存飞书用户访问令牌
   *
   * @param code 授权码
   * @param larkSate 飞书授权参数
   */
  public static void saveUserAccessToken(String code, LarkSateDTO larkSate) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("grant_type", "authorization_code");
    requestBody.put("client_id", larkSate.getAppId());
    requestBody.put("client_secret", larkSate.getAppSecret());
    requestBody.put("code", code);
    requestBody.put("redirect_uri", larkSate.getRedirectUrl());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    try {
      LarkUserAccessTokenDTO response = HttpUtil.post(LARK_GET_TOKEN_URL, requestBody, new ParameterizedTypeReference<LarkUserAccessTokenDTO>() {
      }, headers);
      if (response == null) {
        throw new BssException("获取飞书用户访问令牌失败：响应为空");
      }
      Integer respCode = response.getCode();
      if (respCode != 0) {
        String msg = response.getErrorDescription();
        logger.error("获取飞书用户访问令牌失败, code:{}, msg:{}", respCode, msg);
        throw new BssException("获取飞书用户访问令牌失败：" + msg);
      }
      String accessToken = response.getAccessToken();
      if (StringUtils.isEmpty(accessToken)) {
        throw new BssException("获取飞书用户访问令牌失败：访问令牌为空");
      }
      // 获取飞书用户信息
      response.setUserInfo(getLarkUserInfo(accessToken, larkSate.getPluginName()));
      // 保存飞书用户访问令牌
      larkUserAccessTokenCache.save(larkSate.getUserId(), JsonUtil.toJsonString(response));
    }
    catch (Exception e) {
      logger.error("Failed to get lark userAccessToken", e);
      throw new BssException("获取飞书用户访问令牌失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 获取飞书用户信息
   *
   * @param userAccessToken 飞书用户访问令牌
   * @return 飞书用户信息
   */
  @SuppressWarnings("unchecked")
  private static LarkUserAuthInfoDTO getLarkUserInfo(String userAccessToken, String pluginName) {
    HttpHeaders headers = buildHeaders(userAccessToken);
    Map<String, Object> response = HttpUtil.get(LARK_GET_USER_INFO_URL, null, new ParameterizedTypeReference<Map<String, Object>>() {
    }, headers);
    if (response == null) {
      throw new BssException("获取飞书用户信息失败：响应为空");
    }
    Integer code = MapUtils.getInteger(response, "code");
    if (code != 0) {
      String msg = MapUtils.getString(response, "msg");
      logger.error("Failed to get lark user info, code:{}, msg:{}", code, msg);
      throw new BssException("获取飞书用户信息失败：" + msg);
    }
    Map<String, Object> data = (Map<String, Object>) MapUtils.getObject(response, "data");
    if (data == null) {
      throw new BssException("获取飞书用户信息失败：用户信息为空");
    }
    String userId = MapUtils.getString(data, "user_id");
    String userName = MapUtils.getString(data, "name");
    LarkUserAuthInfoDTO userAuthInfo = new LarkUserAuthInfoDTO();
    userAuthInfo.setUserId(userId);
    userAuthInfo.setName(userName);
    userAuthInfo.setPluginName(pluginName);
    userAuthInfo.setAuthSuccess(true);
    return userAuthInfo;
  }

  /**
   * 构建请求头
   */
  public static HttpHeaders buildHeaders(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    return headers;
  }
}
