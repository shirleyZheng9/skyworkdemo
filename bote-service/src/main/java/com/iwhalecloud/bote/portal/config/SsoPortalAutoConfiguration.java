package com.iwhalecloud.bote.portal.config;

import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.portal.adapter.SsoPortalAuthProvider;
import com.iwhalecloud.bote.portal.config.properties.SsoPortalProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * sso portal 门户自动配置
 *
 * @author chen.linfa
 * @since 2025-02-26
 */
@AutoConfiguration
@EnableConfigurationProperties(SsoPortalProperties.class)
@ConditionalOnProperty(name = "bote.portal.type", havingValue = "sso")
public class SsoPortalAutoConfiguration {

  /**
   * 鉴权提供者
   */
  @Bean
  public IAuthProvider portalAuthProvider(SsoPortalProperties properties) {
    return new SsoPortalAuthProvider(properties);
  }
}
