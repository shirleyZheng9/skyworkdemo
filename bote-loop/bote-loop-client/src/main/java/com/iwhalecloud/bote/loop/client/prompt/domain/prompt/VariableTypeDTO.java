package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import lombok.Getter;

/**
 * 变量类型枚举
 * 迁移对应关系: Thrift VariableType
 */
@Getter
public enum VariableTypeDTO {
  STRING("string"),
  PLACEHOLDER("placeholder");

  private final String value;

  VariableTypeDTO(String value) {
    this.value = value;
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
