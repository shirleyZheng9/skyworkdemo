package com.iwhalecloud.bote.loop.client.evaluation.domain.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 内容类型枚举
 */
public enum ContentTypeDTO {
  /** 文本类型 */
  @JsonProperty("text")
  TEXT("text"),
  /** 图片类型 */
  @JsonProperty("image")
  IMAGE("image"),
  /** 音频类型 */
  @JsonProperty("audio")
  AUDIO("audio"),
  /** 多部分类型 */
  @JsonProperty("multipart")
  MULTI_PART("multipart");

  private final String value;

  ContentTypeDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static ContentTypeDTO fromValue(String value) {
    for (ContentTypeDTO type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown content type: " + value);
  }
}
