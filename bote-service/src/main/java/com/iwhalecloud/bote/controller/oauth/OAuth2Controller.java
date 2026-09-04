package com.iwhalecloud.bote.controller.oauth;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.Oauth2GrantTypeEnum;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.ServletUtil;
import com.iwhalecloud.bote.dto.oauth.OAuth2TokenRequest;
import com.iwhalecloud.bote.dto.oauth.OAuth2TokenResponse;
import com.iwhalecloud.bote.dto.oauth.OAuth2UserInfo;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.service.oauth.OAuth2Service;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OAuth2控制器
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "oauth2", produces = MediaType.APPLICATION_JSON_VALUE)
@IgnoreSession
@IgnoreSign
@RequiredArgsConstructor
@ConditionalOnBooleanProperty("bote.oauth2.enabled")
@Tag(name = "OAuth2：认证授权")
public class OAuth2Controller {
  private static final Logger logger = LoggerFactory.getLogger(OAuth2Controller.class);
  private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
  private final OAuth2Service oauth2Service;
  private final IAuthProvider authProvider;

  @IgnoreSession
  @IgnoreSign
  @GetMapping("callback")
  @Operation(summary = "授权成功后，在此处理重定向")
  public void callback(@RequestParam("systemCode") String systemCode, @RequestParam(value = "redirect", defaultValue = "/") String redirect,
    HttpServletResponse response) throws IOException {
    Assert.hasText(systemCode, "systemCode 参数不能为空");
    // 重定向地址，拼接请求参数
    Map<String, String> params = ServletUtil.getParametersAsMap();
    List<String> queryPath = new ArrayList<>();
    for (Entry<String, String> entry : params.entrySet()) {
      if (!"redirect".equals(entry.getKey())) {
        queryPath.add(entry.getKey() + "=" + entry.getValue());
      }
    }
    redirect = redirect + "?" + StringUtils.join(queryPath, "&");
    // 重定向到指定页面
    String redirectUrl = URLDecoder.decode(redirect, StandardCharsets.UTF_8);
    response.sendRedirect(redirectUrl);
  }

