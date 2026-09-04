package com.iwhalecloud.bote.dto.wechat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信配置中转DTO
 *
 * @author lizuyin
 * @since 2025-08-16
 */
@Getter
@Setter
@ToString
public class WechatMsgDTO {

  /** 智能体编码 */
  private String code;
  /** 签名 */
  private String msgSignature;
  /** 时间戳 */
  private String timestamp;
  /** 随机数 */
  private String nonce;
  /** 公众号标识 */
  private String openid;
  /** 消息内容 */
  private String xml;
  /** 握手响应 */
  private String echoStr;

}
