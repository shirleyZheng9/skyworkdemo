package com.iwhalecloud.bote.common.util;

import org.springframework.lang.Nullable;

/**
 * 技能编码工具类
 *
 * <p>统一管理技能相关字符串的规范化处理</p>
 */
public final class SkillCodeUtils {

  private SkillCodeUtils() {
  }

  /**
   * 将字符串转为安全标识符：将 {@code [^a-zA-Z0-9._-]} 替换为下划线 {@code _}。
   * <p>适用于 skillCode、owner、repo、version 等需要写入文件名或 DB 字段的标识符。</p>
   *
   * @param input 原始字符串，null 时返回空字符串
   */
  public static String toSafeId(@Nullable String input) {
    if (input == null) {
      return "";
    }
    return input.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  /**
   * 将展示名（支持中文）转为 slug 风格的 skillCode：
   * <ol>
   *   <li>将非 {@code [a-zA-Z0-9\u4e00-\u9fa5._-]} 字符替换为 {@code -}</li>
   *   <li>折叠连续的 {@code -}</li>
   *   <li>去除首尾的 {@code -}</li>
   *   <li>转为小写</li>
   * </ol>
   *
   * @param name 技能名称，blank 时返回 null
   */
  public static String nameToSlug(String name) {
    return name.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5._-]", "-")
      .replaceAll("-+", "-")
      .replaceAll("^-|-$", "")
      .toLowerCase();
  }
}
