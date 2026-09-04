package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * 微信认证缓存
 *
 * <p>用于缓存微信access_token，有效期7100秒。</p>
 *
 * @author lizuyin
 * @since 2025/08/15
 */
@Component
public class WechatAuthCache {

  /**
   * 缓存有效时间(秒)
   */
  private static final int CACHE_EXPIRE_SECONDS = 7100;

  private final ICacheClient cacheClient;

  public WechatAuthCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_WECHAT_AUTH);
  }

  /**
   * 保存token到缓存
   *
   * @param appKey 应用凭证（appid_secret格式）
   * @param token 访问令牌
   */
  public void save(String appKey, String token) {
    cacheClient.opsForValue().set(appKey, token, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
  }

  /**
   * 获取token
   *
   * @param appKey 应用凭证（appid_secret格式）
   * @return 访问令牌，如果不存在或已过期则返回null
   */
  @Nullable
  public String get(String appKey) {
    return cacheClient.opsForValue().get(appKey);
  }

  /**
   * 删除缓存
   *
   * @param appKey 应用凭证（appid_secret格式）
   */
  public void delete(String appKey) {
    cacheClient.delete(appKey);
  }

}
