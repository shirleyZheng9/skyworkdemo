package com.iwhalecloud.bote.loop.prompt.domain.entity;

/**
 * 模板类型枚举
 * 迁移对应关系: Go语言entity.TemplateType
 * - 功能: 定义Prompt模板的类型
 * - 常量:
 * * NORMAL - 普通模板类型
 * <p>
 * Java实现说明:
 * - 对应Go的TemplateType类型别名
 * - 使用Java枚举定义
 * - 提供字符串值映射
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举常量
 */
public enum TemplateType {
  /**
   * 普通模板类型
   * 迁移对应关系: Go语言TemplateTypeNormal
   * - 功能: 使用{{}}语法的普通模板
   * - 值: "normal"
   */
  NORMAL("normal");

  private final String value;

  TemplateType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static TemplateType fromValue(String value) {
    for (TemplateType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown TemplateType: " + value);
  }
}
