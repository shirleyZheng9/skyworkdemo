package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 字段显示格式DTO枚举
 * 对应Go: dataset.FieldDisplayFormat (int64类型)
 */
@Getter
public enum FieldDisplayFormatDTO {

  /**
   * 纯文本格式
   * 对应Go: FieldDisplayFormat_PlainText = 1
   */
  @JsonProperty("1")
  PLAIN_TEXT(1, "PlainText"),

  /**
   * Markdown格式
   * 对应Go: FieldDisplayFormat_Markdown = 2
   */
  @JsonProperty("2")
  MARKDOWN(2, "Markdown"),

  /**
   * JSON格式
   * 对应Go: FieldDisplayFormat_JSON = 3
   */
  @JsonProperty("3")
  JSON(3, "JSON"),

  /**
   * YAML格式
   * 对应Go: FieldDisplayFormat_YAML = 4
   */
  @JsonProperty("4")
  YAML(4, "YAML"),

  /**
   * 代码格式
   * 对应Go: FieldDisplayFormat_Code = 5
   */
  @JsonProperty("5")
  CODE(5, "Code");

  private final int value;
  private final String description;

  FieldDisplayFormatDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public static FieldDisplayFormatDTO fromValue(int value) {
    for (FieldDisplayFormatDTO format : values()) {
      if (format.value == value) {
        return format;
      }
    }
    throw new IllegalArgumentException("Invalid FieldDisplayFormat value: " + value);
  }

  public static FieldDisplayFormatDTO fromString(String str) {
    for (FieldDisplayFormatDTO format : values()) {
      if (format.description.equals(str)) {
        return format;
      }
    }
    throw new IllegalArgumentException("Invalid FieldDisplayFormat string: " + str);
  }
}
