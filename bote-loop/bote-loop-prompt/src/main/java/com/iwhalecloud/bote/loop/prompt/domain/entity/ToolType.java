package com.iwhalecloud.bote.loop.prompt.domain.entity;

/**
 * 工具类型枚举
 * 迁移对应关系: Go语言entity.ToolType
 * - 功能: 定义工具的类型
 * - 常量:
 * * FUNCTION - 函数类型
 * <p>
 * Java实现说明:
 * - 对应Go的ToolType类型别名
 * - 使用Java枚举定义
 * - 提供字符串值映射
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举常量
 */
public enum ToolType {
  /**
   * 函数类型
   * 迁移对应关系: Go语言ToolTypeFunction
   * - 功能: 函数工具类型
   * - 值: "function"
   */
  FUNCTION("function");

  private final String value;

  ToolType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static ToolType fromValue(String value) {
    for (ToolType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ToolType: " + value);
  }
}
