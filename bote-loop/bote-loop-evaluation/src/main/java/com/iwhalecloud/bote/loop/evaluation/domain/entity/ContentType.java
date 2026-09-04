package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * 内容类型实体枚举
 * 对应Go: entity.ContentType (string类型)
 */
public enum ContentType {

  /**
   * 未知类型
   * 对应Go: ContentTypeUnknown = ""
   */
  UNKNOWN(""),

  /**
   * 文本类型
   * 对应Go: ContentTypeText = "text"
   */
  TEXT("text"),

  /**
   * 图片类型
   * 对应Go: ContentTypeImage = "image"
   */
  IMAGE("image"),

  /**
   * 音频类型
   * 对应Go: ContentTypeAudio = "audio"
   */
  AUDIO("audio"),

  /**
   * 视频类型
   * 对应Go: ContentTypeVideo = "video"
   */
  VIDEO("video"),

  /**
   * 多部分类型
   * 对应Go: ContentTypeMultiPart = "multipart"
   */
  MULTI_PART("multipart");

  private final String value;

  ContentType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static ContentType fromString(String str) {
    for (ContentType type : values()) {
      if (type.value.equals(str)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid ContentType string: " + str);
  }

  /**
   * 判断是否为多模态类型
   * 对应Go: IsMultiModal() bool
   */
  public boolean isMultiModal() {
    return this == IMAGE || this == AUDIO || this == VIDEO || this == MULTI_PART;
  }
}
