package com.iwhalecloud.bote.service.publish;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 发布平台回调服务接口
 *
 * @author system
 * @since 2025-01-09
 */
public interface IPublishWebhookService {

  /**
   * 处理回调消息
   *
   * @param callbackCode 回调编码
   * @param request HTTP请求
   * @return 响应内容
   */
  String handleWebhook(String callbackCode, HttpServletRequest request, String encryptedMsg);

  /**
   * 验证回调地址
   *
   * @param callbackCode 回调编码
   * @param request HTTP请求
   * @return 验证响应
   */
  String verifyWebhook(String callbackCode, HttpServletRequest request);
}
