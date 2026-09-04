package com.iwhalecloud.bote.loop.client.data.domain.tag;

/**
 * 变更目标类型枚举
 */
public enum ChangeTargetTypeDTO {
  TAG("tag"),
  TAG_NAME("tag_name"),
  TAG_DESCRIPTION("tag_description"),
  TAG_STATUS("tag_status"),
  TAG_TYPE("tag_type"),
  TAG_CONTENT_TYPE("tag_content_type"),
  TAG_VALUE_NAME("tag_value_name"),
  TAG_VALUE_STATUS("tag_value_status");

  private final String value;

  ChangeTargetTypeDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static ChangeTargetTypeDTO fromValue(String value) {
    for (ChangeTargetTypeDTO type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ChangeTargetTypeDTO value: " + value);
  }
}
