package com.iwhalecloud.bote.portal.tg;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;

/**
 * 天工 Token 校验和上下文解析边界。
 *
 * <p>该接口用于隔离 AADP 门户逻辑与 starter 的具体 DTO/API。真实实现必须使用
 * {@code tg-common-oidc-starter}，不得在 AADP 内自行实现 JWT 验签。这样在 starter 升级时，
 * 只需要调整适配实现，不需要修改门户和用户同步代码。</p>
 *
 * @author zhengxueli
 * @since 2026-09-07
 */
public interface TGAuthService {

  /**
   * 校验门户记录中的 Cookie 名称是否与 OIDC starter 实际读取的 Cookie 一致。
   *
   * <p>OIDC starter 会按 {@code tokenPrefix + cookieName} 读取 Cookie；门户记录若配置成
   * 另一个名称，会出现 Provider 能读到 Token、starter 却判定未登录的隐蔽故障。</p>
   *
   * @param portalCookieName {@code bt_external_portal.cookie_name}
   */
  void assertPortalCookieName(String portalCookieName);

  /**
   * 校验当前请求中的天工 Token。
   *
   * <p>传入 request 而不是只传 Token，是为了让 starter 在临期续签后能通过当前
   * response 写回新 Cookie。</p>
   *
   * @param request 当前 Servlet 请求
   * @return Token 有效返回 true
   */
  boolean validateLogin(HttpServletRequest request);

  /**
   * 解析已验证的 Token，并获取用户和当前天工租户信息。
   *
   * @param token 天工共享 Cookie 中的 OIDC Token
   * @return 解析失败或扩展信息不完整时返回 null
   */
  @Nullable
  TGUserInfo getUserInfo(String token);
}
