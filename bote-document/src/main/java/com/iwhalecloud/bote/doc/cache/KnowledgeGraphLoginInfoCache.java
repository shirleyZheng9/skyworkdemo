package com.iwhalecloud.bote.doc.cache;

import com.iwhalecloud.bote.cache.AbstractTenantCache;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp.KnowledgeGraphSsoLoginResponse;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 知识图谱登录信息缓存
 *
 * @author qian.sisheng
 * @since 2026-06-03
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class KnowledgeGraphLoginInfoCache extends AbstractTenantCache<KnowledgeGraphSsoLoginResponse> {

  /** 缓存过期安全余量（秒），避免缓存中的 token 刚好过期 */
  private static final int CACHE_EXPIRY_BUFFER_SECONDS = 5 * 60;

  public KnowledgeGraphLoginInfoCache() {
    super(CacheConsts.KEY_PREFIX_KNOWLEDGE_GRAPH_LOGIN_INFO);
    disableLocalCache();
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_KNOWLEDGE_GRAPH_LOGIN_INFO;
  }

  /**
   * 保存知识图谱令牌信息
   *
   * @param tenantId 租户ID
   * @param response 令牌信息
   * @param expireTime 过期时间（秒）
   */
  public void save(Long tenantId, KnowledgeGraphSsoLoginResponse response, int expireTime) {
    put(String.valueOf(tenantId), response, expireTime - CACHE_EXPIRY_BUFFER_SECONDS);
  }

  /**
   * 查询知识图谱令牌信息
   *
   * @param tenantId 租户ID
   * @return 知识图谱令牌信息
   */
  @Nullable
  public KnowledgeGraphSsoLoginResponse getLoginInfo(Long tenantId) {
    return get(String.valueOf(tenantId));
  }

  /**
   * 删除指定租户的登录信息缓存
   *
   * @param tenantId 租户ID
   */
  public void remove(Long tenantId) {
    delete(String.valueOf(tenantId));
  }

  @Nullable
  @Override
  protected KnowledgeGraphSsoLoginResponse load(String key) {
    // 令牌缓存仅通过 save 写入，不支持回源加载
    return null;
  }

  @Override
  public void refresh(List<String> keys) {
    // 缓存 key 只有 tenantId，不能复用 AbstractTenantCache 中的刷新逻辑
    if (logger.isDebugEnabled()) {
      logger.debug("Refresh by keys: cacheName={}, keys={}", getCacheName(), keys);
    }
    if (CollectionUtils.isNotEmpty(keys)) {
      deleteDistributedCache(keys);
      deleteLocalCache(keys);
    }
  }
}
