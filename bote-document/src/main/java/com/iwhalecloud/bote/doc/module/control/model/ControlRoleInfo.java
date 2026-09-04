package com.iwhalecloud.bote.doc.module.control.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * control role info.
 */
@Getter
@Setter
@ToString
public class ControlRoleInfo {

  /**
   * 角色的控制对象ID： 节点ID
   */
  private String controlId;

  /**
   * 主体ID： 用户ID或者组织ID
   */
  private Long subjectId;

  /**
   * 主体类型： 用户或者组织
   */
  private String subjectType;

  /**
   * 角色
   */
  private String role;
}
