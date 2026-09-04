package com.iwhalecloud.bote.service.security;

import com.iwhalecloud.bassc.basiccenter.config.CookiesProperties;
import com.iwhalecloud.bassc.basiccenter.config.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * 自定义 Cookie 序列化器。
 *
 * <p>BOTE_SESSION 始终以构造期确定的固定 path 写入（取自 cookiesProperties.getCookie().getPath()，默认 "/"），
 * 不再按请求参数 basePath 动态切换 path。同名 cookie 不同 path 会在浏览器并存导致混乱，固定 path 可从根本上避免。
 *
 * <p>此外做一次"自愈"：当请求中检测到多个 BOTE_SESSION（历史残留的不同 path cookie）时，
 * 对配置的历史 path（以及当前请求的 basePath）下发删除头，配合固定 path 的正常写入，
 * 让受影响客户端在下次会话写入时收敛为单个 cookie。
 *
 * <p>注意：本类为单例，writeCookieValue 不再修改任何共享可变状态（不再调用 setCookiePath），线程安全。
 *
 * @author tingyun.wang
 * @since 2025-07-25
 */
public final class BoteCookieSerializer extends DefaultCookieSerializer {

  private final String cookieName;
  private final String cookiePath;
  private final String domainName;
  private final boolean secure;
  private final String sameSite;
  private final List<String> cleanupPaths;

  public BoteCookieSerializer(SecurityProperties securityProperties, CookiesProperties cookiesProperties,
      List<String> cleanupPaths) {
    // 保持鉴权模块所有原有的配置
    this.setCookieName(cookiesProperties.getCookie().getName());
    this.setCookieMaxAge(cookiesProperties.getCookie().getMaxAge());
    this.setDomainName(cookiesProperties.getCookie().getDomain());
    if (StringUtils.isNotEmpty(cookiesProperties.getCookie().getDomainNamePattern())) {
      this.setDomainNamePattern(cookiesProperties.getCookie().getDomainNamePattern());
    }
    this.setUseBase64Encoding(false);
    this.setUseSecureCookie(securityProperties.isCookieSecure());
    this.setSameSite(cookiesProperties.getCookie().getSameSite());

    // 固定 path：构造期一次性设置，之后不再变动
    String path = StringUtils.defaultIfEmpty(cookiesProperties.getCookie().getPath(), "/");
    this.setCookiePath(path);

    // 缓存构造删除头所需属性
    this.cookieName = cookiesProperties.getCookie().getName();
    this.cookiePath = path;
    this.domainName = cookiesProperties.getCookie().getDomain();
    this.secure = securityProperties.isCookieSecure();
    this.sameSite = cookiesProperties.getCookie().getSameSite();
    this.cleanupPaths = cleanupPaths == null ? List.of() : cleanupPaths;
  }

  @Override
  public void writeCookieValue(CookieValue cookieValue) {
    // 检测请求中是否存在多个同名 BOTE_SESSION（历史残留），自愈清理其它 path 的副本
    HttpServletRequest request = cookieValue.getRequest();
    Set<String> pathsToDelete = collectCleanupPaths(request);
    if (!pathsToDelete.isEmpty()) {
      HttpServletResponse response = cookieValue.getResponse();
      for (String path : pathsToDelete) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildDeleteHeader(path));
      }
    }
    // 以固定 path 写入当前值（或失效时的删除）
    super.writeCookieValue(cookieValue);
  }

  /**
   * 仅当请求中存在 ≥2 个同名 BOTE_SESSION 时，返回需要清理的历史 path 集合（排除当前固定 path）。
   */
  private Set<String> collectCleanupPaths(HttpServletRequest request) {
    if (StringUtils.isBlank(cookieName) || !hasDuplicateSessionCookies(request)) {
      return Set.of();
    }
    Set<String> paths = new LinkedHashSet<>(cleanupPaths);
    String basePath = request.getParameter("basePath");
    if (StringUtils.isNotBlank(basePath)) {
      paths.add(basePath.trim());
    }
    paths.removeIf(p -> StringUtils.isBlank(p) || p.equals(cookiePath));
    return paths;
  }

  private boolean hasDuplicateSessionCookies(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.COOKIE);
    if (StringUtils.isBlank(header)) {
      return false;
    }
    String prefix = cookieName + "=";
    int count = 0;
    for (String part : header.split(";\\s*")) {
      if (part.startsWith(prefix) && ++count >= 2) {
        return true;
      }
    }
    return false;
  }

  /**
   * 构造删除指定 path 的 Set-Cookie 头。属性需与写入时一致，浏览器才会真正删除。
   */
  private String buildDeleteHeader(String path) {
    StringBuilder sb = new StringBuilder(cookieName)
      .append("=; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=").append(path);
    if (StringUtils.isNotBlank(domainName)) {
      sb.append("; Domain=").append(domainName);
    }
    if (secure) {
      sb.append("; Secure");
    }
    if (StringUtils.isNotBlank(sameSite)) {
      sb.append("; SameSite=").append(sameSite);
    }
    return sb.toString();
  }

}
