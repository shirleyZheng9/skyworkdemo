package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 评估器类型枚举
 * 迁移对应关系: Go语言EvaluatorType
 * - 功能: 评估器类型枚举
 * - 常量: Prompt, Code
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluatorType枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go int64 -> Java int
 */
public enum EvaluatorType {
  @JsonProperty("1")
  PROMPT(1),
  @JsonProperty("2")
  CODE(2);

  private final int value;

  EvaluatorType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static EvaluatorType fromValue(Integer value) {
    for (EvaluatorType evaluatorType : values()) {
      if (evaluatorType.value == value) {
        return evaluatorType;
      }
    }
    throw new IllegalArgumentException("Unknown EvaluatorType: " + value);
  }
}
