package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 分数类型枚举
 * 迁移对应关系: Go语言ScoreType
 * - 功能: 分数类型枚举
 * - 常量: Range, Enum
 * <p>
 * Java实现说明:
 * - 对应Go的ScoreType枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go int64 -> Java int
 */
public enum ScoreType {
  @JsonProperty("1")
  RANGE(1),
  @JsonProperty("2")
  ENUM(2);

  private final int value;

  ScoreType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
