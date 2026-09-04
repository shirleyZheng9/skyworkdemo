package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

/**
 * 来源类型枚举
 */
public enum SourceTypeDTO {
  EVALUATION(1, "Evaluation"),
  AUTO_TASK(2, "AutoTask");

  private final int value;
  private final String name;

  SourceTypeDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static SourceTypeDTO fromValue(int value) {
    for (SourceTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown source type: " + value);
  }
}
