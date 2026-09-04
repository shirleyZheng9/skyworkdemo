package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 聚合结果数据类型枚举
 * 迁移对应关系: Go语言AggrResultDataType
 * - 功能: 聚合结果数据类型枚举
 * - 常量: Double, ScoreDistribution
 * <p>
 * Java实现说明:
 * - 对应Go的AggrResultDataType枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go int -> Java int
 */
public enum AggrResultDataType {
  @JsonProperty("0")
  DOUBLE(0),
  @JsonProperty("1")
  SCORE_DISTRIBUTION(1);

  private final int value;

  AggrResultDataType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
