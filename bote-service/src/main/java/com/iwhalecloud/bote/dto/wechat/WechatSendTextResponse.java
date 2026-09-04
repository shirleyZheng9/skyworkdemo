package com.iwhalecloud.bote.dto.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信发送文本消息响应
 *
 * @author lizuyin
 * @since 2025-08-12
 */
@Getter
@Setter
@ToString
public class WechatSendTextResponse {

  /** 错误码，0表示成功 */
  @JsonProperty("errcode")
  private Integer errcode;
  /** 错误描述 */
  @JsonProperty("errmsg")
  private String errmsg;

  /**
   * 判断响应是否成功
   *
   * @return true表示成功，false表示失败
   */
  public boolean isSuccess() {
    return errcode != null && errcode == 0;
  }
}
