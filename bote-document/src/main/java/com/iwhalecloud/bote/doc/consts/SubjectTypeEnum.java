package com.iwhalecloud.bote.doc.consts;

import lombok.Getter;

/**
 * 权限主体类型枚举
 */
@Getter
public enum SubjectTypeEnum {

  /**
   * 单个用户
   */
  USER("USER", "用户"),

  /**
   * 组织部门
   */
  ORG("ORG", "部门"),

  /**
   * 用户角色
   */
  ROLE("ROLE", "角色"),

  /**
   * 自定义分组
   */
  GROUP("GROUP", "分组");

  private final String code;
  private final String description;

  SubjectTypeEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

}
