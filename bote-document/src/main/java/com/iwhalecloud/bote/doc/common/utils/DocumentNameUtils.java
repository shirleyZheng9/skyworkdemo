package com.iwhalecloud.bote.doc.common.utils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文档名称工具类
 * 用于处理文档名称的序号管理
 *
 * @author Aiqing
 * @since 2025/8/15
 */
public final class DocumentNameUtils {
  private DocumentNameUtils() {
  }

  // 正则表达式，用于匹配文档名称中的序号部分
  private static final Pattern DOC_NAME_PATTERN = Pattern.compile("^(.+?)\\s*[（(](\\d+)[）)]$");

  /**
   * 生成默认文档名称，包含智能序号管理
   *
   * @param baseName 基础名称（如"无标题文档"、"无标题表格"）
   * @param existingNames 现有文档名称列表
   * @return 生成的文档名称
   */
  public static String generateDefaultDocumentName(String baseName, List<String> existingNames) {
    // 检查是否存在基础名称的文档
    boolean hasBaseName = existingNames.stream().anyMatch(baseName::equals);

    // 找出可用的最小序号
    int nextNumber = findNextAvailableNumber(baseName, existingNames);

    // 生成最终文档名称
    // 如果存在基础名称文档，新文档必须使用序号
    if (hasBaseName) {
      return baseName + "（" + nextNumber + "）";
    }
    else {
      // 如果不存在基础名称文档，且序号为1，则使用基础名称
      return nextNumber == 1 ? baseName : baseName + "（" + nextNumber + "）";
    }
  }

  /**
   * 查找下一个可用的序号
   * 算法逻辑：
   * 1. 检查是否存在基础名称的文档
   * 2. 提取所有已使用的序号
   * 3. 按序号排序
   * 4. 查找第一个缺失的序号
   * 5. 如果没有缺失，返回下一个序号
   *
   * @param baseName 基础名称
   * @param existingNames 现有文档名称列表
   * @return 下一个可用序号
   */
  public static int findNextAvailableNumber(String baseName, List<String> existingNames) {
    // 检查是否存在基础名称的文档
    boolean hasBaseName = existingNames.stream()
      .anyMatch(baseName::equals);

    // 提取所有已使用的序号
    List<Integer> usedNumbers = existingNames.stream()
      .filter(name -> name.startsWith(baseName))
      .map(name -> extractNumberFromName(baseName, name))
      .filter(Objects::nonNull)
      .sorted()
      .collect(Collectors.toList());

    // 如果存在基础名称的文档，从序号1开始查找
    if (hasBaseName) {
      // 查找第一个缺失的序号
      int expectedNumber = 1;
      for (Integer usedNumber : usedNumbers) {
        if (usedNumber > expectedNumber) {
          // 找到了缺失的序号
          return expectedNumber;
        }
        expectedNumber = usedNumber + 1;
      }
      // 如果没有缺失的序号，返回下一个序号
      return expectedNumber;
    }
    else {
      // 如果不存在基础名称的文档，且没有已使用的序号，返回1
      if (usedNumbers.isEmpty()) {
        return 1;
      }

      // 查找第一个缺失的序号
      int expectedNumber = 1;
      for (Integer usedNumber : usedNumbers) {
        if (usedNumber > expectedNumber) {
          // 找到了缺失的序号
          return expectedNumber;
        }
        expectedNumber = usedNumber + 1;
      }
      // 如果没有缺失的序号，返回下一个序号
      return expectedNumber;
    }
  }

  /**
   * 从文档名称中提取序号
   *
   * @param baseName 基础名称
   * @param fullName 完整文档名称
   * @return 序号，如果没有序号则返回null
   */
  public static Integer extractNumberFromName(String baseName, String fullName) {
    // 如果名称完全匹配基础名称，说明没有序号
    if (baseName.equals(fullName)) {
      return null;
    }

    // 使用正则表达式匹配序号
    Matcher matcher = DOC_NAME_PATTERN.matcher(fullName);
    if (matcher.matches()) {
      String namePart = matcher.group(1).trim();
      // 检查名称部分是否匹配基础名称
      if (baseName.equals(namePart)) {
        try {
          return Integer.parseInt(matcher.group(2));
        }
        catch (NumberFormatException e) {
          // 序号解析失败，返回null
          return null;
        }
      }
    }

    return null;
  }

  /**
   * 检查文档名称是否包含序号
   *
   * @param documentName 文档名称
   * @return 是否包含序号
   */
  public static boolean hasNumberSuffix(String documentName) {
    return DOC_NAME_PATTERN.matcher(documentName).matches();
  }

  /**
   * 获取文档名称的基础部分（去除序号）
   *
   * @param documentName 文档名称
   * @return 基础名称
   */
  public static String getBaseName(String documentName) {
    Matcher matcher = DOC_NAME_PATTERN.matcher(documentName);
    if (matcher.matches()) {
      return matcher.group(1).trim();
    }
    return documentName;
  }

  /**
   * 获取文档名称的序号部分
   *
   * @param documentName 文档名称
   * @return 序号，如果没有序号则返回null
   */
  public static Integer getNumberSuffix(String documentName) {
    Matcher matcher = DOC_NAME_PATTERN.matcher(documentName);
    if (matcher.matches()) {
      try {
        return Integer.parseInt(matcher.group(2));
      }
      catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }
}
