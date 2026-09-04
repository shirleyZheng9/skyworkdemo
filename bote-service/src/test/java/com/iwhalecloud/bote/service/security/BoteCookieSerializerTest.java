package com.iwhalecloud.bote.service.security;

import com.iwhalecloud.bassc.basiccenter.config.CookiesProperties;
import com.iwhalecloud.bassc.basiccenter.config.SecurityProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.session.web.http.CookieSerializer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BoteCookieSerializer 单元测试。
 *
 * <p>验证：固定 path（不再随 basePath 动态切换）、重复 BOTE_SESSION 自愈清理、
 * 清理 path 集合构建（含排除固定 path）、删除头属性拼接。
 *
 * @author zhaolei2
 * @since 2026-07-08
 */
class BoteCookieSerializerTest {

  private static final String COOKIE_NAME = "BOTE_SESSION";

  private SecurityProperties securityProperties(boolean secure) {
    SecurityProperties sp = new SecurityProperties();
    sp.setCookieSecure(secure);
    return sp;
  }

  private CookiesProperties cookiesProperties(String name, String path, String domain, String sameSite) {
    CookiesProperties cp = new CookiesProperties();
    cp.getCookie().setName(name);
    cp.getCookie().setPath(path);
    cp.getCookie().setDomain(domain);
    cp.getCookie().setSameSite(sameSite);
    return cp;
  }

  private BoteCookieSerializer newSerializer(String path, List<String> cleanupPaths) {
    return new BoteCookieSerializer(securityProperties(false),
        cookiesProperties(COOKIE_NAME, path, null, null), cleanupPaths);
  }

  private MockHttpServletRequest request(String cookieHeader, String basePath) {
    MockHttpServletRequest req = new MockHttpServletRequest();
    if (cookieHeader != null) {
      req.addHeader("Cookie", cookieHeader);
    }
    if (basePath != null) {
      req.addParameter("basePath", basePath);
    }
    return req;
  }

  private MockHttpServletResponse writeAndGetResponse(BoteCookieSerializer serializer,
      MockHttpServletRequest req) {
    MockHttpServletResponse res = new MockHttpServletResponse();
    serializer.writeCookieValue(new CookieSerializer.CookieValue(req, res, "session-value"));
    return res;
  }

  /** 精确判断 Set-Cookie 头中是否存在 Path=<path> 段（避免 "/" 误匹配 "/bote"）。 */
  private boolean containsPathSegment(String header, String path) {
    for (String part : header.split(";")) {
      if (part.trim().equals("Path=" + path)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasHeaderWithPath(MockHttpServletResponse res, String path) {
    return res.getHeaders("Set-Cookie").stream().anyMatch(h -> containsPathSegment(h, path));
  }

  private boolean hasDeleteHeaderWithPath(MockHttpServletResponse res, String path) {
    return res.getHeaders("Set-Cookie").stream()
        .anyMatch(h -> h.contains("Max-Age=0") && containsPathSegment(h, path));
  }

  @Test
  void writeCookieIgnoresBasePathAndUsesFixedPath() {
    BoteCookieSerializer serializer = newSerializer("/", List.of("/bote"));
    MockHttpServletResponse res = writeAndGetResponse(serializer, request(null, "/app"));

    // 核心 bug 回归：原实现 setCookiePath(basePath) 会写出 Path=/app；固定 path 后不应出现
    assertFalse(hasHeaderWithPath(res, "/app"),
        "写入 path 不应取自 basePath，但出现 Path=/app: " + res.getHeaders("Set-Cookie"));
  }

  @Test
  void emitsCleanupHeadersForDuplicateCookies() {
    BoteCookieSerializer serializer = newSerializer("/", List.of("/bote"));
    // 两个同名 BOTE_SESSION（历史残留不同 path）
    String cookieHeader = COOKIE_NAME + "=AAA; " + COOKIE_NAME + "=BBB";
    MockHttpServletResponse res = writeAndGetResponse(serializer, request(cookieHeader, "/app"));

    assertTrue(hasDeleteHeaderWithPath(res, "/bote"),
        "应清理历史 path /bote: " + res.getHeaders("Set-Cookie"));
    assertTrue(hasDeleteHeaderWithPath(res, "/app"),
        "应清理 basePath /app: " + res.getHeaders("Set-Cookie"));
    assertFalse(hasDeleteHeaderWithPath(res, "/"),
        "不应清理固定 path /: " + res.getHeaders("Set-Cookie"));
  }

  @Test
  void noCleanupHeadersWithoutDuplicate() {
    BoteCookieSerializer serializer = newSerializer("/", List.of("/bote"));
    MockHttpServletResponse res = writeAndGetResponse(serializer,
        request(COOKIE_NAME + "=AAA", "/app"));

    assertFalse(hasDeleteHeaderWithPath(res, "/bote"),
        "无重复 cookie 时不应下发清理头: " + res.getHeaders("Set-Cookie"));
  }

  @Test
  void cleanupHeadersCarrySerializerAttributes() {
    SecurityProperties sp = securityProperties(true);
    CookiesProperties cp = cookiesProperties(COOKIE_NAME, "/", "example.com", "Lax");
    BoteCookieSerializer serializer = new BoteCookieSerializer(sp, cp, List.of("/bote"));

    String cookieHeader = COOKIE_NAME + "=AAA; " + COOKIE_NAME + "=BBB";
    MockHttpServletResponse res = writeAndGetResponse(serializer, request(cookieHeader, null));

    String deleteHeader = res.getHeaders("Set-Cookie").stream()
        .filter(h -> h.contains("Max-Age=0") && containsPathSegment(h, "/bote"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("未找到 /bote 删除头: " + res.getHeaders("Set-Cookie")));
    assertTrue(deleteHeader.contains("Domain=example.com"), "删除头应含 Domain: " + deleteHeader);
    assertTrue(deleteHeader.contains("Secure"), "删除头应含 Secure: " + deleteHeader);
    assertTrue(deleteHeader.contains("SameSite=Lax"), "删除头应含 SameSite: " + deleteHeader);
  }

  @Test
  void cleanupPathsExcludesFixedPath() {
    // cleanupPaths 显式含固定 path "/"，应被排除不清理
    BoteCookieSerializer serializer = newSerializer("/", List.of("/bote", "/"));
    String cookieHeader = COOKIE_NAME + "=AAA; " + COOKIE_NAME + "=BBB";
    MockHttpServletResponse res = writeAndGetResponse(serializer, request(cookieHeader, null));

    assertTrue(hasDeleteHeaderWithPath(res, "/bote"),
        "应清理 /bote: " + res.getHeaders("Set-Cookie"));
    assertFalse(hasDeleteHeaderWithPath(res, "/"),
        "固定 path / 不应被清理: " + res.getHeaders("Set-Cookie"));
  }
}
