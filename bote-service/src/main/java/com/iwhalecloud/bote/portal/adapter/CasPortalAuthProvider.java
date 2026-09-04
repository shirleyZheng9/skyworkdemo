package com.iwhalecloud.bote.portal.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.iwhalecloud.bote.cache.SessionTicketMappingCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.config.properties.CasPortalProperties;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * CAS Portal 鉴权提供者实现
 *
 * @author system
 * @since 2025-01-22
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class CasPortalAuthProvider extends AbstractAuthProvider {
  private static final Logger logger = LoggerFactory.getLogger(CasPortalAuthProvider.class);
  /** 将 CAS的service参数保存在当前线程中 */
  private static final ThreadLocal<String> casServiceUrlThreadLocal = new ThreadLocal<>();
  /** XML 解析器 */
  private static final XmlMapper xmlMapper = XmlMapper.builder().build();
  private static final SessionTicketMappingCache mappingCache = SpringUtil.getBean(SessionTicketMappingCache.class);

  private final CasPortalProperties properties;

  public CasPortalAuthProvider(CasPortalProperties properties) {
    this.properties = properties;
  }

  @Override
  @Nullable
  public String getLoginUrl() {
    return null;
  }

  @Override
  protected String getCookieName() {
    return "";
  }

  @Override
  protected String getParamName() {
    return "ticket";
  }

  @Nullable
  @Override
  public String getDefaultRole() {
    return properties.getDefaultRole();
  }

  @Nullable
  @Override
  public String getRedirectUrl(HttpServletRequest request) {
    String ticket = getSessionId(request);
    String casServiceUrl = request.getParameter("casServiceUrl");
    casServiceUrlThreadLocal.set(casServiceUrl);
    // 如果请求没带 ticket参数，需要重定向
    return StringUtils.isEmpty(ticket) ? getCasRedirectUrl(casServiceUrl) : null;
  }

  @Override
  public void saveExtSessionMapping(LoginInfo loginInfo) {
    String sessionId = loginInfo.getToken();
    String systemCode = loginInfo.getSystemCode();
    String extSessionId = BaseConsts.PORTAL_TYPE_CAS + loginInfo.getExtSessionId();
    if (StringUtils.isEmpty(sessionId) || StringUtils.isEmpty(systemCode)) {
      return;
    }
    mappingCache.saveMapping(sessionId, extSessionId, systemCode);
    mappingCache.saveMapping(extSessionId, sessionId, systemCode);
  }

  @Override
  @Nullable
  public LoginInfo getLoginInfo(String ticket) {
    if (StringUtils.isEmpty(ticket)) {
      return null;
    }

    // 参考 org.jasig.cas.client.validation.Cas30ServiceTicketValidator#validate
    // CAS 接口地址
    URI uri = UriComponentsBuilder.fromUriString(Strings.CS.appendIfMissing(properties.getServiceUrl(), "/") + "p3/serviceValidate")
      .queryParam("ticket", ticket)
      .queryParam("service", getAppLoginUrl(ticket))
      .build()
      .toUri();
    // 调用接口
    ResponseEntity<String> responseEntity;
    try {
      responseEntity = HttpUtil.getRestTemplate().getForEntity(uri, String.class);
    }
    catch (HttpStatusCodeException e) {
      logger.error("Request CAS api failed: url={}, status={}, headers={}, response={}", uri, e.getStatusCode().value(), e.getResponseHeaders(), e.getResponseBodyAsString());
      throw new BssException("CAS 登录验证失败: " + e.getMessage(), e);
    }

    // 校验、解析响应
    String responseBody = responseEntity.getBody();
    if (StringUtils.isEmpty(responseBody) || !responseBody.startsWith("<cas:serviceResponse")) {
      logger.error("Failed to get CAS login info, invalid response: url={}, status={}, headers={}, response={}", uri,
        responseEntity.getStatusCode(), responseEntity.getHeaders(), responseBody);
      throw new BssException("CAS 登录验证失败: 响应不合法");
    }
    JsonNode node;
    try {
      node = xmlMapper.readTree(responseBody);
    }
    catch (JsonProcessingException e) {
      logger.error("Failed to get CAS login info, invalid response: url={}, status={}, headers={}, response={}", uri,
        responseEntity.getStatusCode(), responseEntity.getHeaders(), responseBody);
      throw new BssException("CAS 登录验证失败: 响应不合法", e);
    }

    // 识别错误
    JsonNode authenticationFailure = node.get("authenticationFailure");
    if (authenticationFailure != null) {
      String errorCode = authenticationFailure.path("code").asText("");
      String errorMsg = authenticationFailure.path("").asText("");
      logger.warn("Failed to get CAS login info: url={}, code={}, message={}", uri, errorCode, errorMsg);
      throw new BssException("CAS 登录验证失败: " + errorMsg);
    }

    // 提取用户信息
    JsonNode attributes = node.path("authenticationSuccess").path("attributes");
    if (attributes.isMissingNode()) {
      logger.error("Failed to get CAS login info, invalid response: url={}, response={}", uri, responseBody);
      throw new BssException("CAS 登录验证失败: 缺少用户信息");
    }
    String userId = attributes.path("uid").asText(null);
    String userName = attributes.path("cn").asText(null);
    String realName = attributes.path("userName").asText(null);
    String userType = attributes.path("userType").asText(null);
    if (StringUtils.isEmpty(userId)) {
      logger.error("Failed to get CAS login info, missing userId: url={}, response={}", uri, responseBody);
      throw new BssException("CAS 登录验证失败: 缺少用户 ID");
    }
    if (StringUtils.isEmpty(userName)) {
      logger.error("Failed to get CAS login info, missing userName: url={}, response={}", uri, responseBody);
      throw new BssException("CAS 登录验证失败: 缺少用户名");
    }
    // 登录信息
    return LoginInfo.builder()
      .extUserId(userId)
      .userName(userName)
      .realName(realName)
      .userType(userType)
      .defaultTenantId(properties.getDefaultTenantId())
      .build();
  }

  private String getAppLoginUrl(String ticket) {
    String casServiceUrl = casServiceUrlThreadLocal.get();
    if (StringUtils.isEmpty(casServiceUrl)) {
      return properties.getLoggedUrl();
    }
    // 去除 ticket
    return Strings.CS.remove(casServiceUrl, "%26ticket%3D" + ticket);
  }

  private String getCasRedirectUrl(String casServiceUrl) {
    String redirectUrl = StringUtils.isNotEmpty(casServiceUrl) ? casServiceUrl : properties.getLoggedUrl();
    return properties.getServiceUrl() + "/login?service=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
  }
}
