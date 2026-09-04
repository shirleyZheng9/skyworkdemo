package com.iwhalecloud.bote.dto.tenant.setting;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户的联想术语设置
 *
 * @author auto
 * @since 2025-01-17
 */
@Getter
@Setter
@ToString
public class TenantSuggestionSettingDTO {
  /** 匹配阈值 */
  private Double score;
  /** 匹配条数 */
  private Integer limit;
} 