package com.iwhalecloud.bote.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 内嵌 Lcdp 数据类型映射枚举类
 *
 * @author qian.sisheng
 * @since 2024/8/8
 */
@Getter
@RequiredArgsConstructor
public enum SqlTypeEnum {
  TINYINT(AttrDataType.INTEGER),
  SMALLINT(AttrDataType.INTEGER),
  MEDIUMINT(AttrDataType.INTEGER),
  INT(AttrDataType.INTEGER),
  INT2(AttrDataType.INTEGER),
  INT4(AttrDataType.INTEGER),
  INT8(AttrDataType.INTEGER),
  BIGINT(AttrDataType.INTEGER),
  FLOAT(AttrDataType.NUMBER),
  FLOAT4(AttrDataType.NUMBER),
  BINARY_FLOAT(AttrDataType.NUMBER),
  FLOAT8(AttrDataType.NUMBER),
  DOUBLE(AttrDataType.NUMBER),
  NUMERIC(AttrDataType.NUMBER),
  DECIMAL(AttrDataType.NUMBER),
  INTEGER(AttrDataType.INTEGER),
  NUMBER(AttrDataType.INTEGER),
  MONEY(AttrDataType.NUMBER),
  N(AttrDataType.INTEGER),
  SMALLSERIAL(AttrDataType.INTEGER),
  SERIAL(AttrDataType.INTEGER),
  BIGSERIAL(AttrDataType.INTEGER),
  INTUNSIGNED(AttrDataType.INTEGER),

  /** 映射为 String 类型 */
  BPCHAR(AttrDataType.STRING),
  CHAR(AttrDataType.STRING),
  NCHAR(AttrDataType.STRING),
  VARCHAR(AttrDataType.STRING),
  VARCHAR2(AttrDataType.STRING),
  NVARCHAR2(AttrDataType.STRING),
  TINYTEXT(AttrDataType.STRING),
  MEDIUMTEXT(AttrDataType.STRING),
  TEXT(AttrDataType.STRING),
  LONGTEXT(AttrDataType.STRING),
  TINYBLOB(AttrDataType.STRING),
  MEDIUMBLOB(AttrDataType.STRING),
  BLOB(AttrDataType.STRING),
  LONGBLOB(AttrDataType.STRING),
  LONG(AttrDataType.STRING),
  VA(AttrDataType.STRING),
  CLOB(AttrDataType.STRING),
  NCLOB(AttrDataType.STRING),
  BIT(AttrDataType.STRING),
  ENUM(AttrDataType.STRING),

  /** 映射为 Date 类型 */
  YEAR(AttrDataType.DATE),
  TIME(AttrDataType.DATETIME),
  DATE(AttrDataType.DATETIME),
  DATETIME(AttrDataType.DATETIME),
  TIMESTAMP(AttrDataType.DATETIME),
  TIMESTAMPTZ(AttrDataType.DATETIME),
  DT(AttrDataType.DATETIME),
  D(AttrDataType.DATETIME);

  /** 属性类型 */
  private final AttrDataType dataType;

  /**
   * 根据 SQL 类型名称获取属性类型
   */
  @Nullable
  public static AttrDataType getAttrDataTypeByTypeName(@Nullable String sqlTypeName) {
    if (StringUtils.isEmpty(sqlTypeName)) {
      return null;
    }
    for (SqlTypeEnum type : values()) {
      if (type.name().equals(sqlTypeName)) {
        return type.getDataType();
      }
    }
    return null;
  }
}
