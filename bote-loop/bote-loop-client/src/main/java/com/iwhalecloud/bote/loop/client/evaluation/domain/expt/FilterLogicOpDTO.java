package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

/**
 * 过滤逻辑操作枚举
 */
public enum FilterLogicOpDTO {
  UNKNOWN(0, "Unknown"),
  AND(1, "And"),
  OR(2, "Or");

  private final int value;
  private final String name;

  FilterLogicOpDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static FilterLogicOpDTO fromValue(int value) {
    for (FilterLogicOpDTO op : values()) {
      if (op.value == value) {
        return op;
      }
    }
    throw new IllegalArgumentException("Unknown filter logic op: " + value);
  }
}
