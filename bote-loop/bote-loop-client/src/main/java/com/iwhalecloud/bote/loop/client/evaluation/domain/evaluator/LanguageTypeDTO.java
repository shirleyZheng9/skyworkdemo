package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

/**
 * 语言类型枚举
 */
public enum LanguageTypeDTO {
  PYTHON(1, "Python"),
  JS(2, "JS");

  private final int value;
  private final String name;

  LanguageTypeDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }
}
