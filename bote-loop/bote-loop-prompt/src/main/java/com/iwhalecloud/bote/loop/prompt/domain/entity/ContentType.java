package com.iwhalecloud.bote.loop.prompt.domain.entity;

/**
 * 内容类型枚举
 * 迁移对应关系: Go语言entity.ContentType
 * - 功能: 定义内容部分的类型
 * - 常量:
 * * TEXT - 文本类型
 * * IMAGE_URL - 图片URL类型
 * <p>
 * Java实现说明:
 * - 对应Go的ContentType类型别名
 * - 使用Java枚举定义
 * - 提供字符串值映射
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举常量
 */
public enum ContentType {
  /**
   * 文本类型
   * 迁移对应关系: Go语言ContentTypeText
   * - 功能: 文本内容类型
   * - 值: "text"
   */
  TEXT("text"),

  /**
   * 图片URL类型
   * 迁移对应关系: Go语言ContentTypeImageURL
   * - 功能: 图片URL内容类型
   * - 值: "image_url"
   */
  IMAGE_URL("image_url");

  private final String value;

  ContentType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static ContentType fromValue(String value) {
    for (ContentType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ContentType: " + value);
  }
}