  /**
   * 授权端点 - 用户授权
   */
  @GetMapping("/authorize")
  @Operation(
    summary = "OAuth2授权端点",
    description = "OAuth2授权码流程的授权端点，用于获取用户授权并生成授权码。用户需要先登录博特系统，然后通过此端点获取授权码。"
  )
  @Parameters({
    @Parameter(name = "response_type", description = "响应类型，必须为'code'", required = true),
    @Parameter(name = "client_id", description = "客户端ID，用于标识OAuth2客户端", required = true),
    @Parameter(name = "redirect_uri", description = "授权成功后的重定向URI", required = true),
    @Parameter(name = "scope", description = "请求的权限范围，可选"),
    @Parameter(name = "state", description = "状态参数，用于防止CSRF攻击，可选")
  })
  @SuppressFBWarnings("UNVALIDATED_REDIRECT")
  public void authorize(HttpServletRequest request, HttpServletResponse response,
                        @RequestParam("response_type") String responseType,
                        @RequestParam("client_id") String clientId,
                        @RequestParam("redirect_uri") String redirectUri,
                        @RequestParam(name = "scope", required = false) String scope,
                        @RequestParam(name = "state", required = false) String state) throws IOException {
    if (StringUtils.isEmpty(redirectUri)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "redirect_uri参数不能为空");
      return;
    }
    // 验证参数
    if (!"code".equals(responseType)) {
      String errorUrl = buildErrorUrl(redirectUri, state);
      if (HttpUtil.isValid(errorUrl)) {
        response.sendRedirect(errorUrl);
      }
      else {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不支持的响应类型");
      }
      return;
    }
    // 验证重定向URI的安全性
    if (!HttpUtil.isValid(redirectUri)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的重定向URI");
      return;
    }
    // 检查用户是否已登录
    LoginInfo loginInfo = authProvider.getLoginInfo(request);
    if (loginInfo == null) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "用户未登录");
      return;
    }
    // 生成授权码
    String authCode = oauth2Service.generateAuthorizationCode(clientId, redirectUri, scope, state, loginInfo.getUserId());
    // 构建重定向URL - 使用安全的URL构建方法
    response.sendRedirect(buildSecureRedirectUrl(redirectUri, authCode, state));
  }

  /**
   * 令牌端点 - 获取访问令牌
   */
  @PostMapping("/token")
  @Operation(
    summary = "OAuth2令牌端点",
    description = "OAuth2令牌端点，用于获取访问令牌。支持授权码模式、刷新令牌模式、客户端凭证模式"
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "成功获取令牌",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = OAuth2TokenResponse.class))
    ),
    @ApiResponse(responseCode = "400", description = "请求参数错误或授权码无效"),
    @ApiResponse(responseCode = "401", description = "客户端认证失败"),
    @ApiResponse(responseCode = "415", description = "不支持的媒体类型")
  })
  public OAuth2TokenResponse token(HttpServletRequest request) {
    // 构建令牌请求对象
    OAuth2TokenRequest tokenRequest = buildTokenRequest(request);
    // 处理令牌请求
    return oauth2Service.processTokenRequest(tokenRequest);
  }


  /**
   * 用户信息端点 - 获取用户信息
   * 用于获取当前登录用户的详细信息，包括用户ID、姓名、邮箱、角色等。
   * 客户端需要在Authorization头中提供有效的访问令牌。
   * 返回的用户信息可用于第三方应用的权限控制和用户管理。
   */
  @GetMapping("/userinfo")
  @Operation(
    summary = "OAuth2用户信息端点",
    description = "获取当前登录用户的详细信息，包括用户基本信息、角色、租户信息等。需要有效的访问令牌。"
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "成功获取用户信息",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = OAuth2UserInfo.class))
    ),
    @ApiResponse(responseCode = "401", description = "访问令牌无效或缺失"),
    @ApiResponse(responseCode = "403", description = "访问令牌已过期")
  })
  public OAuth2UserInfo userinfo(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (StringUtils.isEmpty(authorization) || !authorization.startsWith("Bearer ")) {
      throw new BssException("无效的Authorization头");
    }
    // 安全地提取访问令牌
    String accessToken = authorization.substring(7);
    if (StringUtils.isEmpty(accessToken)) {
      throw new BssException("访问令牌不能为空");
    }
    // 验证访问令牌格式
    if (StringUtils.countMatches(accessToken, '.') != 2) {
      throw new BssException("无效的访问令牌格式");
    }
    Long userId = oauth2Service.validateAccessToken(accessToken);
    if (userId == null) {
      throw new BssException("无效的访问令牌");
    }
    return oauth2Service.getUserInfo(userId);
  }

  /**
   * 构建令牌请求对象
   *
   * @param request HTTP请求对象
   * @return 构建的OAuth2令牌请求对象
   */
  private OAuth2TokenRequest buildTokenRequest(HttpServletRequest request) {
    OAuth2TokenRequest tokenRequest = new OAuth2TokenRequest();
    // 设置基本参数
    setBasicParameters(tokenRequest, request);
    // 设置客户端认证信息
    setClientCredentials(tokenRequest, request);
    return tokenRequest;
  }

  /**
   * 设置令牌请求的基本参数
   *
   * @param tokenRequest 令牌请求对象
   * @param request HTTP请求对象
   */
  private void setBasicParameters(OAuth2TokenRequest tokenRequest, HttpServletRequest request) {
    String grantType = request.getParameter("grant_type");
    String code = request.getParameter("code");
    String redirectUri = request.getParameter("redirect_uri");
    String refreshToken = request.getParameter("refresh_token");
    String scope = request.getParameter("scope");
    // 验证授权类型安全性
    if (StringUtils.isNotEmpty(grantType) && !isValidGrantType(grantType)) {
      throw new BssException("不支持的授权类型: " + grantType);
    }
    tokenRequest.setGrantType(grantType);
    tokenRequest.setCode(code);
    tokenRequest.setRedirectUri(redirectUri);
    tokenRequest.setRefreshToken(refreshToken);
    tokenRequest.setScope(scope);
  }

  /**
   * 设置客户端认证信息
   *
   * @param tokenRequest 令牌请求对象
   * @param request HTTP请求对象
   */
  private void setClientCredentials(OAuth2TokenRequest tokenRequest, HttpServletRequest request) {
    // 优先从Authorization头获取
    if (!extractCredentialsFromHeader(tokenRequest, request)) {
      // 如果Authorization头中没有，从请求参数获取
      extractCredentialsFromParams(tokenRequest, request);
    }
  }

  /**
   * 从Authorization头中提取客户端认证信息
   *
   * @param tokenRequest 令牌请求对象
   * @param request HTTP请求对象
   * @return 是否成功提取
   */
  private boolean extractCredentialsFromHeader(OAuth2TokenRequest tokenRequest, HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (StringUtils.isEmpty(authorization) || !authorization.startsWith("Basic ")) {
      return false;
    }
    try {
      String credentials = authorization.substring(6);
      String decodedCredentials = new String(Base64.getDecoder().decode(credentials), StandardCharsets.UTF_8);
      String[] parts = decodedCredentials.split(":");
      if (parts.length == 2) {
        String clientCode = parts[0];
        String clientSecret = parts[1];
        if (StringUtils.isNotEmpty(clientCode) && !isValidClientCode(clientCode)) {
          throw new BssException("无效的客户端ID");
        }
        tokenRequest.setClientCode(clientCode);
        tokenRequest.setClientSecret(clientSecret);
        return true;
      }
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      logger.error("解析Authorization头失败", e);
      throw new BssException("无效的Authorization头", e);
    }
    return false;
  }

  /**
   * 从请求参数中提取客户端认证信息
   *
   * @param tokenRequest 令牌请求对象
   * @param request HTTP请求对象
   */
  private void extractCredentialsFromParams(OAuth2TokenRequest tokenRequest, HttpServletRequest request) {
    if (StringUtils.isEmpty(tokenRequest.getClientCode())) {
      String clientId = request.getParameter("client_id");
      if (StringUtils.isNotEmpty(clientId) && !isValidClientCode(clientId)) {
        throw new BssException("无效的客户端ID");
      }
      tokenRequest.setClientCode(clientId);
    }
    if (StringUtils.isEmpty(tokenRequest.getClientSecret())) {
      String clientSecret = request.getParameter("client_secret");
      tokenRequest.setClientSecret(clientSecret);
    }
  }

  /**
   * 验证授权类型的安全性
   * <p>
   * 检查授权类型是否为允许的类型，防止恶意请求。
   *
   * @param grantType 要验证的授权类型
   * @return 如果授权类型安全则返回true，否则返回false
   */
  private boolean isValidGrantType(String grantType) {
    // 只允许特定的授权类型
    Oauth2GrantTypeEnum grantTypeEnum = Oauth2GrantTypeEnum.getByCode(grantType);
    return grantTypeEnum != null;
  }

  /**
   * 验证客户端ID的安全性
   *
   * @param clientId 要验证的客户端ID
   * @return 如果客户端ID安全则返回true，否则返回false
   */
  private boolean isValidClientCode(String clientId) {
    // 检查客户端ID格式：只允许字母、数字、连字符和下划线
    return CLIENT_ID_PATTERN.matcher(clientId).matches();
  }

  /**
   * 构建安全的重定向URL
   *
   * @param baseUri 基础URI
   * @param authCode 授权码
   * @param state 状态参数
   * @return 安全构建的重定向URL
   */
  private String buildSecureRedirectUrl(String baseUri, String authCode, String state) {
    try {
      return UriComponentsBuilder.fromUriString(baseUri).queryParam("code", authCode).queryParam("state", state).toUriString();
    }
    catch (Exception e) {
      logger.error("构建重定向URL失败", e);
      throw new BssException("构建重定向URL失败", e);
    }
  }

  /**
   * 构建错误重定向URL
   *
   * @param redirectUri 客户端提供的重定向URI
   * @param state 状态参数（可选）
   * @return 包含错误信息的重定向URL
   */
  private String buildErrorUrl(String redirectUri, String state) {
    try {
      return UriComponentsBuilder.fromUriString(redirectUri).queryParam("error", "unsupported_response_type").queryParam("state", state).toUriString();
    }
    catch (Exception e) {
      logger.error("构建错误URL失败", e);
      // 如果编码失败，返回基础URI
      return redirectUri + "?error=unsupported_response_type";
    }
  }
}
