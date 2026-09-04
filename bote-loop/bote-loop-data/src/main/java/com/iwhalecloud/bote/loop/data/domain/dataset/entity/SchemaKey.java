package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

/**
 * Schema键枚举
 * 迁移对应关系: Go语言entity.SchemaKey
 * - 功能: Schema键标识
 * - 字段定义: 各种Schema键常量
 * <p>
 * Java实现说明:
 * - 对应Go的SchemaKey类型别名
 * - 使用Java枚举定义各种Schema键
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum SchemaKey {
  UNKNOWN(""),
  STRING("string"),
  INTEGER("integer"),
  FLOAT("float"),
  BOOL("bool"),
  MESSAGE("message");

  private final String value;

  SchemaKey(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static SchemaKey fromValue(String value) {
    for (SchemaKey key : values()) {
      if (key.value.equals(value)) {
        return key;
      }
    }
    return UNKNOWN;
  }
}
