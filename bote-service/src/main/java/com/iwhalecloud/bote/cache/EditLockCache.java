package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.cache.iterator.KeysIterator;
import java.util.concurrent.TimeUnit;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 编辑锁缓存
 *
 * @author qian.sisheng
 * @since 2025-05-08
 */
@Component
public class EditLockCache {

  /** 锁失效时间（秒） */
  public static final long LOCK_EXPIRE_SECONDS = 300;

  private final ICacheClient cacheClient;

  public EditLockCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_EDIT_LOCK);
  }

  /**
   * 如果不存在保存
   *
   * @param key 键
   * @param value 值
   */
  @Nullable
  public Boolean saveIfAbsent(String key, String value) {
    return cacheClient.opsForValue().setIfAbsent(key, value, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
  }

  /**
   * 保存
   *
   * @param key 键
   * @param value 值
   */
  public void save(String key, String value) {
    cacheClient.opsForValue().set(key, value, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
  }

  /**
   * 删除
   *
   * @param key 键
   */
  public void delete(String key) {
    cacheClient.delete(key);
  }

  /**
   * 查询
   *
   * @param key 键
   * @return 值
   */
  @Nullable
  public String get(String key) {
    return cacheClient.opsForValue().get(key);
  }

  /**
   * 扫描
   *
   * @param pattern 匹配模式
   * @return 分批迭代器
   */
  public KeysIterator scan(String pattern) {
    return cacheClient.scan(pattern);
  }

}
