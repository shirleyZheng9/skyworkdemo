package com.iwhalecloud.bote.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;

/**
 * OAuth2缓存
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@Component
public class OAuth2Cache {

  public static final String REFRESH_TOKEN = "refresh_token:";
  public static final String ACCESS_TOKEN = "access_token:";
  public static final String AUTH_CODE = "auth_code:";
  private final ICacheClient cacheClient;

  public OAuth2Cache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_OAUTH2TOKEN);
  }

  /**
   * 保存授权码
   *
   * @param key           键
   * @param value         值
   * @param expireSeconds 过期时间（秒）
   */
  public void saveAuthorizationCode(String key, String value, long expireSeconds) {
    cacheClient.opsForValue().set(AUTH_CODE + key, value, expireSeconds, TimeUnit.SECONDS);
  }

  /**
   * 获取授权码
   *
   * @param key 键
   * @return 值
   */
  @Nullable
  public String getAuthorizationCode(String key) {
    return cacheClient.opsForValue().get(AUTH_CODE + key);
  }

  /**
   * 删除授权码
   *
   * @param key 键
   */
  public void deleteAuthorizationCode(String key) {
    cacheClient.delete(AUTH_CODE + key);
  }

  /**
   * 保存访问令牌
   *
   * @param key           键
   * @param value         值
   * @param expireSeconds 过期时间（秒）
   */
  public void saveAccessToken(String key, String value, long expireSeconds) {
    cacheClient.opsForValue().set(ACCESS_TOKEN + key, value, expireSeconds, TimeUnit.SECONDS);
  }

  /**
   * 获取访问令牌
   *
   * @param key 键
   * @return 值
   */
  @Nullable
  public String getAccessToken(String key) {
    return cacheClient.opsForValue().get(ACCESS_TOKEN + key);
  }


  /**
   * 保存刷新令牌
   *
   * @param key           键
   * @param value         值
   * @param expireSeconds 过期时间（秒）
   */
  public void saveRefreshToken(String key, String value, long expireSeconds) {
    cacheClient.opsForValue().set(REFRESH_TOKEN + key, value, expireSeconds, TimeUnit.SECONDS);
  }

  /**
   * 获取刷新令牌
   *
   * @param key 键
   * @return 值
   */
  @Nullable
  public String getRefreshToken(String key) {
    return cacheClient.opsForValue().get(REFRESH_TOKEN + key);
  }

  /**
   * 删除刷新令牌
   *
   * @param key 键
   */
  public void deleteRefreshToken(String key) {
    cacheClient.delete(REFRESH_TOKEN + key);
  }

}
