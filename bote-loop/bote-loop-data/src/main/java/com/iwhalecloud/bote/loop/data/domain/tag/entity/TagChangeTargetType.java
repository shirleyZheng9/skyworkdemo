package com.iwhalecloud.bote.loop.data.domain.tag.entity;

/**
 * 标签变更目标类型枚举
 * 迁移对应关系: Go语言entity.TagChangeTargetType
 * - 功能: 标签变更目标类型标识
 * - 字段定义: 各种标签变更目标类型常量
 * <p>
 * Java实现说明:
 * - 对应Go的TagChangeTargetType类型别名
 * - 使用Java枚举定义各种变更目标类型
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum TagChangeTargetType {
  UNDEFINED(""),
  TAG("tag"),
  TAG_NAME("tag_name"),
  TAG_DESCRIPTION("tag_description"),
  TAG_STATUS("tag_status"),
  TAG_TYPE("tag_type"),
  TAG_VALUE_NAME("tag_value_name"),
  TAG_VALUE_STATUS("tag_value_status"),
  TAG_CONTENT_TYPE("tag_content_type");

  private final String value;

  TagChangeTargetType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static TagChangeTargetType fromValue(String value) {
    for (TagChangeTargetType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return UNDEFINED;
  }
}
