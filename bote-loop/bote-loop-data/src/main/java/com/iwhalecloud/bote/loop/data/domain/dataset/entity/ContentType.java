package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

/**
 * 内容类型枚举
 * 迁移对应关系: Go语言entity.ContentType
 * - 功能: 内容类型标识
 * - 字段定义: 各种内容类型常量
 * <p>
 * Java实现说明:
 * - 对应Go的ContentType类型别名
 * - 使用Java枚举定义各种内容类型
 * - 提供值和名称的访问方法
 * - 包含多模态判断方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 * - Go方法 -> Java方法
 */
public enum ContentType {
  UNKNOWN(""),
  TEXT("text"),
  IMAGE("image"),
  AUDIO("audio"),
  VIDEO("video"),
  MULTIPART("multipart");

  private final String value;

  ContentType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static ContentType fromValue(String value) {
    for (ContentType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return UNKNOWN;
  }

  /**
   * 判断是否为多模态类型
   * 迁移对应关系: Go语言entity.ContentType.IsMultiModal()
   * - 功能: 判断内容类型是否为多模态
   * - 返回: 是否为多模态类型
   */
  public boolean isMultiModal() {
    return this == IMAGE || this == AUDIO || this == VIDEO || this == MULTIPART;
  }
}
