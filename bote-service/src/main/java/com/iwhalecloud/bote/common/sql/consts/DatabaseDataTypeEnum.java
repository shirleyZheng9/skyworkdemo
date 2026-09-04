package com.iwhalecloud.bote.common.sql.consts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 数据库字段数据类型映射枚举类
 *
 * @author wangtinyun
 * @since 2025-11-28
 */
@Getter
@RequiredArgsConstructor
public enum DatabaseDataTypeEnum {

  /** 映射为 integer 类型 */
  TINYINT("integer"),
  SMALLINT("integer"),
  MEDIUMINT("integer"),
  INT("integer"),
  INT2("integer"),
  INT4("integer"),
  INT8("integer"),
  BIGINT("integer"),
  INTEGER("integer"),
  NUMBER("integer"),
  N("integer"),
  SMALLSERIAL("integer"),
  SERIAL("integer"),
  BIGSERIAL("integer"),
  INTUNSIGNED("integer"),

  /** 映射为 number 类型 */
  FLOAT("number"),
  FLOAT4("number"),
  BINARY_FLOAT("number"),
  FLOAT8("number"),
  DOUBLE("number"),
  NUMERIC("number"),
  DECIMAL("number"),
  MONEY("number"),

  /** 映射为 string 类型 */
  BPCHAR("string"),
  CHAR("string"),
  NCHAR("string"),
  VARCHAR("string"),
  VARCHAR2("string"),
  NVARCHAR2("string"),
  TINYTEXT("string"),
  MEDIUMTEXT("string"),
  TINYBLOB("string"),
  MEDIUMBLOB("string"),
  VA("string"),
  BIT("string"),
  ENUM("string"),

  /** 映射为 text(大字段) 类型 */
  TEXT("text"),
  LONGTEXT("text"),
  LONGBLOB("text"),
  LONG("text"),
  BLOB("text"),
  CLOB("text"),
  NCLOB("text"),

  /** 映射为 date 类型 */
  YEAR("date"),
  TIME("date"),
  DATE("date"),
  D("date"),

  /** 映射为 datetime 类型 */
  DATETIME("datetime"),
  TIMESTAMP("datetime"),
  TIMESTAMPTZ("datetime"),
  DT("datetime");

  /** 字段数据类型 */
  private final String columnDataType;

  /**
   * 查找指定字段类型对应的属性类型，支持动态配置
   * <p>先查缓存，再查已有</p>
   *
   * @param dataType 字段类型
   * @return 属性类型
   */
  public static String findColumnDataType(String dataType) {
    for (DatabaseDataTypeEnum value : values()) {
      if (value.name().equalsIgnoreCase(dataType)) {
        return value.getColumnDataType();
      }
    }
    return "string";
  }

}
