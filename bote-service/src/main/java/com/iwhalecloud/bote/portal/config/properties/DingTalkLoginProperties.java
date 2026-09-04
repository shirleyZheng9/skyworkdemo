package com.iwhalecloud.bote.portal.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 钉钉认证配置文件
 *
 * @author Aiqing
 * @since 2025/06/04
 */
@ConfigurationProperties(prefix = "bote.dingtalk")
@Data
@Validated
public class DingTalkLoginProperties {

  /**
   * 是否启用钉钉登录
   */
  private Boolean enabled = false;

  /**
   * 企业ID
   */
  private String corpId;

  /**
   * clientId
   */
  private String clientId;
  /**
   * clientSecret
   */
  private String clientSecret;
  /**
   * 是否是开发模式
   */
  private Boolean devMode;

  /**
   * 钉钉开放平台地址
   */
  private String serverUrl = "https://oapi.dingtalk.com";

  /**
   * 登录后跳转的业务系统地址
   */
  private String redirectUrl;
}
