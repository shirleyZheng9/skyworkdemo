package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

/**
 * 模板类型枚举
 */
public enum TemplateTypeDTO {
  PROMPT(1, "Prompt"),
  CODE(2, "Code");

  private final int value;
  private final String name;

  TemplateTypeDTO(int value, String name) {
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
