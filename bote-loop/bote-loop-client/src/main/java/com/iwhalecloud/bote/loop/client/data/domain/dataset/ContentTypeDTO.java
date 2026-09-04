package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * Content Type Enum
 */
@Getter
public enum ContentTypeDTO {
  @JsonProperty("text")
  TEXT(1, "text"),
  @JsonProperty("image")
  IMAGE(2, "image"),
  @JsonProperty("audio")
  AUDIO(3, "audio"),
  @JsonProperty("video")
  VIDEO(4, "video"),
  @JsonProperty("multipart")
  MULTI_PART(100, "multipart");

  private final int value;
  private final String name;

  ContentTypeDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public static ContentTypeDTO fromValue(String value) {
    for (ContentTypeDTO type : values()) {
      if (type.name.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown content type: " + value);
  }
}
