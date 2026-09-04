package com.iwhalecloud.bote.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.iwhalecloud.bote.cache.OAuth2Cache;
import com.iwhalecloud.bote.cache.OAuth2ClientCache;
import com.iwhalecloud.bote.config.properties.OAuth2Properties;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.service.oauth.IOAuth2ClientManageService;
import com.iwhalecloud.bote.service.oauth.OAuth2Service;
import com.iwhalecloud.bote.service.oauth.impl.OAuth2ServiceImpl;

/**
 * OAuth2自动配置
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@AutoConfiguration
@EnableConfigurationProperties(OAuth2Properties.class)
@ConditionalOnBooleanProperty("bote.oauth2.enabled")
public class OAuth2AutoConfiguration {

  /**
   * OAuth2服务
   */
  @Bean
  public OAuth2Service oauth2Service(OAuth2Properties oauth2Properties, OAuth2Cache oauth2Cache,
                                     OAuth2ClientCache oauth2ClientCache, UserManageMapper userManageMapper,
                                     IOAuth2ClientManageService oauth2ClientManageService) {
    return new OAuth2ServiceImpl(oauth2Properties, oauth2Cache, oauth2ClientCache, userManageMapper, oauth2ClientManageService);
  }
}
