package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 场景枚举
 * 迁移对应关系: Go语言Scenario
 * - 功能: 场景枚举
 * - 常量: Default, EvalTarget, Evaluator
 * <p>
 * Java实现说明:
 * - 对应Go的Scenario枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go string -> Java String
 */
public enum Scenario {
  @JsonProperty("default")
  DEFAULT("default"),
  @JsonProperty("eval_target")
  EVAL_TARGET("eval_target"),
  @JsonProperty("evaluator")
  EVALUATOR("evaluator");

  private final String value;

  Scenario(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
