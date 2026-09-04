package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.config.properties.DocChainProperties;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.GenerateApiKeyResponse;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.QueryApiKeyResponse;
import com.iwhalecloud.bote.dto.tenant.setting.SimpleTenantSettingInfo;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * DocChain 登录辅助类
 *
 * @author chen.linfa
 * @since 2024-10-16
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public final class DocChainLoginHelper {
  private static final Logger logger = LoggerFactory.getLogger(DocChainLoginHelper.class);

  /** 令牌 cookie 名称 */
  private static final String ACCESS_TOKEN_COOKIE = "access_token";
  /** 用户 cookie 名称 */
  private static final String USER_ID_COOKIE = "user_id";

  private final DocChainProperties properties;
  private final TenantSettingInfoCache tenantSettingInfoCache;

  /** Cookie 缓存 */
  private final LoadingCache<@NonNull Long, @NonNull String> cookieCache = CacheBuilder.newBuilder()
    .maximumSize(50)
    .expireAfterWrite(Duration.ofMinutes(5))
    .build(CacheLoader.from(this::login));

  /**
   * 登录
   *
   * @return 令牌 cookie 的值
   */
  public String login(Long tenantId) {
    String username = properties.getUsername(tenantId);
    String password;
    // 优先使用配置文件中配置的账号
    if (StringUtils.isNotEmpty(username)) {
      password = properties.getPassword(tenantId);
      Assert.hasLength(password, () -> "配置文件中未配置 DocChain 密码: tenantId=" + tenantId);
      password = Base64.encodeBase64String(password.getBytes(StandardCharsets.UTF_8));
    }
    else {
      SimpleTenantSettingInfo info = tenantSettingInfoCache.get(tenantId.toString());
      Assert.notNull(info, "当前租户未配置 DocChain 用户信息");
      username = info.getKnowledgeUserName();
      password = info.getKnowledgePassword();
      Assert.hasLength(username, "未配置 DocChain 用户名");
      Assert.hasLength(password, "未配置 DocChain 密码");
    }
    ResponseEntity<Map<String, Object>> responseEntity = doLogin(tenantId, username, password);
    String accessToken = extractCookies(responseEntity.getHeaders().get(HttpHeaders.SET_COOKIE));
    Assert.notNull(accessToken, "未返回 access_token cookie");
    return accessToken;
  }

  public ResponseEntity<Map<String, Object>> doLogin(Long tenantId, String username, String password) {
    Map<String, Object> params = new HashMap<>();
    params.put("username", username);
    params.put("password", password);
    HttpEntity<?> requestEntity = new HttpEntity<>(params);
    ResponseEntity<Map<String, Object>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(properties.getLoginApiUrl(tenantId), HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
      });
    boolean success = MapUtils.getBooleanValue(responseEntity.getBody(), "success", false);
    if (!success) {
      String errMsg = MapUtils.getString(responseEntity.getBody(), "err", "");
      throw new BssException("DocChain 鉴权失败: " + errMsg);
    }
    return responseEntity;
  }

  /**
   * 获取鉴权 Cookie
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  public String getCookie(Long tenantId) {
    Assert.notNull(tenantId, "tenantId 不能为空");
    try {
      return cookieCache.get(tenantId);
    }
    catch (ExecutionException | UncheckedExecutionException | ExecutionError e) {
      if (e.getCause() instanceof BssException) {
        throw (BssException) e.getCause();
      }
      logger.error("Failed to get DocChain cookie: tenantId={}", tenantId, e);
      throw new BssException("获取 DocChain 令牌失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取 API Key
   */
  public String getApiKey(Long tenantId) {
    String apiKey = properties.getApiKey(tenantId);
    // 优先使用配置文件中配置的账号
    if (StringUtils.isNotEmpty(apiKey)) {
      return apiKey;
    }
    else {
      SimpleTenantSettingInfo info = tenantSettingInfoCache.get(tenantId.toString());
      Assert.notNull(info, "当前租户未配置 DocChain 用户信息");
      return info.getKnowledgeApiKey();
    }
  }

  /**
   * 提取令牌 cookie 的值
   */
  @Nullable
  private static String extractCookies(@Nullable List<String> cookieHeaders) {
    if (CollectionUtils.isEmpty(cookieHeaders)) {
      return null;
    }

    List<String> cookieItems = new ArrayList<>();
    for (String cookieHeader : cookieHeaders) {
      if (!cookieHeader.startsWith(ACCESS_TOKEN_COOKIE) && !cookieHeader.startsWith(USER_ID_COOKIE)) {
        continue;
      }
      List<HttpCookie> cookies = HttpCookie.parse(cookieHeader);
      for (HttpCookie cookie : cookies) {
        if (ACCESS_TOKEN_COOKIE.equals(cookie.getName()) || USER_ID_COOKIE.equals(cookie.getName())) {
          cookieItems.add(cookies.get(0).getName() + "=" + cookies.get(0).getValue());
        }
      }
    }
    return cookieItems.isEmpty() ? null : String.join(";", cookieItems);
  }

  /**
   * 构造带有令牌信息的请求头部
   */
  public HttpHeaders buildHeader(Long tenantId) {
    HttpHeaders headers = new HttpHeaders();
    // 优先使用 API Key 方式鉴权
    String apikey = getApiKey(tenantId);
    if (StringUtils.isNotEmpty(apikey)) {
      headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apikey);
    }
    else {
      headers.set(HttpHeaders.COOKIE, getCookie(tenantId));
    }
    return headers;
  }

  /**
   * 构造带有令牌信息的请求头部
   */
  public Headers buildOkHttpHeaders(Long tenantId) {
    return Headers.of(HttpHeaders.COOKIE, getCookie(tenantId));
  }

  /**
   * 注册新用户
   */
  public void register(Long tenantId, String userName, String password) {
    Map<String, Object> params = new HashMap<>();
    params.put("username", userName);
    params.put("password", password);
    params.put("role", "TOPIC_ADMIN");
    HttpEntity<?> requestEntity = new HttpEntity<>(params);
    ResponseEntity<Map<String, Object>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(properties.getRegisterApiUrl(tenantId), HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
      });
    boolean success = MapUtils.getBooleanValue(responseEntity.getBody(), "success", false);
    if (!success) {
      String errMsg = MapUtils.getString(responseEntity.getBody(), "err", "");
      throw new BssException("DocChain 注册新用户失败: " + errMsg);
    }
  }

  /**
   * 判断是否存在 API Key
   */
  public boolean existsApiKey(Long tenantId, String apiKey) {
    try {
      HttpHeaders headers = buildHeader(tenantId);
      HttpEntity<?> requestEntity = new HttpEntity<>(headers);
      String url = properties.getQueryApiKeysApiUrl(tenantId);
      ResponseEntity<QueryApiKeyResponse> responseEntity = HttpUtil.getRestTemplate()
        .exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {
        });
      QueryApiKeyResponse response = responseEntity.getBody();
      if (response == null || !BooleanUtils.isTrue(response.getSuccess())) {
        String errMsg = response == null ? "" : response.getErr();
        logger.error("Failed to query api key: {}", errMsg);
        // 兼容低版本，忽略异常
        return false;
      }
      else {
        return IterableUtils.matchesAny(CollectionUtils.emptyIfNull(response.getData()),
          p -> apiKey.equals(p.getApiKey()) && "A".equals(p.getState()));
      }
    }
    catch (Exception e) {
      logger.error("Failed to query api key: {}", e.getMessage());
      // 兼容低版本，忽略异常
      return false;
    }
  }

  /**
   * 通过管理员账号，给指定用户生成 API Key
   */
  public String generateApiKey(Long tenantId, String userName) {
    try {
      HttpHeaders headers = buildHeader(tenantId);
      HttpEntity<?> requestEntity = new HttpEntity<>(headers);
      String url = properties.getGenerateApiKeyApiUrl(tenantId) + "?user_name=" + userName;
      ResponseEntity<GenerateApiKeyResponse> responseEntity = HttpUtil.getRestTemplate()
        .exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {
        });
      GenerateApiKeyResponse response = responseEntity.getBody();
      if (response == null || !BooleanUtils.isTrue(response.getSuccess())) {
        String errMsg = response == null ? "" : response.getErr();
        logger.error("Failed to generate api key: {}", errMsg);
        // 兼容低版本，忽略异常
        return null;
      }
      else {
        return response.getData().getApiKey();
      }
    }
    catch (Exception e) {
      logger.error("Failed to generate api key: {}", e.getMessage());
      // 兼容低版本，忽略异常
      return null;
    }
  }
}
