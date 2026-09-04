package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

/**
 * 安全等级枚举
 * 迁移对应关系: Go语言entity.SecurityLevel
 * - 功能: 数据集安全等级标识
 * - 字段定义: 各种安全等级常量
 * <p>
 * Java实现说明:
 * - 对应Go的SecurityLevel类型别名
 * - 使用Java枚举定义各种安全等级
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum SecurityLevel {
  UNKNOWN(""),
  L1("l1"),
  L2("l2"),
  L3("l3"),
  L4("l4");

  private final String value;

  SecurityLevel(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static SecurityLevel fromValue(String value) {
    for (SecurityLevel level : values()) {
      if (level.value.equals(value)) {
        return level;
      }
    }
    return UNKNOWN;
  }
}
