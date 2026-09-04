package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

/**
 * 更新实验轮次结果过滤类型枚举
 */
public enum UpsertExptTurnResultFilterTypeDTO {
  MANUAL("manual"),
  AUTO("auto"),
  CHECK("check");

  private final String value;

  UpsertExptTurnResultFilterTypeDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static UpsertExptTurnResultFilterTypeDTO fromValue(String value) {
    for (UpsertExptTurnResultFilterTypeDTO type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown filter type: " + value);
  }
}
