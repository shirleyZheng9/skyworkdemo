package com.iwhalecloud.bote.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 数据类型映射枚举类
 *
 * @author chen.linfa
 * @since 2024-09-12
 */
@Getter
@RequiredArgsConstructor
public enum DataTypeMapEnum {

  /** 映射为 Long 类型 */
  TINYINT("Long"),
  SMALLINT("Long"),
  MEDIUMINT("Long"),
  INT("Long"),
  INT2("Long"),
  INT4("Long"),
  INT8("Long"),
  BIGINT("Long"),
  INTEGER("Long"),
  NUMBER("Long"),
  N("Long"),
  SMALLSERIAL("Long"),
  SERIAL("Long"),
  BIGSERIAL("Long"),
  INTUNSIGNED("Long"),

  /** 映射为 BigDecimal */
  FLOAT("BigDecimal"),
  FLOAT4("BigDecimal"),
  BINARY_FLOAT("BigDecimal"),
  FLOAT8("BigDecimal"),
  DOUBLE("BigDecimal"),
  NUMERIC("BigDecimal"),
  DECIMAL("BigDecimal"),
  MONEY("BigDecimal"),

  /** 映射为 String 类型 */
  BPCHAR("String"),
  CHAR("String"),
  NCHAR("String"),
  VARCHAR("String"),
  VARCHAR2("String"),
  NVARCHAR2("String"),
  TINYTEXT("String"),
  TEXT("String"),
  MEDIUMTEXT("String"),
  LONGTEXT("String"),
  TINYBLOB("String"),
  BLOB("String"),
  MEDIUMBLOB("String"),
  LONGBLOB("String"),
  LONG("String"),
  VA("String"),
  CLOB("String"),
  NCLOB("String"),
  BIT("String"),
  ENUM("String"),

  /** 映射为 Date 类型 */
  TIME("Date"),
  DATE("Date"),
  DATETIME("Date"),
  TIMESTAMP("Date"),
  TIMESTAMPTZ("Date"),
  DT("Date"),
  D("Date");

  /** java数据类型 */
  private final String javaDataType;
}
