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
import com.iwhalecloud.bote.service.base.IEditLockService;
import com.iwhalecloud.bote.service.organization.IOrganizationManageService;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
  public PortalAdapterCache portalAdapterCache(ExternalPortalMapper externalPortalMapper) {
    return new PortalAdapterCache(externalPortalMapper);
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
