package com.iwhalecloud.bote.loop.client.data.domain.tag;

/**
 * 标签内容类型枚举
 */
public enum TagContentTypeDTO {
  CATEGORICAL("categorical"),
  BOOLEAN("boolean"),
  CONTINUOUS_NUMBER("continuous_number"),
  FREE_TEXT("free_text");

  private final String value;

  TagContentTypeDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static TagContentTypeDTO fromValue(String value) {
    for (TagContentTypeDTO type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown TagContentTypeDTO value: " + value);
  }
}
