package com.iwhalecloud.bote.loop.client.data.domain.dataset;

/**
 * 字段转换类型枚举
 */
public enum FieldTransformationTypeDTO {
  REMOVE_EXTRA_FIELDS(1, "RemoveExtraFields");

  private final int value;
  private final String name;

  FieldTransformationTypeDTO(int value, String name) {
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
