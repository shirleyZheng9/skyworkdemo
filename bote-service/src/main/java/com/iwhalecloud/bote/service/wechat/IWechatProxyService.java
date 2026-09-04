package com.iwhalecloud.bote.service.wechat;

import com.iwhalecloud.bote.dto.wechat.WechatMsgDTO;

/**
 * 统一代理服务接口（平台无关，当前支持：微信公众号）
 *
 * <p>职责：
 * - 提供微信服务器 GET 验证能力；
 * - 提供微信消息/事件 POST 入口处理能力。
 *
 * <p>注意：避免使用数据库高级特性，保持实现可移植性。
 *
 * @author lizuyin
 * @since 2025-08-08
 */
public interface IWechatProxyService {

  /**
   * 微信服务器 GET 验证（原样返回 echostr）。
   *
   * @param msgDTO 微信消息DTO
   * @return 验签通过返回 echostr，否则返回 "forbidden"
   */
  String wechatEchoVerify(WechatMsgDTO msgDTO);

  /**
   * 微信消息/事件 POST 投递处理（明文/安全模式）。
   *
   * <p>职责：
   * - 明文模式：校验 signature = sha1(sorted(token, timestamp, nonce)) 后直接返回 success；
   * - 安全模式：校验 msg_signature 并解密出真实明文（仅为后续业务处理预留，此处快速回包 success）。
   *
   * <p>注意：此接口仅做验签与解密，默认同步回包 "success"，业务处理建议异步化。
   *
   * @param msgDTO 微信消息DTO
   * @return 默认返回 "success"，验签失败返回 "forbidden"
   */
  String wechatProxy(WechatMsgDTO msgDTO);

}



