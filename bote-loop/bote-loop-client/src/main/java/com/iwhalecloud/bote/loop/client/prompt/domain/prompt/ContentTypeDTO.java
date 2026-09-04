package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import lombok.Getter;

/**
 * 内容类型枚举
 * 迁移对应关系: Thrift ContentType
 */
@Getter
public enum ContentTypeDTO {
  TEXT("text"),
  IMAGE_URL("image_url");

  private final String value;

  ContentTypeDTO(String value) {
    this.value = value;
  }

  public static ContentTypeDTO fromValue(String value) {
    for (ContentTypeDTO type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ContentType: " + value);
  }
}
