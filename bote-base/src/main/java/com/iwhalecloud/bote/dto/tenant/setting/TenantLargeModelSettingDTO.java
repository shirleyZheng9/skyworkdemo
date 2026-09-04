package com.iwhalecloud.bote.dto.tenant.setting;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户的默认大模型设置
 *
 * @author bianjp
 * @since 2025-01-16
 */
@Getter
@Setter
@ToString
public class TenantLargeModelSettingDTO {
  /** 模型 ID */
  private Long largeModelId;
}
