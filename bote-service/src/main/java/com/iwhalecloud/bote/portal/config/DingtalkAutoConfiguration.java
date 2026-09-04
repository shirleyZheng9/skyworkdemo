package com.iwhalecloud.bote.portal.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.iwhalecloud.bote.cache.ApiAuthCache;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.portal.MultiPortalAdapter;
import com.iwhalecloud.bote.portal.adapter.DefaultPortalAuthProvider;
import com.iwhalecloud.bote.portal.config.properties.DingTalkLoginProperties;
import com.iwhalecloud.bote.portal.support.DingTalkDockClient;
import com.iwhalecloud.bote.portal.support.DingtalkAuthSupport;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;

/**
 * 集成钉钉登录配置类
 *
 * @author Aiqing
 * @since 2025/6/4
 */
@AutoConfiguration
@EnableConfigurationProperties(DingTalkLoginProperties.class)
@ConditionalOnBooleanProperty("bote.dingtalk.enabled")
@AutoConfigureAfter(DefaultPortalAutoConfiguration.class)
public class DingtalkAutoConfiguration {

  @Bean
  @ConditionalOnBean({DefaultPortalAuthProvider.class, MultiPortalAdapter.class})
  public DingtalkAuthSupport dingtalkAuthSupport(DingTalkLoginProperties dingTalkLoginProperties,
                                                 DingTalkDockClient dingTalkDockClient,
                                                 DefaultPortalAuthProvider portalAuthProvider,
                                                 ApiAuthCache apiAuthCache,
                                                 MultiPortalAdapter multiPortalAdapter,
                                                 UserManageMapper userManageMapper) {
    return new DingtalkAuthSupport(dingTalkLoginProperties, dingTalkDockClient, portalAuthProvider,
      apiAuthCache, multiPortalAdapter, userManageMapper);
  }

  @Bean
  public DingTalkDockClient dingTalkDockClient(DingTalkLoginProperties dingTalkLoginProperties, CacheFactory cacheFactory) {
    ICacheClient cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_LOGIN);
    return new DingTalkDockClient(dingTalkLoginProperties, cacheClient);
  }
}
