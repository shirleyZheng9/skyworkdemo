package com.iwhalecloud.bote.doc.cache;

import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bss.litchi.cache.helper.BaseSecondaryCache;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshBroadcastService;
import com.iwhalecloud.bss.litchi.cache.refresh.command.RefreshCommand;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 租户隔离的缓存抽象类
 * <p>为BaseSecondaryCache提供租户隔离功能，自动处理租户ID的缓存key隔离。</p>
 * <p>所有缓存操作都会自动在key前添加租户ID前缀，确保不同租户的数据完全隔离。</p>
 *
 * @author Aiqing
 * @since 2025/9/11
 */
public abstract class AbstractTenantCache<T> extends BaseSecondaryCache<T> {

  private static final String TENANT_PREFIX = "tenant";

  @Autowired
  private IRefreshBroadcastService refreshBroadcastService;

  public AbstractTenantCache(String pkey, String keyPrefix) {
    super(pkey, Strings.CS.appendIfMissing(keyPrefix, DocCacheConsts.CACHE_KEY_SPLIT));
  }

  /**
   * 根据缓存key从db中加载数据
   *
   * @param key 业务key
   * @return 需要缓存的缓存数据
   */
  @Nullable
  protected abstract T loadByKey(String key);

  /**
   * 获取租户隔离的缓存数据
   *
   * @param key 原始key
   * @return 缓存数据
   */
  @Override
  @Nullable
  public T get(String key) {
    Long tenantId = TenantContextHolder.getRequiredTenantId();
    validateTenantId(tenantId);
    validateKey(key);
    String tenantKey = buildTenantCacheKey(tenantId, key);
    return super.get(tenantKey);
  }

  /**
   * 批量获取租户隔离的缓存数据
   *
   * @param keys 原始key列表
   * @return 缓存数据映射，key为原始key
   */
  @Override
  public Map<String, T> mget(List<String> keys) {
    Long tenantId = TenantContextHolder.getRequiredTenantId();
    List<String> tenantKeys = buildTenantCacheKeys(tenantId, keys);
    Map<String, T> tenantResult = super.mget(tenantKeys);

    // 将结果key转换回原始key
    return tenantResult.entrySet().stream()
      .collect(Collectors.toMap(entry -> extractOriginalKey(entry.getKey()), Map.Entry::getValue));
  }

  /**
   * 设置租户隔离的缓存数据
   *
   * @param key 原始key
   * @param value 缓存数据
   * @return 是否设置成功
   */
  @Override
  public boolean put(String key, T value) {
    Long tenantId = TenantContextHolder.getRequiredTenantId();
    String tenantKey = buildTenantCacheKey(tenantId, key);
    Assert.notNull(value, "value 不能为空");
    if (useDistributionCache) {
      String rawValue = serializeValue(value);
      cacheClient.opsForValue().set(key, rawValue);
      if (useKeySetManager) {
        keySetManager.addIfAbsent(key);
      }
    }
    putLocalCache(key, value);
    this.broadcastLocalCacheRefresh(this.getCacheName(), Collections.singletonList(tenantKey));
    return true;
  }

  /**
   * 设置租户隔离的缓存数据（带过期时间）
   *
   * @param key 原始key
   * @param value 缓存数据
   * @param expireTime 过期时间（秒）
   * @return 是否设置成功
   */
  @Override
  public boolean put(String key, T value, int expireTime) {
    Long tenantId = TenantContextHolder.getRequiredTenantId();
    String tenantKey = buildTenantCacheKey(tenantId, key);
    Assert.notNull(value, "value 不能为空");
    if (useDistributionCache) {
      String rawValue = serializeValue(value);
      cacheClient.opsForValue().set(key, rawValue, expireTime, TimeUnit.SECONDS);
      if (useKeySetManager) {
        keySetManager.addIfAbsent(key);
      }
    }
    putLocalCache(key, value);
    this.broadcastLocalCacheRefresh(this.getCacheName(), Collections.singletonList(tenantKey));
    return true;
  }

  /**
   * 删除租户隔离的缓存数据
   *
   * @param key 原始key
   * @return 是否删除成功
   */
  @Override
  public boolean delete(String key) {
    Long tenantId = TenantContextHolder.getRequiredTenantId();
    String tenantKey = buildTenantCacheKey(tenantId, key);
    boolean delete = super.delete(tenantKey);
    this.broadcastLocalCacheRefresh(this.getCacheName(), Collections.singletonList(tenantKey));
    return delete;
  }

  @Override
  protected final T load(String key) {
    String originalKey = extractOriginalKey(key);
    return this.loadByKey(originalKey);
  }

  /**
   * 获取指定租户的所有缓存key
   *
   * @param tenantId 租户ID
   * @return 原始key集合
   */
  public Set<String> getKeys(Long tenantId) {
    // todo 需要优化此方法，不能直接获取所有key， 单独管理某个租户的key
    String tenantPrefix = TENANT_PREFIX + DocCacheConsts.CACHE_KEY_SPLIT + tenantId + DocCacheConsts.CACHE_KEY_SPLIT;
    Set<String> allKeys = super.getKeys();

    return allKeys.stream().filter(key -> key.startsWith(tenantPrefix)).collect(Collectors.toSet());
  }

