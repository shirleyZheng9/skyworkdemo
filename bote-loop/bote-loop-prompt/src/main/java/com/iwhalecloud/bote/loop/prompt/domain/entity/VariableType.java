package com.iwhalecloud.bote.loop.prompt.domain.entity;

import lombok.Getter;

/**
 * 变量类型枚举
 * 迁移对应关系: Go语言entity.VariableType
 * - 功能: 定义变量的类型
 * - 常量:
 * * STRING - 字符串类型
 * * PLACEHOLDER - 占位符类型
 * <p>
 * Java实现说明:
 * - 对应Go的VariableType类型别名
 * - 使用Java枚举定义
 * - 提供字符串值映射
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举常量
 */
@Getter
public enum VariableType {
  /**
   * 字符串类型
   * 迁移对应关系: Go语言VariableTypeString
   * - 功能: 字符串变量类型
   * - 值: "string"
   */
  STRING("string"),

  /**
   * 占位符类型
   * 迁移对应关系: Go语言VariableTypePlaceholder
   * - 功能: 占位符变量类型
   * - 值: "placeholder"
   */
  PLACEHOLDER("placeholder");

  private final String value;

  VariableType(String value) {
    this.value = value;
  }

  public static VariableType fromValue(String value) {
    for (VariableType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown VariableType: " + value);
  }
}
