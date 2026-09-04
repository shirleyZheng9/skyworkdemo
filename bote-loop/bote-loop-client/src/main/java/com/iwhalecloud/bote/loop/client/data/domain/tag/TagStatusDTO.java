package com.iwhalecloud.bote.loop.client.data.domain.tag;

/**
 * 标签状态枚举
 */
public enum TagStatusDTO {
  ACTIVE("active"),
  INACTIVE("inactive"),
  DEPRECATED("deprecated");

  private final String value;

  TagStatusDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static TagStatusDTO fromValue(String value) {
    for (TagStatusDTO status : values()) {
      if (status.value.equals(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown TagStatusDTO value: " + value);
  }

}
