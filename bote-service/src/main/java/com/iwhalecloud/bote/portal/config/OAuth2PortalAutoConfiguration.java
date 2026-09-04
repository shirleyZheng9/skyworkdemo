package com.iwhalecloud.bote.portal.config;

import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.portal.adapter.OAuth2PortalAuthProvider;
import com.iwhalecloud.bote.portal.config.properties.OAuth2PortalProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * oauth2 portal 门户自动配置
 *
 * @author chen.linfa
 * @since 2026-02-05
 */
@AutoConfiguration
@EnableConfigurationProperties(OAuth2PortalProperties.class)
@ConditionalOnProperty(name = "bote.portal.type", havingValue = "oauth2")
public class OAuth2PortalAutoConfiguration {

  /**
   * 鉴权提供者
   */
  @Bean
  public IAuthProvider portalAuthProvider(OAuth2PortalProperties properties) {
    return new OAuth2PortalAuthProvider(properties);
  }
}
