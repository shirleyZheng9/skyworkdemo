package com.iwhalecloud.bote.doc.consts;


import lombok.Getter;

/**
 * 内容来源枚举
 */
@Getter
public enum ContentSourceEnum {

  /**
   * 通过在线编辑器创建的文档
   */
  ONLINE("ONLINE", "在线文档"),

  /**
   * 用户上传的原始文件（可转换为在线文档）
   */
  UPLOAD("UPLOAD", "上传文件");

  private final String code;
  private final String description;

  ContentSourceEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }
}
