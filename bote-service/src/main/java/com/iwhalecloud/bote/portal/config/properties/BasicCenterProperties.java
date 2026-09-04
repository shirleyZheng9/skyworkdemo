package com.iwhalecloud.bote.portal.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * basiccenter 配置项
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@ConfigurationProperties("bote.basiccenter")
@Validated
@Getter
@Setter
@ToString(callSuper = true)
public class BasicCenterProperties extends AbstractPortalProperties {

  @Override
  public void afterPropertiesSet() {
    // 无需处理
  }
}
