package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

/**
 * 工具选择类型枚举
 * 对应Thrift: ToolChoiceType
 */
public enum ToolChoiceTypeDTO {
  AUTO("auto"),
  NONE("none");

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
