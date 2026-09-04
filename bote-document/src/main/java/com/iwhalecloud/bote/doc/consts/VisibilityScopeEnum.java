package com.iwhalecloud.bote.doc.consts;

import lombok.Getter;

/**
 * 文档库可见范围枚举
 */
@Getter
public enum VisibilityScopeEnum {

  /**
   * 组织内所有用户可见
   */
  PUBLIC("PUBLIC", "全员可见"),

  /**
   * 仅授权成员可见
   */
  MEMBERS("MEMBERS", "成员可见"),

  /**
   * 私有文档库，仅所有者可见
   */
  PRIVATE("PRIVATE", "私有");

  private final String code;
  private final String description;

  VisibilityScopeEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

}
