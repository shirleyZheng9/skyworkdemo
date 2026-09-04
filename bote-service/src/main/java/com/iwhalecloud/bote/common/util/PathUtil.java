package com.iwhalecloud.bote.common.util;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 * 文件路径工具类
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
public final class PathUtil {

  private PathUtil() {
  }

  /** 是否应该替换路径中的分隔符 */
  private static final boolean shouldReplaceSeparator = File.separatorChar != '/';

  /**
   * 解析路径
   *
   * @param basePath 基础路径
   * @param segments 路径片段
   * @return 解析后的路径
   */
  public static Path resolvePath(String basePath, String... segments) {
    if (segments.length == 0) {
      return Paths.get(basePath);
    }
    return Paths.get(basePath, normalizePathSegments(segments));
  }

  /**
   * 规范路径
   * <p>将 "/" 替换为当前系统使用的文件分隔符，并删除重复的 "/"</p>
   *
   * @param path 原始路径
   * @return 规范后的路径
   */
  public static String normalizePath(String path) {
    if (StringUtils.isEmpty(path)) {
      return "";
    }
    // 消除重复的 "/"
    path = Strings.CS.replace(path, "//", "/");
    if (shouldReplaceSeparator) {
      path = path.replace('/', File.separatorChar);
    }
    return path;
  }

  /**
   * 规范路径片段数组
   */
  public static String[] normalizePathSegments(String[] segments) {
    if (shouldReplaceSeparator) {
      return Arrays.stream(segments).filter(StringUtils::isNotEmpty).map(PathUtil::normalizePath).toArray(String[]::new);
    }
    return Arrays.stream(segments).filter(StringUtils::isNotEmpty).toArray(String[]::new);
  }
}
