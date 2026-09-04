package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 字段状态DTO枚举
 * 对应Go: dataset.FieldStatus (int64类型)
 */
@Getter
public enum FieldStatusDTO {

  /**
   * 可用状态
   * 对应Go: FieldStatus_Available = 1
   */
  @JsonProperty("1")
  AVAILABLE(1, "Available"),

  /**
   * 已删除状态
   * 对应Go: FieldStatus_Deleted = 2
   */
  @JsonProperty("2")
  DELETED(2, "Deleted");

  private final int value;
  private final String description;

  FieldStatusDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public static FieldStatusDTO fromValue(int value) {
    for (FieldStatusDTO status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid FieldStatus value: " + value);
  }

  public static FieldStatusDTO fromString(String str) {
    for (FieldStatusDTO status : values()) {
      if (status.description.equals(str)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid FieldStatus string: " + str);
  }
}
