package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 实验项结果状态枚举
 * 迁移对应关系: Go语言ExptItemResultState
 * - 功能: 实验项结果状态枚举
 * - 常量: Default, Logged, Resulted
 * <p>
 * Java实现说明:
 * - 对应Go的ExptItemResultState枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go int -> Java int
 */
public enum ExptItemResultState {
  @JsonProperty("0")
  DEFAULT(0),
  @JsonProperty("2")
  LOGGED(2),
  @JsonProperty("1")
  RESULTED(1);

  private final int value;

  ExptItemResultState(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
