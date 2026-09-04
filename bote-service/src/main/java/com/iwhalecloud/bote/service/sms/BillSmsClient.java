package com.iwhalecloud.bote.service.sms;

/**
 * 河南电信SMS消息发送客户端
 *
 * @author wangtingyun
 * @since 2025-10-14
 */
public interface BillSmsClient {

  /**
   * 发送收集短信消息
   *
   * @param phone 接收短信的手机号码
   * @param msgContent 短信消息内容
   */
  boolean sendSmsMessage(String phone, String msgContent);

}
