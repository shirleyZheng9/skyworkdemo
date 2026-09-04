package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 数据项错误类型DTO枚举
 * 对应Go: ItemErrorType
 */
@Getter
public enum ItemErrorTypeDTO {

  /**
   * schema 不匹配
   * 对应Go: ItemErrorType_MismatchSchema = 1
   */
  @JsonProperty("1")
  MISMATCH_SCHEMA(1, "MismatchSchema"),

  /**
   * 空数据
   * 对应Go: ItemErrorType_EmptyData = 2
   */
  @JsonProperty("2")
  EMPTY_DATA(2, "EmptyData"),

  /**
   * 单条数据大小超限
   * 对应Go: ItemErrorType_ExceedMaxItemSize = 3
   */
  @JsonProperty("3")
  EXCEED_MAX_ITEM_SIZE(3, "ExceedMaxItemSize"),

  /**
   * 数据集容量超限
   * 对应Go: ItemErrorType_ExceedDatasetCapacity = 4
   */
  @JsonProperty("4")
  EXCEED_DATASET_CAPACITY(4, "ExceedDatasetCapacity"),

  /**
   * 文件格式错误
   * 对应Go: ItemErrorType_MalformedFile = 5
   */
  @JsonProperty("5")
  MALFORMED_FILE(5, "MalformedFile"),

  /**
   * 包含非法内容
   * 对应Go: ItemErrorType_IllegalContent = 6
   */
  @JsonProperty("6")
  ILLEGAL_CONTENT(6, "IllegalContent"),

  /**
   * 缺少必填字段
   * 对应Go: ItemErrorType_MissingRequiredField = 7
   */
  @JsonProperty("7")
  MISSING_REQUIRED_FIELD(7, "MissingRequiredField"),

  /**
   * 数据嵌套层数超限
   * 对应Go: ItemErrorType_ExceedMaxNestedDepth = 8
   */
  @JsonProperty("8")
  EXCEED_MAX_NESTED_DEPTH(8, "ExceedMaxNestedDepth");

  private final int value;
  private final String description;

  ItemErrorTypeDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  public static ItemErrorTypeDTO fromValue(int value) {
    for (ItemErrorTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ItemErrorType value: " + value);
  }
}
