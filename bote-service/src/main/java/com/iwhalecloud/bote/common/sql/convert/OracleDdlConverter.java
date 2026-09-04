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
public class OracleDdlConverter extends AbstractDdlConverter {

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
        definition = " varchar2(" + dataLength + ")";
        break;
      case DatabaseConsts.DATA_TYPE_TEXT:
        definition = " clob";
        break;
      case DatabaseConsts.DATA_TYPE_INTEGER:
        definition = " number(" + dataLength + ") ";
        break;
      case DatabaseConsts.DATA_TYPE_DATETIME:
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
      return " default sysdate";
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
    String sql = TableModelConsts.TABLE_COLUMN_ALTER_OM;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
    String fragment = tableColumn.getColumnCode() + " " + getDefinitionForDataTypeAndLength(tableColumn.getDataType(), tableColumn.getDataLength(),
      tableColumn.getDataScale()) + getDefinitionForDefaultValue(tableColumn.getDataType(), tableColumn.getDefaultValue());
    if (isModNullable) {
      fragment = fragment + (BaseConsts.TRUE.equals(tableColumn.getNullable()) ? " null" : " not null");
    }
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, fragment);
    return sql;
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
    String sql = TableModelConsts.CREATE_SEQUENCE_O;
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
    String sql = TableModelConsts.SEQUENCE_RENAME_O;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, oldSequenceCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, newSequenceCode);
    return sql;
  }
}
