package com.iwhalecloud.bote.common.sql.convert;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.sql.consts.DatabaseConsts;
import com.iwhalecloud.bote.common.sql.consts.TableModelConsts;
import com.iwhalecloud.bote.dto.base.DataSourceProperties;
import com.iwhalecloud.bote.dto.database.DataTableColumnDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class MysqlDdlConverter extends AbstractDdlConverter {
  /**
   * 获取字段类型和长度定义，子类实现
   *
   * @param dataType 字段类型
   * @param dataLength 字段长度
   * @param dataScale 字段精度
   * @return 字段类型和长度定义
   */
  @Override
  protected String getDefinitionForDataTypeAndLength(String dataType, @Nullable Long dataLength, @Nullable Long dataScale) {
    dataLength = dataLength == null ? Long.valueOf(1L) : dataLength;
    String definition = "";
    switch (dataType) {
      case DatabaseConsts.DATA_TYPE_STRING:
        definition = " varchar(" + dataLength + ")";
        break;
      case DatabaseConsts.DATA_TYPE_TEXT:
        definition = " longtext";
        break;
      case DatabaseConsts.DATA_TYPE_INTEGER:
        if (dataLength <= 8) {
          definition = " int(" + dataLength + ") ";
        }
        else {
          definition = " bigint";
        }
        break;
      case DatabaseConsts.DATA_TYPE_DATETIME:
        definition = " datetime";
        break;
      case DatabaseConsts.DATA_TYPE_DATE:
        definition = " date";
        break;
      case DatabaseConsts.DATA_TYPE_NUMBER:
        definition = " numeric(" + dataLength + "," + dataScale + ") ";
        break;
      default:
        throw BaseErrorConstant.ILLEGAL_DATA_TYPE.toException(dataType);
    }
    return definition;
  }

  /**
   * 获取默认值定义，子类实现
   *
   * @param dataType 字段类型
   * @param defaultValue 默认值
   * @return 默认值定义
   */
  @Override
  protected String getDefinitionForDefaultValue(String dataType, String defaultValue) {
    if (StringUtils.isEmpty(defaultValue)) {
      return "";
    }
    if (DatabaseConsts.DATA_TYPES_TIME.contains(dataType)) {
      return " default now()";
    }
    return " default '" + defaultValue + "'";
  }

  /**
   * 获取更改字段编码 SQL，子类实现
   *
   * @param tableColumn 字段信息
   * @param oldColumnCode 旧字段编码
   * @param newColumnCode 新字段编码
   * @return 更改字段编码 SQL
   */
  @Override
  protected String getRenameColumnCodeSql(DataTableColumnDTO tableColumn, String oldColumnCode, String newColumnCode) {
    String sql = TableModelConsts.TABLE_COLUMN_RENAME_M;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, oldColumnCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, newColumnCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, getColumnFragment(tableColumn));
    return sql;
  }

  /**
   * 获取更改字段定义 SQL，子类实现
   *
   * @param tableColumn 字段信息
   * @return 更改字段 SQL
   */
  @Override
  protected String getModifyColumnSql(DataTableColumnDTO tableColumn, boolean isModNullable) {
    String sql = TableModelConsts.TABLE_COLUMN_ALTER_OM;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
    String fragment = tableColumn.getColumnCode() + " " + getColumnFragment(tableColumn);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, fragment);
    return sql;
  }

  /**
   * 获取更改字段备注 SQL，子类实现
   *
   * @param tableColumn 字段信息
   * @return 更改字段备注 SQL
   */
  @Override
  protected String getModifyColumnCommonSql(DataTableColumnDTO tableColumn) {
    String sql = TableModelConsts.TABLE_COLUMN_REMARK_M;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getColumnCode());
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, getColumnFragment(tableColumn));
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getRemark());
    return sql.toLowerCase();
  }

  /**
   * 获取更改表名备注 SQL，默认逻辑，可子类实现
   *
   * @param tableCode 表编码
   * @param tableName 表备注
   * @return 更改表名备注 SQL
   */
  @Override
  protected String getModifyTableCommentSql(String tableCode, String tableName) {
    String sql = TableModelConsts.TABLE_COMMENT_M;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableName);
    return sql.toLowerCase();
  }

  private String getColumnFragment(DataTableColumnDTO tableColumn) {
    StringBuilder fragment = new StringBuilder();
    fragment.append(getDefinitionForDataTypeAndLength(tableColumn.getDataType(), tableColumn.getDataLength(), tableColumn.getDataScale()));
    fragment.append(getDefinitionForDefaultValue(tableColumn.getDataType(), tableColumn.getDefaultValue()));
    fragment.append(BaseConsts.TRUE.equals(tableColumn.getNullable()) ? "" : " not null");
    return fragment.toString();
  }

  /**
   * 根据序列信息，构造新增序列脚本，子类实现
   *
   * @param sequenceCode 序列编码
   * @param startBy 初始值
   * @param incrementBy 步长
   * @param maxValue 最大值
   * @return 新增序列脚本
   */
  @Override
  protected String getAddSequenceSql(String sequenceCode, Long startBy, Long incrementBy, Long maxValue) {
    String sql = TableModelConsts.CREATE_SEQUENCE_M;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, sequenceCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, incrementBy.toString());
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, maxValue.toString());
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, startBy.toString());
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, startBy.toString());
    return sql;
  }

  /**
   * 根据序列信息，构造删除序列脚本，子类实现
   *
   * @param sequenceCode 序列编码
   * @return 删除序列脚本
   */
  @Override
  protected String getDropSequenceSql(String sequenceCode) {
    String sql = TableModelConsts.DROP_SEQUENCE_M;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, sequenceCode);
    return sql;
  }

  /**
   * 根据序列信息，构造修改序列脚本，由子类实现
   *
   * @param sequenceCode 序列编码
   * @param isIncrementBy 是否修改步长
   * @param incrementBy 步长
   * @param isMaxValue 是否修改最大值
   * @param maxValue 最大值
   * @return 修改序列脚本
   */
  @Override
  protected String getModifySequenceSql(String sequenceCode, boolean isIncrementBy, Long incrementBy, boolean isMaxValue, Long maxValue) {
    String sql = "";
    if (isIncrementBy) {
      String sql2 = TableModelConsts.ALTER_SEQUENCE_M;
      sql2 = sql2.replaceFirst(TableModelConsts.REPLACE_CHAR, "increment_by");
      sql2 = sql2.replaceFirst(TableModelConsts.REPLACE_CHAR, incrementBy.toString());
      sql2 = sql2.replaceFirst(TableModelConsts.REPLACE_CHAR, sequenceCode);
      sql = sql + sql2;
    }
    if (isMaxValue) {
      String sql2 = TableModelConsts.ALTER_SEQUENCE_M;
      sql2 = sql2.replaceFirst(TableModelConsts.REPLACE_CHAR, "max_value");
      sql2 = sql2.replaceFirst(TableModelConsts.REPLACE_CHAR, maxValue.toString());
      sql2 = sql2.replaceFirst(TableModelConsts.REPLACE_CHAR, sequenceCode);
      sql = sql + sql2;
    }
    return sql;
  }

  /**
   * 根据序列信息，构造改序列名脚本
   *
   * @param oldSequenceCode 序列编码旧值
   * @param newSequenceCode 序列编码新值
   * @return 修改序列名脚本
   */
  @Override
  protected String getRenameSequenceSql(String oldSequenceCode, String newSequenceCode, DataSourceProperties dataSourceProperties) {
    if (Strings.CI.equals(oldSequenceCode, newSequenceCode)) {
      return "";
    }
    String sql = TableModelConsts.SEQUENCE_RENAME_M;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, oldSequenceCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, newSequenceCode);
    return sql;
  }

  /**
   * 获取删除索引脚本，子类实现
   *
   * @param indexCode 索引编码
   * @param tableCode 表编码
   * @return 删除索引脚本
   */
  @Override
  protected String getDropIndexSql(String indexCode, String tableCode) {
    String sql = TableModelConsts.DROP_INDEX_M;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, indexCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableCode);
    return sql;
  }
}
