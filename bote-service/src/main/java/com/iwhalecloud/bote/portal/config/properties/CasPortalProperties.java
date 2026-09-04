package com.iwhalecloud.bote.portal.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * CAS Portal 配置属性
 *
 * @author system
 * @since 2025-01-22
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CasPortalProperties extends AbstractPortalProperties {
  /** CAS 登录地址 */
  private String serviceUrl;

  @Override
  public void afterPropertiesSet() {
    // 无需处理
  }
}
