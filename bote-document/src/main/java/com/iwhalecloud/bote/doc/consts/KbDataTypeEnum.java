package com.iwhalecloud.bote.doc.consts;

/**
 * 知识库数据类型枚举
 */
public enum KbDataTypeEnum {

  /**
   * Excel、CSV等有明确行列结构的数据文件
   */
  STRUCTURED("STRUCTURED", "结构化数据"),

  /**
   * Word、PDF、Markdown等文本文档
   */
  UNSTRUCTURED("UNSTRUCTURED", "非结构化数据");

  private final String code;
  private final String description;

  KbDataTypeEnum(String code, String description) {
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
