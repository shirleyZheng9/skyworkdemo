package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 项错误类型枚举
 * 迁移对应关系: Go语言ItemErrorType
 * - 功能: 项错误类型枚举
 * - 常量: MismatchSchema, EmptyData, ExceedMaxItemSize, ExceedDatasetCapacity, MalformedFile, IllegalContent, MissingRequiredField, ExceedMaxNestedDepth, TransformItemFailed, InternalError
 * <p>
 * Java实现说明:
 * - 对应Go的ItemErrorType枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * - 实现toString方法
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go int64 -> Java int
 * - Go String()方法 -> Java toString()方法
 */
public enum ItemErrorType {
  @JsonProperty("1")
  MISMATCH_SCHEMA(1, "MismatchSchema"),
  @JsonProperty("2")
  EMPTY_DATA(2, "EmptyData"),
  @JsonProperty("3")
  EXCEED_MAX_ITEM_SIZE(3, "ExceedMaxItemSize"),
  @JsonProperty("4")
  EXCEED_DATASET_CAPACITY(4, "ExceedDatasetCapacity"),
  @JsonProperty("5")
  MALFORMED_FILE(5, "MalformedFile"),
  @JsonProperty("6")
  ILLEGAL_CONTENT(6, "IllegalContent"),
  @JsonProperty("7")
  MISSING_REQUIRED_FIELD(7, "MissingRequiredField"),
  @JsonProperty("8")
  EXCEED_MAX_NESTED_DEPTH(8, "ExceedMaxNestedDepth"),
  @JsonProperty("9")
  TRANSFORM_ITEM_FAILED(9, "TransformItemFailed"),
  @JsonProperty("100")
  INTERNAL_ERROR(100, "InternalError");

  private final int value;
  private final String name;

  ItemErrorType(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  public static ItemErrorType fromValue(int value) {
    for (ItemErrorType type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ItemErrorType value: " + value);
  }
}
