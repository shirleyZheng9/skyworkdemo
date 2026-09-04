package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 数据集状态DTO枚举
 * 对应Go: dataset.DatasetStatus
 */
public enum DatasetStatusDTO {

  /**
   * 可用状态
   * 对应Go: DatasetStatus_Available = 1
   */
  @JsonProperty("1")
  AVAILABLE(1, "Available"),

  /**
   * 已删除状态
   * 对应Go: DatasetStatus_Deleted = 2
   */
  @JsonProperty("2")
  DELETED(2, "Deleted"),

  /**
   * 已过期状态
   * 对应Go: DatasetStatus_Expired = 3
   */
  @JsonProperty("3")
  EXPIRED(3, "Expired"),

  /**
   * 导入中状态
   * 对应Go: DatasetStatus_Importing = 4
   */
  @JsonProperty("4")
  IMPORTING(4, "Importing"),

  /**
   * 导出中状态
   * 对应Go: DatasetStatus_Exporting = 5
   */
  @JsonProperty("5")
  EXPORTING(5, "Exporting"),

  /**
   * 索引中状态
   * 对应Go: DatasetStatus_Indexing = 6
   */
  @JsonProperty("6")
  INDEXING(6, "Indexing");

  private final int value;
  private final String description;

  DatasetStatusDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public int getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }

  /**
   * 根据值获取枚举
   * 对应Go: DatasetStatusFromString
   */
  public static DatasetStatusDTO fromValue(int value) {
    for (DatasetStatusDTO status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid DatasetStatus value: " + value);
  }

  /**
   * 根据字符串获取枚举
   * 对应Go: DatasetStatusFromString
   */
  public static DatasetStatusDTO fromString(String str) {
    if (str == null) {
      return null;
    }
    for (DatasetStatusDTO status : values()) {
      if (status.description.equals(str)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid DatasetStatus string: " + str);
  }

  @Override
  public String toString() {
    return description;
  }
}
