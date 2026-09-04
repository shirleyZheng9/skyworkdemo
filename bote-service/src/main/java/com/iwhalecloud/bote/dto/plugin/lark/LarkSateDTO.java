package com.iwhalecloud.bote.dto.plugin.lark;

import lombok.Getter;
import lombok.Setter;

/**
 * 飞书回调参数
 * <p>在授权完成回调时会原样回传此参数。应用可以根据此字符串来判断上下文关系，同时该参数也可以用以防止 CSRF 攻击</p>
 *
 * @author qian.sisheng
 * @since 2025-08-20
 */
@Setter
@Getter
public class LarkSateDTO {
  /** 用户ID */
  private Long userId;
  /** 飞书应用ID */
  private String appId;
  /** 飞书应用密钥 */
  private String appSecret;
  /** 回调地址 */
  private String redirectUrl;
  /** 插件名称 */
  private String pluginName;
}
