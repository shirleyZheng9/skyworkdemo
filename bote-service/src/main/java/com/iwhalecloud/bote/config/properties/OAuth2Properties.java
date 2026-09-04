package com.iwhalecloud.bote.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * OAuth2配置属性
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@ConfigurationProperties(prefix = "bote.oauth2")
@Getter
@Setter
@ToString
public class OAuth2Properties {

  /**
   * 是否启用OAuth2
   */
  private boolean enabled;

  /**
   * JWT密钥
   */
  private String jwtSecret;

  /**
   * 访问令牌过期时间(秒)
   */
  private int accessTokenExpireSeconds;

  /**
   * 刷新令牌过期时间(秒)
   */
  private int refreshTokenExpireSeconds;

  /**
   * 授权码过期时间(秒)
   */
  private int authCodeExpireSeconds;
}
