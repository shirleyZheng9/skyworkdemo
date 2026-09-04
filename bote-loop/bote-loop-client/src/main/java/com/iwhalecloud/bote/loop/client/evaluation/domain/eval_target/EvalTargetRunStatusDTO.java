package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 评估目标运行状态DTO枚举
 * 对应Go: eval_target.EvalTargetRunStatus
 */
@Getter
public enum EvalTargetRunStatusDTO {

  /**
   * 未知状态
   * 对应Go: EvalTargetRunStatusUnknown = 0
   */
  @JsonProperty("0")
  UNKNOWN(0, "Unknown"),

  /**
   * 成功状态
   * 对应Go: EvalTargetRunStatusSuccess = 1
   */
  @JsonProperty("1")
  SUCCESS(1, "Success"),

  /**
   * 失败状态
   * 对应Go: EvalTargetRunStatusFail = 2
   */
  @JsonProperty("2")
  FAIL(2, "Fail");

  @JsonValue
  private final int value;
  private final String description;

  EvalTargetRunStatusDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  @JsonCreator
  public static EvalTargetRunStatusDTO fromValue(int value) {
    for (EvalTargetRunStatusDTO status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown EvalTargetRunStatusDTO value: " + value);
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

  /**
   * 是否是运行中状态
   */
  public boolean isRunning() {
    return this == UNKNOWN; // 在某些上下文中，UNKNOWN表示运行中
  }

  @Override
  public String toString() {
    return this.description;
  }
}
