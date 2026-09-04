package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.cache.SensitiveWordCache;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 敏感词工具类
 *
 * @author bianjp
 * @since 2024-07-30
 */
public final class SensitiveWordUtil {
  private static final SensitiveWordCache sensitiveWordCache = SpringUtil.getBean(SensitiveWordCache.class);

  private SensitiveWordUtil() {
  }

  /**
   * 检查文本中是否包含敏感词
   *
   * @param text 文本
   * @return 是否包含敏感词
   */
  public static boolean isSensitive(String text) {
    // 检查是否开启敏感词过滤
    if (!SystemParameter.SENSITIVE_WORD_ENABLED.getBooleanValueFromDb()) {
      return false;
    }
    return sensitiveWordCache.isSensitive(text);
  }

}
