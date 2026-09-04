package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应组织项
 *
 * @author lizuyin
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString
public class BeyondOrgItem {
  /** 组织ID */
  private Long orgId;
  /** 组织名称 */
  private String orgName;
  /** 父组织ID，-1表示顶层组织 */
  private Long parentOrgId;
  /** 路径编码，格式为父组织ID.当前组织ID */
  private String pathCode;
  /** 子级数量 */
  private Integer total;
}
