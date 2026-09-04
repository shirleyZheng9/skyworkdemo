package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 解析类型枚举
 * 迁移对应关系: Go语言ParseType
 * - 功能: 解析类型枚举
 * - 常量: FunctionCall, Content
 * <p>
 * Java实现说明:
 * - 对应Go的ParseType枚举
 * - 使用Java枚举定义
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go string -> Java String
 */
public enum ParseType {
  @JsonProperty("function_call")
  FUNCTION_CALL("function_call"),
  @JsonProperty("content")
  CONTENT("content");

  private final String value;

  ParseType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
