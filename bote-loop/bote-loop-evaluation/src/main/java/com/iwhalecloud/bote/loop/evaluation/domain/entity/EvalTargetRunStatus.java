package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 评估目标运行状态枚举
 * 迁移对应关系: Go语言EvalTargetRunStatus
 * - 功能: 评估目标运行状态枚举
 * - 常量: Unknown, Success, Fail
 * <p>
 * Java实现说明:
 * - 对应Go的EvalTargetRunStatus枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go int64 -> Java int
 */
public enum EvalTargetRunStatus {
  @JsonProperty("0")
  UNKNOWN(0),
  @JsonProperty("1")
  SUCCESS(1),
  @JsonProperty("2")
  FAIL(2);

  private final int value;

  EvalTargetRunStatus(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static EvalTargetRunStatus fromValue(Integer value) {
    for (EvalTargetRunStatus evalTargetRunStatus : values()) {
      if (evalTargetRunStatus.value == value) {
        return evalTargetRunStatus;
      }
    }
    throw new IllegalArgumentException("Unknown EvalTargetRunStatus: " + value);
  }
}
