package com.iwhalecloud.bote.portal.adapter;

import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.config.properties.TGPortalProperties;
import com.iwhalecloud.bote.portal.tg.TGAuthService;
import com.iwhalecloud.bote.portal.tg.TGUserInfo;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import jakarta.servlet.http.Cookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TGPortal 登录信息映射测试。
 *
 * @author Codex
 * @since 2026-09-07
 */
class TGPortalAuthProviderTest {

  private final StubTGAuthService authService = new StubTGAuthService();

  @Test
  void mapsSharedCookieContextToLoginInfo() {
    TGPortalAuthProvider provider = provider(true);
    authService.result = validUser().build();
    MockHttpServletRequest request = requestWithTokenAndState("oidc-token", "3001", "智能体项目", "model, agent");

    assertEquals(64, provider.getSessionId(request).length());
    LoginInfo loginInfo = provider.getLoginInfo(request);

    assertEquals("1001", loginInfo.getExtUserId());
    assertEquals("1001", loginInfo.getUserName());
    assertEquals("天工用户", loginInfo.getRealName());
    assertEquals("13800000000", loginInfo.getPhoneNo());
    assertNull(loginInfo.getEmail());
    assertEquals(3001L, loginInfo.getExtTenantId());
    assertEquals("auto3001", loginInfo.getTenantCode());
    assertEquals("智能体项目", loginInfo.getTenantName());
    assertEquals("2001", loginInfo.getAttributes().get("extSpaceId"));
    assertEquals("研发团队", loginInfo.getAttributes().get("spaceName"));
    assertTrue((Boolean) loginInfo.getAttributes().get("autoCreateTenant"));
    assertEquals(1, authService.validateCount);
    assertEquals(1, authService.userInfoCount);
  }

  @Test
  void blankTargetPlatformsKeepsBackwardCompatibleAutoCreation() {
    TGPortalAuthProvider provider = provider(true);
    authService.result = validUser().build();

    LoginInfo loginInfo = provider.getLoginInfo(requestWithTokenAndState("token", "3001", "智能体项目", " "));

    assertTrue((Boolean) loginInfo.getAttributes().get("autoCreateTenant"));
    assertEquals(3001L, loginInfo.getExtTenantId());
  }

  @Test
  void targetPlatformsWithoutAgentDoesNotRequireProject() {
    TGPortalAuthProvider provider = provider(true);
    authService.result = validUser().build();

    LoginInfo loginInfo = provider.getLoginInfo(
      requestWithTokenAndState("token", null, null, "model,knowledge"));

    assertFalse((Boolean) loginInfo.getAttributes().get("autoCreateTenant"));
    assertNull(loginInfo.getExtTenantId());
    assertNull(loginInfo.getTenantCode());
  }

  @Test
  void missingProjectContextKeepsLegacyNoTenantBehavior() {
    TGPortalAuthProvider provider = provider(true);
    authService.result = validUser().build();

    LoginInfo loginInfo = provider.getLoginInfo(requestWithToken("token"));

    assertTrue((Boolean) loginInfo.getAttributes().get("autoCreateTenant"));
    assertNull(loginInfo.getExtTenantId());
    assertNull(loginInfo.getTenantCode());
    assertNull(loginInfo.getTenantName());
  }

  @Test
  void fallsBackToExternalUserIdWhenUserNameIsUnavailable() {
    TGPortalAuthProvider provider = provider(true);
    authService.result = validUser().nickname(null).username(null).build();

    LoginInfo loginInfo = provider.getLoginInfo(
      requestWithTokenAndState("token", "3001", "智能体项目", "agent"));

    assertEquals("1001", loginInfo.getRealName());
  }

  @Test
  void invalidTokenIsTreatedAsMissingExternalSession() {
    TGPortalAuthProvider provider = provider(true);
    authService.result = null;

    assertNull(provider.getSessionId(requestWithToken("invalid-token")));
  }

