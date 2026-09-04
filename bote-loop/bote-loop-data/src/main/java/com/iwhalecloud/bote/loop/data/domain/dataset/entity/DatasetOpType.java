package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

/**
 * 数据集操作类型枚举
 * 迁移对应关系: Go语言entity.DatasetOpType
 * - 功能: 数据集操作类型标识
 * - 字段定义: 各种操作类型常量
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetOpType类型别名
 * - 使用Java枚举定义各种操作类型
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum DatasetOpType {
  CREATE_DATASET("create_dataset"),
  IMPORT("import"),
  CREATE_VERSION("create_version"),
  UPDATE_SCHEMA("update_schema"),
  WRITE_ITEM("write_item"),
  CLEAR_DATASET("clear_dataset");

  private final String value;

  DatasetOpType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static DatasetOpType fromValue(String value) {
    for (DatasetOpType opType : values()) {
      if (opType.value.equals(value)) {
        return opType;
      }
    }
    throw new IllegalArgumentException("Unknown dataset operation type: " + value);
  }
}
