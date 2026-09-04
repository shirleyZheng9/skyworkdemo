package com.iwhalecloud.bote.loop.client.data.domain.tag;

/**
 * 标签类型枚举
 */
public enum TagTypeDTO {
  TAG("tag"),
  OPTION("option");

  private final String value;

  TagTypeDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static TagTypeDTO fromValue(String value) {
    for (TagTypeDTO tagType : values()) {
      if (tagType.value.equals(value)) {
        return tagType;
      }
    }
    throw new IllegalArgumentException("Unknown TagTypeDTO value: " + value);
  }
}
