package com.iwhalecloud.bote.dto.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信发送文本消息请求
 *
 * @author lizuyin
 * @since 2025-08-12
 */
@Getter
@Setter
@ToString
public class WechatSendTextRequest {

  /** 用户的OpenID */
  @JsonProperty("touser")
  private String toUser;
  /** 消息类型，固定为 text */
  @JsonProperty("msgtype")
  private String msgType = "text";
  /** 文本消息内容 */
  private TextContent text = new TextContent();

  /**
   * 文本消息内容
   */
  @Getter
  @Setter
  @ToString
  public static class TextContent {
    /** 文本内容 */
    private String content;
  }

}
