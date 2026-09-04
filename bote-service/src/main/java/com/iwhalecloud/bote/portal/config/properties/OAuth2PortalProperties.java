package com.iwhalecloud.bote.portal.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * oauth2 门户配置
 *
 * @author chen.linfa
 * @since 2026-02-05
 */
@ConfigurationProperties("bote.oauth")
@Validated
@Getter
@Setter
@ToString(callSuper = true)
public class OAuth2PortalProperties extends AbstractPortalProperties {

  /** 是否自动新增租户 */
  private Boolean autoCreateTenant;
  /** 指定登录鉴权通过后，需要重定向的博特访问地址 */
  private String redirectUrl;
  /** 自定义 groovy 脚本 */
  private String script;


  @Override
  public void afterPropertiesSet() {
    // 无需处理
  }
}
