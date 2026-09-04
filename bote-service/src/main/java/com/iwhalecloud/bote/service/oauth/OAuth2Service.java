package com.iwhalecloud.bote.service.oauth;

import com.iwhalecloud.bote.dto.oauth.OAuth2TokenRequest;
import com.iwhalecloud.bote.dto.oauth.OAuth2TokenResponse;
import com.iwhalecloud.bote.dto.oauth.OAuth2UserInfo;
import org.springframework.lang.Nullable;
/**
 * OAuth2服务接口
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
public interface OAuth2Service {
  /**
   * 生成授权码
   *
   * @param clientId    客户端ID
   * @param redirectUri 重定向URI
   * @param scope       权限范围
   * @param state       状态参数
   * @param userId      用户ID
   * @return 授权码
   */
  String generateAuthorizationCode(String clientId, String redirectUri, String scope, String state, Long userId);
  /**
   * 验证授权码
   *
   * @param code        授权码
   * @param clientId    客户端ID
   * @param redirectUri 重定向URI
   * @return 用户ID，验证失败返回null
   */
  @Nullable
  Long validateAuthorizationCode(String code, String clientId, String redirectUri);
  /**
   * 生成访问令牌
   *
   * @param userId   用户ID（可为null，用于系统级token）
   * @param clientId 客户端ID
   * @param scope    权限范围
   * @return 访问令牌
   */
  String generateAccessToken(Long userId, String clientId, String scope);
  /**
   * 生成系统级访问令牌（客户端凭证模式）
   *
   * @param clientId 客户端ID
   * @param scope    权限范围
   * @return 访问令牌
   */
  String generateSystemAccessToken(String clientId, String scope);

  /**
   * 生成刷新令牌
   *
   * @param userId   用户ID（可为null，用于系统级token）
   * @param clientId 客户端ID
   * @return 刷新令牌
   */
  String generateRefreshToken(Long userId, String clientId);
  /**
   * 验证访问令牌
   *
   * @param accessToken 访问令牌
   * @return 用户ID，验证失败返回null（系统级token返回null）
   */
  @Nullable
  Long validateAccessToken(String accessToken);
  /**
   * 验证访问令牌并返回客户端ID
   *
   * @param accessToken 访问令牌
   * @return 客户端ID，验证失败返回null
   */
  @Nullable
  String validateAccessTokenAndGetClientId(String accessToken);

  /**
   * 验证刷新令牌
   *
   * @param refreshToken 刷新令牌
   * @param clientId     客户端ID
   * @return 用户ID，验证失败返回null
   */
  @Nullable
  Long validateRefreshToken(String refreshToken, String clientId);
  /**
   * 获取用户信息
   *
   * @param userId 用户ID
   * @return 用户信息
   */
  @Nullable
  OAuth2UserInfo getUserInfo(Long userId);
  /**
   * 处理令牌请求
   *
   * @param request 令牌请求
   * @return 令牌响应
   */
  OAuth2TokenResponse processTokenRequest(OAuth2TokenRequest request);
}
