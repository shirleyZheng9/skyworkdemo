package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

/**
 * 工具选择类型枚举
 * 迁移对应关系: Thrift ToolChoiceType
 */
public enum ToolChoiceTypeDTO {
  NONE("none"),
  AUTO("auto");

  private final String value;

  ToolChoiceTypeDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static ToolChoiceTypeDTO fromValue(String value) {
    for (ToolChoiceTypeDTO type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ToolChoiceType: " + value);
  }
}
