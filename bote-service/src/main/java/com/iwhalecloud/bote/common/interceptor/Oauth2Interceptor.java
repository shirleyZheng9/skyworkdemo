package com.iwhalecloud.bote.common.interceptor;

import com.iwhalecloud.bote.common.annotation.RequireOAuth2;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.config.properties.OAuth2Properties;
import com.iwhalecloud.bote.dto.oauth.OAuth2UserInfo;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.service.oauth.OAuth2Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

/**
 * OAuth2认证拦截器
 * <p>
 * 基于OAuth2 accessToken进行接口认证拦截，支持通过{@link RequireOAuth2}注解标识需要认证的接口。
 * </p>
 * <p>
 * 功能特性：
 * <ul>
 *   <li>从Authorization头提取Bearer token</li>
 *   <li>验证accessToken有效性</li>
 *   <li>支持scope权限范围验证</li>
 *   <li>设置用户信息到线程变量</li>
 *   <li>支持可选认证（required=false）</li>
 * </ul>
 * </p>
 *
 * @author Aiqing
 * @since 2025/12/26
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "bote.oauth2.enabled", havingValue = "true")
@SuppressWarnings("PMD.GuardLogStatement")
public class Oauth2Interceptor implements AsyncHandlerInterceptor, InitializingBean {

  private static final Logger logger = LoggerFactory.getLogger(Oauth2Interceptor.class);

  /** 认证失败的响应内容 */
  private static final String FAILED_RESPONSE_TEXT = "{\"resultCode\": \"401\", \"resultMsg\": \"%s\"}";
  /** 权限不足的响应内容 */
  private static final String FORBIDDEN_RESPONSE_TEXT = "{\"resultCode\": \"403\", \"resultMsg\": \"%s\"}";
  /** Bearer token前缀 */
  private static final String BEARER_PREFIX = "Bearer ";
  /** Authorization请求头名称 */
  private static final String AUTHORIZATION_HEADER = "Authorization";

  private final OAuth2Service oauth2Service;
  private final OAuth2Properties oauth2Properties;
  private SecretKey secretKey;

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // 避免处理静态资源请求
    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return true;
    }

    // 检查是否需要OAuth2认证
    RequireOAuth2 annotation = getRequireOAuth2Annotation(handlerMethod);
    if (annotation == null) {
      // 没有注解，跳过OAuth2认证
      return true;
    }

    // 提取accessToken
    String accessToken = extractAccessToken(request);

    // 如果required=false且没有token，允许访问但不设置用户信息
    if (!annotation.required() && StringUtils.isEmpty(accessToken)) {
      return true;
    }

    // 验证token
    if (StringUtils.isEmpty(accessToken)) {
      sendAuthFailed(response, "OAuth2认证失败：缺少访问令牌");
      return false;
    }

    // 验证accessToken有效性
    Long userId = oauth2Service.validateAccessToken(accessToken);
    if (userId == null) {
      logger.warn("OAuth2认证失败：无效的访问令牌, uri={}", request.getRequestURI());
      sendAuthFailed(response, "OAuth2认证失败：无效的访问令牌");
      return false;
    }

    // 验证scope权限范围
    if (annotation.scope().length > 0) {
      if (!validateScope(accessToken, annotation.scope())) {
        String clientId = oauth2Service.validateAccessTokenAndGetClientId(accessToken);
        logger.warn("OAuth2权限不足：缺少必需的权限范围, clientId={}, requiredScopes={}, uri={}",
          clientId, Arrays.toString(annotation.scope()), request.getRequestURI());
        sendForbidden(response, String.format("OAuth2权限不足：缺少必需的权限范围 %s", Arrays.toString(annotation.scope())));
        return false;
      }
    }

    // 判断是系统级token还是用户级token
    boolean isSystemToken = userId == 0L; // 系统级token返回0L

    if (isSystemToken) {
      return autoForSystemToken(request, response, accessToken);
    }
    else {
      return authForUserToken(request, response, userId, accessToken);
    }
  }

  private boolean autoForSystemToken(HttpServletRequest request, HttpServletResponse response, String accessToken) throws IOException {
    // 系统级token（客户端凭证模式）- 不需要用户信息
    String clientId = oauth2Service.validateAccessTokenAndGetClientId(accessToken);
    if (clientId == null) {
      logger.warn("OAuth2认证失败：无法获取客户端信息, uri={}", request.getRequestURI());
      sendAuthFailed(response, "OAuth2认证失败：无法获取客户端信息");
      return false;
    }

    // 创建系统级LoginInfo（使用clientId作为标识）
    LoginInfo loginInfo = LoginInfo.builder()
      .userId(0L) // 系统级token使用0L
      .userName("system_" + clientId)
      .realName("系统调用")
      .token(accessToken)
      .build();

    SessionUtil.setSessionId(accessToken);
    SessionUtil.setLoginInfo(loginInfo);
    logger.trace("OAuth2系统级认证成功：clientId={}, uri={}", clientId, request.getRequestURI());
    return true;
  }

  private boolean authForUserToken(HttpServletRequest request, HttpServletResponse response, Long userId, String accessToken) throws IOException {
    // 用户级token（授权码模式）- 需要用户信息
    OAuth2UserInfo userInfo = oauth2Service.getUserInfo(userId);
    if (userInfo == null) {
      logger.warn("OAuth2认证失败：无法获取用户信息, userId={}, uri={}", userId, request.getRequestURI());
      sendAuthFailed(response, "OAuth2认证失败：无法获取用户信息");
      return false;
    }

    // 转换为LoginInfo并设置到线程变量
    LoginInfo loginInfo = convertToLoginInfo(userInfo, accessToken);
    SessionUtil.setSessionId(accessToken);
    SessionUtil.setLoginInfo(loginInfo);

    // 设置租户ID
    Long tenantId = null;
    if (StringUtils.isNotEmpty(userInfo.getTenantId())) {
      try {
        tenantId = Long.parseLong(userInfo.getTenantId());
      }
      catch (NumberFormatException e) {
        logger.warn("OAuth2认证：租户ID格式错误, tenantId={}, userId={}", userInfo.getTenantId(), userId);
      }
    }
    TenantIdUtil.setTenantId(tenantId);

    logger.debug("OAuth2用户级认证成功：userId={}, userName={}, uri={}", userId, userInfo.getPreferredUsername(), request.getRequestURI());
    return true;
  }

  /**
   * 获取RequireOAuth2注解（方法级别优先于类级别）
   *
   * @param handlerMethod 处理方法
   * @return 注解实例，如果不存在则返回null
   */
  @Nullable
  private RequireOAuth2 getRequireOAuth2Annotation(HandlerMethod handlerMethod) {
    // 方法级别注解优先
    RequireOAuth2 methodAnnotation = handlerMethod.getMethodAnnotation(RequireOAuth2.class);
    if (methodAnnotation != null) {
      return methodAnnotation;
    }
    // 类级别注解
    return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequireOAuth2.class);
  }

  /**
   * 从请求头中提取accessToken
   *
   * @param request HTTP请求
   * @return accessToken，如果不存在则返回null
   */
  @Nullable
  private String extractAccessToken(HttpServletRequest request) {
    String authorization = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.isEmpty(authorization)) {
      return null;
    }

    // 验证Bearer token格式
    if (!authorization.startsWith(BEARER_PREFIX)) {
      return null;
    }

    // 安全地提取token（避免字符串注入）
    String token = authorization.substring(BEARER_PREFIX.length()).trim();
    if (StringUtils.isEmpty(token)) {
      return null;
    }

    return token;
  }

  /**
   * 验证token的scope是否包含所需的权限范围
   *
   * @param accessToken 访问令牌
   * @param requiredScopes 需要的权限范围
   * @return 是否满足权限要求
   */
  private boolean validateScope(String accessToken, String[] requiredScopes) {
    try {
      // 解析JWT token获取scope
      String tokenScope = extractScopeFromToken(accessToken);
      if (StringUtils.isEmpty(tokenScope)) {
        return false;
      }

      // 将scope字符串转换为Set（支持空格分隔的多个scope）
      Set<String> tokenScopes = new HashSet<>(Arrays.asList(tokenScope.split("\\s+")));

      // 验证是否包含所有必需的scope
      for (String requiredScope : requiredScopes) {
        if (!tokenScopes.contains(requiredScope)) {
          return false;
        }
      }

      return true;
    }
    catch (Exception e) {
      logger.error("验证scope权限范围失败", e);
      return false;
    }
  }

  /**
   * 从JWT token中提取scope信息
   *
   * @param accessToken 访问令牌
   * @return scope字符串，如果不存在则返回null
   */
  @Nullable
  private String extractScopeFromToken(String accessToken) {
    try {
      Claims claims = Jwts.parser()
        .verifyWith(this.secretKey)
        .build()
        .parseSignedClaims(accessToken)
        .getPayload();

      Object scopeObj = claims.get("scope");
      if (scopeObj == null) {
        return null;
      }

      return scopeObj.toString();
    }
    catch (Exception e) {
      logger.error("从token中提取scope失败", e);
      return null;
    }
  }

  /**
   * 将OAuth2UserInfo转换为LoginInfo
   *
   * @param userInfo OAuth2用户信息
   * @param token 访问令牌
   * @return LoginInfo对象
   */
  private LoginInfo convertToLoginInfo(OAuth2UserInfo userInfo, String token) {
    LoginInfo loginInfo = new LoginInfo();
    loginInfo.setToken(token);

    // 设置用户ID
    if (StringUtils.isNotEmpty(userInfo.getSub())) {
      try {
        loginInfo.setUserId(Long.parseLong(userInfo.getSub()));
      }
      catch (NumberFormatException e) {
        logger.warn("OAuth2用户ID格式错误: sub={}", userInfo.getSub());
      }
    }

    // 设置用户名
    loginInfo.setUserName(userInfo.getPreferredUsername());
    loginInfo.setRealName(userInfo.getName());
    loginInfo.setEmail(userInfo.getEmail());

    // 设置租户ID
    if (StringUtils.isNotEmpty(userInfo.getTenantId())) {
      try {
        loginInfo.setDefaultTenantId(Long.parseLong(userInfo.getTenantId()));
      }
      catch (NumberFormatException e) {
        logger.warn("OAuth2租户ID格式错误: tenantId={}", userInfo.getTenantId());
      }
    }

    return loginInfo;
  }

  /**
   * 发送认证失败响应
   *
   * @param response HTTP响应
   * @param message 错误消息
   */
  private void sendAuthFailed(HttpServletResponse response, String message) throws IOException {
    response.setStatus(401);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    String escapedMessage = StringEscapeUtils.escapeJson(message);
    response.getWriter().print(String.format(FAILED_RESPONSE_TEXT, escapedMessage));
  }

  /**
   * 发送权限不足响应
   *
   * @param response HTTP响应
   * @param message 错误消息
   */
  private void sendForbidden(HttpServletResponse response, String message) throws IOException {
    response.setStatus(403);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    String escapedMessage = StringEscapeUtils.escapeJson(message);
    response.getWriter().print(String.format(FORBIDDEN_RESPONSE_TEXT, escapedMessage));
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.secretKey = Keys.hmacShaKeyFor(oauth2Properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
  }
}
