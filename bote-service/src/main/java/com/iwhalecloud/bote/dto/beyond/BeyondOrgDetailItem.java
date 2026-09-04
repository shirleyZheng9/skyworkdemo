package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应组织详情项
 *
 * @author bianjp
 * @since 2025-09-25
 */
@Getter
@Setter
@ToString
public class BeyondOrgDetailItem {
  /** 组织ID */
  private Long orgId;
  /** 组织名称 */
  private String orgName;
  /** 组织编码 */
  private String orgCode;
  /** 组织类型 */
  private String orgType;
  /** 上级组织ID */
  private Long parentOrgId;
  /** 组织层级 */
  private Integer orgLevel;
  /** 排序 */
  private Integer orgIndex;
  /** 组织路径 */
  private String pathName;
  /** 组织路径编码 */
  private String pathCode;
  /** 创建时间 */
  private String createDate;
}
