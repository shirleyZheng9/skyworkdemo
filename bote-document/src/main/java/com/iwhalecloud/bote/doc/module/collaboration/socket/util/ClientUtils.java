package com.iwhalecloud.bote.doc.module.collaboration.socket.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 客户端工具类
 * 提供客户端IP获取、URL参数解析等通用方法
 *
 * @author Aiqing
 * @since 2025/8/29
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class ClientUtils {
  private static final Logger logger = LoggerFactory.getLogger(ClientUtils.class);

  private ClientUtils() {

  }

  /**
   * 获取客户端IP
   */
  public static String getClientIp(HttpHeaders headers, ChannelHandlerContext ctx) {
    String ip = headers.get("X-Forwarded-For");
    if (isValidIp(ip)) {
      // 多级代理的情况，取第一个IP
      int index = ip.indexOf(',');
      if (index != -1) {
        ip = ip.substring(0, index);
      }
      return ip.trim();
    }

    ip = headers.get("X-Real-IP");
    if (isValidIp(ip)) {
      return ip;
    }

    ip = headers.get("Proxy-Client-IP");
    if (isValidIp(ip)) {
      return ip;
    }

    ip = headers.get("WL-Proxy-Client-IP");
    if (isValidIp(ip)) {
      return ip;
    }

    // 如果都没有，返回远程地址
    return ctx == null ? null : ctx.channel().remoteAddress().toString();
  }

  private static boolean isValidIp(String ip) {
    return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
  }

  /**
   * 解析URI参数
   * 使用Spring的UriComponentsBuilder来安全解析URL参数
   *
   * @param uri 完整的URI字符串
   * @return 参数映射表，如果解析失败返回空Map
   */
  public static Map<String, String> parseQueryParams(String uri) {
    try {
      MultiValueMap<String, String> queryParams = UriComponentsBuilder
        .fromUriString(uri)
        .build()
        .getQueryParams();

      // 将MultiValueMap转换为Map<String, String>，取每个参数的第一个值
      return queryParams.entrySet().stream()
        .collect(Collectors.toMap(
          Map.Entry::getKey,
          entry -> entry.getValue().isEmpty() ? "" : entry.getValue().get(0)
        ));
    }
    catch (Exception e) {
      logger.warn("解析URI参数失败: uri={}, error={}", uri, e.getMessage());
      return Collections.emptyMap();
    }
  }

  /**
   * 解析URI参数（支持多值参数）
   *
   * @param uri 完整的URI字符串
   * @return 参数映射表，支持多值参数
   */
  public static MultiValueMap<String, String> parseMultiValueQueryParams(String uri) {
    try {
      return UriComponentsBuilder
        .fromUriString(uri)
        .build()
        .getQueryParams();
    }
    catch (Exception e) {
      logger.warn("解析URI多值参数失败: uri={}, error={}", uri, e.getMessage());
      return new LinkedMultiValueMap<>();
    }
  }

  /**
   * 获取单个查询参数值
   *
   * @param uri 完整的URI字符串
   * @param paramName 参数名称
   * @return 参数值，如果不存在返回null
   */
  public static String getQueryParam(String uri, String paramName) {
    Map<String, String> params = parseQueryParams(uri);
    return params.get(paramName);
  }

  /**
   * 获取单个查询参数值，带默认值
   *
   * @param uri 完整的URI字符串
   * @param paramName 参数名称
   * @param defaultValue 默认值
   * @return 参数值，如果不存在返回默认值
   */
  public static String getQueryParam(String uri, String paramName, String defaultValue) {
    String value = getQueryParam(uri, paramName);
    return value != null ? value : defaultValue;
  }

  /**
   * 检查URI是否包含指定的查询参数
   *
   * @param uri 完整的URI字符串
   * @param paramName 参数名称
   * @return 是否包含该参数
   */
  public static boolean hasQueryParam(String uri, String paramName) {
    return getQueryParam(uri, paramName) != null;
  }

  /**
   * 构建带查询参数的URI
   *
   * @param baseUri 基础URI
   * @param params 查询参数
   * @return 完整的URI字符串
   */
  public static String buildUriWithParams(String baseUri, Map<String, String> params) {
    try {
      UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUri);

      params.forEach(builder::queryParam);

      return builder.build().toUriString();
    }
    catch (Exception e) {
      logger.warn("构建URI失败: baseUri={}, params={}, error={}", baseUri, params, e.getMessage());
      return baseUri;
    }
  }
}