  /**
   * 刷新指定租户和空间的所有相关缓存数据
   *
   * @param tenantId 租户ID
   * @param spaceId 空间ID
   * @return 刷新/删除的数据数量
   */
  public int clearTenantCache(Long tenantId, @Nullable Long spaceId) {
    validateTenantId(tenantId);
    if (spaceId == null) {
//      throw new IllegalArgumentException("空间ID不能为空");
      return 0;
    }

    Set<String> tenantKeys = getKeys(tenantId);
    if (tenantKeys.isEmpty()) {
      return 0;
    }

    // 过滤出与指定spaceId相关的key
    List<String> relatedKeys = tenantKeys.stream().filter(tenantKey -> {
      String originalKey = extractOriginalKey(tenantKey);
      if (StringUtils.isBlank(originalKey)) {
        return false;
      }
      // 原始key格式为: userId:spaceId
      String[] parts = originalKey.split(DocCacheConsts.CACHE_KEY_SPLIT, 2);
      if (parts.length >= 2) {
        try {
          Long keySpaceId = Long.parseLong(parts[1]);
          return keySpaceId.equals(spaceId);
        }
        catch (NumberFormatException e) {
          return false;
        }
      }
      return false;
    }).collect(Collectors.toList());

    if (relatedKeys.isEmpty()) {
      return 0;
    }

    // 删除相关key并广播刷新
    int deletedCount = 0;
    for (String tenantKey : relatedKeys) {
      if (super.delete(tenantKey)) {
        deletedCount++;
      }
    }
    this.broadcastLocalCacheRefresh(this.getCacheName(), relatedKeys);
    return deletedCount;
  }

  /**
   * 检查租户隔离的缓存数据是否存在
   *
   * @param tenantId 租户ID
   * @param key 原始key
   * @return 是否存在
   */
  public boolean exists(Long tenantId, String key) {
    String tenantKey = buildTenantCacheKey(tenantId, key);
    return get(tenantKey) != null;
  }

  /**
   * 验证租户ID
   *
   * @param tenantId 租户ID
   * @throws IllegalArgumentException 如果租户ID为空
   */
  protected void validateTenantId(@Nullable Long tenantId) {
    if (tenantId == null) {
      throw new IllegalArgumentException("租户ID不能为空");
    }
  }

  /**
   * 验证缓存key
   *
   * @param key 缓存key
   * @throws IllegalArgumentException 如果key为空
   */
  protected void validateKey(String key) {
    if (StringUtils.isBlank(key)) {
      throw new IllegalArgumentException("缓存key不能为空");
    }
  }

  /**
   * 构建租户隔离的缓存key
   *
   * @param tenantId 租户ID
   * @param key 原始key
   * @return 租户隔离的缓存key
   */
  public String buildTenantCacheKey(Long tenantId, String key) {
    validateTenantId(tenantId);
    validateKey(key);
    String tenantPrefix = TENANT_PREFIX + DocCacheConsts.CACHE_KEY_SPLIT + tenantId + DocCacheConsts.CACHE_KEY_SPLIT;
    if (key.startsWith(tenantPrefix)) {
      return key;
    }
    return tenantPrefix + key;
  }

  /**
   * 从租户隔离的缓存key中提取原始key
   *
   * @param tenantCacheKey 租户隔离的缓存key
   * @return 原始key
   */
  protected String extractOriginalKey(String tenantCacheKey) {
    if (StringUtils.isBlank(tenantCacheKey)) {
      return tenantCacheKey;
    }

    // 格式: tenant:tenantId:originalKey
    String[] parts = tenantCacheKey.split(DocCacheConsts.CACHE_KEY_SPLIT, 3);
    if (parts.length >= 3) {
      return parts[2];
    }
    return tenantCacheKey;
  }

  /**
   * 批量构建租户隔离的缓存key
   *
   * @param tenantId 租户ID
   * @param keys 原始key列表
   * @return 租户隔离的缓存key列表
   */
  public List<String> buildTenantCacheKeys(Long tenantId, List<String> keys) {
    if (keys.isEmpty()) {
      throw new IllegalArgumentException("租户ID和key列表不能为空");
    }
    return keys.stream().map(key -> buildTenantCacheKey(tenantId, key)).collect(Collectors.toList());
  }

  /**
   * 按 key 广播刷新所有应用实例的本地缓存
   *
   * @param cacheName 缓存名称
   */
  protected void broadcastLocalCacheRefresh(String cacheName, List<String> keys) {
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }
    if (!this.isLocalCacheEnabled()) {
      return;
    }
    RefreshCommand command = new RefreshCommand(cacheName, keys);
    refreshBroadcastService.broadcast(command);
  }
}
