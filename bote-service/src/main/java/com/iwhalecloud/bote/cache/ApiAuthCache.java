package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.base.SimpleApiAuthDTO;
import com.iwhalecloud.bote.mapper.base.ApiAuthManageMapper;
import com.iwhalecloud.bss.litchi.cache.helper.BaseLocalCache;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * API 鉴权数据缓存
 *
 * @author chen.linfa
 * @since 2025-01-22
 */
@Component
public class ApiAuthCache extends BaseLocalCache<SimpleApiAuthDTO> implements TenantCacheMarker {
  private final ApiAuthManageMapper apiAuthManageMapper;

  public ApiAuthCache(ApiAuthManageMapper apiAuthManageMapper) {
    super();
    this.apiAuthManageMapper = apiAuthManageMapper;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_API_AUTH;
  }

  /**
   * 获取 API 鉴权信息
   * <p>1. 鉴权信息来源于请求头部参数 Authorization </p>
   * <p>2. 鉴权信息来源于请求参数 token </p>
   */
  @Nullable
  public SimpleApiAuthDTO getApiAuth(HttpServletRequest request) {
    String signature;
    String authorization = request.getHeader("Authorization");
    if (StringUtils.isNotEmpty(authorization)) {
      signature = authorization.replace("Bearer ", "");
    }
    else {
      signature = request.getParameter("token");
    }
    if (StringUtils.isEmpty(signature)) {
      return null;
    }
    return get(signature);
  }

  /**
   * 根据鉴权的apiKey 查询鉴权信息
   *
   * @param signature apiKey
   * @return 鉴权信息
   */
  @Nullable
  public SimpleApiAuthDTO getApiAuth(String signature) {
    if (StringUtils.isEmpty(signature)) {
      return null;
    }
    return get(signature);
  }

  @Override
  @Nullable
  protected SimpleApiAuthDTO load(String key) {
    if (StringUtils.isEmpty(key)) {
      return null;
    }
    return apiAuthManageMapper.getApiAuthByKey(key);
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void refreshLocalCache(List<String> keys) {
    logger.debug("Refresh local by keys: cacheName={}, keys={}", getCacheName(), keys);
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }
    // 删除单个租户的缓存
    if (keys.size() == 1 && StringUtils.isNumeric(keys.get(0))) {
      Long tenantId = Long.parseLong(keys.get(0));
      List<String> keysOfTenant = localCache.asMap().entrySet().stream()
        .filter(e -> tenantId.equals(e.getValue().getTenantId()))
        .map(Entry::getKey)
        .collect(Collectors.toList());
      if (!keysOfTenant.isEmpty()) {
        localCache.invalidateAll(keysOfTenant);
      }
    }
    else {
      localCache.invalidateAll(keys);
    }
  }
}
