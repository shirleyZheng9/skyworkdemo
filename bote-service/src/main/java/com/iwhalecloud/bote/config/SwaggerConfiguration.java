package com.iwhalecloud.bote.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.utils.Constants;
import org.springdoc.webmvc.ui.SwaggerWelcomeWebMvc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 文档配置
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SwaggerWelcomeWebMvc.class)
@ConditionalOnBooleanProperty(name = Constants.SPRINGDOC_ENABLED, matchIfMissing = true)
@OpenAPIDefinition(
  // @formatter:off
  info = @Info(
    title = "博特",
    description = "博特后端 API 文档",
    version = "1.0.0"
  ),
  servers = @Server(
    // 后端地址使用相对路径以兼容通过网关访问且网关中配置了上下文的场景
    url = "./",
    description = "Default"
  )
  // @formatter:on
)
public class SwaggerConfiguration {
}
