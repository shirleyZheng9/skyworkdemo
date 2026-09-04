package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应组织管理员项
 *
 * @author lizuyin
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString
public class BeyondOrgAdminItem {
  /** 组织ID */
  private Long orgId;
  /** 用户ID */
  private Long userId;
  /** 用户名称 */
  private String userName;
  /** 用户编码 */
  private String userCode;
}
