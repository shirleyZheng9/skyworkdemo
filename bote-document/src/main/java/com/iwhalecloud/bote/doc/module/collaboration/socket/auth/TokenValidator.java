package com.iwhalecloud.bote.doc.module.collaboration.socket.auth;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Token校验器接口
 *
 * @author Aiqing
 * @since 2025/08/29
 */
public interface TokenValidator {

  /**
   * 校验token并返回用户信息
   *
   * @param token token
   * @return 用户认证信息，如果token无效则返回null
   */
  UserAuthInfo validateToken(String token, String connectType);

  /**
   * 请请求Cookie中校验
   *
   * @param request 请求信息
   * @return 用户信息
   */
  UserAuthInfo validateFromRequest(FullHttpRequest request);

  /**
   * 检查token是否有效（不返回用户信息）
   *
   * @param token token
   * @return 是否有效
   */
  default boolean isValidToken(String token, String connectType) {
    return validateToken(token, connectType) != null;
  }
}
