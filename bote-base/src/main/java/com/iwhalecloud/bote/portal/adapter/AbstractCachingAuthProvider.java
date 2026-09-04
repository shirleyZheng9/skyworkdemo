package com.iwhalecloud.bote.portal.adapter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.iwhalecloud.bote.common.util.SysParamUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import com.iwhalecloud.bote.portal.config.properties.AbstractPortalProperties;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

/**
 * 支持本地缓存的鉴权提供者抽象类
 *
 * @author bianjp
 * @since 2024-10-28
 */
public abstract class AbstractCachingAuthProvider<T extends AbstractPortalProperties> extends AbstractAuthProvider {
  /** 空的登录信息对象，表示查不到登录信息 */
  protected static final LoginInfo EMPTY_LOGIN_INFO = new LoginInfo();

  /** 登录信息缓存。避免频繁请求外系统拖慢响应速度。有效期设置短一些，以便可以定期调用外系统接口使其续期 session */
  protected final LoadingCache<@NonNull String, @NonNull LoginInfo> loginInfoCache;
  /** 门户配置 */
  protected final T properties;

  protected AbstractCachingAuthProvider(T properties) {
    this.properties = properties;
    Long expireSeconds = SpringUtil.getProperty("login.cache.expire.seconds", Long.class, 60L);
    loginInfoCache = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(Duration.ofSeconds(expireSeconds))
      .build(new CacheLoader<>() {
        @Override
        public LoginInfo load(String key) {
          LoginInfo loginInfo = loadLoginInfo(key);
          // 未返回用户 ID 时当作没登录
          return loginInfo != null && (loginInfo.getUserId() != null || StringUtils.isNotEmpty(loginInfo.getExtUserId()))
            ? loginInfo
            : EMPTY_LOGIN_INFO;
        }
      });
  }

  @Override
  public String getLoginUrl() {
    return properties.getLoginUrl();
  }

  @Override
  protected String getCookieName() {
    return properties.getCookieName();
  }

  @Override
  protected String getParamName() {
    return properties.getParamName();
  }

  @Override
  public String getDefaultRole() {
    return properties.getDefaultRole();
  }

  @Override
  @Nullable
  @SuppressWarnings("PMD.PreserveStackTrace")
  public LoginInfo getLoginInfo(String sessionId) {
    try {
      LoginInfo loginInfo = loginInfoCache.get(sessionId);
      return loginInfo == EMPTY_LOGIN_INFO ? null : loginInfo;
    }
    catch (ExecutionException | UncheckedExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof BssException) {
        throw (BssException) cause;
      }
      throw new BssException("查询登录状态失败: " + cause.getMessage(), cause);
    }
  }

  /**
   * 根据 sessionId 加载登录信息
   */
  @Nullable
  protected abstract LoginInfo loadLoginInfo(String sessionId);

  /**
   * 构造请求对象
   */
  protected final HttpEntity<?> buildRequest(String sessionId) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.set(HttpHeaders.COOKIE, String.format("%s=%s; ZSMART_LOCALE=zh", getCookieName(), sessionId));
    fillCustomHeader(headers);
    return new HttpEntity<>(headers);
  }

  /**
   * 填充自定义请求头
   */
  private void fillCustomHeader(HttpHeaders headers) {
    List<HeaderItem> customHeaderList = properties.getHeaders();
    if (CollectionUtils.isNotEmpty(customHeaderList)) {
      for (HeaderItem headerItem : customHeaderList) {
        // 解析请求头变量值，支持动态参数，比如 $.cookie.userId
        Object paramValue = SysParamUtil.getParamValue(headerItem.getValue());
        headers.set(headerItem.getName(), paramValue != null ? String.valueOf(paramValue) : null);
      }
    }
  }

}
