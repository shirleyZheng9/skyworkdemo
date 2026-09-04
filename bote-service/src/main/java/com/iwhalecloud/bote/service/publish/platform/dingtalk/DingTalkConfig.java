package com.iwhalecloud.bote.service.publish.platform.dingtalk;

import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 钉钉配置类
 * 替代Map<String, Object>，提供类型安全
 *
 * @author system
 * @since 2025-01-09
 */
@Getter
@Setter
@ToString
public class DingTalkConfig {
  /** 应用Key */
  private String appKey;
  /** 应用密钥 */
  private String appSecret;
  /** 机器人Code */
  private String robotCode;

  /**
   * 从Map创建DingTalkConfig
   */
  public static DingTalkConfig fromMap(PublishChannelDTO configMap) {
    DingTalkConfig config = new DingTalkConfig();
    if (configMap != null) {
      config.setAppKey(configMap.getAppId());
      config.setAppSecret(configMap.getSecret());
      config.setRobotCode(configMap.getRobotCode());
    }
    return config;
  }
  /**
   * 验证配置完整性
   */
  public boolean isValid() {
    return appKey != null && !appKey.isEmpty() &&
      appSecret != null && !appSecret.isEmpty() &&
      robotCode != null && !robotCode.isEmpty();
  }
}
