package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 聚合器类型枚举
 * 迁移对应关系: Go语言AggregatorType
 * - 功能: 聚合器类型枚举
 * - 常量: Average, Sum, Max, Min, Distribution
 * <p>
 * Java实现说明:
 * - 对应Go的AggregatorType枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go int -> Java int
 */
public enum AggregatorType {
  @JsonProperty("1")
  AVERAGE(1),
  @JsonProperty("2")
  SUM(2),
  @JsonProperty("3")
  MAX(3),
  @JsonProperty("4")
  MIN(4),
  @JsonProperty("5")
  DISTRIBUTION(5);

  private final int value;

  AggregatorType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
