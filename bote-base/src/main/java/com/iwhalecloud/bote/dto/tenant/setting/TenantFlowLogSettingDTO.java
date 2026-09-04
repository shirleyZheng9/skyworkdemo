package com.iwhalecloud.bote.dto.tenant.setting;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户的流程日志设置
 *
 * @author bianjp
 * @since 2025-03-05
 */
@Getter
@Setter
@ToString
public class TenantFlowLogSettingDTO {
  /** 是否开启 */
  private Boolean enabled;
}
