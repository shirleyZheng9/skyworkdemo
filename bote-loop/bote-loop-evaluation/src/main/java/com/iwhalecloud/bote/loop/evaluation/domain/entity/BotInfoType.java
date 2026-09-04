package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.Getter;

/**
 * Bot信息类型枚举
 * 对应Go: CozeBotInfoType
 */
@Getter
public enum BotInfoType {

  /**
   * 草稿 bot
   * 对应Go: CozeBotInfoTypeDraftBot = 1
   */
  DRAFT_BOT(1, "DraftBot"),

  /**
   * 商店 bot
   * 对应Go: CozeBotInfoTypeProductBot = 2
   */
  PRODUCT_BOT(2, "ProductBot");

  private final int value;
  private final String description;

  BotInfoType(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  public static BotInfoType fromValue(int value) {
    for (BotInfoType type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown BotInfoType value: " + value);
  }

  /**
   * 根据描述获取枚举
   */
  public static BotInfoType fromDescription(String description) {
    for (BotInfoType type : values()) {
      if (type.description.equals(description)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown BotInfoType description: " + description);
  }

  @Override
  public String toString() {
    return this.description;
  }
}

