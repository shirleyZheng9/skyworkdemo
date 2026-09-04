package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

/**
 * 字段转换类型实体枚举
 * 对应Go: FieldTransformationType
 */
public enum FieldTransformationType {

  /**
   * 移除未在当前列的 jsonSchema 中定义的字段（包括 properties 和 patternProperties），仅在列类型为 struct 时有效
   * 对应Go: FieldTransformationType_RemoveExtraFields = 1
   */
  REMOVE_EXTRA_FIELDS(1, "RemoveExtraFields");

  private final int value;
  private final String description;

  FieldTransformationType(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public int getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }

  public static FieldTransformationType fromValue(int value) {
    for (FieldTransformationType type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid FieldTransformationType value: " + value);
  }

  public static FieldTransformationType fromString(String str) {
    for (FieldTransformationType type : values()) {
      if (type.description.equals(str)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid FieldTransformationType string: " + str);
  }
}
