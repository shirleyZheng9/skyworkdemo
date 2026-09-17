package com.iwhalecloud.bote.portal.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.config.properties.TGPortalProperties;
import com.iwhalecloud.bote.portal.tg.TGAuthService;
import com.iwhalecloud.bote.portal.tg.TGUserInfo;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 天工共享 Cookie 门户鉴权提供者。
 *
 * @author zhengxueli
 * @since 2026-09-07
 */
public class TGPortalAuthProvider extends AbstractAuthProvider {
  /** 同一请求中标记 Token 已验证，避免控制器先取 sessionId、再取 LoginInfo 时重复验证。 */
  private static final String REQUEST_VALIDATED_ATTRIBUTE = TGPortalAuthProvider.class.getName() + ".validated";
  /** 天工约定的智能体平台精确标识；不能使用 contains，避免误匹配相似平台名。 */
  private static final String TARGET_PLATFORM_AGENT = "agent";

  private final TGPortalProperties properties;
  private final TGAuthService authService;

  public TGPortalAuthProvider(TGPortalProperties properties, TGAuthService authService) {
    this.properties = properties;
    this.authService = authService;
  }

  /**
   * 每次 logged 请求均校验天工 Cookie，并在当前 request 记录已验证状态。
   */
  @Nullable
  @Override
  public String getSessionId(HttpServletRequest request) {
    // 复用 AbstractAuthProvider 的 Cookie 读取能力，不允许从 URL 传递 OIDC Token。
    String token = super.getSessionId(request);
    if (StringUtils.isBlank(token)) {
      logger.info("[TGPortal] 请求未携带共享 Cookie: requestUri={}, cookieName={}",
        request.getRequestURI(), properties.getCookieName());
      return null;
    }
    // 每次 /logged 只做验签/必要续签，不重复调用 extinfo 查用户详情。
    if (!authService.validateLogin(request)) {
      // 返回 null 后，现有 LoginController 会清理对应的 AADP 会话并返回天工登录地址。
      logger.warn("[TGPortal] Token 校验失败: requestUri={}, statePresent={}",
        request.getRequestURI(), StringUtils.isNotBlank(request.getParameter("state")));
      return null;
    }
    request.setAttribute(REQUEST_VALIDATED_ATTRIBUTE, Boolean.TRUE);
    // 控制器会把该值保存到 LoginInfo.extSessionId 并返回前端，不能暴露真实 OIDC Token。
    // state 一并参与摘要，使同一用户切换天工项目后能够触发重新同步。
    String fingerprint = sessionFingerprint(token, request.getParameter("state"));
    logger.info("[TGPortal] Token 校验成功: requestUri={}, sessionFingerprint={}",
      request.getRequestURI(), fingerprint.substring(0, 12));
    return fingerprint;
  }

  @Nullable
  @Override
  public LoginInfo getLoginInfo(HttpServletRequest request) {
    // 这里只读取 Cookie 原值；若 getSessionId 已执行，Token 校验状态已放入 request attribute。
    String token = super.getSessionId(request);
    if (StringUtils.isBlank(token)) {
      logger.info("[TGPortal] 跳过登录信息查询，请求未携带共享 Cookie: requestUri={}, cookieName={}",
        request.getRequestURI(), properties.getCookieName());
      return null;
    }
    // LoginController 只在首次登录或 Token 变化时调用本方法，此时才查 extinfo。
    boolean alreadyValidated = Boolean.TRUE.equals(request.getAttribute(REQUEST_VALIDATED_ATTRIBUTE));
    if (!alreadyValidated && !authService.validateLogin(request)) {
      logger.warn("[TGPortal] 拒绝查询登录信息，Token 校验失败: requestUri={}", request.getRequestURI());
      return null;
    }
    logger.info("[TGPortal] 开始查询天工用户扩展信息: requestUri={}, reusedRequestValidation={}",
      request.getRequestURI(), alreadyValidated);
    TGUserInfo userInfo = authService.getUserInfo(token);
    if (userInfo == null) {
      logger.warn("[TGPortal] Token 校验成功，但未获取到用户扩展信息: requestUri={}", request.getRequestURI());
      return null;
    }
    return toLoginInfo(userInfo, parseProjectContext(request));
  }

  @Nullable
  @Override
  public LoginInfo getLoginInfo(String token) {
    // TG 字段映射依赖当前请求携带的 state，不能只凭 Token 构造不完整的登录信息。
    return null;
  }

