package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应组织详情请求参数
 *
 * @author bianjp
 * @since 2025-09-25
 */
@Getter
@Setter
@ToString
public class BeyondOrgDetailRequest {
  /** 组织ID */
  private Long orgId;
}
