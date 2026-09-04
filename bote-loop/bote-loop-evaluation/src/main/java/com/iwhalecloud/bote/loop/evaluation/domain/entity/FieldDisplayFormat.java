package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * 字段显示格式实体枚举
 * 对应Go: entity.FieldDisplayFormat (int64类型)
 */
public enum FieldDisplayFormat {

  /**
   * 纯文本格式
   * 对应Go: FieldDisplayFormat_PlainText = 1
   */
  PLAIN_TEXT(1, "PlainText"),

  /**
   * Markdown格式
   * 对应Go: FieldDisplayFormat_Markdown = 2
   */
  MARKDOWN(2, "Markdown"),

  /**
   * JSON格式
   * 对应Go: FieldDisplayFormat_JSON = 3
   */
  JSON(3, "JSON"),

  /**
   * YAML格式
   * 对应Go: FieldDisplayFormat_YAML = 4
   */
  YAML(4, "YAML"),

  /**
   * 代码格式
   * 对应Go: FieldDisplayFormat_Code = 5
   */
  CODE(5, "Code");

  private final int value;
  private final String description;

  FieldDisplayFormat(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public int getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }

  public static FieldDisplayFormat fromValue(int value) {
    for (FieldDisplayFormat format : values()) {
      if (format.value == value) {
        return format;
      }
    }
    throw new IllegalArgumentException("Invalid FieldDisplayFormat value: " + value);
  }

  public static FieldDisplayFormat fromString(String str) {
    for (FieldDisplayFormat format : values()) {
      if (format.description.equals(str)) {
        return format;
      }
    }
    throw new IllegalArgumentException("Invalid FieldDisplayFormat string: " + str);
  }
}
