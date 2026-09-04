package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.config.properties.WeKnoraProperties;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req.WeKnoraLoginRequest;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req.WeKnoraRegisterRequest;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraDataResponse;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraLoginResponse;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraRegisterResponse;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraSessionDTO;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.portal.WeKnoraAccountSettingDTO;
import com.iwhalecloud.bote.mapper.portal.TenantSettingInfoManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;

import lombok.RequiredArgsConstructor;

/**
 * WeKnora 登录辅助类，负责 API Key 的获取与缓存
 *
 * <p>WeKnora 使用 {@code X-API-Key} Header 鉴权，API Key 来自登录响应中的
 * {@code tenant.api_key} 字段，为租户维度的长期凭证，不会过期。
 * 仅在 API 返回 401 时触发重新登录并更新缓存。</p>
 *
 * <p>创建 WeKnora 会话（{@code POST /v1/sessions}）亦在本类中通过 Bearer Token 调用。</p>
 *
 * @author huangyunming
 * @since 2026-03-31
 */
@Component
@ConditionalOnBooleanProperty(name = "knowledge.weknora.enabled")
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public final class WeKnoraLoginHelper {
  private static final Logger logger = LoggerFactory.getLogger(WeKnoraLoginHelper.class);

  private final WeKnoraProperties properties;
  private final TenantSettingInfoManageMapper tenantSettingInfoMapper;

  /**
   * API Key 缓存（key = tenantId）。
   * API Key 长期有效，不设 TTL；仅在 401 时手动失效。
   */
  private final LoadingCache<@NonNull Long, @NonNull String> apiKeyCache = CacheBuilder.newBuilder()
    .maximumSize(50)
    .build(CacheLoader.from(this::fetchApiKey));

  /**
   * API Key 缓存（key = tenantId）。
   * API Key 长期有效，不设 TTL；仅在 401 时手动失效。
   */
  private final LoadingCache<@NonNull Long, @NonNull WeKnoraLoginResponse> loginInfoCache = CacheBuilder.newBuilder()
    .maximumSize(50)
    .build(CacheLoader.from(this::fetchToken));

  /**
   * 获取 API Key，自动从缓存或重新登录
   *
   * @param tenantId 租户 ID
   * @return API Key（sk-xxx 格式）
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  public String getApiKey(Long tenantId) {
    Assert.notNull(tenantId, "tenantId 不能为空");
    try {
      return apiKeyCache.get(tenantId);
    }
    catch (ExecutionException | UncheckedExecutionException | ExecutionError e) {
      Throwable cause = e.getCause();
      if (cause instanceof BssException) {
        throw (BssException) cause;
      }
      logger.error("Failed to get WeKnora API key: tenantId={}", tenantId, e);
      throw new BssException("获取 WeKnora API Key 失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取 token，自动从缓存或重新登录
   *
   * @param tenantId 租户 ID
   * @return token
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  public String getToken(Long tenantId) {
    Assert.notNull(tenantId, "tenantId 不能为空");
    try {
      return loginInfoCache.get(tenantId).getToken();
    }
    catch (ExecutionException | UncheckedExecutionException | ExecutionError e) {
      Throwable cause = e.getCause();
      if (cause instanceof BssException) {
        throw (BssException) cause;
      }
      logger.error("Failed to get WeKnora token: tenantId={}", tenantId, e);
      throw new BssException("获取 WeKnora token 失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取登录信息，自动从缓存或重新登录
   *
   * @param tenantId 租户 ID
   * @return 登录信息
   */
  public WeKnoraLoginResponse getLoginInfo(Long tenantId) {
    try {
      return loginInfoCache.get(tenantId);
    }
    catch (Exception e) {
      logger.error("Failed to get WeKnora login info: tenantId={}", tenantId, e);
      throw new BssException("获取 WeKnora 登录信息失败: " + e.getMessage(), e);
    }
  }

  /**
   * 构建携带 X-API-Key 的 OkHttp Headers
   *
   * @param tenantId 租户 ID
   * @return OkHttp Headers
   */
  public HttpHeaders buildOkHttpHeaders(Long tenantId) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + getToken(tenantId));
    return headers;
  }



  /**
   * 当接口返回 401 时，清除缓存并重新登录
   *
   * @param tenantId 租户 ID
   * @return 新的 API Key
   */
  public String refreshApiKey(Long tenantId) {
    apiKeyCache.invalidate(tenantId);
    return getApiKey(tenantId);
  }

  /**
   * 从 bt_tenant_setting_info 读取账号配置，调用 WeKnora 登录接口，返回 API Key
   */
  private String fetchApiKey(Long tenantId) {
    TenantSettingInfoDTO setting = tenantSettingInfoMapper.getTenantSettingInfoByTenantIdAndType(tenantId, CommonConsts.FUNC_TYPE_WEKNORA);
    Assert.notNull(setting, "当前租户未配置 WeKnora 账号信息 (funcType=weknora)");
    Assert.hasLength(setting.getSettingInfo(), "WeKnora 账号配置内容为空");

    WeKnoraAccountSettingDTO account = JsonUtil.parseJsonRequired(setting.getSettingInfo(), WeKnoraAccountSettingDTO.class);
    Assert.hasLength(account.getUserName(), "未配置 WeKnora 用户名");
    Assert.hasLength(account.getPassword(), "未配置 WeKnora 密码");
    Assert.hasLength(account.getEmail(), "未配置 WeKnora 邮箱");

    // AES 解密密码
    String plainPassword = AesUtil.aesDecrypt(account.getPassword(), BaseSystemParameter.ENCRYPTION_AES.getValueFromDb());
    if (StringUtils.isEmpty(plainPassword)) {
      // 解密失败时尝试当作明文使用
      plainPassword = account.getPassword();
    }

    WeKnoraLoginResponse response = login(tenantId, account.getEmail(), plainPassword);
    return response.getTenant().getApiKey();
  }

  /**
   * 从 bt_tenant_setting_info 读取账号配置，调用 WeKnora 登录接口，返回 API Key
   */
  private WeKnoraLoginResponse fetchToken(Long tenantId) {
    TenantSettingInfoDTO setting = tenantSettingInfoMapper.getTenantSettingInfoByTenantIdAndType(tenantId, CommonConsts.FUNC_TYPE_WEKNORA);
    Assert.notNull(setting, "当前租户未配置 WeKnora 账号信息 (funcType=weknora)");
    Assert.hasLength(setting.getSettingInfo(), "WeKnora 账号配置内容为空");

    WeKnoraAccountSettingDTO account = JsonUtil.parseJsonRequired(setting.getSettingInfo(), WeKnoraAccountSettingDTO.class);
    Assert.hasLength(account.getUserName(), "未配置 WeKnora 用户名");
    Assert.hasLength(account.getPassword(), "未配置 WeKnora 密码");
    Assert.hasLength(account.getEmail(), "未配置 WeKnora 邮箱");

    // AES 解密密码
    String plainPassword = AesUtil.aesDecrypt(account.getPassword(), BaseSystemParameter.ENCRYPTION_AES.getValueFromDb());
    if (StringUtils.isEmpty(plainPassword)) {
      // 解密失败时尝试当作明文使用
      plainPassword = account.getPassword();
    }

    return login(tenantId, account.getEmail(), plainPassword);
  }

  /**
   * 调用 WeKnora 注册接口
   *
   * @param tenantId  租户 ID
   * @param request   注册请求
   */
  public void register(Long tenantId, WeKnoraRegisterRequest request) {
    String registerUrl = properties.getRegisterApiUrl(tenantId);
    try {
      ResponseEntity<WeKnoraRegisterResponse> responseEntity = HttpUtil.getRestTemplate().exchange(
          registerUrl, HttpMethod.POST,  new HttpEntity<>(request), new ParameterizedTypeReference<>() {
          });
      WeKnoraRegisterResponse response = responseEntity.getBody();
      if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
        String msg = response == null ? "" : response.getMessage();
        throw new BssException("WeKnora 注册失败: " + msg);
      }
      logger.info("WeKnora register success: tenantId={}", tenantId);
    }
    catch (HttpClientErrorException e) {
      throw new BssException("WeKnora 注册接口调用失败: " + e.getStatusCode() + " " + e.getMessage(), e);
    }
  }

  /**
   * 调用 WeKnora 登录接口，返回完整登录响应
   */
  public WeKnoraLoginResponse login(Long tenantId, String email, String password) {
    String loginUrl = properties.getLoginApiUrl(tenantId);
    WeKnoraLoginRequest request = new WeKnoraLoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    try {
      ResponseEntity<WeKnoraLoginResponse> responseEntity = HttpUtil.getRestTemplate().exchange(
        loginUrl, HttpMethod.POST, new HttpEntity<>(request), new ParameterizedTypeReference<>() {
        });
      WeKnoraLoginResponse response = responseEntity.getBody();
      if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
        String msg = response == null ? "" : response.getMessage();
        throw new BssException("WeKnora 登录失败: " + msg);
      }
      if (response.getTenant() == null || StringUtils.isEmpty(response.getTenant().getApiKey())) {
        throw new BssException("WeKnora 登录响应中未包含 API Key");
      }
      logger.info("WeKnora login success: tenantId={}", tenantId);
      return response;
    }
    catch (HttpClientErrorException e) {
      throw new BssException("WeKnora 登录接口调用失败: " + e.getStatusCode() + " " + e.getMessage(), e);
    }
  }

  /**
   * 创建会话
   *
   * <p>对应 API：{@code POST /v1/sessions}，绑定知识库和检索策略。</p>
   *
   * @param tenantId 博特租户 ID
   * @param request  创建会话请求参数（包含 knowledge_base_id、session_strategy 等）
   * @return WeKnora session 信息（其中 id 为 weknora_session_id）
   */
  public WeKnoraSessionDTO createSession(Long tenantId, Map<String, Object> request) {
    String url = properties.getCreateSessionApiUrl(tenantId);
    WeKnoraDataResponse<WeKnoraSessionDTO> result = exchangeBearer(tenantId, url, HttpMethod.POST, request,
      new ParameterizedTypeReference<>() {
      });
    assertDataResponseSuccess(result, "创建 WeKnora 会话失败");
    return result.getData();
  }

  /**
   * 发送 HTTP 请求，使用 Bearer Token 鉴权，支持 401 自动重试
   */
  private <T> T exchangeBearer(Long tenantId, String url, HttpMethod method, @Nullable Object body,
                               ParameterizedTypeReference<T> responseType) {
    try {
      return doExchangeBearer(tenantId, url, method, body, responseType);
    }
    catch (BssException e) {
      if (e.getMessage() != null && e.getMessage().contains("401")) {
        logger.info("WeKnora Bearer API returned 401, refreshing token and retrying: tenantId={}", tenantId);
        refreshApiKey(tenantId);
        return doExchangeBearer(tenantId, url, method, body, responseType);
      }
      throw e;
    }
  }

  private <T> T doExchangeBearer(Long tenantId, String url, HttpMethod method, @Nullable Object body,
                                 ParameterizedTypeReference<T> responseType) {
    HttpHeaders headers = buildOkHttpHeaders(tenantId);
    headers.set("Content-Type", "application/json");
    HttpEntity<?> entity = body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
    try {
      ResponseEntity<T> response = HttpUtil.getRestTemplate().exchange(url, method, entity, responseType);
      return response.getBody();
    }
    catch (HttpClientErrorException.Unauthorized e) {
      throw new BssException("WeKnora Bearer API 401 鉴权失败", e);
    }
    catch (Exception e) {
      throw new BssException("WeKnora Bearer API 调用失败 [" + method + " " + url + "]: " + e.getMessage(), e);
    }
  }

  private void assertDataResponseSuccess(WeKnoraDataResponse<?> result, String errorMsg) {
    if (result == null || !Boolean.TRUE.equals(result.getSuccess())) {
      String detail = result == null || result.getMessage() == null ? "" : result.getMessage();
      throw new BssException(errorMsg + (detail.isEmpty() ? "" : ": " + detail));
    }
  }

  public void updatePassword(Long tenantId, String oldPassword, String newPassword) {
    String registerUrl = properties.getChangePasswordApiUrl(tenantId);
    try {
      Map<String, Object> request = new HashMap<>();
      request.put("old_password", oldPassword);
      request.put("new_password", newPassword);
      ResponseEntity<WeKnoraRegisterResponse> responseEntity = HttpUtil.getRestTemplate().exchange(
        registerUrl, HttpMethod.POST, new HttpEntity<>(request, buildOkHttpHeaders(tenantId)), new ParameterizedTypeReference<>() {
        });
      WeKnoraRegisterResponse response = responseEntity.getBody();
      if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
        String msg = response == null ? "" : response.getMessage();
        throw new BssException("WeKnora 修改密码失败: " + msg);
      }
      logger.info("WeKnora updatePassword success: tenantId={}", tenantId);
    }
    catch (HttpClientErrorException e) {
      throw new BssException("WeKnora 修改密码接口调用失败: " + e.getStatusCode() + " " + e.getMessage(), e);
    }
  }
}
