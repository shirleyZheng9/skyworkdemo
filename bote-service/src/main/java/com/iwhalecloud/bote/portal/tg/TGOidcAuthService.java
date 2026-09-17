package com.iwhalecloud.bote.portal.tg;

import com.sy4cloud.tg.common.oidc.JwtUserInfo;
import com.sy4cloud.tg.common.oidc.OidcProperties;
import com.sy4cloud.tg.common.oidc.TenantExtInfo;
import com.sy4cloud.tg.common.oidc.UserInfo;
import com.sy4cloud.tg.common.oidc.OidcUtil;
import com.sy4cloud.tg.common.oidc.support.AesUtils;
import com.sy4cloud.tg.common.oidc.support.OidcExtInfo;
import com.sy4cloud.tg.common.oidc.support.OidcTokenGenerator;
import com.sy4cloud.tg.common.oidc.support.OidcTokenRenewClient;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 基于天工 {@code tg-common-oidc-starter} 的真实认证适配实现。
 *
 * <p>AADP 不自行解析或验签 JWT。Token 校验、续签和 Cookie 回写均交给 starter；
 * 本类只把 starter 返回的用户/租户 DTO 转换为 AADP 门户中间模型。</p>
 *
 * @author zhengxueli
 * @since 2026-09-10
 */
