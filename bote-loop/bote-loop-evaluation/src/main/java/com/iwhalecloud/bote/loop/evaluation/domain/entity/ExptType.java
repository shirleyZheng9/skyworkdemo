package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.Getter;

/**
 * 实验类型枚举
 * 对应Go: ExptType
 */
@Getter
public enum ExptType {

  /**
   * 离线实验
   * 对应Go: ExptType_Offline = 1
   */
  OFFLINE(1, "Offline"),

  /**
   * 在线实验
   * 对应Go: ExptType_Online = 2
   */
  ONLINE(2, "Online");

  private final int value;
  private final String description;

  ExptType(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  public static ExptType fromValue(int value) {
    for (ExptType type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ExptType value: " + value);
  }

  /**
   * 是否是离线实验
   */
  public boolean isOffline() {
    return this == OFFLINE;
  }

  /**
   * 是否是在线实验
   */
  public boolean isOnline() {
    return this == ONLINE;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
