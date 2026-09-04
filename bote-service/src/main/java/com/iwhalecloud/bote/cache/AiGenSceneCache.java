package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 一句话智能生成智能体缓存
 *
 * @author wang.tingyun
 * @since 2025-06-12
 */
@Component
public class AiGenSceneCache {

  private final ICacheClient cacheClient;

  public AiGenSceneCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, "aiGenScene:");
  }

  /**
   * 保存进程数据
   */
  public void save(String key, String value) {
    cacheClient.opsForValue().set(key, value, 300, TimeUnit.SECONDS);
  }

  /**
   * 获取进程数据
   */
  @Nullable
  public String get(String key) {
    return cacheClient.opsForValue().get(key);
  }

}
