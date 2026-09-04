package com.iwhalecloud.bote.service.oauth.impl;

import com.iwhalecloud.bote.cache.OAuth2Cache;
import com.iwhalecloud.bote.cache.OAuth2ClientCache;
import com.iwhalecloud.bote.common.consts.Oauth2GrantTypeEnum;
import com.iwhalecloud.bote.config.properties.OAuth2Properties;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientDTO;
import com.iwhalecloud.bote.dto.oauth.OAuth2TokenRequest;
import com.iwhalecloud.bote.dto.oauth.OAuth2TokenResponse;
import com.iwhalecloud.bote.dto.oauth.OAuth2UserInfo;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.service.oauth.IOAuth2ClientManageService;
import com.iwhalecloud.bote.service.oauth.OAuth2Service;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * OAuth2服务实现类
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {
  private static final Logger logger = LoggerFactory.getLogger(OAuth2ServiceImpl.class);
  private final OAuth2Properties oauth2Properties;
  private final OAuth2Cache oauth2Cache;
  private final OAuth2ClientCache oauth2ClientCache;
  private final UserManageMapper userManageMapper;
  private final IOAuth2ClientManageService oauth2ClientManageService;

  @Override
  public String generateAuthorizationCode(String clientId, String redirectUri, String scope, String state, Long userId) {
    // 验证客户端
    OAuth2ClientDTO client = validateClient(clientId, redirectUri);
    // 生成授权码
    String authCode = UUID.randomUUID().toString();
    // 存储授权码信息
    AuthorizationCodeInfo codeInfo = new AuthorizationCodeInfo();
    codeInfo.setClientId(clientId);
    codeInfo.setRedirectUri(redirectUri);
    codeInfo.setScope(scope);
    codeInfo.setState(state);
    codeInfo.setUserId(userId);
    codeInfo.setExpireTime(Instant.now().plusSeconds(client.getAuthCodeExpireSeconds() != null ? client.getAuthCodeExpireSeconds() : oauth2Properties.getAuthCodeExpireSeconds()));
    oauth2Cache.saveAuthorizationCode(authCode, JsonUtil.toJsonString(codeInfo),
      client.getAuthCodeExpireSeconds() != null ? client.getAuthCodeExpireSeconds() : oauth2Properties.getAuthCodeExpireSeconds());
    return authCode;
  }

  @Override
  @Nullable
  public Long validateAuthorizationCode(String code, String clientId, String redirectUri) {
    String codeInfoJson = oauth2Cache.getAuthorizationCode(code);
    if (StringUtils.isEmpty(codeInfoJson)) {
      return null;
    }
    try {
      AuthorizationCodeInfo codeInfo = JsonUtil.parseJsonRequired(codeInfoJson, AuthorizationCodeInfo.class);
      // 验证客户端
      if (!clientId.equals(codeInfo.getClientId())) {
        return null;
      }
      // 验证重定向URI
      if (!redirectUri.equals(codeInfo.getRedirectUri())) {
        return null;
      }
      // 验证是否过期
      if (Instant.now().isAfter(codeInfo.getExpireTime())) {
        oauth2Cache.deleteAuthorizationCode(code);
        return null;
      }
      // 删除已使用的授权码
      oauth2Cache.deleteAuthorizationCode(code);
      return codeInfo.getUserId();
    }
    catch (Exception e) {
      logger.error("解析授权码信息失败", e);
      return null;
    }
  }

  @Override
  public String generateAccessToken(Long userId, String clientId, String scope) {
    SecretKey key = Keys.hmacShaKeyFor(oauth2Properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    // 获取客户端配置（使用缓存）
    OAuth2ClientDTO client = oauth2ClientCache.get(clientId);
    Assert.notNull(client, "无效的客户端");
    int expireSeconds = client.getAccessTokenExpireSeconds() != null ? client.getAccessTokenExpireSeconds() : oauth2Properties.getAccessTokenExpireSeconds();
    String token = Jwts.builder()
      .claim("userId", userId)
      .claim("clientId", clientId)
      .claim("scope", scope)
      .claim("type", "access_token")
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + expireSeconds * 1000L))
      .signWith(key)
      .compact();
    // 缓存访问令牌
    oauth2Cache.saveAccessToken(token, String.valueOf(userId), expireSeconds);
    return token;
  }

  @Override
  public String generateSystemAccessToken(String clientId, String scope) {
    // 系统级token不需要userId
    return generateAccessToken(0L, clientId, scope);
  }

  @Override
  public String generateRefreshToken(Long userId, String clientId) {
    String refreshToken = UUID.randomUUID().toString();
    // 获取客户端配置（使用缓存）
    OAuth2ClientDTO client = oauth2ClientCache.get(clientId);
    Assert.notNull(client, "无效的客户端");
    int expireSeconds = client.getRefreshTokenExpireSeconds() != null ? client.getRefreshTokenExpireSeconds() : oauth2Properties.getRefreshTokenExpireSeconds();
    RefreshTokenInfo tokenInfo = new RefreshTokenInfo();
    tokenInfo.setUserId(userId);
    tokenInfo.setClientId(clientId);
    tokenInfo.setExpireTime(Instant.now().plusSeconds(expireSeconds));
    oauth2Cache.saveRefreshToken(refreshToken, JsonUtil.toJsonString(tokenInfo), expireSeconds);
    return refreshToken;
  }

  @Override
  @Nullable
  public Long validateAccessToken(String accessToken) {
    if (StringUtils.isEmpty(accessToken)) {
      return null;
    }
    try {
      SecretKey key = Keys.hmacShaKeyFor(oauth2Properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(accessToken)
        .getPayload();
      // 验证令牌类型
      if (!"access_token".equals(claims.get("type"))) {
        return null;
      }
      Long userId = claims.get("userId", Long.class);
      // 检查缓存中的令牌是否还存在（用于撤销功能）
      String cachedValue = oauth2Cache.getAccessToken(accessToken);
      if (cachedValue == null) {
        return null;
      }
      // 系统级token（userId为null）缓存值为空字符串，用户级token缓存值为userId字符串
      if (userId == null) {
        // 系统级token，验证缓存值是否为空字符串
        return StringUtils.isEmpty(cachedValue) ? 0L : null; // 返回0L表示系统级token
      }
      else {
        // 用户级token，验证缓存值是否匹配
        if (!String.valueOf(userId).equals(cachedValue)) {
          return null;
        }
      }
      return userId;
    }
    catch (Exception e) {
      logger.error("验证访问令牌失败", e);
      return null;
    }
  }

  @Override
  @Nullable
  public String validateAccessTokenAndGetClientId(String accessToken) {
    if (StringUtils.isEmpty(accessToken)) {
      return null;
    }
    try {
      SecretKey key = Keys.hmacShaKeyFor(oauth2Properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
      Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(accessToken)
        .getPayload();
      // 验证令牌类型
      if (!"access_token".equals(claims.get("type"))) {
        return null;
      }
      // 检查缓存中的令牌是否还存在（用于撤销功能）
      String cachedValue = oauth2Cache.getAccessToken(accessToken);
      if (cachedValue == null) {
        return null;
      }
      return claims.get("clientId", String.class);
    }
    catch (Exception e) {
      logger.error("验证访问令牌失败", e);
      return null;
    }
  }

  @Override
  @Nullable
  public Long validateRefreshToken(String refreshToken, String clientId) {
    String tokenInfoJson = oauth2Cache.getRefreshToken(refreshToken);
    if (StringUtils.isEmpty(tokenInfoJson)) {
      return null;
    }
    try {
      RefreshTokenInfo tokenInfo = JsonUtil.parseJsonRequired(tokenInfoJson, RefreshTokenInfo.class);
      // 验证客户端
      if (!clientId.equals(tokenInfo.getClientId())) {
        return null;
      }
      // 验证是否过期
      if (Instant.now().isAfter(tokenInfo.getExpireTime())) {
        oauth2Cache.deleteRefreshToken(refreshToken);
        return null;
      }
      return tokenInfo.getUserId();
    }
    catch (Exception e) {
      logger.error("解析刷新令牌信息失败", e);
      return null;
    }
  }

  @Override
  @Nullable
  public OAuth2UserInfo getUserInfo(Long userId) {
    UserEntity user = userManageMapper.getUser(userId);
    if (user == null) {
      return null;
    }
    OAuth2UserInfo userInfo = new OAuth2UserInfo();
    userInfo.setSub(String.valueOf(user.getUserId()));
    userInfo.setName(user.getRealName());
    userInfo.setPreferredUsername(user.getUserName());
    userInfo.setEmail(user.getEmail());
    userInfo.setEmailVerified(true);
    // 设置角色 - 确保返回Grafana期望的格式
    if ("10".equals(user.getUserType())) {
      userInfo.setRoles(new String[]{"admin"});
    }
    else {
      userInfo.setRoles(new String[]{"viewer"});
    }
    // 设置租户信息
    if (user.getDefaultTenantId() != null) {
      userInfo.setTenantId(String.valueOf(user.getDefaultTenantId()));
      // 这里可以根据需要查询租户名称
      userInfo.setTenantName("默认租户");
    }
    return userInfo;
  }

  @Override
  public OAuth2TokenResponse processTokenRequest(OAuth2TokenRequest request) {
    OAuth2TokenResponse response = new OAuth2TokenResponse();
    // 验证客户端（使用缓存）
    OAuth2ClientDTO client = oauth2ClientManageService.findOAuth2ClientByCodeAndSecret(request.getClientCode(), request.getClientSecret());
    if (client == null) {
      throw new BssException("客户端认证失败");
    }
    // 验证授权类型
    if (!client.isSupportedGrantType(request.getGrantType())) {
      throw new BssException("不支持的授权类型: " + request.getGrantType());
    }
    if (Oauth2GrantTypeEnum.AUTHORIZATION_CODE.getCode().equals(request.getGrantType())) {
      processForAuthorizationCode(request, client, response);
    }
    else if (Oauth2GrantTypeEnum.REFRESH_TOKEN.getCode().equals(request.getGrantType())) {
      processForRefreshToken(request, response, client);
    }
    else if (Oauth2GrantTypeEnum.CLIENT_CREDENTIALS.getCode().equals(request.getGrantType())) {
      processForClientCredentials(request, client, response);
    }
    else {
      throw new BssException("不支持的授权类型: " + request.getGrantType());
    }
    return response;
  }

  private void processForClientCredentials(OAuth2TokenRequest request, OAuth2ClientDTO client, OAuth2TokenResponse response) {
    // 客户端凭证模式（系统间调用）
    // 验证权限范围
    if (!client.isSupportedScope(request.getScope())) {
      throw new BssException("不支持的权限范围: " + request.getScope());
    }
    // 生成系统级token（不需要userId）
    response.setAccessToken(generateSystemAccessToken(request.getClientCode(), request.getScope()));
    // 客户端凭证模式不返回refresh_token
    response.setExpiresIn(client.getAccessTokenExpireSeconds() != null ? client.getAccessTokenExpireSeconds() :
      oauth2Properties.getAccessTokenExpireSeconds());
    response.setScope(request.getScope());
  }

  private void processForRefreshToken(OAuth2TokenRequest request, OAuth2TokenResponse response, OAuth2ClientDTO client) {
    // 刷新令牌模式
    Long userId = validateRefreshToken(request.getRefreshToken(), request.getClientCode());
    if (userId == null) {
      throw new BssException("无效的刷新令牌");
    }
    response.setAccessToken(generateAccessToken(userId, request.getClientCode(), "read write"));
    response.setRefreshToken(generateRefreshToken(userId, request.getClientCode()));
    response.setExpiresIn(client.getAccessTokenExpireSeconds() != null ? client.getAccessTokenExpireSeconds() :
      oauth2Properties.getAccessTokenExpireSeconds());
    response.setScope("read write");
  }

  private void processForAuthorizationCode(OAuth2TokenRequest request, OAuth2ClientDTO client, OAuth2TokenResponse response) {
    // 授权码模式
    Long userId = validateAuthorizationCode(request.getCode(), request.getClientCode(), request.getRedirectUri());
    if (userId == null) {
      throw new BssException("无效的授权码");
    }
    // 验证权限范围
    if (!client.isSupportedScope(request.getScope())) {
      throw new BssException("不支持的权限范围: " + request.getScope());
    }
    response.setAccessToken(generateAccessToken(userId, request.getClientCode(), request.getScope()));
    response.setRefreshToken(generateRefreshToken(userId, request.getClientCode()));
    response.setExpiresIn(client.getAccessTokenExpireSeconds() != null ? client.getAccessTokenExpireSeconds() :
      oauth2Properties.getAccessTokenExpireSeconds());
    response.setScope(request.getScope());
  }

  private OAuth2ClientDTO validateClient(String clientId, String redirectUri) {
    // 从缓存查询客户端配置
    OAuth2ClientDTO client = oauth2ClientCache.get(clientId);
    // 验证重定向URI - 使用关联表数据
    Assert.notNull(client, "无效的客户端");
    if (client.getRedirectUris() == null || client.getRedirectUris().isEmpty()) {
      throw new BssException("客户端未配置重定向URI: " + clientId);
    }
    boolean isValidUri = client.getRedirectUris().stream().anyMatch(uri -> {
      String configuredUri = uri.getRedirectUri();
      // 对于包含查询参数的URL，我们需要更灵活的匹配
      if (configuredUri.contains("?")) {
        // 如果配置的URI包含查询参数，进行完整匹配
        return redirectUri.equals(configuredUri);
      }
      else {
        // 如果配置的URI不包含查询参数，检查基础URL是否匹配
        String baseUri = redirectUri.split("\\?")[0];
        return configuredUri.equals(baseUri);
      }
    });
    if (!isValidUri) {
      throw new BssException("无效的重定向URI: " + redirectUri);
    }
    return client;
  }

  /**
   * 授权码信息
   */
  @Getter
  @Setter
  @ToString
  private static final class AuthorizationCodeInfo {
    private String clientId;
    private String redirectUri;
    private String scope;
    private String state;
    private Long userId;
    private Instant expireTime;
  }

  /**
   * 刷新令牌信息
   */
  @Getter
  @Setter
  @ToString
  private static final class RefreshTokenInfo {
    private Long userId;
    private String clientId;
    private Instant expireTime;
  }
}
