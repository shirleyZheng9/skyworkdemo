package com.iwhalecloud.bote.common.sql.consts;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * 数据库常量
 *
 * @author wangtingyun
 * @since 2025-11-21
 */
public final class DatabaseConsts {

  private DatabaseConsts() {
  }

  /** 数据库类型 postgresql */
  public static final String DATABASE_TYPE_PG = "postgresql";
  /** 数据库类型 mysql */
  public static final String DATABASE_TYPE_MYSQL = "mysql";
  /** 数据库类型 oracle */
  public static final String DATABASE_TYPE_ORACLE = "oracle";
  /** 数据库类型 udal */
  public static final String DATABASE_TYPE_UDAL = "udal";
  /** 数据库类型 mdb */
  public static final String DATABASE_TYPE_MDB = "mdb";
  /** 数据库类型 oceanbase */
  public static final String DATABASE_TYPE_OB = "oceanbase";
  /** 数据库类型 达梦 */
  public static final String DATABASE_TYPE_DM = "dm";
  /** 数据库类型 goldendb */
  public static final String DATABASE_TYPE_GOLDEN_DB = "goldendb";
  /** 数据库类型 磐维 */
  public static final String DATABASE_TYPE_PANWEI = "panwei";

  /** 数据库类型 mysql、udal 集合 */
  public static final List<String> DATABASE_TYPES_MYSQL = ImmutableList.of(DATABASE_TYPE_MYSQL, DATABASE_TYPE_UDAL);
  /** 数据库类型 oracle、oceanbase（所内使用 oracle 模式） 集合 */
  public static final List<String> DATABASE_TYPES_ORACLE = ImmutableList.of(DATABASE_TYPE_ORACLE, DATABASE_TYPE_OB);

  /** 数据库系统时间 now() */
  public static final String DATABASE_CURRENT_TIME_NOW = "NOW()";
  /** 数据库系统时间 sysdate */
  public static final String DATABASE_CURRENT_TIME_SYSDATE = "SYSDATE";

  /** 数据库配置变量 - 默认表空间 */
  public static final String DEFAULT_TABLESPACE = "BOTE";
  /** 数据库配置变量 - tablespace */
  public static final String TABLESPACE = "tablespace";
  /** 数据库配置变量 - dbName */
  public static final String DB_NAME = "dbName";
  /** 数据库配置变量 - url */
  public static final String URL = "url";
  /** 数据库配置变量 - username */
  public static final String USERNAME = "username";
  /** 数据库配置变量 - password */
  public static final String PASSWORD = "password";
  /** 数据库配置变量 - 父username */
  public static final String PARENT_USERNAME = "parentUsername";
  /** 数据库配置变量 - 父password */
  public static final String PARENT_PASSWORD = "parentPassword";

  /** 数据类型：字符串 */
  public static final String DATA_TYPE_STRING = "string";
  /** 数据类型：长文本 */
  public static final String DATA_TYPE_TEXT = "text";
  /** 数据类型：整型 */
  public static final String DATA_TYPE_INTEGER = "integer";
  /** 数据类型：日期 */
  public static final String DATA_TYPE_DATE = "date";
  /** 数据类型：日期时间 */
  public static final String DATA_TYPE_DATETIME = "datetime";
  /** 数据类型：浮点数 */
  public static final String DATA_TYPE_NUMBER = "number";

  /** 数据类型：文本型 */
  public static final List<String> DATA_TYPES_STRING = ImmutableList.of(DATA_TYPE_STRING, DATA_TYPE_TEXT);
  /** 数据类型：时间型 */
  public static final List<String> DATA_TYPES_TIME = ImmutableList.of(DATA_TYPE_DATE, DATA_TYPE_DATETIME);

}
