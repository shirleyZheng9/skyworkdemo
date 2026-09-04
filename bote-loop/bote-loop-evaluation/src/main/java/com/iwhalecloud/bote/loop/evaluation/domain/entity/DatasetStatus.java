package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 数据集状态枚举
 * 迁移对应关系: Go语言DatasetStatus
 * - 功能: 数据集状态枚举
 * - 常量: Available, Deleted, Expired, Importing, Exporting, Indexing
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetStatus枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * - 实现toString方法
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go int64 -> Java int
 * - Go String()方法 -> Java toString()方法
 */
public enum DatasetStatus {
  @JsonProperty("1")
  AVAILABLE(1, "Available"),
  @JsonProperty("2")
  DELETED(2, "Deleted"),
  @JsonProperty("3")
  EXPIRED(3, "Expired"),
  @JsonProperty("4")
  IMPORTING(4, "Importing"),
  @JsonProperty("5")
  EXPORTING(5, "Exporting"),
  @JsonProperty("6")
  INDEXING(6, "Indexing");

  private final int value;
  private final String name;

  DatasetStatus(int value, String name) {
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

  public static DatasetStatus fromValue(int value) {
    for (DatasetStatus status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid DatasetStatus value: " + value);
  }

  public static DatasetStatus fromString(String str) {
    if (str == null) {
      return null;
    }
    for (DatasetStatus status : values()) {
      if (status.name.equals(str)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid DatasetStatus string: " + str);
  }

}
