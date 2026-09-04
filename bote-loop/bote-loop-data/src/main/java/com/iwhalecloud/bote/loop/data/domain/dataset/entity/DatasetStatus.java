package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

/**
 * 数据集状态枚举
 * 迁移对应关系: Go语言entity.DatasetStatus
 * - 功能: 数据集状态标识
 * - 字段定义: 各种数据集状态常量
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetStatus类型别名
 * - 使用Java枚举定义各种状态
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum DatasetStatus {
  UNKNOWN(""),
  AVAILABLE("available"),
  DELETED("deleted"),
  EXPIRED("expired"),
  IMPORTING("importing"),
  EXPORTING("exporting"),
  INDEXING("indexing");

  private final String value;

  DatasetStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static DatasetStatus fromValue(String value) {
    for (DatasetStatus status : values()) {
      if (status.value.equals(value)) {
        return status;
      }
    }
    return UNKNOWN;
  }
}
