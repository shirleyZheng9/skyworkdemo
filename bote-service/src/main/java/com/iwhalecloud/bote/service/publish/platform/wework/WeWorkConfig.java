package com.iwhalecloud.bote.service.publish.platform.wework;

import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 企业微信配置类
 * 替代Map<String, Object>，提供类型安全
 *
 * @author system
 * @since 2025-01-09
 */
@Getter
@Setter
@ToString
public class WeWorkConfig {
  /** 应用密钥 */
  private String secret;
  /** 应用ID */
  private String appId;
  /** 回调Token */
  private String token;
  /** 回调AES密钥 */
  private String aesKey;

  /**
   * 从Map创建WeWorkConfig
   */
  public static WeWorkConfig fromMap(PublishChannelDTO configMap) {
    WeWorkConfig config = new WeWorkConfig();
    if (configMap != null) {
      config.setSecret(configMap.getSecret());
      config.setAppId(configMap.getAppId());
      config.setToken(configMap.getToken());
      config.setAesKey(configMap.getAesKey());
    }
    return config;
  }

  /**
   * 验证配置完整性
   */
  public boolean isValid() {
    return secret != null && !secret.isEmpty() &&
      appId != null && !appId.isEmpty() &&
      token != null && !token.isEmpty() &&
      aesKey != null && !aesKey.isEmpty();
  }
}
