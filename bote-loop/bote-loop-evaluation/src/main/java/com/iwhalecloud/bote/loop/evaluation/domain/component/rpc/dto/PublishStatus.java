package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

/**
 * 发布状态枚举
 * 迁移对应关系: Go语言PublishStatus枚举
 */
public enum PublishStatus {
  UNDEFINED(0, "未定义"),
  UNPUBLISH(1, "未发布"),
  PUBLISHED(2, "已发布");

  private final int value;
  private final String description;

  PublishStatus(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public int getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }

  public static PublishStatus fromValue(int value) {
    for (PublishStatus status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    return UNDEFINED;
  }
}
