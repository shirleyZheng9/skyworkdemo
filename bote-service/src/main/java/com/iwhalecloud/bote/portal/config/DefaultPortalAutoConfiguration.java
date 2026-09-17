package com.iwhalecloud.bote.portal.config;

import com.iwhalecloud.bote.cache.PortalAdapterCache;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bote.mapper.base.CatalogManageMapper;
import com.iwhalecloud.bote.mapper.portal.ExternalPortalMapper;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.mapper.skill.ServiceGatewayManageMapper;
import com.iwhalecloud.bote.mapper.skill.ServicePlatformManageMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bote.portal.MultiPortalAdapter;
import com.iwhalecloud.bote.portal.adapter.DefaultPortalAuthProvider;
import com.iwhalecloud.bote.portal.config.properties.DefaultPortalProperties;
import com.iwhalecloud.bote.portal.tg.TGAuthService;
import com.iwhalecloud.bote.portal.tg.TGOidcAuthService;
import com.sy4cloud.tg.common.oidc.OidcProperties;
import com.sy4cloud.tg.common.oidc.support.OidcTokenRenewClient;
import com.iwhalecloud.bote.service.base.IEditLockService;
import com.iwhalecloud.bote.service.organization.IOrganizationManageService;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 博特门户自动配置
 *
 * @author bianjp
 * @since 2024-08-22
 */
@AutoConfiguration
@EnableConfigurationProperties(DefaultPortalProperties.class)
@ConditionalOnProperty(name = "bote.portal.type", havingValue = "default")
public class DefaultPortalAutoConfiguration {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPortalAutoConfiguration.class);

  /**
   * 天工 OIDC 真实适配层。默认关闭，只有天工环境配好密钥和服务地址后才启用。
   */
  @Bean
  @ConditionalOnProperty(name = "bote.tg.oidc.enabled", havingValue = "true")
  public TGAuthService tgAuthService(OidcTokenRenewClient oidcTokenRenewClient, OidcProperties oidcProperties) {
    LOGGER.info("[TGPortal] 天工 OIDC 认证服务已启用");
    return new TGOidcAuthService(oidcTokenRenewClient, oidcProperties);
  }

  @Bean
  public DefaultPortalAuthProvider defaultPortalAuthProvider(DefaultPortalProperties properties, CacheFactory cacheFactory,
                                                             UserManageMapper userManageMapper, PasswordEncoder passwordEncoder,
                                                             IEditLockService editLockService) {
    ICacheClient cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_LOGIN);
    return new DefaultPortalAuthProvider(properties, cacheClient, userManageMapper, passwordEncoder, editLockService);
  }

  /**
   * 门户适配缓存
   */
  @Bean
  @ConditionalOnBooleanProperty("bote.portal.multi.enabled")
  public PortalAdapterCache portalAdapterCache(ExternalPortalMapper externalPortalMapper,
                                               ObjectProvider<TGAuthService> tgAuthServiceProvider) {
    // 本地没有 starter 时允许 bean 缺省，避免影响 AADP 独立登录和其他既有门户。
    TGAuthService tgAuthService = tgAuthServiceProvider.getIfAvailable();
    LOGGER.info("[TGPortal] 门户适配缓存初始化完成: tgPortalAuthAvailable={}", tgAuthService != null);
    return new PortalAdapterCache(externalPortalMapper, tgAuthService);
  }

  /**
   * 多门户适配器
   */
  @Bean
  @ConditionalOnBooleanProperty("bote.portal.multi.enabled")
  public MultiPortalAdapter multiPortalAdapter(UserManageMapper userManageMapper, TenantManageMapper tenantManageMapper,
    CatalogManageMapper catalogManageMapper, PortalAdapterCache portalAdapterCache,
    ServicePlatformManageMapper platformManageMapper, ServiceGatewayManageMapper gatewayManageMapper,
    WorkspaceManageMapper workspaceManageMapper, IOrganizationManageService organizationManageService,
    DistributedLockFactory distributedLockFactory) {
    return new MultiPortalAdapter(userManageMapper, tenantManageMapper, catalogManageMapper, portalAdapterCache,
      platformManageMapper, gatewayManageMapper, workspaceManageMapper, organizationManageService, distributedLockFactory);
  }

}
