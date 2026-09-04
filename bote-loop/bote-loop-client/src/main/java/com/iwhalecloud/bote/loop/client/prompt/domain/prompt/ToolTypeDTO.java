package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

/**
 * 工具类型枚举
 * 迁移对应关系: Thrift ToolType
 */
public enum ToolTypeDTO {
  FUNCTION("function");

  private final String value;

  ToolTypeDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static ToolTypeDTO fromValue(String value) {
    for (ToolTypeDTO type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ToolType: " + value);
  }
}
