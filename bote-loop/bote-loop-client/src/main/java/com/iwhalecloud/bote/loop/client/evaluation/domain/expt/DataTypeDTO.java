package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 数据类型枚举
 */
public enum DataTypeDTO {
  @JsonProperty("0")
  DOUBLE(0, "Double"),
  @JsonProperty("1")
  SCORE_DISTRIBUTION(1, "ScoreDistribution");

  private final int value;
  private final String name;

  DataTypeDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static DataTypeDTO fromValue(int value) {
    for (DataTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown data type: " + value);
  }
}
