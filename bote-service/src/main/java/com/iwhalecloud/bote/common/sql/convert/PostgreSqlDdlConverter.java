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
public class PostgreSqlDdlConverter extends AbstractDdlConverter {

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
        definition = " text";
        break;
      case DatabaseConsts.DATA_TYPE_INTEGER:
        definition = getNumberType(dataLength);
        break;
      case DatabaseConsts.DATA_TYPE_DATETIME:
        definition = " timestamptz";
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
   * 根据字段长度获取字段类型
   *
   * @param dataLength 数据长度
   * @return 字段类型
   */
  private String getNumberType(Long dataLength) {
    String definition;
    if (dataLength <= 6) {
      definition = " int2";
    }
    else if (dataLength <= 11) {
      definition = " int4";
    }
    else {
      definition = " bigint";
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
   * 获取更改字段定义 SQL，子类实现
   *
   * @param tableColumn 字段信息
   * @return 更改字段 SQL
   */
  @Override
  protected String getModifyColumnSql(DataTableColumnDTO tableColumn, boolean isModNullable) {
    StringBuilder sql = new StringBuilder();
    // postgresql 数据库修改字段信息需要分开修改
    // 1. 修改类型 形如：alter table @@ alter column @@ type bigint;
    String alterTypeSql = TableModelConsts.TABLE_COLUMN_ALTER;
    alterTypeSql = alterTypeSql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
    StringBuilder typeFragment = new StringBuilder();
    typeFragment.append(tableColumn.getColumnCode());
    typeFragment.append(" type ");
    String segment = getDefinitionForDataTypeAndLength(tableColumn.getDataType(), tableColumn.getDataLength(), tableColumn.getDataScale());
    typeFragment.append(segment);
    // 拼接 using 字段名::字段类型
    typeFragment.append(String.format(" using %s::%s", tableColumn.getColumnCode(), segment.trim()));
    alterTypeSql = alterTypeSql.replaceFirst(TableModelConsts.REPLACE_CHAR, typeFragment.toString());
    sql.append(alterTypeSql);
    // 2. 修改默认值 形如：alter table @@ alter column @@ set default '1234';
    String definitionForDefaultValue = getDefinitionForDefaultValue(tableColumn.getDataType(), tableColumn.getDefaultValue());
    if (StringUtils.isNotEmpty(definitionForDefaultValue)) {
      String alterDefaultSql = TableModelConsts.TABLE_COLUMN_ALTER;
      alterDefaultSql = alterDefaultSql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
      String defaultFragment = tableColumn.getColumnCode() + " set " + definitionForDefaultValue;
      alterDefaultSql = alterDefaultSql.replaceFirst(TableModelConsts.REPLACE_CHAR, defaultFragment);
      sql.append(alterDefaultSql);
    }
    // 3. 修改非空 形如：alter table @@ alter column @@ set[drop] not null;
    if (isModNullable) {
      String alterNotNullSql = TableModelConsts.TABLE_COLUMN_ALTER;
      alterNotNullSql = alterNotNullSql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
      String notNullFragment =
        tableColumn.getColumnCode() + (BaseConsts.FALSE.equals(tableColumn.getNullable()) ? " set not null" : " drop not null");
      alterNotNullSql = alterNotNullSql.replaceFirst(TableModelConsts.REPLACE_CHAR, notNullFragment);
      sql.append(alterNotNullSql);
    }

    return sql.toString();
  }

  @Override
  protected String getModifyPrimaryKeySql(DataTableColumnDTO tableColumn, boolean isPrimaryKey) {
    if (isPrimaryKey) {
      String sql = TableModelConsts.ADD_PRIMARY_KEY;
      sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
      sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getColumnCode());
      return sql;
    }
    else {
      String sql = TableModelConsts.DROP_PRIMARY_KEY_P;
      sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
      sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
      return sql;
    }
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
    String sql = TableModelConsts.CREATE_SEQUENCE_P;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, sequenceCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, incrementBy.toString());
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, maxValue.toString());
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, startBy.toString());
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
    String sql = TableModelConsts.SEQUENCE_RENAME_P;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, oldSequenceCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, newSequenceCode);
    return sql;
  }
}
