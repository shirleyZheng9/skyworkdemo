package com.iwhalecloud.bote.doc.consts;

/**
 * 目标类型枚举
 */
public enum TargetTypeEnum {

  /**
   * 收藏表、访问记录表、用户置顶表
   */
  DOCUMENT("DOCUMENT", "文档"),

  /**
   * 收藏表
   */
  FOLDER("FOLDER", "文件夹"),

  /**
   * 收藏表、访问记录表、用户置顶表
   */
  LIBRARY("LIBRARY", "文档库"),

  /**
   * 收藏表、用户置顶表
   */
  KNOWLEDGE("KNOWLEDGE", "知识库");

  private final String code;
  private final String description;

  TargetTypeEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }
}
