package com.iwhalecloud.bote.common.sql.consts;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * 表模型常量定义
 *
 * @author wangtingyun
 * @since 2025-11-24
 */
public final class TableModelConsts {
  private TableModelConsts() {
  }

  /** DDL模版替换符 */
  public static final String REPLACE_CHAR = "@@";
  /** DDL模版 表创建 */
  public static final String CREATE_TABLE = "CREATE TABLE @@ ( @@ );";
  /** DDL模版 表创建 DISTRIBUTED */
  public static final String CREATE_TABLE_DISTRIBUTED = "CREATE TABLE @@ ( @@ ) DISTRIBUTED BY DUPLICATE(g1,g2);";
  /** DDL模版 表创建 DISTRIBUTED，需要指定分片 */
  public static final String CREATE_TABLE_DISTRIBUTED_SHADING = "CREATE TABLE @@ ( @@ ) DISTRIBUTED BY DUPLICATE(@@@);";
  /** DDL模版 表主键定义 */
  public static final String TABLE_PRIMARY_KEY = "PRIMARY KEY (@@)";
  /** DDL模版 增加主键 */
  public static final String ADD_PRIMARY_KEY = "ALTER TABLE @@ ADD PRIMARY KEY (@@);";
  /** DDL模版 删除主键 */
  public static final String DROP_PRIMARY_KEY_P = "ALTER TABLE @@ DROP CONSTRAINT @@_PKEY;";
  public static final String DROP_PRIMARY_KEY_OM = "ALTER TABLE @@ DROP PRIMARY KEY;";
  /** DDL模版 表说明 */
  public static final String TABLE_COMMENT = "COMMENT ON TABLE @@ IS '@@';";
  public static final String TABLE_COMMENT_M = "ALTER TABLE @@ COMMENT '@@';";
  /** DDL模版 表重命名 */
  public static final String TABLE_RENAME = "ALTER TABLE @@ RENAME TO @@;";
  /** DDL模版 表删除 */
  public static final String DROP_TABLE = "DROP TABLE @@;";
  /** DDL模版 新加表字段 */
  public static final String ADD_TABLE_COLUMN = "ALTER TABLE @@ ADD @@;";
  /** DDL模版 表字段重命名 */
  public static final String TABLE_COLUMN_RENAME = "ALTER TABLE @@ RENAME COLUMN @@ TO @@;";
  public static final String TABLE_COLUMN_RENAME_M = "ALTER TABLE @@ CHANGE @@ @@ @@;";
  /** DDL模版 修改表字段 */
  public static final String TABLE_COLUMN_ALTER = "ALTER TABLE @@ ALTER COLUMN @@;";
  public static final String TABLE_COLUMN_ALTER_OM = "ALTER TABLE @@ MODIFY @@;";
  /** DDL模版 修改表字段备注 */
  public static final String TABLE_COLUMN_REMARK = "COMMENT ON COLUMN @@.@@ IS '@@';";
  public static final String TABLE_COLUMN_REMARK_M = "ALTER TABLE @@ MODIFY COLUMN @@ @@ COMMENT '@@';";
  /** DDL模版 删除表字段 */
  public static final String DROP_TABLE_COLUMN = "ALTER TABLE @@ DROP COLUMN @@;";
  /** DDL模版 创建索引 */
  public static final String CREATE_INDEX = "CREATE INDEX @@ ON @@(@@);";
  /** DDL模版 创建唯一索引 */
  public static final String CREATE_UNIQUE_INDEX = "CREATE UNIQUE INDEX @@ ON @@(@@);";
  /** DDL模版 删除索引 */
  public static final String DROP_INDEX = "DROP INDEX @@;";
  public static final String DROP_INDEX_M = "DROP INDEX @@ ON @@;";
  /** DDL模版 创建序列 */
  public static final String CREATE_SEQUENCE_P = "CREATE SEQUENCE @@ INCREMENT @@ MAXVALUE @@ START @@ CACHE 1;";
  public static final String CREATE_SEQUENCE_O = "CREATE SEQUENCE @@ INCREMENT BY @@ MAXVALUE @@ START WITH @@ NOCYCLE NOCACHE;";
  public static final String CREATE_SEQUENCE_M =
    "INSERT INTO SYS_SEQUENCE(SEQUENCE_NAME, INCREMENT_BY, MAX_VALUE, START_WITH, `LAST_VALUE`) VALUES ('@@', '@@', '@@', '@@', '@@'); COMMIT;";

  /** DDL模版 序列重命名 */
  public static final String SEQUENCE_RENAME_P = "ALTER SEQUENCE @@ RENAME TO @@ ;";
  public static final String SEQUENCE_RENAME_O = "RENAME @@ TO @@ ;";
  public static final String SEQUENCE_RENAME_M = "UPDATE SYS_SEQUENCE SET SEQUENCE_NAME = '@@' where SEQUENCE_NAME = '@@'; COMMIT;";
  /** DDL模版 修改序列 */
  public static final String ALTER_SEQUENCE = "ALTER SEQUENCE @@ @@ @@;";
  public static final String ALTER_SEQUENCE_M = "UPDATE SYS_SEQUENCE SET @@ = @@ where SEQUENCE_NAME = '@@'; COMMIT;";
  /** DDL模版 删除序列 */
  public static final String DROP_SEQUENCE = "DROP SEQUENCE @@ ;";
  public static final String DROP_SEQUENCE_M = "DELETE FROM SYS_SEQUENCE where SEQUENCE_NAME = '@@'; COMMIT;";

