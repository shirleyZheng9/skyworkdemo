package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import java.util.concurrent.TimeUnit;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 飞书用户accessToken缓存
 *
 * @author qian.sisheng
 * @since 2025-08-20
 */
@Component
public class LarkUserAccessTokenCache {
  /**
   * 缓存有效时间(天)
   */
  private static final int CACHE_EXPIRE_DAYS = 7;

  private final ICacheClient cacheClient;

  public LarkUserAccessTokenCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_LARK_USER_ACCESS_TOKEN);
  }

  /**
   * 根据用户ID保存token
   *
   * @param userId 用户ID
   * @param token accessToken
   */
  public void save(Long userId, String token) {
    cacheClient.opsForValue().set(String.valueOf(userId), token, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
  }

  /**
   * 获取token
   *
   * @param userId 用户ID
   * @return accessToken
   */
  @Nullable
  public String get(Long userId) {
    return cacheClient.opsForValue().get(String.valueOf(userId));
  }

  /**
   * 删除token
   *
   * @param userId 用户ID
   * @return 删除成功返回true
   */
  public boolean delete(Long userId) {
    return cacheClient.delete(String.valueOf(userId));
  }
}
