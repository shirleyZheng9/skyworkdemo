package com.iwhalecloud.bote.portal.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * ngportal 配置项
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@ConfigurationProperties("bote.ngportal")
@Validated
@Getter
@Setter
@ToString(callSuper = true)
public class NgportalProperties extends AbstractPortalProperties {

  @Override
  public void afterPropertiesSet() {
    // 无需处理
  }
}
