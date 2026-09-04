package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import java.util.concurrent.TimeUnit;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 钉钉发送消息缓存
 *
 * @author qian.sisheng
 * @since 2025-04-14
 */
@Component
public class DingDingCache {

  /**
   * 缓存有效时间(秒)
   */
  private static final int CACHE_EXPIRE_SECONDS = 7100;

  private final ICacheClient cacheClient;

  public DingDingCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_DINGDING);
  }

  public void save(String appKey, String token) {
    cacheClient.opsForValue().set(appKey, token, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
  }

  /**
   * 获取token
   *
   * @param appKey 应用凭证
   * @return 验证码
   */
  @Nullable
  public String get(String appKey) {
    return cacheClient.opsForValue().get(appKey);
  }

}
