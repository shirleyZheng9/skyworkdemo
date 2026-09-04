package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 评估器运行状态DTO枚举
 * 对应Go: evaluator.EvaluatorRunStatus
 */
@Getter
public enum EvaluatorRunStatusDTO {

  /**
   * 未知状态
   * 对应Go: EvaluatorRunStatusUnknown = 0
   */
  UNKNOWN(0, "Unknown"),

  /**
   * 成功状态
   * 对应Go: EvaluatorRunStatusSuccess = 1
   */
  SUCCESS(1, "Success"),

  /**
   * 失败状态
   * 对应Go: EvaluatorRunStatusFail = 2
   */
  FAIL(2, "Fail");

  @JsonValue
  private final int value;
  private final String description;

  EvaluatorRunStatusDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  @JsonCreator
  public static EvaluatorRunStatusDTO fromValue(int value) {
    for (EvaluatorRunStatusDTO status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown EvaluatorRunStatusDTO value: " + value);
  }

  /**
   * 是否是成功状态
   */
  public boolean isSuccess() {
    return this == SUCCESS;
  }

  /**
   * 是否是失败状态
   */
  public boolean isFail() {
    return this == FAIL;
  }

  /**
   * 是否是未知状态
   */
  public boolean isUnknown() {
    return this == UNKNOWN;
  }

  /**
   * 是否是完成状态（成功或失败）
   */
  public boolean isCompleted() {
    return this == SUCCESS || this == FAIL;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
