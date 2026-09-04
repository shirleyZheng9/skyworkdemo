package com.iwhalecloud.bote.loop.prompt.domain.entity;

/**
 * 工具选择类型枚举
 * 迁移对应关系: Go语言entity.ToolChoiceType
 * - 功能: 定义工具选择的策略
 * - 常量:
 * * NONE - 不选择工具
 * * AUTO - 自动选择工具
 * <p>
 * Java实现说明:
 * - 对应Go的ToolChoiceType类型别名
 * - 使用Java枚举定义
 * - 提供字符串值映射
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举常量
 */
public enum ToolChoiceType {
  /**
   * 不选择工具
   * 迁移对应关系: Go语言ToolChoiceTypeNone
   * - 功能: 不进行工具选择
   * - 值: "none"
   */
  NONE("none"),

  /**
   * 自动选择工具
   * 迁移对应关系: Go语言ToolChoiceTypeAuto
   * - 功能: 自动选择工具
   * - 值: "auto"
   */
  AUTO("auto");

  private final String value;

  ToolChoiceType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static ToolChoiceType fromValue(String value) {
    for (ToolChoiceType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ToolChoiceType: " + value);
  }
}