  private LoginInfo toLoginInfo(TGUserInfo userInfo, TGProjectContext projectContext) {
    // userId 是复用 OAuth2 时期存量用户的唯一外部标识，缺失时绝不能创建本地用户。
    if (userInfo.getUserId() == null) {
      logger.warn("[TGPortal] 登录信息映射失败，userId 为空");
      throw new BssException("天工统一登录失败: userId 不能为空");
    }
    // 天工团队映射 AADP workspace。0 是兜底值，不能作为真实外部团队 ID。
    if (userInfo.getCurrentTenantId() == null || userInfo.getCurrentTenantId() == 0L) {
      logger.warn("[TGPortal] 登录信息映射失败，当前天工团队无效: userId={}, currentTenantId={}",
        userInfo.getUserId(), userInfo.getCurrentTenantId());
      throw new BssException("天工统一登录失败: currentTenantId 不能为空或 0");
    }
    if (StringUtils.isBlank(userInfo.getCurrentTenantName())) {
      logger.warn("[TGPortal] 登录信息映射失败，当前天工团队名称为空: userId={}, currentTenantId={}",
        userInfo.getUserId(), userInfo.getCurrentTenantId());
      throw new BssException("天工统一登录失败: currentTenantName 不能为空");
    }

    // 创建 tenant 必须同时满足门户开关和天工项目允许进入 agent 平台两个条件。
    boolean allowsAgent = allowsAgentPlatform(projectContext.targetPlatforms);
    boolean autoCreateTenant = Boolean.TRUE.equals(properties.getAutoCreateTenant()) && allowsAgent;
    // 未选择天工项目时保持旧 OAuth2 门户行为：允许登录，但不创建或绑定项目租户。
    boolean hasProjectContext = StringUtils.isNotBlank(projectContext.projectId);
    boolean syncProjectTenant = autoCreateTenant && hasProjectContext;
    logger.info("[TGPortal] 项目映射判定完成: userId={}, currentTenantId={}, projectId={}, "
        + "targetPlatforms={}, configuredAutoCreateTenant={}, allowsAgent={}, autoCreateTenant={}, syncProjectTenant={}",
      userInfo.getUserId(), userInfo.getCurrentTenantId(), projectContext.projectId,
      projectContext.targetPlatforms, properties.getAutoCreateTenant(), allowsAgent, autoCreateTenant, syncProjectTenant);
    Long extTenantId = null;
    if (autoCreateTenant && !hasProjectContext) {
      logger.info("[TGPortal] 未选择天工项目，跳过项目租户同步: userId={}, currentTenantId={}",
        userInfo.getUserId(), userInfo.getCurrentTenantId());
    }
    if (syncProjectTenant) {
      try {
        extTenantId = Long.valueOf(projectContext.projectId.trim());
      }
      catch (NumberFormatException e) {
        logger.warn("[TGPortal] 无法自动创建租户，projectId 不是整数: userId={}, projectId={}",
          userInfo.getUserId(), projectContext.projectId);
        throw new BssException("天工统一登录失败: projectId 必须为整数", e);
      }
      if (StringUtils.isBlank(projectContext.projectName)) {
        logger.warn("[TGPortal] 无法自动创建租户，projectName 为空: userId={}, projectId={}",
          userInfo.getUserId(), projectContext.projectId);
        throw new BssException("天工统一登录失败: projectName 不能为空");
      }
    }

    // MultiPortalAdapter 已通过这些既有 attributes 同步 workspace，不另写一套同步逻辑。
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("extSpaceId", String.valueOf(userInfo.getCurrentTenantId()));
    attributes.put("spaceName", userInfo.getCurrentTenantName());
    attributes.put("autoCreateTenant", autoCreateTenant);

    // 保持 systemCode=tiangong-noheader + extUserId=userId，才能命中 OAuth2 时期的存量用户。
    String externalUserId = String.valueOf(userInfo.getUserId());
    // 页面展示优先使用昵称；昵称缺失时回退到登录名。
    String realName = StringUtils.defaultIfBlank(userInfo.getNickname(),
      StringUtils.defaultIfBlank(userInfo.getUsername(), externalUserId));
    LoginInfo.LoginInfoBuilder builder = LoginInfo.builder()
      .extUserId(externalUserId)
      .userName(externalUserId)
      .realName(realName)
      .phoneNo(userInfo.getPhoneNo())
      .defaultTenantId(properties.getDefaultTenantId())
      .attributes(attributes);
    if (syncProjectTenant) {
      // extTenantId 按天工项目稳定判重；项目改名时仍能复用并更新原 AADP tenant。
      builder.extTenantId(extTenantId)
        .tenantCode("auto" + projectContext.projectId.trim())
        .tenantName(projectContext.projectName);
    }
    logger.info("[TGPortal] 登录信息映射完成: userId={}, currentTenantId={}, projectId={}, "
        + "extTenantId={}, defaultTenantId={}, autoCreateTenant={}",
      userInfo.getUserId(), userInfo.getCurrentTenantId(), projectContext.projectId,
      extTenantId, properties.getDefaultTenantId(), autoCreateTenant);
    return builder.build();
  }

