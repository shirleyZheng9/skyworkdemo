package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 聚合器类型枚举
 */
public enum AggregatorTypeDTO {
  @JsonProperty("1")
  AVERAGE(1, "Average"),
  @JsonProperty("2")
  SUM(2, "Sum"),
  @JsonProperty("3")
  MAX(3, "Max"),
  @JsonProperty("4")
  MIN(4, "Min"),
  @JsonProperty("5")
  DISTRIBUTION(5, "Distribution");

  private final int value;
  private final String name;

  AggregatorTypeDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static AggregatorTypeDTO fromValue(int value) {
    for (AggregatorTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown aggregator type: " + value);
  }
}
