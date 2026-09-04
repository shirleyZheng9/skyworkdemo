package com.iwhalecloud.bote.doc.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ReflectionUtils;

/**
 * 安全切面工具类
 * 提供安全切面中通用的静态方法
 *
 * @author lizuyin
 * @since 2025-10-25
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class SecurityAspectUtils {

  private SecurityAspectUtils() {
  }

  private static final Logger logger = LoggerFactory.getLogger(SecurityAspectUtils.class);

  /**
   * 判断是否是可忽略URL
   *
   * @param ignoreUrls 忽略URL列表
   * @param uri 请求URI
   * @param pathMatcher 路径匹配器
   * @return 是否忽略
   */
  public static boolean isIgnoreUrl(List<String> ignoreUrls, String uri, AntPathMatcher pathMatcher) {
    if (CollectionUtils.isEmpty(ignoreUrls)) {
      return false;
    }
    // 快速匹配，保证性能
    if (ignoreUrls.contains(uri)) {
      return true;
    }
    // 逐个 Ant 路径匹配
    for (String url : ignoreUrls) {
      if (pathMatcher.match(url, uri)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 从参数中提取ID
   *
   * @param parameter 方法参数
   * @param arg 参数值
   * @param paramName 参数名
   * @return ID值，如果匹配失败则返回null
   */
  public static Long extractIdFromParameter(Parameter parameter, Object arg, String paramName) {
    // 检查参数名是否匹配
    if (paramName.equals(parameter.getName())) {
      return convertToLong(arg);
    }
    return null;
  }

  /**
   * 将对象转换为Long类型
   *
   * @param value 待转换的值
   * @return Long值，如果转换失败则返回null
   */
  public static Long convertToLong(Object value) {
    if (value == null) {
      return null;
    }

    if (value instanceof Long) {
      return (Long) value;
    }

    if (value instanceof Number) {
      return ((Number) value).longValue();
    }

    if (value instanceof String) {
      try {
        return Long.valueOf((String) value);
      }
      catch (NumberFormatException e) {
        logger.debug("无法将字符串转换为Long: {}", value);
        return null;
      }
    }

    return null;
  }

  /**
   * 从Header中获取ID
   *
   * @param request HTTP请求
   * @param headerName Header名称
   * @return ID值，如果获取失败则返回null
   */
  public static Long extractIdFromHeader(HttpServletRequest request, String headerName) {
    String header = request.getHeader(headerName);
    if (StringUtils.isBlank(header)) {
      return null;
    }
    try {
      return Long.valueOf(header);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * 通过反射从任意对象中提取ID
   *
   * @param obj 目标对象
   * @param fieldName 字段名
   * @param getterMethodName getter方法名
   * @return ID值，如果未找到则返回null
   */
  public static Long extractIdByReflection(Object obj, String fieldName, String getterMethodName) {
    if (obj == null) {
      return null;
    }
    try {
      Class<?> clazz = obj.getClass();

      // 查找指定字段
      Field idField = ReflectionUtils.findField(clazz, fieldName);
      if (idField != null) {
        ReflectionUtils.makeAccessible(idField);
        Object idValue = ReflectionUtils.getField(idField, obj);
        return convertToLong(idValue);
      }

      // 如果没找到字段，尝试查找getter方法
      Method getIdMethod = ReflectionUtils.findMethod(clazz, getterMethodName);
      if (getIdMethod != null) {
        ReflectionUtils.makeAccessible(getIdMethod);
        Object idValue = ReflectionUtils.invokeMethod(getIdMethod, obj);
        return convertToLong(idValue);
      }

    }
    catch (Exception e) {
      logger.debug("通过反射提取ID失败: className={}, fieldName={}, error={}",
        obj.getClass().getSimpleName(), fieldName, e.getMessage());
    }
    return null;
  }

  /**
   * 从对象数组中尝试提取ID
   *
   * @param args 参数数组
   * @param fieldName 字段名
   * @param getterMethodName getter方法名
   * @return ID值，如果未找到则返回null
   */
  public static Long attemptExtractIdFromObjects(Object[] args, String fieldName, String getterMethodName) {
    if (args == null) {
      return null;
    }

    for (Object arg : args) {
      if (arg == null) {
        continue;
      }

      Long id = extractIdByReflection(arg, fieldName, getterMethodName);
      if (id != null) {
        logger.debug("通过反射从对象中提取到ID: id={}, fieldName={}, className={}",
          id, fieldName, arg.getClass().getSimpleName());
        return id;
      }
    }
    return null;
  }
}
