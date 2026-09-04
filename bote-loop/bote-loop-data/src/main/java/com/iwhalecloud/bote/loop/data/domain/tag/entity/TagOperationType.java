package com.iwhalecloud.bote.loop.data.domain.tag.entity;

/**
 * 标签操作类型枚举
 * 迁移对应关系: Go语言entity.TagOperationType
 * - 功能: 标签操作类型标识
 * - 字段定义: 各种标签操作类型常量
 * <p>
 * Java实现说明:
 * - 对应Go的TagOperationType类型别名
 * - 使用Java枚举定义各种操作类型
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum TagOperationType {
  UNDEFINED(""),
  CREATE("create"),
  UPDATE("update"),
  DELETE("delete");

  private final String value;

  TagOperationType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static TagOperationType fromValue(String value) {
    for (TagOperationType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return UNDEFINED;
  }
}
