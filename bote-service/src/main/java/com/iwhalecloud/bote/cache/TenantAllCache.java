package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.refresh.CacheRefresherRegistry;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 租户的所有缓存
 *
 * <p>虚拟缓存，用来实现刷新单个租户在所有缓存中的缓存数据，而不影响其它租户和平台缓存。</p>
 *
 * @author bianjp
 * @since 2025-04-08
 */
@Component
public class TenantAllCache implements Refreshable {
  private static final Logger logger = LoggerFactory.getLogger(TenantAllCache.class);
  /** 租户的缓存列表 */
  private List<TenantCacheMarker> tenantCaches;

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_TENANT_ALL;
  }

  @Override
  public void refresh(List<String> keys) {
    if (CollectionUtils.size(keys) != 1 || !StringUtils.isNumeric(keys.get(0))) {
      return;
    }
    String tenantId = keys.get(0);
    logger.debug("Refresh tenant cache: tenantId={}", tenantId);
    for (TenantCacheMarker cache : getTenantCaches()) {
      cache.refresh(Collections.singletonList(cache.buildTenantCacheKey(tenantId)));
    }
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    if (CollectionUtils.size(keys) != 1 || !StringUtils.isNumeric(keys.get(0))) {
      return;
    }
    String tenantId = keys.get(0);
    logger.debug("Refresh tenant local cache: tenantId={}", tenantId);
    for (TenantCacheMarker cache : getTenantCaches()) {
      if (cache.isLocalCacheEnabled()) {
        cache.refreshLocalCache(Collections.singletonList(cache.buildTenantCacheKey(tenantId)));
      }
    }
  }

  /**
   * 获取租户的缓存
   */
  private List<TenantCacheMarker> getTenantCaches() {
    // 惰性初始化，避免 bean 循环依赖
    if (tenantCaches == null) {
      tenantCaches = SpringUtil.getBean(CacheRefresherRegistry.class).getAll().stream()
        .filter(cache -> cache instanceof TenantCacheMarker)
        .map(cache -> (TenantCacheMarker) cache)
        .collect(Collectors.toList());
    }
    return tenantCaches;
  }
}
