package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.portal.ExternalPortalDTO;
import com.iwhalecloud.bote.mapper.portal.ExternalPortalMapper;
import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.portal.adapter.BasicCenterAuthProvider;
import com.iwhalecloud.bote.portal.adapter.CasPortalAuthProvider;
import com.iwhalecloud.bote.portal.adapter.NgportalAuthProvider;
import com.iwhalecloud.bote.portal.adapter.OAuth2PortalAuthProvider;
import com.iwhalecloud.bote.portal.adapter.SsoPortalAuthProvider;
import com.iwhalecloud.bote.portal.adapter.UportalAuthProvider;
import com.iwhalecloud.bote.portal.config.properties.AbstractPortalProperties;
import com.iwhalecloud.bote.portal.config.properties.BasicCenterProperties;
import com.iwhalecloud.bote.portal.config.properties.CasPortalProperties;
import com.iwhalecloud.bote.portal.config.properties.NgportalProperties;
import com.iwhalecloud.bote.portal.config.properties.OAuth2PortalProperties;
import com.iwhalecloud.bote.portal.config.properties.SsoPortalProperties;
import com.iwhalecloud.bote.portal.config.properties.UportalProperties;
import com.iwhalecloud.bss.litchi.cache.helper.BaseLocalCache;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;

/**
 * 门户适配缓存
 *
 * <p>只使用本地缓存</p>
 *
 * @author bianjp
 * @since 2025-02-25
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class PortalAdapterCache extends BaseLocalCache<Pair<Long, IAuthProvider>> implements TenantCacheMarker {
  private final ExternalPortalMapper externalPortalMapper;

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_PORTAL_ADAPTER;
  }

  /**
   * 获取鉴权提供者
   */
  @Nullable
  public IAuthProvider getAuthProvider(String systemCode) {
    Pair<Long, IAuthProvider> pair = get(systemCode);
    return pair != null ? pair.getRight() : null;
  }

  @Override
  @Nullable
  protected Pair<Long, IAuthProvider> load(String key) {
    ExternalPortalDTO portal = externalPortalMapper.selectPortalByPortalCode(key);
    if (portal == null) {
      return null;
    }
    return Pair.of(portal.getDefaultTenantId(), buildAuthProvider(portal));
  }

  /**
   * 构造鉴权提供者。门户配置不合法时返回 null
   */
  @Nullable
  private IAuthProvider buildAuthProvider(ExternalPortalDTO portal) {
    switch (StringUtils.defaultString(portal.getPortalType())) {
      case BaseConsts.PORTAL_TYPE_OAUTH2: {
        OAuth2PortalProperties properties = new OAuth2PortalProperties();
        properties.setDefaultTenantId(portal.getDefaultTenantId());
        properties.setDefaultRole(portal.getDefaultRole());
        properties.setParamName(portal.getUrlParamName());
        properties.setLoginUrl(portal.getLoginUrl());
        properties.setAutoCreateTenant(BaseConsts.TRUE.equals(portal.getAutoCreateTenant()));
        properties.setRedirectUrl(portal.getRedirectUrl());
        properties.setScript(portal.getSsoScript());
        return new OAuth2PortalAuthProvider(properties);
      }
      case BaseConsts.PORTAL_TYPE_SSO: {
        SsoPortalProperties properties = new SsoPortalProperties();
        properties.setDefaultTenantId(portal.getDefaultTenantId());
        properties.setDefaultRole(portal.getDefaultRole());
        properties.setParamName(portal.getUrlParamName());
        properties.setLoginUrl(portal.getLoginUrl());
        properties.setAutoCreateTenant(BaseConsts.TRUE.equals(portal.getAutoCreateTenant()));
        properties.setSecretKey(portal.getSecretKey());
        properties.setScript(portal.getSsoScript());
        return new SsoPortalAuthProvider(properties);
      }
      case BaseConsts.PORTAL_TYPE_CAS: {
        CasPortalProperties properties = new CasPortalProperties();
        properties.setServiceUrl(portal.getLoginUrl());
        properties.setLoginUrl(portal.getLoginUrl());
        properties.setLoggedUrl(portal.getLoggedUrl());
        properties.setParamName(portal.getUrlParamName());
        properties.setDefaultRole(portal.getDefaultRole());
        properties.setDefaultTenantId(portal.getDefaultTenantId());
        return new CasPortalAuthProvider(properties);
      }
      case BaseConsts.PORTAL_TYPE_UPORTAL: {
        UportalProperties properties = new UportalProperties();
        fillPortalProperties(properties, portal);
        return new UportalAuthProvider(properties);
      }
      case BaseConsts.PORTAL_TYPE_NGPORTAL: {
        NgportalProperties properties = new NgportalProperties();
        fillPortalProperties(properties, portal);
        return new NgportalAuthProvider(properties);
      }
      case BaseConsts.PORTAL_TYPE_BASIC_CENTER:
        BasicCenterProperties properties = new BasicCenterProperties();
        fillPortalProperties(properties, portal);
        return new BasicCenterAuthProvider(properties);
      default:
        logger.warn("Unknown portal type: id={}, type={}", portal.getId(), portal.getPortalType());
        return null;
    }
  }

  /**
   * 填充门户配置
   */
  private void fillPortalProperties(AbstractPortalProperties properties, ExternalPortalDTO portal) {
    portal.buildHeaders(portal.getHeaderJson());
    properties.setHeaders(portal.getHeaders());
    properties.setLoginUrl(portal.getLoginUrl());
    properties.setLoggedUrl(portal.getLoggedUrl());
    properties.setCookieName(portal.getCookieName());
    properties.setParamName(portal.getUrlParamName());
    properties.setDefaultTenantId(portal.getDefaultTenantId());
    properties.setDefaultRole(portal.getDefaultRole());
    try {
      properties.afterPropertiesSet();
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
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
      Long tenantId = Long.parseLong(keys.get(0));
      List<String> keysOfTenant = localCache.asMap().entrySet().stream()
        .filter(e -> tenantId.equals(e.getValue().getLeft()))
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
