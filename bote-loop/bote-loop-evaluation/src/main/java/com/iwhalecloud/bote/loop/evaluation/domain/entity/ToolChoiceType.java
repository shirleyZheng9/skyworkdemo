package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 工具选择类型枚举
 * 迁移对应关系: Go语言ToolChoiceType
 * - 功能: 工具选择类型枚举
 * - 常量: None, Auto, Required
 * <p>
 * Java实现说明:
 * - 对应Go的ToolChoiceType枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go string -> Java String
 */
public enum ToolChoiceType {
  @JsonProperty("none")
  NONE("none"),
  @JsonProperty("auto")
  AUTO("auto"),
  @JsonProperty("required")
  REQUIRED("required");

  private final String value;

  ToolChoiceType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
