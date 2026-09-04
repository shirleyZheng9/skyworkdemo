package com.iwhalecloud.bote.dto.tenant.setting;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户的插件市场设置
 *
 * @author chen.linfa
 * @since 2025-12-11
 */
@Getter
@Setter
@ToString
public class TenantPluginHubSettingDTO {
  /** 插件市场对应门户用户的 API KEY */
  private String portalUserApiKey;
}
