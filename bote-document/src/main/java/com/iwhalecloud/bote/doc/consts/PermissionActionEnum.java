package com.iwhalecloud.bote.doc.consts;

import lombok.Getter;

/**
 * 权限操作类型枚举
 */
@Getter
public enum PermissionActionEnum {

  /**
   * 为文档库添加新成员
   */
  ADD_MEMBER("ADD_MEMBER", "添加成员"),

  /**
   * 修改现有成员的权限级别
   */
  UPDATE_MEMBER("UPDATE_MEMBER", "更新成员权限"),

  /**
   * 从文档库中移除成员
   */
  REMOVE_MEMBER("REMOVE_MEMBER", "移除成员"),

  /**
   * 批量添加多个成员
   */
  BATCH_ADD("BATCH_ADD", "批量添加"),

  /**
   * 批量更新多个成员权限
   */
  BATCH_UPDATE("BATCH_UPDATE", "批量更新"),

  /**
   * 批量移除多个成员
   */
  BATCH_REMOVE("BATCH_REMOVE", "批量移除");

  private final String code;
  private final String description;

  PermissionActionEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

}
