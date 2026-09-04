package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

/**
 * 过滤操作符类型枚举
 */
public enum FilterOperatorTypeDTO {
  UNKNOWN(0, "Unknown"),
  EQUAL(1, "Equal"),
  NOT_EQUAL(2, "NotEqual"),
  GREATER(3, "Greater"),
  GREATER_OR_EQUAL(4, "GreaterOrEqual"),
  LESS(5, "Less"),
  LESS_OR_EQUAL(6, "LessOrEqual"),
  IN(7, "In"),
  NOT_IN(8, "NotIn"),
  LIKE(9, "Like"),
  NOT_LIKE(10, "NotLike"),
  IS_NULL(11, "IsNull"),
  IS_NOT_NULL(12, "IsNotNull");

  private final int value;
  private final String name;

  FilterOperatorTypeDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static FilterOperatorTypeDTO fromValue(int value) {
    for (FilterOperatorTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown filter operator type: " + value);
  }
}
