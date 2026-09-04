package com.iwhalecloud.bote.loop.prompt.domain.component.conf;

import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Prompt配置类
 * 迁移对应关系: Go语言配置文件加载逻辑
 * - 功能: 配置Spring Boot的配置绑定
 * - 实现: 使用@ConfigurationProperties自动绑定配置
 * <p>
 * Java实现说明:
 * - 对应Go的配置文件加载逻辑
 * - 使用Spring Boot的配置管理功能
 * - 支持配置验证和默认值
 * <p>
 * 技术栈迁移:
 * - Go配置文件 -> Spring Boot application.yml
 * - Go配置加载 -> Spring Boot @ConfigurationProperties
 * - Go错误处理 -> Spring Boot配置验证
 */
@Configuration
public class PromptConfig {

  /**
   * 创建Prompt Hub限流配置Bean
   * 迁移对应关系: Go语言NewPromptConfigProvider函数
   * - 功能: 创建配置Bean实例
   * - 实现: Spring Boot自动配置绑定
   */
  @Bean
  public PromptHubRateLimitConfig promptHubRateLimitConfig() {
    return new PromptHubRateLimitConfig();
  }

  /**
   * 创建Prompt配置提供者Bean
   * 迁移对应关系: Go语言NewPromptConfigProvider函数
   * - 功能: 创建PromptConfigProvider实例
   * - 实现: Spring Boot依赖注入
   */
  @Bean
  public PromptConfigProvider promptConfigProvider(PromptHubRateLimitConfig rateLimitConfig, TenantSettingInfoCache tenantSettingInfoCache) {
    return new PromptConfigProvider(rateLimitConfig, tenantSettingInfoCache);
  }
}
