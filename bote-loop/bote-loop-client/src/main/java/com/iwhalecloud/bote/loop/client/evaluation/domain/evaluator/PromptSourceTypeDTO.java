package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 提示源类型DTO枚举
 * 对应Go: evaluator.PromptSourceType
 */
@Getter
public enum PromptSourceTypeDTO {

  /**
   * 内置模板
   * 对应Go: PromptSourceType_BuiltinTemplate = 1
   */
  BUILTIN_TEMPLATE(1, "BuiltinTemplate"),

  /**
   * Loop提示
   * 对应Go: PromptSourceType_LoopPrompt = 2
   */
  LOOP_PROMPT(2, "LoopPrompt"),

  /**
   * 自定义
   * 对应Go: PromptSourceType_Custom = 3
   */
  CUSTOM(3, "Custom");

  @JsonValue
  private final int value;
  private final String description;

  PromptSourceTypeDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  @JsonCreator
  public static PromptSourceTypeDTO fromValue(int value) {
    for (PromptSourceTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown PromptSourceTypeDTO value: " + value);
  }

  /**
   * 根据字符串获取枚举
   */
  public static PromptSourceTypeDTO fromString(String s) {
    for (PromptSourceTypeDTO type : values()) {
      if (type.description.equals(s)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown PromptSourceTypeDTO string: " + s);
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
