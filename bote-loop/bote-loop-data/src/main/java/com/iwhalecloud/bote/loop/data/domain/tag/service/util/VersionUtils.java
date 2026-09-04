package com.iwhalecloud.bote.loop.data.domain.tag.service.util;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * 版本工具类
 * 迁移对应关系: Go语言version_utils.go
 * - 功能: 提供版本相关的工具方法
 * - 方法定义: 各种版本处理方法
 * <p>
 * Java实现说明:
 * - 对应Go的version_utils.go文件
 * - 使用Java静态方法实现工具功能
 * - 提供版本验证和递增功能
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go字符串处理 -> Java字符串处理
 * - Go错误处理 -> Java异常处理
 */
public final class VersionUtils {
  private VersionUtils() {
    // 工具类，禁止实例化
  }

  private static final int MIN_SEM = 0;
  private static final int MAX_SEM = 999;
  private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$");

  /**
   * 验证版本
   * 迁移对应关系: Go语言ValidateVersion
   * - 功能: 验证版本号
   * - 参数: preVersion - 之前版本, newVersion - 新版本
   * - 用途: 验证版本号的有效性和递增性
   */
  public static boolean validateVersion(String preVersion, String newVersion) {
    if (!StringUtils.hasText(newVersion)) {
      throw new BssException("version is empty");
    }
    // 验证新版本格式
    if (!isValidSemanticVersion(newVersion)) {
      throw new BssException("version '" + newVersion + "' not a valid semantic version");
    }
    // 验证版本段范围
    int[] segments = parseVersionSegments(newVersion);
    for (int segment : segments) {
      if (segment < MIN_SEM || segment > MAX_SEM) {
        throw new BssException("each segment of sem version must be between 0 and 999");
      }
    }
    // 如果之前版本为空，直接返回
    if (!StringUtils.hasText(preVersion)) {
      return true;
    }
    // 验证之前版本格式
    if (!isValidSemanticVersion(preVersion)) {
      throw new BssException("previous version '" + preVersion + "' not a valid semantic version");
    }
    // 验证版本递增
    if (!isVersionGreater(preVersion, newVersion)) {
      throw new BssException("new version '" + newVersion + "' should be greater than '" + preVersion + "'");
    }
    return true;
  }

  /**
   * 简单递增版本号
   * 迁移对应关系: Go语言SimpleIncrementVersion
   * - 功能: 简单的满则进位版本号递增
   * - 参数: version - 当前版本
   * - 返回: 递增后的版本
   * - 用途: 每段范围0-999，满了就进位
   */
  public static String simpleIncrementVersion(String version) {
    if (!StringUtils.hasText(version)) {
      throw new BssException("version is empty");
    }
    String[] parts = version.split("\\.");
    if (parts.length != 3) {
      throw new BssException("version " + version + " is invalid");
    }
    // 将各部分转换为数字
    int[] nums = new int[3];
    for (int i = 0; i < 3; i++) {
      try {
        nums[i] = Integer.parseInt(parts[i]);
      }
      catch (NumberFormatException e) {
        throw new BssException("version " + version + " is invalid", e);
      }
      if (nums[i] < 0 || nums[i] > 999) {
        throw new BssException("version " + version + " is invalid");
      }
    }
    // 从最后一位开始递增并处理进位
    nums[2]++; // 增加修订号
    // 处理进位
    if (nums[2] > 999) {
      nums[2] = 0;
      nums[1]++;
    }
    if (nums[1] > 999) {
      nums[1] = 0;
      nums[0]++;
    }
    if (nums[0] > 999) {
      throw new BssException("version " + version + " is more than max supported version");
    }
    // 重新组合为字符串
    return String.format("%d.%d.%d", nums[0], nums[1], nums[2]);
  }

  /**
   * 验证是否为有效的语义版本
   * 迁移对应关系: Go语言semver.NewVersion
   * - 功能: 验证语义版本格式
   * - 参数: version - 版本字符串
   * - 返回: 是否有效
   * - 用途: 验证版本格式是否正确
   */
  private static boolean isValidSemanticVersion(String version) {
    if (!StringUtils.hasText(version)) {
      return false;
    }
    return VERSION_PATTERN.matcher(version).matches();
  }

  /**
   * 解析版本段
   * 迁移对应关系: Go语言newV.Slice()
   * - 功能: 解析版本段
   * - 参数: version - 版本字符串
   * - 返回: 版本段数组
   * - 用途: 将版本字符串解析为数字数组
   */
  private static int[] parseVersionSegments(String version) {
    String[] parts = version.split("\\.");
    int[] segments = new int[parts.length];
    for (int i = 0; i < parts.length; i++) {
      segments[i] = Integer.parseInt(parts[i]);
    }
    return segments;
  }

  /**
   * 比较版本大小
   * 迁移对应关系: Go语言preV.LessThan(*newV)
   * - 功能: 比较版本大小
   * - 参数: preVersion - 之前版本, newVersion - 新版本
   * - 返回: 新版本是否大于之前版本
   * - 用途: 验证版本递增
   */
  private static boolean isVersionGreater(String preVersion, String newVersion) {
    int[] preSegments = parseVersionSegments(preVersion);
    int[] newSegments = parseVersionSegments(newVersion);
    // 确保两个版本段数相同
    int maxLength = Math.max(preSegments.length, newSegments.length);
    int[] prePadded = padVersionSegments(preSegments, maxLength);
    int[] newPadded = padVersionSegments(newSegments, maxLength);
    for (int i = 0; i < maxLength; i++) {
      if (newPadded[i] > prePadded[i]) {
        return true;
      }
      else if (newPadded[i] < prePadded[i]) {
        return false;
      }
    }
    return false; // 版本相等
  }

  /**
   * 填充版本段
   * 迁移对应关系: Go语言版本段处理
   * - 功能: 填充版本段
   * - 参数: segments - 版本段数组, targetLength - 目标长度
   * - 返回: 填充后的版本段数组
   * - 用途: 确保版本段数一致
   */
  private static int[] padVersionSegments(int[] segments, int targetLength) {
    if (segments.length >= targetLength) {
      return segments;
    }
    int[] padded = new int[targetLength];
    System.arraycopy(segments, 0, padded, 0, segments.length);
    return padded;
  }
}
