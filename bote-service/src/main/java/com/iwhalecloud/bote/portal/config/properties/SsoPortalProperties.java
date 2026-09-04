package com.iwhalecloud.bote.portal.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * sso 门户配置
 *
 * @author chen.linfa
 * @since 2025-02-26
 */
@ConfigurationProperties("bote.sso")
@Validated
@Getter
@Setter
@ToString(callSuper = true)
public class SsoPortalProperties extends AbstractPortalProperties {

  /** 是否自动新增租户 */
  private Boolean autoCreateTenant;
  /** 签名密钥 */
  private String secretKey;
  /** 自定义 groovy 脚本 */
  private String script;

  @Override
  public void afterPropertiesSet() {
    // 无需处理
  }
}
