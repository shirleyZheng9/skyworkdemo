package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

/**
 * 变量类型枚举
 * 对应Thrift: VariableType
 */
public enum VariableTypeDTO {
  STRING("string"),
  PLACEHOLDER("placeholder");

  private final String value;

  VariableTypeDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static VariableTypeDTO fromValue(String value) {
    for (VariableTypeDTO type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown VariableType: " + value);
  }
}
