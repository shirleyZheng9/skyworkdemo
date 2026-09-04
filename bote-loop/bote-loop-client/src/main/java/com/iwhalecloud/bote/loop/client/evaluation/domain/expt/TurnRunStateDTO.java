package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

/**
 * 轮次运行状态枚举
 */
public enum TurnRunStateDTO {
  QUEUEING(0, "Queueing"),
  SUCCESS(1, "Success"),
  FAIL(2, "Fail"),
  PROCESSING(3, "Processing"),
  TERMINAL(4, "Terminal");

  private final int value;
  private final String name;

  TurnRunStateDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static TurnRunStateDTO fromValue(int value) {
    for (TurnRunStateDTO state : values()) {
      if (state.value == value) {
        return state;
      }
    }
    throw new IllegalArgumentException("Unknown turn run state: " + value);
  }
}
