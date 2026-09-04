package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 评估目标类型枚举
 * 迁移对应关系: Go语言EvalTargetType
 * - 功能: 评估目标类型枚举
 * - 常量: Bot, LoopPrompt, Workflow
 * <p>
 * Java实现说明:
 * - 对应Go的EvalTargetType枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * - 实现toString方法
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go int64 -> Java int
 * - Go String()方法 -> Java toString()方法
 */
public enum EvalTargetType {
  @JsonProperty("1")
  BOT(1, "Bot"),
  @JsonProperty("2")
  LOOP_PROMPT(2, "LoopPrompt"),
  @JsonProperty("3")
  WORKFLOW(3, "Workflow");

  private final int value;
  private final String name;

  EvalTargetType(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  public static EvalTargetType fromValue(Integer value) {
    for (EvalTargetType evalTargetType : values()) {
      if (evalTargetType.value == value) {
        return evalTargetType;
      }
    }
    throw new IllegalArgumentException("Unknown EvalTargetType: " + value);
  }
}
