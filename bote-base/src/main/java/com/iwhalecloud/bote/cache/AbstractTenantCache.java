package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.helper.BaseSecondaryCache;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 租户相关的缓存抽象类
 *
 * <p>用于辅助实现只刷新单个租户的缓存。</p>
 *
 * <p>刷新缓存时，只删除不重新加载，以避免缓存不常用数据，同时降低用户频繁修改配置时刷新缓存的开销。</p>
 *
 * @author bianjp
 * @since 2025-04-08
 */
@SuppressWarnings("PMD.GuardLogStatement")
public abstract class AbstractTenantCache<T> extends BaseSecondaryCache<T> implements TenantCacheMarker {
  public AbstractTenantCache(String keyPrefix) {
    super(CacheConsts.GROUP_PLATFORM, keyPrefix);
  }

  @Override
  public void refresh(List<String> keys) {
    logger.debug("Refresh by keys: cacheName={}, keys={}", getCacheName(), keys);
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }

    // 删除单个租户的缓存
    if (keys.size() == 1 && StringUtils.isNumeric(keys.get(0))) {
      String prefix = keys.get(0) + CacheConsts.COLON;
      // 删除分布式缓存
      if (useDistributionCache) {
        List<String> keysOfTenant = getKeys().stream().filter(key -> key.startsWith(prefix)).collect(Collectors.toList());
        if (!keysOfTenant.isEmpty()) {
          deleteDistributedCache(keysOfTenant);
        }
      }
      // 删除本地缓存
      deleteLocalCacheByTenantId(prefix);
    }
    else {
      deleteDistributedCache(keys);
      deleteLocalCache(keys);
    }
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    logger.debug("Refresh local by keys: cacheName={}, keys={}", getCacheName(), keys);
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }
    // 删除单个租户的缓存
    if (keys.size() == 1 && StringUtils.isNumeric(keys.get(0))) {
      deleteLocalCacheByTenantId(keys.get(0) + CacheConsts.COLON);
    }
    else {
      localCache.invalidateAll(keys);
    }
  }

  /**
   * 删除单个租户的本地缓存
   */
  private void deleteLocalCacheByTenantId(String tenantIdPrefix) {
    if (!useLocalCache) {
      return;
    }
    List<String> keysOfTenant = localCache.asMap().keySet().stream().filter(key -> key.startsWith(tenantIdPrefix)).collect(Collectors.toList());
    if (!keysOfTenant.isEmpty()) {
      localCache.invalidateAll(keysOfTenant);
    }
  }
}
