package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

/**
 * 数据集分类枚举
 * 迁移对应关系: Go语言entity.DatasetCategory
 * - 功能: 业务场景分类标识
 * - 字段定义: 各种数据集分类常量
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetCategory类型别名
 * - 使用Java枚举定义各种分类
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum DatasetCategory {
  UNKNOWN(""),
  GENERAL("general"),
  TRAINING("training"),
  VALIDATION("validation"),
  EVALUATION("evaluation");

  private final String value;

  DatasetCategory(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static DatasetCategory fromValue(String value) {
    for (DatasetCategory category : values()) {
      if (category.value.equals(value)) {
        return category;
      }
    }
    return UNKNOWN;
  }
}
