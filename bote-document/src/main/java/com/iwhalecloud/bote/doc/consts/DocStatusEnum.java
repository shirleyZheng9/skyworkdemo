package com.iwhalecloud.bote.doc.consts;

/**
 * 知识库构建状态枚举
 */
public enum DocStatusEnum {

  /**
   * 文档刚添加，尚未开始处理
   */
  UNPROCESSED("10A", "未处理"),

  /**
   * 正在解析文档内容和结构
   */
  PARSING("10B", "解析中"),

  /**
   * 正在进行关键信息抽取和分块
   */
  EXTRACTING("10C", "要素抽取中"),

  /**
   * 文档处理完成，可用于检索
   */
  COMPLETED("10D", "构建完成"),

  /**
   * 构建过程中出现错误
   */
  FAILED("10E", "构建失败");

  private final String code;
  private final String description;

  DocStatusEnum(String code, String description) {
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
