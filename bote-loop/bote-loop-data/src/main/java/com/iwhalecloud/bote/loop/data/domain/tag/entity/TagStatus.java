package com.iwhalecloud.bote.loop.data.domain.tag.entity;

/**
 * 标签状态枚举
 * 迁移对应关系: Go语言entity.TagStatus
 * - 功能: 标签状态标识
 * - 字段定义: 各种标签状态常量
 * <p>
 * Java实现说明:
 * - 对应Go的TagStatus类型别名
 * - 使用Java枚举定义各种状态
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum TagStatus {
  UNDEFINED(""),
  ACTIVE("active"),
  INACTIVE("inactive"),
  DEPRECATED("deprecated");

  private final String value;

  TagStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static TagStatus fromValue(String value) {
    for (TagStatus status : values()) {
      if (status.value.equals(value)) {
        return status;
      }
    }
    return UNDEFINED;
  }
}
