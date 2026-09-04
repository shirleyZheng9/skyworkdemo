package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.Getter;

/**
 * 提示源类型枚举
 * 对应Go: entity.PromptSourceType
 */
@Getter
public enum PromptSourceType {

  /**
   * 内置模板
   * 对应Go: PromptSourceTypeBuiltinTemplate = 1
   */
  BUILTIN_TEMPLATE(1, "BuiltinTemplate"),

  /**
   * Loop提示
   * 对应Go: PromptSourceTypeLoopPrompt = 2
   */
  LOOP_PROMPT(2, "LoopPrompt"),

  /**
   * 自定义
   * 对应Go: PromptSourceTypeCustom = 3
   */
  CUSTOM(3, "Custom");

  private final int value;
  private final String description;

  PromptSourceType(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  public static PromptSourceType fromValue(int value) {
    for (PromptSourceType type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown PromptSourceType value: " + value);
  }

  /**
   * 根据字符串获取枚举
   */
  public static PromptSourceType fromString(String s) {
    for (PromptSourceType type : values()) {
      if (type.description.equals(s)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown PromptSourceType string: " + s);
  }

  /**
   * 是否是内置模板
   */
  public boolean isBuiltinTemplate() {
    return this == BUILTIN_TEMPLATE;
  }

  /**
   * 是否是Loop提示
   */
  public boolean isLoopPrompt() {
    return this == LOOP_PROMPT;
  }

  /**
   * 是否是自定义
   */
  public boolean isCustom() {
    return this == CUSTOM;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
