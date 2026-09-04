package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 评估器类型DTO枚举
 * 对应Go: evaluator.EvaluatorType
 */
@Getter
public enum EvaluatorTypeDTO {

  /**
   * Prompt评估器
   * 对应Go: EvaluatorType_Prompt = 1
   */
  PROMPT(1, "Prompt"),

  /**
   * Code评估器
   * 对应Go: EvaluatorType_Code = 2
   */
  CODE(2, "Code");

  @JsonValue
  private final int value;
  private final String description;

  EvaluatorTypeDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  @JsonCreator
  public static EvaluatorTypeDTO fromValue(int value) {
    for (EvaluatorTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown EvaluatorTypeDTO value: " + value);
  }

  /**
   * 根据字符串获取枚举
   */
  public static EvaluatorTypeDTO fromString(String s) {
    for (EvaluatorTypeDTO type : values()) {
      if (type.description.equals(s)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown EvaluatorTypeDTO string: " + s);
  }

  /**
   * 是否是Prompt类型
   */
  public boolean isPrompt() {
    return this == PROMPT;
  }

  /**
   * 是否是Code类型
   */
  public boolean isCode() {
    return this == CODE;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
