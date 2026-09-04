package com.iwhalecloud.bote.service.publish;

import java.util.Map;

/**
 * 个人微信（ClawBot）扫码登录与长连接管理：发布记录、渠道状态、SDK 连接协同。
 *
 * @author chen.linfa
 * @since 2026-04-11
 */
public interface IWeClawBotAuthService {

  /**
   * 按有效发布记录拉取登录二维码（依赖 publish_params 中的 baseUrl）。
   */
  Map<String, Object> getQrcode(String callbackCode);

  /**
   * 轮询扫码状态，确认后回填 botToken 并重启连接。
   */
  Map<String, Object> confirmLogin(String callbackCode, String qrcode, Integer maxWaitSeconds, Integer pollIntervalMs, Long userId);

  /**
   * 禁用渠道、逻辑删除发布记录、停止长轮询。
   */
  void disconnect(String callbackCode, Long userId);

}
