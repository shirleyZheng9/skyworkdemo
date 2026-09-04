package com.iwhalecloud.bote.loop.data.domain.tag.entity;

/**
 * 标签内容类型枚举
 * 迁移对应关系: Go语言entity.TagContentType
 * - 功能: 标签内容类型标识
 * - 字段定义: 各种标签内容类型常量
 * <p>
 * Java实现说明:
 * - 对应Go的TagContentType类型别名
 * - 使用Java枚举定义各种内容类型
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum TagContentType {
  UNDEFINED(""),
  CATEGORICAL("categorical"),
  BOOLEAN("boolean"),
  CONTINUOUS_NUMBER("continuous_number"),
  FREE_TEXT("free_text");

  private final String value;

  TagContentType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static TagContentType fromValue(String value) {
    for (TagContentType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return UNDEFINED;
  }
}
