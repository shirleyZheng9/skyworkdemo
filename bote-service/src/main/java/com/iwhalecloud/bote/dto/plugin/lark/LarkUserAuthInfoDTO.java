package com.iwhalecloud.bote.dto.plugin.lark;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书用户授权信息
 *
 * @author qian.sisheng
 * @since 2025-08-21
 */
@Getter
@Setter
@ToString
public class LarkUserAuthInfoDTO {
  /** 用户名 */
  private String name;
  /** 用户ID */
  private String userId;
  /** 插件名称 */
  private String pluginName;
  /** 是否授权成功 */
  private boolean isAuthSuccess;
}
