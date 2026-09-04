package com.iwhalecloud.bote.portal.config;

import com.iwhalecloud.bote.portal.adapter.NgportalAuthProvider;
import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.portal.config.properties.NgportalProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ngportal 门户自动配置
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@AutoConfiguration
@EnableConfigurationProperties(NgportalProperties.class)
@ConditionalOnProperty(name = "bote.portal.type", havingValue = "ngportal")
@RequiredArgsConstructor
public class NgportalAutoConfiguration {
  private final NgportalProperties properties;

  /**
   * 鉴权提供者
   */
  @Bean
  public IAuthProvider portalAuthProvider() {
    return new NgportalAuthProvider(properties);
  }
}
