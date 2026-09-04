package com.iwhalecloud.bote.doc.common.mybatis.plugins.helper;

import com.iwhalecloud.bote.doc.common.mybatis.annoation.InterceptorIgnore;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 拦截器忽略助手
 * 提供多种方式来判断是否忽略租户拦截器
 *
 * @author Aiqing
 * @since 2025-01-06
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class InterceptorIgnoreHelper {
  private static final Logger logger = LoggerFactory.getLogger(InterceptorIgnoreHelper.class);

  private InterceptorIgnoreHelper() {
  }

  /**
   * 缓存已检查的Mapper方法ID，避免重复反射检查
   */
  private static final Set<String> IGNORED_METHODS = ConcurrentHashMap.newKeySet();
  private static final Set<String> NON_IGNORED_METHODS = ConcurrentHashMap.newKeySet();

  /**
   * 配置的忽略方法列表（支持通配符）
   */
  private static final Set<String> CONFIGURED_IGNORE_METHODS = ConcurrentHashMap.newKeySet();

  /**
   * 插件启用的包路径
   */
  private static final Set<String> CONFIGURED_ENABLED_PACKAGES = ConcurrentHashMap.newKeySet();

  /**
   * 判断是否忽略租户拦截器
   *
   * @param mappedStatementId Mapper方法ID
   * @return 是否忽略
   */
  public static boolean willIgnoreTenantLine(String mappedStatementId) {
    if (!StringUtils.hasText(mappedStatementId)) {
      return false;
    }
    // 1. 检查缓存
    if (IGNORED_METHODS.contains(mappedStatementId)) {
      return true;
    }
    if (NON_IGNORED_METHODS.contains(mappedStatementId)) {
      return false;
    }

    // 2. 检查包路径匹配
    // 如果配置了启用包路径，只有匹配的包路径才启用插件，否则忽略
    if (!CONFIGURED_ENABLED_PACKAGES.isEmpty()) {
      if (!isInEnabledPackage(mappedStatementId)) {
        logger.debug("Mapper方法 {} 不在启用的包路径内，忽略拦截器", mappedStatementId);
        IGNORED_METHODS.add(mappedStatementId);
        return true;
      }
    }
    // 3. 检查配置的忽略方法
    if (isConfiguredIgnoreMethod(mappedStatementId)) {
      IGNORED_METHODS.add(mappedStatementId);
      return true;
    }

    // 4. 通过反射检查注解
    boolean shouldIgnore = checkAnnotationIgnore(mappedStatementId);

    // 5. 缓存结果
    if (shouldIgnore) {
      IGNORED_METHODS.add(mappedStatementId);
    }
    else {
      NON_IGNORED_METHODS.add(mappedStatementId);
    }
    return shouldIgnore;
  }

  /**
   * 检查Mapper方法是否在启用的包路径内
   *
   * @param mappedStatementId Mapper方法ID
   * @return 是否在启用的包路径内
   */
  private static boolean isInEnabledPackage(String mappedStatementId) {
    // 解析Mapper方法ID，获取类名部分
    String mapperClassName = extractMapperClassName(mappedStatementId);
    if (mapperClassName == null) {
      return false;
    }

    // 检查是否匹配任何启用的包路径
    for (String enabledPackage : CONFIGURED_ENABLED_PACKAGES) {
      if (mapperClassName.startsWith(enabledPackage)) {
        logger.debug("Mapper类 {} 匹配启用的包路径: {}", mapperClassName, enabledPackage);
        return true;
      }
    }
    return false;
  }

  /**
   * 从Mapper方法ID中提取Mapper类名
   *
   * @param mappedStatementId Mapper方法ID
   * @return Mapper类名，如果解析失败返回null
   */
  private static String extractMapperClassName(String mappedStatementId) {
    try {
      // 解析Mapper方法ID，格式通常为：com.example.mapper.UserMapper.selectById
      String[] parts = mappedStatementId.split("\\.");
      if (parts.length < 2) {
        return null;
      }
      // 获取Mapper接口类名（去掉方法名）
      return String.join(".", java.util.Arrays.copyOf(parts, parts.length - 1));
    }
    catch (Exception e) {
      logger.debug("解析Mapper类名失败: {}, error: {}", mappedStatementId, e.getMessage());
      return null;
    }
  }

  /**
   * 检查是否为配置的忽略方法
   */
  private static boolean isConfiguredIgnoreMethod(String mappedStatementId) {
    for (String ignorePattern : CONFIGURED_IGNORE_METHODS) {
      if (mappedStatementId.matches(ignorePattern.replace("*", ".*"))) {
        return true;
      }
    }
    return false;
  }

  /**
   * 通过反射检查注解
   */
  private static boolean checkAnnotationIgnore(String mappedStatementId) {
    try {
      // 使用统一的方法提取Mapper类名
      String mapperClassName = extractMapperClassName(mappedStatementId);
      if (mapperClassName == null) {
        return false;
      }

      // 提取方法名
      String methodName = extractMethodName(mappedStatementId);
      if (methodName == null) {
        return false;
      }

      Class<?> mapperClass = Class.forName(mapperClassName);

      // 检查类级别的注解
      InterceptorIgnore annotation = mapperClass.getAnnotation(InterceptorIgnore.class);
      if (annotation != null && Objects.equals(annotation.tenantLine(), Boolean.TRUE)) {
        logger.debug("Mapper类 {} 标记了@InterceptorIgnore注解", mapperClassName);
        return true;
      }

      // 检查方法级别的注解
      Method[] methods = mapperClass.getDeclaredMethods();
      for (Method method : methods) {
        if (method.getName().equals(methodName)) {
          InterceptorIgnore methodAnnotation = method.getAnnotation(InterceptorIgnore.class);
          if (methodAnnotation != null && Objects.equals(methodAnnotation.tenantLine(), Boolean.TRUE)) {
            logger.debug("Mapper方法 {} 标记了@InterceptorIgnore注解", mappedStatementId);
            return true;
          }
        }
      }
    }
    catch (Exception e) {
      logger.debug("检查Mapper方法注解失败: {}, error: {}", mappedStatementId, e.getMessage());
    }
    return false;
  }

  /**
   * 从Mapper方法ID中提取方法名
   *
   * @param mappedStatementId Mapper方法ID
   * @return 方法名，如果解析失败返回null
   */
  private static String extractMethodName(String mappedStatementId) {
    try {
      String[] parts = mappedStatementId.split("\\.");
      if (parts.length < 2) {
        return null;
      }
      return parts[parts.length - 1];
    }
    catch (Exception e) {
      logger.debug("解析方法名失败: {}, error: {}", mappedStatementId, e.getMessage());
      return null;
    }
  }

  /**
   * 添加启用的包路径
   *
   * @param packagePath 包路径，如：com.iwhalecloud.bote.doc.mapper
   */
  public static void addEnabledPackage(String packagePath) {
    if (StringUtils.hasText(packagePath)) {
      // 确保包路径以点结尾，避免部分匹配
      String normalizedPackage = packagePath.trim();
      if (!normalizedPackage.endsWith(".")) {
        normalizedPackage += ".";
      }
      CONFIGURED_ENABLED_PACKAGES.add(normalizedPackage);
      logger.info("添加mybatis 租户插件启用的包路径: {}", normalizedPackage);
    }
  }

  /**
   * 移除启用的包路径
   *
   * @param packagePath 包路径
   */
  public static void removeEnabledPackage(String packagePath) {
    if (StringUtils.hasText(packagePath)) {
      String normalizedPackage = packagePath.trim();
      if (!normalizedPackage.endsWith(".")) {
        normalizedPackage += ".";
      }
      CONFIGURED_ENABLED_PACKAGES.remove(normalizedPackage);
      logger.info("移除启用的包路径: {}", normalizedPackage);
    }
  }

  /**
   * 获取所有启用的包路径
   *
   * @return 启用的包路径集合
   */
  public static Set<String> getEnabledPackages() {
    return new java.util.HashSet<>(CONFIGURED_ENABLED_PACKAGES);
  }

  /**
   * 清空所有启用的包路径
   */
  public static void clearEnabledPackages() {
    CONFIGURED_ENABLED_PACKAGES.clear();
    logger.info("已清空所有启用的包路径");
  }

  /**
   * 添加忽略的方法模式
   *
   * @param methodPattern 方法模式，支持通配符，如：com.example.mapper.*.selectAll
   */
  public static void addIgnoreMethod(String methodPattern) {
    if (StringUtils.hasText(methodPattern)) {
      CONFIGURED_IGNORE_METHODS.add(methodPattern);
      logger.info("添加忽略的Mapper方法模式: {}", methodPattern);
    }
  }

  /**
   * 清除缓存
   */
  public static void clearCache() {
    IGNORED_METHODS.clear();
    NON_IGNORED_METHODS.clear();
    logger.info("已清除拦截器忽略缓存");
  }

  /**
   * 获取缓存统计信息
   */
  public static String getCacheStats() {
    return String.format("忽略方法缓存: %d, 非忽略方法缓存: %d, 配置忽略方法: %d, 启用包路径: %d",
      IGNORED_METHODS.size(), NON_IGNORED_METHODS.size(),
      CONFIGURED_IGNORE_METHODS.size(), CONFIGURED_ENABLED_PACKAGES.size());
  }
}
