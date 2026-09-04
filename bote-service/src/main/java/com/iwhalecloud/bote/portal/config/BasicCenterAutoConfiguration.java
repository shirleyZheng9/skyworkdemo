package com.iwhalecloud.bote.portal.config;

import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.portal.adapter.BasicCenterAuthProvider;
import com.iwhalecloud.bote.portal.config.properties.BasicCenterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * basicCenter 门户自动配置
 *
 * @author chen.linfa
 * @since 2024-11-16
 */
@AutoConfiguration
@EnableConfigurationProperties(BasicCenterProperties.class)
@ConditionalOnProperty(name = "bote.portal.type", havingValue = "basiccenter")
@RequiredArgsConstructor
public class BasicCenterAutoConfiguration {
  private final BasicCenterProperties properties;

  /**
   * 鉴权提供者
   */
  @Bean
  public IAuthProvider portalAuthProvider() {
    return new BasicCenterAuthProvider(properties);
  }
}

