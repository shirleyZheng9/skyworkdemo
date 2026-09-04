package com.iwhalecloud.bote.service.publish.platform.feishu.dto;

import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书配置类
 * 替代Map<String, Object>，提供类型安全
 *
 * @author system
 * @since 2025-01-09
 */
@Getter
@Setter
@ToString
public class FeishuConfig {
  /** 应用ID */
  private String appId;
  /** 应用密钥 */
  private String appSecret;
  /** 验证令牌 */
  private String verificationToken;
  /** 加密密钥 */
  private String encryptKey;

  /**
   * 从Map创建配置
   */
  public static FeishuConfig fromMap(PublishChannelDTO config) {
    FeishuConfig feishuConfig = new FeishuConfig();
    feishuConfig.setAppId(config.getAppId());
    feishuConfig.setAppSecret(config.getSecret());
    feishuConfig.setVerificationToken(config.getVerificationToken());
    feishuConfig.setEncryptKey(config.getEncryptKey());
    return feishuConfig;
  }

  /**
   * 验证配置是否完整
   */
  public boolean isValid() {
    return appId != null && !appId.isEmpty() &&
      appSecret != null && !appSecret.isEmpty();
  }
}
