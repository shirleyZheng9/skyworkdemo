package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

/**
 * 项目错误类型枚举
 * 迁移对应关系: Go语言entity.ItemErrorType
 * - 功能: 项目错误类型标识
 * - 字段定义: 各种错误类型常量
 * <p>
 * Java实现说明:
 * - 对应Go的ItemErrorType类型别名
 * - 使用Java枚举定义各种错误类型
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum ItemErrorType {
  MISMATCH_SCHEMA(1),
  EMPTY_DATA(2),
  EXCEED_MAX_ITEM_SIZE(3),
  EXCEED_DATASET_CAPACITY(4),
  MALFORMED_FILE(5),
  ILLEGAL_CONTENT(6),
  INTERNAL_ERROR(100);

  private final int value;

  ItemErrorType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static ItemErrorType fromValue(int value) {
    for (ItemErrorType type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown item error type: " + value);
  }
}
