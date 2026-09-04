package com.iwhalecloud.bote.loop.client.data.domain.dataset;

/**
 * Schema键枚举
 */
public enum SchemaKeyDTO {
  STRING(1, "String"),
  INTEGER(2, "Integer"),
  FLOAT(3, "Float"),
  BOOL(4, "Bool"),
  MESSAGE(5, "Message");

  private final int value;
  private final String name;

  SchemaKeyDTO(int value, String name) {
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
