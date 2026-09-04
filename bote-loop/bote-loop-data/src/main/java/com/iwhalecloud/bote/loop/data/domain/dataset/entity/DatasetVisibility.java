package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

/**
 * 数据集可见性枚举
 * 迁移对应关系: Go语言entity.DatasetVisibility
 * - 功能: 数据集可见性标识
 * - 字段定义: 各种可见性常量
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetVisibility类型别名
 * - 使用Java枚举定义各种可见性
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum DatasetVisibility {
  UNKNOWN(""),
  PUBLIC("public"),
  SPACE("space"),
  SYSTEM("system");

  private final String value;

  DatasetVisibility(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static DatasetVisibility fromValue(String value) {
    for (DatasetVisibility visibility : values()) {
      if (visibility.value.equals(value)) {
        return visibility;
      }
    }
    return UNKNOWN;
  }
}
