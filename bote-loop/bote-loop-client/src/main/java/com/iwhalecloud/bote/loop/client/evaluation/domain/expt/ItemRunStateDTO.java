package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * 项目运行状态枚举
 */
public enum ItemRunStateDTO {
  @JsonProperty("-1")
  UNKNOWN(-1, "Unknown"),
  @JsonProperty("0")
  QUEUEING(0, "Queueing"),
  @JsonProperty("1")
  PROCESSING(1, "Processing"),
  @JsonProperty("2")
  SUCCESS(2, "Success"),
  @JsonProperty("3")
  FAIL(3, "Fail"),
  @JsonProperty("5")
  TERMINAL(5, "Terminal");

  private final int value;
  private final String name;

  ItemRunStateDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static ItemRunStateDTO fromValue(int value) {
    for (ItemRunStateDTO state : values()) {
      if (state.value == value) {
        return state;
      }
    }
    throw new IllegalArgumentException("Unknown item run state: " + value);
  }
}
