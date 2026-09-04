package com.iwhalecloud.bote.doc.consts;

import lombok.Getter;

/**
 * 内置类型枚举
 */
@Getter
public enum BuiltinTypeEnum {

  /**
   * 系统预设的对话上传存储文件夹
   */
  DIALOG_FOLDER("DIALOG_FOLDER", "对话上传文件夹"),

  /**
   * 系统预设的应用上传存储文件夹
   */
  APP_FOLDER("APP_FOLDER", "应用上传文件夹"),

  /**
   * 用户在我的文档库中创建的文件夹
   */
  USER_FOLDER("USER_FOLDER", "用户文件夹"),

  /**
   * 普通文档或非系统预设的文件夹
   */
  NULL("NULL", "非内置");

  private final String code;
  private final String description;

  BuiltinTypeEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

}
