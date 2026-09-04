package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 签名缓存，用于校验签名是否被使用过
 * @author wang.chunquan
 * @since 2024-08-05
 */
@Component
public class SignReplayCache {
  private final ICacheClient cacheClient;

  public SignReplayCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_SING_REPLAY);
  }

  /**
   * 保存签名
   *
   * @param sign 签名
   * @param expireMills 过期时间（秒）
   */
  public void save(String sign, long expireMills) {
    cacheClient.opsForValue().set(sign, "1", expireMills, TimeUnit.SECONDS);
  }

  /**
   * 检查签名是否已经使用
   *
   * @param sign 签名
   * @return  结果
   */
  public Boolean hasKey(String sign) {
    return cacheClient.hasKey(sign);
  }
}
