package com.iwhalecloud.bote.loop.client.data.domain.tag;

/**
 * 操作类型枚举
 */
public enum OperationTypeDTO {
  CREATE("create"),
  UPDATE("update"),
  DELETE("delete");

  private final String value;

  OperationTypeDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static OperationTypeDTO fromValue(String value) {
    for (OperationTypeDTO type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown OperationTypeDTO value: " + value);
  }
}