public class TGOidcAuthService implements TGAuthService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TGOidcAuthService.class);

  /**
   * 直接使用 starter 的单次 extinfo 调用，避免分别调用 fetchCurrentUserInfo/fetchTenants
   * 导致同一登录请求重复访问两次天工。
   */
  private final OidcTokenRenewClient oidcClient;
  private final OidcProperties oidcProperties;

  public TGOidcAuthService(OidcTokenRenewClient oidcClient, OidcProperties oidcProperties) {
    this.oidcClient = oidcClient;
    this.oidcProperties = oidcProperties;
    LOGGER.info("[TGPortal] OIDC 认证服务初始化完成: expectedCookieName={}", expectedCookieName());
  }

  @Override
  public void assertPortalCookieName(String portalCookieName) {
    String expectedCookieName = expectedCookieName();
    if (!StringUtils.equals(portalCookieName, expectedCookieName)) {
      LOGGER.error("[TGPortal] Cookie 配置不一致: portalCookieName={}, expectedCookieName={}",
        portalCookieName, expectedCookieName);
      throw new IllegalStateException("TGPortal Cookie 名称与 security.oidc 配置不一致: portalCookieName="
        + portalCookieName + ", expectedCookieName=" + expectedCookieName);
    }
    LOGGER.info("[TGPortal] Cookie 配置一致性校验通过: cookieName={}", expectedCookieName);
  }

  @Override
  public boolean validateLogin(HttpServletRequest request) {
    if (request == null) {
      LOGGER.warn("[TGPortal] 跳过 OIDC 校验，Servlet 请求为空");
      return false;
    }
    try {
      boolean valid = OidcUtil.validateLogin(request);
      if (valid) {
        LOGGER.info("[TGPortal] OIDC 校验成功: requestUri={}", request.getRequestURI());
      }
      else {
        LOGGER.warn("[TGPortal] OIDC 校验未通过: requestUri={}", request.getRequestURI());
      }
      return valid;
    }
    catch (RuntimeException e) {
      LOGGER.warn("[TGPortal] OIDC 校验异常: requestUri={}", request.getRequestURI(), e);
      throw e;
    }
  }

  @Nullable
  @Override
  public TGUserInfo getUserInfo(String token) {
    //调用 starter 的工具方法，从 Token 中解析出天工用户信息，得到一个 JwtUserInfo
    JwtUserInfo jwtUser = OidcUtil.currentUserFromToken(token);
    Long userId = parseUserId(jwtUser);
    if (userId == null || jwtUser.getCurrentTenantId() == null) {
      LOGGER.warn("[TGPortal] Token 中的用户上下文不完整: userId={}, currentTenantIdPresent={}",
        userId, jwtUser != null && jwtUser.getCurrentTenantId() != null);
      return null;
    }

    try {
      LOGGER.info("[TGPortal] 开始调用 extinfo 查询用户扩展信息: userId={}, tokenTenantId={}",
        userId, jwtUser.getCurrentTenantId());
      OidcExtInfo extInfo = oidcClient.fetchExtInfo(userId);
      if (extInfo == null || extInfo.getCurrentUserInfo() == null) {
        LOGGER.warn("[TGPortal] extinfo 返回的用户扩展信息为空: userId={}", userId);
        return null;
      }
      UserInfo user = extInfo.getCurrentUserInfo();
      if (!Objects.equals(userId, user.getUserId())) {
        LOGGER.warn("[TGPortal] Token 用户与 extinfo 用户不一致: tokenUserId={}, extUserId={}", userId, user.getUserId());
        return null;
      }
      Long currentTenantId = jwtUser.getCurrentTenantId();
      if (user.getCurrentTenantId() != null && !Objects.equals(currentTenantId, user.getCurrentTenantId())) {
        LOGGER.warn("[TGPortal] Token 团队与 extinfo 当前团队不一致: userId={}, tokenTenantId={}, extTenantId={}",
          userId, currentTenantId, user.getCurrentTenantId());
        return null;
      }

      TenantExtInfo currentTenant = findCurrentTenant(extInfo.getTenantInfoList(), currentTenantId);
      if (currentTenant == null || StringUtils.isBlank(currentTenant.getTenantName())) {
        LOGGER.warn("[TGPortal] Token 当前团队不在 extinfo 团队列表中，或团队名称为空: userId={}, tenantId={}",
          userId, currentTenantId);
        return null;
      }

      TGUserInfo userInfo = TGUserInfo.builder()
        .userId(userId)
        // 3.9.3 starter 的 UserInfo 暂无 nickname，Provider 会按映射规则回退到 username。
        .username(decryptNullable(user.getUsername(), "username"))
        .phoneNo(decryptNullable(user.getPhone(), "phone"))
        .currentTenantId(currentTenantId)
        .currentTenantName(currentTenant.getTenantName())
        .roleType(StringUtils.defaultIfBlank(user.getRoleType(), currentTenant.getRoleType()))
        .build();
      LOGGER.info("[TGPortal] extinfo 用户扩展信息映射完成: userId={}, currentTenantId={}, tenantCount={}, "
          + "usernamePresent={}, phonePresent={}, roleType={}",
        userId, currentTenantId, extInfo.getTenantInfoList() == null ? 0 : extInfo.getTenantInfoList().size(),
        StringUtils.isNotBlank(userInfo.getUsername()), StringUtils.isNotBlank(userInfo.getPhoneNo()),
        userInfo.getRoleType());
      return userInfo;
    }
    catch (RuntimeException e) {
      // extinfo 不可用时不创建信息不完整的本地用户，由上层按未登录处理。
      LOGGER.warn("[TGPortal] 获取或处理 extinfo 用户扩展信息失败: userId={}, tokenTenantId={}",
        userId, jwtUser.getCurrentTenantId(), e);
      return null;
    }
  }

  @Nullable
  private Long parseUserId(@Nullable JwtUserInfo jwtUser) {
    if (jwtUser == null || StringUtils.isBlank(jwtUser.getUserId())) {
      LOGGER.warn("[TGPortal] Token 中缺少 userId");
      return null;
    }
    try {
      return Long.valueOf(jwtUser.getUserId());
    }
    catch (NumberFormatException e) {
      LOGGER.warn("[TGPortal] Token 中的 userId 不是有效整数: valueLength={}", jwtUser.getUserId().length());
      return null;
    }
  }

  @Nullable
  private TenantExtInfo findCurrentTenant(@Nullable List<TenantExtInfo> tenants, Long currentTenantId) {
    for (TenantExtInfo tenant : tenants == null ? Collections.<TenantExtInfo>emptyList() : tenants) {
      if (tenant != null && Objects.equals(currentTenantId, tenant.getTenantId())) {
        return tenant;
      }
    }
    return null;
  }

  @Nullable
  private String decryptNullable(@Nullable String encryptedValue, String fieldName) {
    if (StringUtils.isBlank(encryptedValue)) {
      return null;
    }
    try {
      return AesUtils.decrypt(encryptedValue, OidcTokenGenerator.DEFAULT_ISSUER);
    }
    catch (RuntimeException e) {
      // 不把无法解密的密文当成用户名/手机号落库。
      LOGGER.warn("[TGPortal] 用户敏感字段解密失败: field={}", fieldName, e);
      return null;
    }
  }

  private String expectedCookieName() {
    return StringUtils.defaultString(oidcProperties.getTokenPrefix())
      + StringUtils.defaultString(oidcProperties.getCookieName());
  }
}
