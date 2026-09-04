package com.iwhalecloud.bote.cache;

import com.google.common.cache.Cache;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;

/**
 * 租户缓存标记
 *
 * <p>实现此接口的缓存表示缓存的是租户的数据，按 key 刷新缓存时支持传 tenantId 刷新该租户下所有缓存</p>
 *
 * @author bianjp
 * @since 2025-04-08
 */
public interface TenantCacheMarker extends Refreshable {

  /**
   * 构造刷新租户缓存的 key
   *
   * <p>如果缓存自身的 key 可能是一串数字，需要覆盖此方法，添加前缀以避免无法区分普通缓存 key 和 tenantId</p>
   */
  default String buildTenantCacheKey(String tenantId) {
    return tenantId;
  }

  /**
   * 失效本地缓存
   */
  default void invalidateLocalCache(Cache<@NonNull Pair<Long, Long>, ?> cache, List<String> keys) {
    // 删除单个租户的缓存
    if (keys.size() == 1 && StringUtils.isNumeric(keys.get(0))) {
      Long tenantId = Long.parseLong(keys.get(0));
      List<Pair<Long, Long>> keysOfTenant = cache.asMap().keySet().stream()
        .filter(p -> tenantId.equals(p.getLeft()))
        .collect(Collectors.toList());
      if (!keysOfTenant.isEmpty()) {
        cache.invalidateAll(keysOfTenant);
      }
    }
    else {
      for (String key : keys) {
        String[] pieces = StringUtils.split(key, ':');
        if (pieces != null && pieces.length == 2 && StringUtils.isNumeric(pieces[0]) && StringUtils.isNumeric(pieces[1])) {
          cache.invalidate(Pair.of(Long.parseLong(pieces[0]), Long.parseLong(pieces[1])));
        }
      }
    }
  }
}