  /** 字段类型 int2 */
  public static final String INT2 = " int2";
  /** 字段类型 int4 */
  public static final String INT4 = " int4";
  /** 字段类型 int8 */
  public static final String INT8 = " int8";
  /** 字段类型 timestamptz */
  public static final String TIMESTAMPTZ = " timestamptz";
  /** 字段类型 timestamptz */
  public static final String DATETIME = " DATETIME";
  /** 字段类型 timestamptz */
  public static final String DATE = " DATE";

  /** 唯一索引命名模板 - 表名_字段名_uindex */
  public static final String UNIQUE_INDEX_NAME = "@@_@@_uindex";

  /** 审核对象 表 */
  public static final String ITEM_TYPE_TABLE = "1";
  /** 审核对象 序列 */
  public static final String ITEM_TYPE_SEQUENCE = "2";
  /** 审核对象 脚本 */
  public static final String ITEM_TYPE_SCRIPT = "3";

  /** 审核状态 待审核 */
  public static final String ITEM_STATUS_AUDITING = "1000";
  /** 审核状态 审核通过 */
  public static final String ITEM_STATUS_PASS = "2000";
  /** 审核状态 不通过 */
  public static final String ITEM_STATUS_REJECT = "3000";

  /** 应用模型（表） */
  public static final String APP_MODEL_TABLE = "APP_MODEL_TABLE";
  /** 应用模型（表字段） */
  public static final String APP_MODEL_TABLE_COLUMN = "APP_MODEL_TABLE_COLUMN";
  /** 应用模型（索引） */
  public static final String APP_MODEL_TABLE_INDEX = "APP_MODEL_TABLE_INDEX";
  /** 应用模型（序列） */
  public static final String APP_MODEL_SEQUENCE = "APP_MODEL_SEQUENCE";
  /** 应用模型（脚本） */
  public static final String APP_MODEL_SCRIPT = "APP_MODEL_SCRIPT";
  /** 应用模型表关联状态 外键关联 */
  public static final String APP_MODEL_TABLE_RELATION_TYPE_FOREIGN_KEY = "1000";
  /** 默认文件上传后缀 **/
  public static final String DEFAULT_SUFFIX = ".";

  /** 应用表关联方式为主关联 **/
  public static final String TABLE_RELATION_TYPE_MAIN = "mainRel";
  /** 应用表关联方式为被关联 **/
  public static final String TABLE_RELATION_TYPE_PASSIVE = "passiveRel";

  /** 普通表 */
  public static final String TABLE_USE_TYPE_COMMON = "1000";
  /** 关联表 */
  public static final String TABLE_USE_TYPE_RELATION = "1001";
  /** 层级树表 */
  public static final String TABLE_USE_TYPE_TREE = "1002";
  /** 表用途 */
  public static final List<String> TABLE_USE_TYPE_LIST = ImmutableList.of(TABLE_USE_TYPE_COMMON, TABLE_USE_TYPE_RELATION, TABLE_USE_TYPE_TREE);
  /** 应用系统表 */
  public static final List<String> APP_SYSTEM_TABLE_CODE = ImmutableList.of("app_flow_order", "app_flow_work_order",
    "lcdp_app_file_info", "lcdp_business_number", "lcdp_domain_common_inst", "lcdp_domain_organization",
    "lcdp_domain_role", "lcdp_domain_user", "lcdp_domain_user_role_rel", "sys_sequence");

  /** 表导入字段数据类型---整数 **/
  public static final String TABLE_IMPORT_DATA_TYPE_NUMBER = "N";
  /** 表导入字段数据类型---字符串 **/
  public static final String TABLE_IMPORT_DATA_TYPE_VARCHAR = "VA";
  /** 表导入状态1001---未导入 **/
  public static final String TABLE_IMPORT_STATUS_1001 = "1001";

  /** 模型分类 数据库建表 */
  public static final String MODEL_TYPE_DB = "db";
  /** 模型分类 元数据 */
  public static final String MODEL_TYPE_META = "metadata";
  /** 模型分类 正则校验 */
  public static final String MODEL_TYPE_REGEXP = "db|metadata";

  /** 表类型 横向*/
  public static final String TABLE_TYPE_HORIZONTAL = "1000";
  /** 表类型 纵向*/
  public static final String TABLE_TYPE_VERTICAL = "1001";

  /** 元数据原子服务目录节点Id */
  public static final String DC_PARAM_META_PROVIDER_DATA_CATALOG_ITEM_ID = "META_DATA_PROVIDER_CATALOG_ITEM_ID";

  /** 元数据模型上下级关联字段编码 */
  public static final String META_DATA_RELATION_CODE = "relation_code";
  public static final String META_DATA_RELATION_NAME = "关联字段";

  /** 模型编码字段编码:只能包含字母、数字、下划线，且必须以字母开头 正则校验 */
  public static final String CODE_REGEXP = "^[a-zA-Z][a-zA-Z0-9_]*$";
}
