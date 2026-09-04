package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 工具类型DTO枚举
 * 对应Go: evaluator.ToolType
 */
@Getter
public enum ToolTypeDTO {

  /**
   * 函数工具
   * 对应Go: ToolType_Function = 1
   */
  FUNCTION(1, "Function"),

  /**
   * Google搜索工具（用于Gemini原生工具）
   * 对应Go: ToolType_GoogleSearch = 2
   */
  GOOGLE_SEARCH(2, "GoogleSearch");

  @JsonValue
  private final int value;
  private final String description;

  ToolTypeDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  @JsonCreator
  public static ToolTypeDTO fromValue(int value) {
    for (ToolTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ToolTypeDTO value: " + value);
  }

  /**
   * 根据字符串获取枚举
   */
  public static ToolTypeDTO fromString(String s) {
    for (ToolTypeDTO type : values()) {
      if (type.description.equals(s)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ToolTypeDTO string: " + s);
  }

  /**
   * 是否是函数工具
   */
  public boolean isFunction() {
    return this == FUNCTION;
  }

  /**
   * 是否是Google搜索工具
   */
  public boolean isGoogleSearch() {
    return this == GOOGLE_SEARCH;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
