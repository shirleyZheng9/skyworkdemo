package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.enums.ParamSourceType;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import okhttp3.Headers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 系统动态参数解析工具
 *
 * @author wangtingyun
 * @since 2025-09-26
 */
@SuppressWarnings("PMD.UnusedFormalParameter")
public final class SysParamUtil {

  private SysParamUtil() {
  }

  private static final Logger logger = LoggerFactory.getLogger(SysParamUtil.class);

  /** 变量值解析器映射 */
  private static final Map<ParamSourceType, ParamResolver> paramResolvers = new LinkedHashMap<>();

  static {
    // Cookie 信息，形式为 $.cookie[.KEY_NAME], KEY_NAME 是Cookie的Key名称
    paramResolvers.put(ParamSourceType.COOKIE, SysParamUtil::getCookie);

    // 系统变量，形式为 ${system.VARIABLE_NAME}, VARIABLE_NAME 是系统变量的Key名称
    paramResolvers.put(ParamSourceType.SYSTEM_VARIABLE, SysParamUtil::getSystemVariable);
    // 登录信息变量，形式为 ${session.VARIABLE_NAME}, VARIABLE_NAME 是登录信息变量的Key名称
    paramResolvers.put(ParamSourceType.SESSION, SysParamUtil::getSessionVariable);
  }

  /**
   * 获取参数值
   *
   * @param spec 参数描述
   * @return 参数值
   */
  @Nullable
  public static Object getParamValue(@Nullable String spec) {
    if (StringUtils.isEmpty(spec)) {
      return null;
    }
    Object result = doGetParamValue(spec);
    if (result == null) {
      logger.warn("Parse param failed: spec={}", spec);
    }
    return result;
  }

  /**
   * 获取参数值，字符串类型
   *
   * @param spec 参数描述
   * @return 参数值
   */
  public static String getParamValueAsString(@Nullable String spec) {
    if (StringUtils.isEmpty(spec)) {
      return "";
    }
    Object result = doGetParamValue(spec);
    if (result == null) {
      logger.warn("Parse param failed: spec={}", spec);
    }
    return convertParamToString(result);
  }

  /**
   * 获取参数值底层逻辑
   */
  @Nullable
  private static Object doGetParamValue(String spec) {
    // 处理变量格式 ${ENV_VAR}
    if (spec.startsWith("${") && spec.endsWith("}")) {
      spec = "$." + spec.substring(2, spec.length() - 1);
    }
    // 字面量
    if (!spec.startsWith("$.")) {
      return spec;
    }
    // 使用第一个匹配的解析器
    for (Map.Entry<ParamSourceType, ParamResolver> entry : paramResolvers.entrySet()) {
      if (spec.startsWith(entry.getKey().getPrefix())) {
        return entry.getValue().resolve(spec, entry.getKey().removePrefix(spec));
      }
    }
    // 没有匹配的解析器时当作字面量，字符串形式
    return spec;
  }

  /**
   * 从 Cookie 信息获取属性值
   */
  @Nullable
  private static Object getCookie(String spec, String keyName) {
    HttpServletRequest request = ServletUtil.getRequest();
    if (request == null) {
      return null;
    }
    // 未指定具体Key时返回整个 cookie 信息
    String cookieHeader = request.getHeader(HttpHeaders.COOKIE);
    if (StringUtils.isEmpty(keyName)) {
      return cookieHeader;
    }
    else {
      // 获取cookie中的某个key值
      if (StringUtils.isNotBlank(cookieHeader)) {
        String[] cookies = cookieHeader.split(";\\s*");
        for (String cookie : cookies) {
          String[] parts = cookie.split("=", 2);
          if (parts.length == 2 && keyName.equals(parts[0].trim())) {
            return parts[1].trim();
          }
        }
      }
      // 没有找到对应的 key
      return null;
    }
  }

  /**
   * 获取系统变量属性值
   */
  @Nullable
  private static Object getSystemVariable(String spec, String keyName) {
    return switch (keyName) {
      case "now" -> new Date();
      case "today" -> LocalDate.now();
      case "uuid" -> UUID.randomUUID().toString();
      case "chatSessionId" -> ChatContextUtil.getChatSessionId();
      case "cookie" -> {
        HttpServletRequest request = ServletUtil.getRequest();
        yield request != null ? request.getHeader(HttpHeaders.COOKIE) : null;
      }
      default -> "";
    };
  }

  /**
   * 获取登录信息属性值
   */
  @Nullable
  private static Object getSessionVariable(String spec, String keyName) {
    LoginInfo loginInfo = SessionUtil.getOptionalLoginInfo();
    if (loginInfo == null) {
      return null;
    }
    return switch (keyName) {
      case "userName" -> loginInfo.getUserName();
      case "userId" -> loginInfo.getUserId();
      case "sessionId" -> loginInfo.getToken();
      case "realName" -> loginInfo.getRealName();
      case "attributes" -> loginInfo.getAttributes();
      default -> "";
    };
  }

  /**
   * 将参数转为字符串类型
   */
  private static String convertParamToString(@Nullable Object value) {
    // 特殊处理日期时间类型。日期类型 LocalDate 不需要处理，默认就会转为 yyyy-MM-dd 格式
    if (value instanceof Date) {
      return DateUtil.format((Date) value);
    }
    // 列表、对象转为 JSON 格式
    if (value instanceof Collection || value instanceof Map) {
      return JsonUtil.toJsonString(value);
    }
    // 使用 toString
    return value != null ? value.toString() : "";
  }

  /**
   * 构建请求头，替换其中的参数值
   */
  public static Headers buildHeaders(@Nullable Headers headers) {
    if (headers == null) {
      return Headers.of();
    }
    Headers.Builder builder = new Headers.Builder();
    for (int i = 0; i < headers.size(); i++) {
      String name = headers.name(i);
      String value = headers.value(i);
      builder.add(name, getParamValueAsString(value));
    }
    return builder.build();
  }

  /**
   * 参数值解析器
   */
  @FunctionalInterface
  interface ParamResolver {
    /**
     * 解析参数值
     *
     * @param spec 参数值规格
     * @param propertyPath 属性路径
     * @return 参数值
     */
    @Nullable
    Object resolve(String spec, String propertyPath);
  }

}
