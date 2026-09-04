package com.iwhalecloud.bote.portal.config;

import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.portal.adapter.NonePortalAuthProvider;
import com.iwhalecloud.bote.portal.config.properties.NonePortalProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * none portal 门户自动配置
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@AutoConfiguration
@EnableConfigurationProperties(NonePortalProperties.class)
@ConditionalOnProperty(name = "bote.portal.type", havingValue = "none")
public class NonePortalAutoConfiguration {

  /**
   * 鉴权提供者
   */
  @Bean
  public IAuthProvider portalAuthProvider(NonePortalProperties properties) {
    return new NonePortalAuthProvider(properties);
  }
}
