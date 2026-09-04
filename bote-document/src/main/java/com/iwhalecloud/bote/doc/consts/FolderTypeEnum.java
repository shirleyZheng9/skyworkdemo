package com.iwhalecloud.bote.doc.consts;

import lombok.Getter;

/**
 * 文件夹类型枚举
 */
@Getter
public enum FolderTypeEnum {

  /**
   * 通过AI对话功能上传的文档
   */
  DIALOG("DIALOG", "对话上传"),

  /**
   * 通过应用功能上传的文档
   */
  APP("APP", "应用生成"),

  /**
   * 用户主动创建的文件夹和文档
   */
  USER("USER", "用户创建");

  private final String code;
  private final String description;

  FolderTypeEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

}
