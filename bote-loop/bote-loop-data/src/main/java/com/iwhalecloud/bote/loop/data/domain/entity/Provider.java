package com.iwhalecloud.bote.loop.data.domain.entity;



/**
 * 存储提供商枚举
 * 迁移对应关系: Go语言entity.Provider
 * - 功能: 存储提供商类型标识
 * - 字段定义: 各种存储提供商常量
 * <p>
 * Java实现说明:
 * - 对应Go的Provider类型别名
 * - 使用Java枚举定义各种提供商
 * - 提供值的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum Provider {
  UNKNOWN(""),
  TOS("TOS"),
  VETOS("VETOS"),
  HDFS("HDFS"),
  IMAGE_X("ImageX"),
  S3("S3"),
  LOCAL_FS("LocalFS"),
  ABASE("Abase"),
  RDS("RDS");

  private final String value;

  Provider(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static Provider fromValue(String value) {
    for (Provider provider : values()) {
      if (provider.value.equals(value)) {
        return provider;
      }
    }
    return UNKNOWN;
  }
}
