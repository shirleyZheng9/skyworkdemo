package com.iwhalecloud.bote.portal.config;

import com.iwhalecloud.bote.portal.adapter.UportalAuthProvider;
import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.portal.config.properties.UportalProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * uportal 门户自动配置
 *
 * @author bianjp
 * @since 2021-07-28
 */
@AutoConfiguration
@EnableConfigurationProperties(UportalProperties.class)
@ConditionalOnProperty(name = "bote.portal.type", havingValue = "uportal")
@RequiredArgsConstructor
public class UportalAutoConfiguration {
  private final UportalProperties properties;

  /**
   * 鉴权提供者
   */
  @Bean
  public IAuthProvider portalAuthProvider() {
    return new UportalAuthProvider(properties);
  }
}

