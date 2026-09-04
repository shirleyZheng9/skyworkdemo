package com.iwhalecloud.bote.sandbox.config;

import com.iwhalecloud.bote.sandbox.pool.AgentPoolBackendFactory;
import com.iwhalecloud.bote.sandbox.pool.OpenSandboxBackendFactory;
import com.iwhalecloud.bote.sandbox.pool.SandboxBackendFactory;
import com.iwhalecloud.bote.sandbox.session.CacheBasedUserSandboxBindingRepository;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 沙箱引擎自动配置：连接、池、执行服务、REST API、定时回收。
 * <p>
 * 通过 {@code bote.sandbox.engine.provider} 在 OpenSandbox 与 AgentPool SDK 之间切换。
 * </p>
 */
@Configuration
@EnableConfigurationProperties(SandboxEngineProperties.class)
@ConditionalOnProperty(name = "bote.sandbox.engine.enabled", havingValue = "true")
@EnableScheduling
@ComponentScan(basePackages = "com.iwhalecloud.bote.sandbox")
public class SandboxEngineAutoConfiguration {

  @Bean
  @ConditionalOnProperty(name = "bote.sandbox.engine.provider", havingValue = "opensandbox", matchIfMissing = true)
  public SandboxBackendFactory openSandboxBackendFactory(SandboxEngineProperties properties) {
    return new OpenSandboxBackendFactory(properties);
  }

  @Bean
  @ConditionalOnProperty(name = "bote.sandbox.engine.provider", havingValue = "agentpool")
  public SandboxBackendFactory agentPoolBackendFactory(SandboxEngineProperties properties) {
    return new AgentPoolBackendFactory(properties);
  }

  @Bean
  public CacheBasedUserSandboxBindingRepository cacheBasedUserSandboxBindingRepository(CacheFactory cacheFactory, SandboxEngineProperties properties) {
    return new CacheBasedUserSandboxBindingRepository(cacheFactory, properties);
  }
}
