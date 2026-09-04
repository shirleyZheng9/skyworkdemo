package com.iwhalecloud.bote.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition;
import com.iwhalecloud.bote.service.plugin.IPluginManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Duration;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 插件缓存
 *
 * @author chen.linfa
 * @since 2025-12-17
 */
@Component
@SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
public class PluginHubCache implements Refreshable {

  private final IRefreshCacheService refreshCacheService;
  private final IPluginManageService pluginManageService;
  /** 本地缓存, key 为 (租户 ID, 插件 ID), 确保任一因子变化后都能自动失效缓存 */
  private final Cache<@NonNull Pair<Long, Long>, @NonNull PluginDefinition> localCache = CacheBuilder.newBuilder()
    .maximumSize(100)
    .expireAfterAccess(Duration.ofHours(1))
    .build();

  public PluginHubCache(IRefreshCacheService refreshCacheService, IPluginManageService pluginManageService) {
    this.refreshCacheService = refreshCacheService;
    this.pluginManageService = pluginManageService;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_PLUGIN_HUB;
  }

  /**
   * 获取缓存的插件定义
   */
  @Nullable
  public PluginDefinition get(Long tenantId, Long pluginId) {
    Pair<Long, Long> localKey = buildLocalKey(tenantId, pluginId);
    PluginDefinition value = localCache.getIfPresent(localKey);
    if (value == null) {
      ResultVO<PluginDefinition> result = pluginManageService.getPluginDefinition(tenantId, pluginId, true);
      Assert.isTrue(result.isSuccess(), () -> "查询插件定义出现异常，pluginId=" + pluginId);
      if (result.getResultObject() != null) {
        value = result.getResultObject();
        localCache.put(localKey, value);
      }
    }
    return value;
  }

  /**
   * 缓存插件
   */
  public void put(Long tenantId, Long pluginId, PluginDefinition value) {
    localCache.put(buildLocalKey(tenantId, pluginId), value);
  }

  /**
   * 删除缓存的插件
   */
  public void delete(Long tenantId, Long pluginId) {
    localCache.invalidate(buildLocalKey(tenantId, pluginId));
    // 广播刷新其它实例的本地缓存
    String key = tenantId + CacheConsts.COLON + pluginId;
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_PLUGIN_HUB, key);
  }

  /**
   * 构造本地缓存 key
   */
  private Pair<Long, Long> buildLocalKey(Long tenantId, Long pluginId) {
    return Pair.of(tenantId, pluginId);
  }

  @Override
  public void refreshLocalCache() {
    localCache.invalidateAll();
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    for (String key : CollectionUtils.emptyIfNull(keys)) {
      String[] parts = key.split(CacheConsts.COLON);
      if (parts.length != 2) {
        continue;
      }
      localCache.invalidate(buildLocalKey(Long.valueOf(parts[0]), Long.valueOf(parts[1])));
    }
    localCache.invalidateAll();
  }

  @Override
  public void refresh() {
    refreshLocalCache();
  }

  @Override
  public void refresh(List<String> keys) {
    refreshLocalCache(keys);
  }

}
