package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.config.properties.KnowledgeGraphProperties;
import com.iwhalecloud.bote.doc.cache.KnowledgeGraphLoginInfoCache;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.req.KnowledgeGraphRetrieveRequest;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.req.KnowledgeGraphSsoLoginRequest;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.req.KnowledgeGraphSsoTicketPlainRequest;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp.KnowledgeGraphApiResponse;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp.KnowledgeGraphCreateSessionResponse;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp.KnowledgeGraphResponse;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp.KnowledgeGraphRetrieveResponse;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp.KnowledgeGraphSsoLoginResponse;
import com.iwhalecloud.bote.dto.portal.KnowledgeGraphAccountSettingDTO;
import com.iwhalecloud.bote.dto.tenant.setting.KnowledgeGraphLoginDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * knowledgeGraph 客户端辅助类
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty("knowledge.knowledgeGraph.enabled")
public class KnowledgeGraphClientHelper {

  private final Logger logger = LoggerFactory.getLogger(KnowledgeGraphClientHelper.class);
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final KnowledgeGraphProperties properties;
  private final KnowledgeGraphLoginInfoCache loginInfoCache;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  /** knowledgeGraph 登录失败重试次数 */
  private static final int MAX_RETRY = 1;

  /**
   * 获取 token（租户级缓存）
   *
   * @param tenantId 租户 ID
   * @return token
   */
  public String getToken(Long tenantId) {
    Assert.notNull(tenantId, "tenantId 不能为空");
    try {
      KnowledgeGraphSsoLoginResponse loginInfo = loginInfoCache.getLoginInfo(tenantId);
      if (loginInfo == null) {
        loginInfo = login(tenantId);
        loginInfoCache.save(tenantId, loginInfo, properties.getCookieMaxAge());
      }
      return loginInfo.getToken();
    }
    catch (Exception e) {
      logger.error("Failed to get knowledgeGraph token: tenantId={}", tenantId, e);
      throw new BssException("获取 knowledgeGraph token 失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取登录信息（租户级缓存）
   *
   * @param tenantId 租户 ID
   * @return 登录信息
   */
  public KnowledgeGraphSsoLoginResponse getLoginInfo(Long tenantId) {
    Assert.notNull(tenantId, "tenantId 不能为空");
    try {
      KnowledgeGraphSsoLoginResponse loginInfo = loginInfoCache.getLoginInfo(tenantId);
      if (loginInfo == null) {
        loginInfo = login(tenantId);
        loginInfoCache.save(tenantId, loginInfo, properties.getCookieMaxAge());
      }
      return loginInfo;
    }
    catch (Exception e) {
      logger.error("Failed to get knowledgeGraph login info: tenantId={}", tenantId, e);
      throw new BssException("获取 knowledgeGraph login info 失败: " + e.getMessage(), e);
    }
  }

  /**
   * 使用前端传入必填信息测试登录，不依赖租户缓存配置。
   *
   * @param account 登录参数
   */
  public void testLogin(KnowledgeGraphAccountSettingDTO account) {
    KnowledgeGraphLoginDTO knowledgeGraphLogin = new KnowledgeGraphLoginDTO();
    knowledgeGraphLogin.setProjectId(account.getProjectId());
    knowledgeGraphLogin.setProjectUserId(account.getProjectUserId());
    knowledgeGraphLogin.setProjectUserName(account.getProjectUserName());
    // 60秒过期
    knowledgeGraphLogin.setExp(System.currentTimeMillis() / 1000 + 60);
    KnowledgeGraphSsoLoginResponse loginResponse = login(knowledgeGraphLogin);
    Assert.hasText(loginResponse.getToken(), "knowledgeGraph 鉴权失败: token 为空");
  }

  /**
   * 查询知识库任务列表
   *
   * @param tenantId 租户 ID
   * @param limit 返回数量
   * @return 任务列表
   */
  public List<KnowledgeGraphResponse> queryKnowledgeList(Long tenantId, int limit, String knowledgeName) {
    String url = properties.getKnowledgeBaseApiUrl();
    Map<String, String> query = new HashMap<>();
    query.put("limit", String.valueOf(limit));
    if (StringUtils.isNotBlank(knowledgeName)) {
      query.put("database", knowledgeName);
    }
    List<KnowledgeGraphResponse> payload = exchangePayload(tenantId, url, HttpMethod.GET, query, null,
      new ParameterizedTypeReference<>() {
      });
    return payload != null ? payload : List.of();
  }

  /**
   * 知识检索
   *
   * @param tenantId 租户 ID
   * @param request 检索请求
   * @return 检索 payload
   */
  public KnowledgeGraphRetrieveResponse retrieve(Long tenantId, KnowledgeGraphRetrieveRequest request) {
    String url = properties.getKnowledgeRetrievalApiUrl();
    return exchangePayload(tenantId, url, HttpMethod.POST, null, request, new ParameterizedTypeReference<>() {
    });
  }

  /**
   * 创建会话
   *
   * @param tenantId 租户 ID
   * @param question 首轮问题（query 参数）
   * @return 第三方 sessionId
   */
  public String createSession(Long tenantId, String question) {
    Map<String, String> query = Collections.singletonMap("question", question);
    String url = properties.getCreateSessionApiUrl();
    ParameterizedTypeReference<KnowledgeGraphApiResponse<KnowledgeGraphCreateSessionResponse>> typeRef = new ParameterizedTypeReference<>() {
    };
    KnowledgeGraphCreateSessionResponse payload = exchangePayload(tenantId, url, HttpMethod.POST, query, null, typeRef);
    Assert.notNull(payload, "knowledgeGraph 创建会话 payload 为空");
    Assert.hasText(payload.getSessionId(), "knowledgeGraph 创建会话失败: sessionId 为空");
    return payload.getSessionId();
  }

  /**
   * 登录并获取 token（不经过 token 缓存头）
   */
  private KnowledgeGraphSsoLoginResponse login(Long tenantId) {
    KnowledgeGraphLoginDTO loginPayload = tenantSettingInfoCache.getKnowledgeGraphLoginPayload(tenantId);
    return login(loginPayload);
  }

  /**
   * 使用指定登录载荷登录并获取 token
   */
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  private KnowledgeGraphSsoLoginResponse login(KnowledgeGraphLoginDTO knowledgeGraphLogin) {
    String ticket = buildSsoTicket(knowledgeGraphLogin);
    if (logger.isDebugEnabled()) {
      logger.debug("knowledgeGraph 鉴权 ticket: {}", ticket);
    }
    KnowledgeGraphSsoLoginRequest request = new KnowledgeGraphSsoLoginRequest(ticket);
    ResponseEntity<KnowledgeGraphApiResponse<KnowledgeGraphSsoLoginResponse>> response = HttpUtil.getRestTemplate().exchange(
      properties.getLoginApiUrl(), HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<>() {
      });
    if (response.getBody() == null) {
      throw new BssException("knowledgeGraph 鉴权失败, 响应为空");
    }
    KnowledgeGraphSsoLoginResponse loginResponse = response.getBody().getPayload();
    if (loginResponse == null) {
      throw new BssException("knowledgeGraph 鉴权失败, 获取token失败");
    }
    return loginResponse;
  }

  /**
   * 执行一次 HTTP 调用
   */
  private <T> T exchangePayload(Long tenantId, String url, HttpMethod method, Map<String, String> queryParams, Object body,
    ParameterizedTypeReference<KnowledgeGraphApiResponse<T>> typeRef) {
    return exchangePayload(tenantId, url, method, queryParams, body, typeRef, 0);
  }

  /**
   * 执行一次 HTTP 调用，包含重试逻辑。
   */
  private <T> T exchangePayload(Long tenantId, String url, HttpMethod method, Map<String, String> queryParams, Object body,
    ParameterizedTypeReference<KnowledgeGraphApiResponse<T>> typeRef, int retryCount) {
    String requestUrl = buildUrlWithQuery(url, queryParams);
    HttpHeaders headers = buildCookieHeaders(tenantId);
    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    HttpEntity<?> entity = body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("knowledgeGraph API 调用, method={}, url={}, entity={}, body={}, queryParams={}", method, requestUrl, entity, body, queryParams);
      }
      ResponseEntity<KnowledgeGraphApiResponse<T>> response = HttpUtil.getRestTemplate().exchange(requestUrl, method, entity, typeRef);
      if (logger.isDebugEnabled()) {
        logger.debug("knowledgeGraph API 响应, method={}, url={}, response={}", method, requestUrl, response.getBody());
      }
      KnowledgeGraphApiResponse<T> result = response.getBody();
      if (result == null) {
        throw new BssException("knowledgeGraph 接口返回为空");
      }
      // 票据过期，尝试重新请求
      if (isExpired(tenantId, result)) {
        if (retryCount >= MAX_RETRY) {
          throw new BssException("knowledgeGraph 鉴权失败");
        }
        return exchangePayload(tenantId, url, method, queryParams, body, typeRef, retryCount + 1);
      }
      return result.getPayload();
    }
    catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("knowledgeGraph API 调用失败, method={}, url={}, message={}", method, requestUrl, e.getMessage(), e);
      }
      throw new BssException("knowledgeGraph API 调用失败 [" + method + " " + requestUrl + "]: " + e.getMessage(), e);
    }
  }

  /**
   * 构建携带 Cookie token 的请求头
   *
   * @param tenantId 租户 ID
   * @return HTTP headers
   */
  public HttpHeaders buildCookieHeaders(Long tenantId) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, getCookieInfo(tenantId));
    return headers;
  }

  /**
   * 获取 Cookie 信息
   */
  public String getCookieInfo(Long tenantId) {
    try {
      KnowledgeGraphSsoLoginResponse loginInfo = getLoginInfo(tenantId);
      String token = loginInfo.getToken();
      String account = loginInfo.getUserInfo().getUser().getAccount();
      Long uid = loginInfo.getUserInfo().getUser().getUid();
      return properties.getCookieName() + "=" + token + "; account=" + account + "; uid=" + uid;
    }
    catch (Exception e) {
      logger.error("获取 Cookie 信息失败", e);
      throw new BssException("获取 Cookie 信息失败", e);
    }
  }

  /**
   * 判断接口调用结果是否因为票据过期导致，并尝试重新登录。
   */
  private <T> boolean isExpired(Long tenantId, KnowledgeGraphApiResponse<T> result) {
    if (result.getCode() == null || result.getCode() != 0) {
      // 票据过期，尝试重新登录
      if (StringUtils.isNotEmpty(result.getMessage()) && result.getMessage().contains("expired ticket")) {
        if (logger.isWarnEnabled()) {
          logger.warn("knowledgeGraph 票据过期，尝试重新鉴权: code={}, message={}", result.getCode(), result.getMessage());
        }
        loginInfoCache.remove(tenantId);
        return true;
      }
      String message = StringUtils.defaultIfBlank(result.getMessage(), "unknown error");
      if (logger.isErrorEnabled()) {
        logger.error("knowledgeGraph 接口调用失败: code={}, message={}", result.getCode(), message);
      }
      throw new BssException("knowledgeGraph 接口调用失败: code=" + result.getCode() + ", message=" + message);
    }
    return false;
  }

  /**
   * 将 query 参数拼接到 URL，保持与文档中入参传递方式一致。
   */
  private String buildUrlWithQuery(String url, Map<String, String> queryParams) {
    if (queryParams == null || queryParams.isEmpty()) {
      return url;
    }
    LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.setAll(queryParams);
    return UriComponentsBuilder.fromUriString(url).queryParams(map).build().toUriString();
  }

  /**
   * 构建 SSO ticket
   */
  private String buildSsoTicket(KnowledgeGraphLoginDTO knowledgeGraphLogin) {
    String secretKey = StringUtils.trimToEmpty(properties.getSecret());
    Assert.hasText(secretKey, "knowledgeGraph 未配置 secretKey");
    Assert.hasText(knowledgeGraphLogin.getProjectId(), "knowledgeGraph 未配置项目ID");
    Assert.hasText(knowledgeGraphLogin.getProjectUserId(), "knowledgeGraph 未配置项目用户ID");
    Assert.hasText(knowledgeGraphLogin.getProjectUserName(), "knowledgeGraph 未配置项目用户名称");
    long exp = System.currentTimeMillis() / 1000 + properties.getCookieMaxAge();
    KnowledgeGraphSsoTicketPlainRequest request = new KnowledgeGraphSsoTicketPlainRequest();
    request.setProjectId(knowledgeGraphLogin.getProjectId());
    request.setProjectUserId(knowledgeGraphLogin.getProjectUserId());
    request.setProjectUserName(knowledgeGraphLogin.getProjectUserName());
    request.setExp(knowledgeGraphLogin.getExp() == 0 ? exp : knowledgeGraphLogin.getExp());
    request.setNonce(UUID.randomUUID().toString().replace("-", ""));
    request.setUsername(knowledgeGraphLogin.getProjectUserName());
    return encryptTicket(JsonUtil.toJsonString(request), secretKey);
  }

  /**
   * AES-CBC 加密 ticket（Base64Url(IV + CipherText)）
   */
  @SuppressFBWarnings({"CIPHER_INTEGRITY", "PADDING_ORACLE"})
  private String encryptTicket(String plainText, String secretKey) {
    try {
      byte[] keyBytes = new byte[16];
      byte[] source = secretKey.getBytes(StandardCharsets.UTF_8);
      if (logger.isDebugEnabled()) {
        logger.debug("knowledgeGraph secretKey length: {}", source.length);
      }
      System.arraycopy(source, 0, keyBytes, 0, Math.min(source.length, keyBytes.length));
      byte[] iv = new byte[16];
      SECURE_RANDOM.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(iv));
      byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
      byte[] all = new byte[iv.length + encrypted.length];
      System.arraycopy(iv, 0, all, 0, iv.length);
      System.arraycopy(encrypted, 0, all, iv.length, encrypted.length);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(all);
    }
    catch (Exception e) {
      logger.error("knowledgeGraph ticket 加密失败", e);
      throw new BssException("knowledgeGraph ticket 加密失败", e);
    }
  }

}
