package com.iwhalecloud.bote.portal.adapter;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.GroovyUtil;
import com.iwhalecloud.bote.common.util.ServletUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.config.properties.OAuth2PortalProperties;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * oauth2 portal 鉴权提供者实现
 *
 * @author chen.linfa
 * @since 2026-02-05
 */
public class OAuth2PortalAuthProvider extends AbstractCachingAuthProvider<OAuth2PortalProperties> {
  public OAuth2PortalAuthProvider(OAuth2PortalProperties properties) {
    super(properties);
  }

  @Override
  protected String getCookieName() {
    return "";
  }

  @Override
  protected String getParamName() {
    return StringUtils.defaultIfEmpty(properties.getParamName(), "code");
  }

  @Nullable
  @Override
  public String getRedirectUrl(HttpServletRequest request) {
    String baseUrl = StringUtils.stripEnd(SystemParameter.BOTE_API_URL.getValueFromEnv(), "/");
    // 指定回调地址为
    String callbackUrl = baseUrl + "/bote/oauth2/callback";

    // 透传所有参数
    Map<String, String> parameters = ServletUtil.getParametersAsMap();
    List<String> urlParameters = new ArrayList<>();
    String relativeUrl = "";
    for (Entry<String, String> entry : parameters.entrySet()) {
      if ("redirect".equals(entry.getKey())) {
        // 支持用户动态指定重定向地址
        relativeUrl = entry.getValue();
      }
      else {
        urlParameters.add(entry.getKey() + "=" + entry.getValue());
      }
    }
    if (!urlParameters.isEmpty()) {
      callbackUrl = callbackUrl + "?" + String.join("&", urlParameters);
    }

    // 构造相对地址
    if (StringUtils.isEmpty(relativeUrl)) {
      relativeUrl = properties.getRedirectUrl();
    }
    // 重定向地址中的 # 符号需要转义为 %23，避免 oauth2 回调时出现参数位置错误
    relativeUrl = relativeUrl.replaceAll("#", "%23");
    if (!relativeUrl.startsWith("http")) {
      // 相对路径，确保 / 开头
      relativeUrl = "/" + StringUtils.stripStart(relativeUrl, "/");
    }
    callbackUrl = callbackUrl + "&redirect=" + relativeUrl;

    // URL 编码处理
    return String.format(properties.getLoginUrl(), URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8));
  }

  @Override
  @Nullable
  protected LoginInfo loadLoginInfo(String sessionId) {
    LoginInfo loginInfo = null;
    Map<String, String> parameters = ServletUtil.getParametersAsMap();
    String code = parameters.get(getParamName());
    if (StringUtils.isEmpty(code)) {
      return null;
    }
    // oauth2 获取用户信息的操作流程
    // 1、根据 code 获取 token
    // 2、根据 token 获取用户信息
    // 由于相关的 API 没有固定协议规范，不适合硬编码处理。
    // 这里采用自定义脚本方式，按照实际对接的门户 API 灵活适配
    if (StringUtils.isNotEmpty(properties.getScript())) {
      loginInfo = loadLoginInfoByScript();
    }
    if (loginInfo != null) {
      loginInfo.setDefaultTenantId(properties.getDefaultTenantId());
      if (BaseConsts.SYSTEM_TYPE_BEYOND.equals(loginInfo.getSystemType())) {
        if (parameters.containsKey("tenantId")) {
          loginInfo.setDefaultTenantId(Long.valueOf(parameters.get("tenantId")));
        }
      }
      boolean autoCreateTenant = properties.getAutoCreateTenant();
      Map<String, Object> attributes = MapUtils.isNotEmpty(loginInfo.getAttributes())
        ? new LinkedHashMap<>(loginInfo.getAttributes())
        : new LinkedHashMap<>();
      loginInfo.setAttributes(attributes);
      attributes.put("autoCreateTenant", autoCreateTenant);
    }
    return loginInfo;
  }

  /**
   * 根据脚本加载登录信息
   */
  @Nullable
  private LoginInfo loadLoginInfoByScript() {
    Map<String, Object> params = buildScriptParams();
    Object result;
    try {
      result = GroovyUtil.invoke(properties.getScript(), params);
      if (result == null) {
        return null;
      }
    }
    catch (BssException e) {
      logger.error("Failed to load login info by oauth2 script: params={}", params, e);
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to load login info by oauth2 script: params={}", params, e);
      throw new BssException("单点登录失败: " + ExpUtil.getMsg(e), e);
    }

    try {
      return JsonUtil.convert(result, LoginInfo.class);
    }
    catch (IllegalArgumentException e) {
      logger.error("Failed to load login info by oauth2 script, convert failed: result={}", result, e);
      throw new BssException("单点登录失败: 登录信息不合法", e);
    }
  }

  /**
   * 构造脚本参数
   */
  private Map<String, Object> buildScriptParams() {
    HttpServletRequest request = ServletUtil.getRequest();
    if (request == null) {
      return Map.of();
    }
    Map<String, Object> params = new HashMap<>();
    // 添加 URL 参数
    for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
      params.put(entry.getKey(), entry.getValue()[0]);
    }
    // 添加请求头
    params.put("headers", ServletUtil.getHeadersAsMap(request));
    return params;
  }
}
