package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import lombok.Getter;

/**
 * 字段状态实体枚举
 * 对应Go: entity.FieldStatus (string类型)
 */
@Getter
public enum FieldStatus {

  /**
   * 可用状态
   * 对应Go: FieldStatus_Available = 1
   */
  AVAILABLE(1, "Available"),

  /**
   * 已删除状态
   * 对应Go: FieldStatus_Deleted = 2
   */
  DELETED(2, "Deleted"),
  UNKNOWN(0, "");

  private final int value;
  private final String description;

  FieldStatus(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public static FieldStatus fromValue(int value) {
    for (FieldStatus status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid FieldStatus value: " + value);
  }

  public static FieldStatus fromString(String str) {
    for (FieldStatus status : values()) {
      if (status.description.equals(str)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid FieldStatus string: " + str);
  }
}

