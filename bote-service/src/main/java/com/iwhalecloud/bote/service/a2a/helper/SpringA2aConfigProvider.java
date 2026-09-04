package com.iwhalecloud.bote.service.a2a.helper;

import io.a2a.server.config.A2AConfigProvider;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 使用 Spring 配置机制的 A2A 配置实现
 *
 * @author bianjp
 * @since 2025-11-28
 */
@Component
@RequiredArgsConstructor
public class SpringA2aConfigProvider implements A2AConfigProvider {
  private final Environment environment;

  @Override
  public String getValue(String name) {
    return environment.getRequiredProperty(name);
  }

  @Override
  public Optional<String> getOptionalValue(String name) {
    return Optional.ofNullable(environment.getProperty(name));
  }
}
