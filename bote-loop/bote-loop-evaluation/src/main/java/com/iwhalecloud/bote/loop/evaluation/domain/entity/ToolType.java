package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.Getter;

/**
 * 工具类型枚举
 * 对应Go: entity.ToolType
 */
@Getter
public enum ToolType {

  /**
   * 函数工具
   * 对应Go: ToolTypeFunction = 1
   */
  FUNCTION(1, "Function"),

  /**
   * Google搜索工具（用于Gemini原生工具）
   * 对应Go: ToolType_GoogleSearch = 2
   */
  GOOGLE_SEARCH(2, "GoogleSearch");

  private final int value;
  private final String description;

  ToolType(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  public static ToolType fromValue(int value) {
    for (ToolType type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ToolType value: " + value);
  }

  /**
   * 根据字符串获取枚举
   */
  public static ToolType fromString(String s) {
    for (ToolType type : values()) {
      if (type.description.equals(s)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ToolType string: " + s);
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
