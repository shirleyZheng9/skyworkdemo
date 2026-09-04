package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import lombok.Getter;

/**
 * 文件格式枚举
 * 迁移对应关系: Go语言FileFormat
 * - 功能: 定义文件格式类型
 * - 值范围: 1-3, 100
 * <p>
 * Java实现说明:
 * - 对应Go的FileFormat类型
 * - 使用Java枚举定义，包含格式值和方法
 * - 提供格式转换和判断功能
 * <p>
 * 技术栈迁移:
 * - Go常量 -> Java枚举
 * - Go方法 -> Java方法
 * - Go字符串转换 -> Java toString方法
 */
public enum FileFormat {

  /**
   * JSONL格式
   * 迁移对应关系: Go语言FileFormat_JSONL
   * - 值: 1
   * - 功能: JSON Lines格式
   */
  JSONL(1, "JSONL"),

  /**
   * Parquet格式
   * 迁移对应关系: Go语言FileFormat_Parquet
   * - 值: 2
   * - 功能: Parquet列式存储格式
   */
  PARQUET(2, "Parquet"),

  /**
   * CSV格式
   * 迁移对应关系: Go语言FileFormat_CSV
   * - 值: 3
   * - 功能: 逗号分隔值格式
   */
  CSV(3, "CSV"),

  /**
   * ZIP压缩格式
   * 迁移对应关系: Go语言FileFormat_ZIP
   * - 值: 100
   * - 功能: ZIP压缩格式
   */
  ZIP(100, "ZIP");

  /**
   * -- GETTER --
   * 获取格式值
   * 迁移对应关系: Go语言FileFormat的值
   * - 功能: 获取格式的数值表示
   * - 返回: 格式值
   */
  @Getter
  private final int value;
  @Getter
  private final String displayName;

  FileFormat(int value, String displayName) {
    this.value = value;
    this.displayName = displayName;
  }

  /**
   * 获取显示名称
   * 迁移对应关系: Go语言FileFormat.String()
   * - 功能: 获取格式的字符串表示
   * - 返回: 格式显示名称
   */
  @Override
  public String toString() {
    return displayName;
  }

  /**
   * 根据字符串获取格式
   * 迁移对应关系: Go语言FileFormat的字符串转换
   * - 功能: 根据字符串获取对应的格式
   * - 参数: s - 格式字符串
   * - 返回: 对应的格式枚举
   * - 异常: 如果字符串无效则抛出异常
   */
  public static FileFormat fromString(String s) {
    if (s == null) {
      throw new IllegalArgumentException("FileFormat string cannot be null");
    }

    for (FileFormat format : values()) {
      if (format.displayName.equals(s)) {
        return format;
      }
    }

    throw new IllegalArgumentException("not a valid FileFormat string: " + s);
  }

  /**
   * 根据值获取格式
   * 迁移对应关系: Go语言FileFormat的值转换
   * - 功能: 根据数值获取对应的格式
   * - 参数: value - 格式值
   * - 返回: 对应的格式枚举
   * - 异常: 如果值无效则抛出异常
   */
  public static FileFormat fromValue(int value) {
    for (FileFormat format : values()) {
      if (format.value == value) {
        return format;
      }
    }

    throw new IllegalArgumentException("not a valid FileFormat value: " + value);
  }

  /**
   * 判断是否为压缩格式
   * 迁移对应关系: Go语言FileFormat的值范围判断
   * - 功能: 判断格式是否为压缩格式
   * - 返回: 是否为压缩格式
   * - 用途: 根据值范围判断格式类型
   */
  public boolean isCompressFormat() {
    return value >= 100 && value < 200;
  }
}
