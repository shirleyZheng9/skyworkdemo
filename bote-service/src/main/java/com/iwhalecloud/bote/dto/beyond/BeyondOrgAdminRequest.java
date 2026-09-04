package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应组织管理员检索请求对象
 *
 * @author lizuyin
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString
public class BeyondOrgAdminRequest {
  /** 组织标识 */
  private Long orgId;
}
