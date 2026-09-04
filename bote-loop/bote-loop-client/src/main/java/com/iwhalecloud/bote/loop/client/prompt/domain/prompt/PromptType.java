package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Prompt类型枚举
 * 对应数据库字段: bt_prompt_basic.prompt_type
 */
@Schema(description = "Prompt类型枚举")
public enum PromptType {
  /**
   * 系统提示词
   */
  @Schema(description = "系统提示词")
  SystemPrompt("SystemPrompt"),

  /**
   * 问题分类
   */
  @Schema(description = "问题分类")
  QuestionClassifier("QuestionClassifier"),

  /**
   * 参数提取
   */
  @Schema(description = "参数提取")
  ParamExtractor("ParamExtractor");

  @JsonValue
  private final String value;

  PromptType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * 根据字符串值获取枚举
   */
  @JsonCreator
  public static PromptType fromValue(String value) {
    if (value == null) {
      return null;
    }
    for (PromptType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown PromptType: " + value);
  }

  @Override
  public String toString() {
    return value;
  }
}

