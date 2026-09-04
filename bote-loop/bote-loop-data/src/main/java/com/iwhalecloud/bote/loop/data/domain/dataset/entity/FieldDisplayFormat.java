package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import lombok.Getter;

/**
 * 字段显示格式枚举
 * 迁移对应关系: Go语言entity.FieldDisplayFormat
 * - 功能: 字段显示格式标识
 * - 字段定义: 各种显示格式常量
 * <p>
 * Java实现说明:
 * - 对应Go的FieldDisplayFormat类型别名
 * - 使用Java枚举定义各种显示格式
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
@Getter
public enum FieldDisplayFormat {
  UNKNOWN(""),
  PLAIN_TEXT("PlainText"),
  MARKDOWN("Markdown"),
  JSON("JSON"),
  YAML("YAML"),
  CODE("Code");

  private final String value;

  FieldDisplayFormat(String value) {
    this.value = value;
  }

  public static FieldDisplayFormat fromValue(String value) {
    for (FieldDisplayFormat format : values()) {
      if (format.value.equals(value)) {
        return format;
      }
    }
    return UNKNOWN;
  }
}
