package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Bot信息类型DTO枚举
 * 对应Go: eval_target.CozeBotInfoType
 */
@Getter
public enum BotInfoTypeDTO {

  /**
   * 草稿 bot
   * 对应Go: CozeBotInfoType_DraftBot = 1
   */
  DRAFT_BOT(1, "DraftBot"),

  /**
   * 商店 bot
   * 对应Go: CozeBotInfoType_ProductBot = 2
   */
  PRODUCT_BOT(2, "ProductBot");

  /**
   * 枚举值
   * 对应Go: int64
   */
  @JsonValue
  private final int value;

  /**
   * 枚举描述
   */
  private final String description;

  BotInfoTypeDTO(int value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   */
  @JsonCreator
  public static BotInfoTypeDTO fromValue(int value) {
    for (BotInfoTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown BotInfoTypeDTO value: " + value);
  }

  /**
   * 根据描述获取枚举
   */
  public static BotInfoTypeDTO fromDescription(String description) {
    for (BotInfoTypeDTO type : values()) {
      if (type.description.equals(description)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown BotInfoTypeDTO description: " + description);
  }

  /**
   * 是否是草稿bot
   */
  public boolean isDraftBot() {
    return this == DRAFT_BOT;
  }

  /**
   * 是否是商店bot
   */
  public boolean isProductBot() {
    return this == PRODUCT_BOT;
  }

  @Override
  public String toString() {
    return this.description;
  }
}

