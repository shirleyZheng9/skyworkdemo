package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import lombok.Getter;

/**
 * 任务类型枚举
 * 迁移对应关系: Go语言JobType
 * - 功能: 定义IO任务的类型
 * - 值范围: 1-3
 * <p>
 * Java实现说明:
 * - 对应Go的JobType类型
 * - 使用Java枚举定义，包含类型值和方法
 * - 提供类型转换和判断功能
 * <p>
 * 技术栈迁移:
 * - Go常量 -> Java枚举
 * - Go方法 -> Java方法
 * - Go字符串转换 -> Java toString方法
 */
public enum JobType {

  /**
   * 从文件导入
   * 迁移对应关系: Go语言JobType_ImportFromFile
   * - 值: 1
   * - 功能: 从文件导入数据到数据集
   */
  IMPORT_FROM_FILE(1, "ImportFromFile"),

  /**
   * 导出到文件
   * 迁移对应关系: Go语言JobType_ExportToFile
   * - 值: 2
   * - 功能: 从数据集导出数据到文件
   */
  EXPORT_TO_FILE(2, "ExportToFile"),

  /**
   * 导出到数据集
   * 迁移对应关系: Go语言JobType_ExportToDataset
   * - 值: 3
   * - 功能: 从数据集导出数据到另一个数据集
   */
  EXPORT_TO_DATASET(3, "ExportToDataset");

  /**
   * -- GETTER --
   * 获取类型值
   * 迁移对应关系: Go语言JobType的值
   * - 功能: 获取类型的数值表示
   * - 返回: 类型值
   */
  @Getter
  private final int value;
  @Getter
  private final String displayName;

  JobType(int value, String displayName) {
    this.value = value;
    this.displayName = displayName;
  }

  /**
   * 获取显示名称
   * 迁移对应关系: Go语言JobType.String()
   * - 功能: 获取类型的字符串表示
   * - 返回: 类型显示名称
   */
  @Override
  public String toString() {
    return displayName;
  }

  /**
   * 根据字符串获取类型
   * 迁移对应关系: Go语言JobTypeFromString
   * - 功能: 根据字符串获取对应的类型
   * - 参数: s - 类型字符串
   * - 返回: 对应的类型枚举
   * - 异常: 如果字符串无效则抛出异常
   */
  public static JobType fromString(String s) {
    if (s == null) {
      throw new IllegalArgumentException("JobType string cannot be null");
    }

    for (JobType type : values()) {
      if (type.displayName.equals(s)) {
        return type;
      }
    }

    throw new IllegalArgumentException("not a valid JobType string: " + s);
  }

  /**
   * 根据值获取类型
   * 迁移对应关系: Go语言JobType的值转换
   * - 功能: 根据数值获取对应的类型
   * - 参数: value - 类型值
   * - 返回: 对应的类型枚举
   * - 异常: 如果值无效则抛出异常
   */
  public static JobType fromValue(int value) {
    for (JobType type : values()) {
      if (type.value == value) {
        return type;
      }
    }

    throw new IllegalArgumentException("not a valid JobType value: " + value);
  }
}