  private TGProjectContext parseProjectContext(HttpServletRequest request) {
    String state = request.getParameter("state");
    if (StringUtils.isBlank(state)) {
      logger.info("[TGPortal] 请求未携带项目 state: requestUri={}", request.getRequestURI());
      return TGProjectContext.EMPTY;
    }
    try {
      // 与存量 Groovy 保持一致：URLDecode -> Base64Decode -> URLDecode -> JSON。
      String base64Text = URLDecoder.decode(state, StandardCharsets.UTF_8);
      String encodedJson = new String(Base64.getDecoder().decode(base64Text), StandardCharsets.UTF_8);
      String json = URLDecoder.decode(encodedJson, StandardCharsets.UTF_8);
      Map<String, Object> stateData = JsonUtil.parseJsonRequired(json, new TypeReference<Map<String, Object>>() { });
      Map<?, ?> meta = asMap(stateData.get("meta"));
      Map<?, ?> payload = asMap(stateData.get("payload"));
      TGProjectContext context = new TGProjectContext(getString(meta, "projectId"),
        getString(meta, "projectName"), getString(payload, "target_platforms"));
      logger.info("[TGPortal] 项目 state 解析成功: requestUri={}, projectId={}, projectNamePresent={}, "
          + "targetPlatforms={}",
        request.getRequestURI(), context.projectId, StringUtils.isNotBlank(context.projectName),
        context.targetPlatforms);
      return context;
    }
    catch (RuntimeException e) {
      logger.warn("[TGPortal] 项目 state 解析失败: requestUri={}, stateLength={}",
        request.getRequestURI(), state.length(), e);
      throw new BssException("天工统一登录失败: state 格式不合法", e);
    }
  }

  private String sessionFingerprint(String token, @Nullable String state) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] value = (token + '\0' + StringUtils.defaultString(state)).getBytes(StandardCharsets.UTF_8);
      return HexFormat.of().formatHex(digest.digest(value));
    }
    catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("当前 JDK 不支持 SHA-256", e);
    }
  }

  private Map<?, ?> asMap(@Nullable Object value) {
    return value instanceof Map<?, ?> map ? map : null;
  }

  @Nullable
  private String getString(@Nullable Map<?, ?> map, String key) {
    Object value = map == null ? null : map.get(key);
    return value == null ? null : String.valueOf(value);
  }

  private boolean allowsAgentPlatform(@Nullable String targetPlatforms) {
    // 兼容旧 Token/旧接口没有该字段的情况：空值按允许创建处理。
    if (StringUtils.isBlank(targetPlatforms)) {
      return true;
    }
    // 协议约定英文逗号分隔；逐项去除空格后做精确匹配。
    for (String platform : StringUtils.split(targetPlatforms, ',')) {
      if (TARGET_PLATFORM_AGENT.equals(StringUtils.trim(platform))) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  @Override
  protected String getCookieName() {
    // Cookie 名由门户记录按环境配置，不能硬编码生产名称。
    return properties.getCookieName();
  }

  @Nullable
  @Override
  protected String getParamName() {
    // OIDC Token 仅允许通过 HttpOnly Cookie 传递，禁止放入 URL。
    return null;
  }

  @Nullable
  @Override
  public String getLoginUrl() {
    return properties.getLoginUrl();
  }

  @Nullable
  @Override
  public String getDefaultRole() {
    return properties.getDefaultRole();
  }

  private static final class TGProjectContext {
    private static final TGProjectContext EMPTY = new TGProjectContext(null, null, null);
    private final String projectId;
    private final String projectName;
    private final String targetPlatforms;

    private TGProjectContext(String projectId, String projectName, String targetPlatforms) {
      this.projectId = projectId;
      this.projectName = projectName;
      this.targetPlatforms = targetPlatforms;
    }
  }
}