  @Test
  void rejectsMissingCurrentTenant() {
    TGPortalAuthProvider provider = provider(true);
    authService.result = validUser().currentTenantId(0L).build();

    assertThrows(BssException.class, () -> provider.getLoginInfo(
      requestWithTokenAndState("token", "3001", "智能体项目", "agent")));
  }

  @Test
  void rejectsNonNumericProjectWhenAutoCreationIsEnabled() {
    TGPortalAuthProvider provider = provider(true);
    authService.result = validUser().build();

    assertThrows(BssException.class, () -> provider.getLoginInfo(
      requestWithTokenAndState("token", "not-a-number", "智能体项目", "agent")));
  }

  @Test
  void externalSessionFingerprintDoesNotExposeTokenAndChangesWithProject() {
    TGPortalAuthProvider provider = provider(true);
    authService.result = validUser().build();
    MockHttpServletRequest firstProject = requestWithTokenAndState("secret-token", "3001", "项目一", "agent");
    MockHttpServletRequest secondProject = requestWithTokenAndState("secret-token", "3002", "项目二", "agent");

    String firstFingerprint = provider.getSessionId(firstProject);
    String secondFingerprint = provider.getSessionId(secondProject);

    assertEquals(64, firstFingerprint.length());
    assertFalse(firstFingerprint.contains("secret-token"));
    assertNotEquals(firstFingerprint, secondFingerprint);
  }

  @Test
  void rejectsMalformedState() {
    TGPortalAuthProvider provider = provider(true);
    authService.result = validUser().build();
    MockHttpServletRequest request = requestWithToken("token");
    request.setParameter("state", "not-base64");

    assertThrows(BssException.class, () -> provider.getLoginInfo(request));
  }

  private TGPortalAuthProvider provider(boolean autoCreateTenant) {
    TGPortalProperties properties = new TGPortalProperties();
    properties.setCookieName("TIANGONG_TOKEN");
    properties.setLoginUrl("https://tiangong.example/login");
    properties.setDefaultTenantId(0L);
    properties.setDefaultRole("EDIT");
    properties.setAutoCreateTenant(autoCreateTenant);
    return new TGPortalAuthProvider(properties, authService);
  }

  private MockHttpServletRequest requestWithToken(String token) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("TIANGONG_TOKEN", token));
    return request;
  }

  private MockHttpServletRequest requestWithTokenAndState(String token, String projectId,
                                                          String projectName, String targetPlatforms) {
    MockHttpServletRequest request = requestWithToken(token);
    request.setParameter("state", state(projectId, projectName, targetPlatforms));
    return request;
  }

  private String state(String projectId, String projectName, String targetPlatforms) {
    String json = "{\"meta\":{\"projectId\":" + jsonString(projectId)
      + ",\"projectName\":" + jsonString(projectName) + "},\"payload\":{\"target_platforms\":"
      + jsonString(targetPlatforms) + "}}";
    String encodedJson = URLEncoder.encode(json, StandardCharsets.UTF_8);
    String base64 = Base64.getEncoder().encodeToString(encodedJson.getBytes(StandardCharsets.UTF_8));
    return URLEncoder.encode(base64, StandardCharsets.UTF_8);
  }

  private String jsonString(String value) {
    return value == null ? "null" : "\"" + value + "\"";
  }

  private TGUserInfo.Builder validUser() {
    return TGUserInfo.builder()
      .userId(1001L)
      .username("tg-user")
      .nickname("天工用户")
      .phoneNo("13800000000")
      .currentTenantId(2001L)
      .currentTenantName("研发团队")
      .roleType("member");
  }

  private static final class StubTGAuthService implements TGAuthService {
    private TGUserInfo result;
    private int validateCount;
    private int userInfoCount;

    @Override
    public void assertPortalCookieName(String portalCookieName) {
      // 单元测试只覆盖 Provider 映射逻辑；Cookie 一致性由真实 OIDC 适配层负责。
    }

    @Override
    public boolean validateLogin(jakarta.servlet.http.HttpServletRequest request) {
      validateCount++;
      return result != null;
    }

    @Override
    public TGUserInfo getUserInfo(String token) {
      userInfoCount++;
      return result;
    }
  }
}
