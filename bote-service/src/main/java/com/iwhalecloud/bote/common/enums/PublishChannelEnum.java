package com.iwhalecloud.bote.common.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 发布渠道枚举
 *
 * @author lizuyin
 * @since 2025-07-24
 */
@Getter
@RequiredArgsConstructor
public enum PublishChannelEnum {
  /** 百应平台 */
  @JsonProperty("BEYOND")
  BEYOND("BEYOND", "百应平台"),
  /** 微信公众号 */
  @JsonProperty("WECHAT")
  WECHAT("WECHAT", "微信公众号"),
  /** 个人微信（ClawBot） */
  @JsonProperty("WECLAWBOT")
  WECLAWBOT("WECLAWBOT", "个人微信"),
  /** A2A */
  @JsonProperty("A2A")
  A2A("A2A", "A2A"),
  /** 企业微信 */
  @JsonProperty("WEWORK")
  WEWORK("WEWORK", "企业微信"),
  /** 钉钉机器人 */
  @JsonProperty("DINGTALK")
  DINGTALK("DINGTALK", "钉钉机器人"),
  /** 飞书机器人 */
  @JsonProperty("FEISHU")
  FEISHU("FEISHU", "飞书机器人");
  /** 发布渠道编码 */
  private final String code;
  /** 发布渠道描述 */
  private final String desc;
  /**
   * 根据编码获取枚举值
   */
  public static PublishChannelEnum getByCode(String code) {
    if (code == null) {
      return null;
    }
    for (PublishChannelEnum channelEnum : values()) {
      if (channelEnum.getCode().equals(code)) {
        return channelEnum;
      }
    }
    return null;
  }
}
