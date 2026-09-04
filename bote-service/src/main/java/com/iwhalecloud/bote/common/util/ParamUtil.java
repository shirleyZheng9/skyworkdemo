package com.iwhalecloud.bote.common.util;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 参数工具类
 *
 * @author bianjp
 * @since 2024-12-24
 */
public final class ParamUtil {
  private static final Logger logger = LoggerFactory.getLogger(ParamUtil.class);

  private ParamUtil() {
  }

  /**
   * 获取嵌套属性
   *
   * @param bean 对象（map 或 pojo）
   * @param propertyPath 属性路径。嵌套路径使用 "." 表示
   * @return 属性值
   */
  @Nullable
  @SuppressWarnings("PMD.GuardLogStatement")
  public static Object getNestedProperty(@Nullable Object bean, @Nullable String propertyPath) {
    if (bean == null || StringUtils.isEmpty(propertyPath)) {
      return bean;
    }
    try {
      return PropertyUtils.getProperty(bean, propertyPath);
    }
    catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      logger.error("Failed to get nested property: property={}, bean={}", propertyPath, bean, e);
    }
    catch (NestedNullException e) {
      logger.warn("Failed to get nested property, possibly missing intermediate property: property={}, bean={}, error={}", propertyPath, bean,
        e.getMessage());
    }
    return null;
  }
}
