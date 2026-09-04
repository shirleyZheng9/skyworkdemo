package com.iwhalecloud.bote.common.sql.convert;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.sql.consts.TableModelConsts;
import com.iwhalecloud.bote.dto.base.DataSourceProperties;
import com.iwhalecloud.bote.dto.database.DataTableColumnDTO;
import com.iwhalecloud.bote.dto.database.DataTableDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractDdlConverter {

  /**
   * 根据建模信息，构造建表脚本
   *
   * @param tableDTO 建模信息
   * @return 建表脚本
   */
  public String parseCreateTableSql(DataTableDTO tableDTO) {
    // 组装建表脚本
    String sql = getCreateTableSql(tableDTO);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableDTO.getTableCode());
    StringBuilder columnStr = new StringBuilder();
    if (CollectionUtils.isNotEmpty(tableDTO.getTableColumns())) {
      List<String> columnSqls = new ArrayList<>();
      for (DataTableColumnDTO column : tableDTO.getTableColumns()) {
        columnSqls.add(getTableColumnSql(column));
      }
      columnStr.append(StringUtils.join(columnSqls, ","));

      // 组装主键脚本
      String primaryKeys = tableDTO.getTableColumns().stream().filter(p -> BaseConsts.TRUE.equals(p.getPrimaryKey()))
        .map(DataTableColumnDTO::getColumnCode).collect(Collectors.joining(","));
      if (StringUtils.isNotEmpty(primaryKeys)) {
        columnStr.append(",").append(TableModelConsts.TABLE_PRIMARY_KEY.replaceFirst(TableModelConsts.REPLACE_CHAR, primaryKeys));
      }
    }

    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, columnStr.toString());

    // 组装索引脚本
    StringBuilder sqlBuilder = new StringBuilder(sql);
    //for (AppModelTableIndexDTO index : CollectionUtils.emptyIfNull(appModelTable.getTableIndexes())) {
    //  String columns = index.getIndexColumns().stream().map(AppModelTableIndexColDTO::getColumnCode).collect(Collectors.joining(","));
    //  sqlBuilder.append(parseCreateIndexSql(index.getIndexName(), index.getTableCode(), columns));
    //}

    // 组装唯一索引脚本
    //List<AppModelTableColumnDTO> uniqueKeys = CollectionUtils.emptyIfNull(appModelTable.getTableColumns()).stream()
    //  .filter(AppModelScriptUtil::isUniqueKey).collect(Collectors.toList());
    //for (AppModelTableColumnDTO uniqueKey : uniqueKeys) {
    //  sqlBuilder.append(parseCreateUniqueIndexSql(uniqueKey.getUniqueIndexCode(), appModelTable.getTableCode(), uniqueKey.getColumnCode()));
    //}

    // 组装字段备注脚本
    for (DataTableColumnDTO column : CollectionUtils.emptyIfNull(tableDTO.getTableColumns())) {
      column.setTableCode(tableDTO.getTableCode());
      if (StringUtils.isEmpty(column.getRemark())) {
        column.setRemark(column.getColumnName());
      }
      sqlBuilder.append(getModifyColumnCommonSql(column));
    }
    // 组装表名备注脚本
    if (StringUtils.isNotBlank(tableDTO.getTableName())) {
      sqlBuilder.append(getModifyTableCommentSql(tableDTO.getTableCode(), tableDTO.getTableName()));
    }

    sql = sqlBuilder.toString();
    return sql;
  }

  /**
   * 获取表创建语句模板
   * @param tableDTO 建模信息
   * @return 结果
   */
  protected String getCreateTableSql(DataTableDTO tableDTO) {
    return TableModelConsts.CREATE_TABLE;
  }

  /**
   * 根据建模信息，构造删表脚本
   * <p>避免删表操作，改为更改表名</p>
   *
   * @param backupTableCode 备份表名
   * @return 建删表脚本
   */
  public String getDropTableSql(String backupTableCode) {
    String[] codes = backupTableCode.split(",");
    return getRenameTableSql(codes[0], codes[1]);
  }

  /**
   * 根据建模信息，构造改表名脚本
   *
   * @param oldValue 旧值
   * @param newValue 新值
   * @return 建表脚本
   */
  public String getRenameTableSql(String oldValue, String newValue) {
    if (oldValue.equalsIgnoreCase(newValue)) {
      return null;
    }
    String sql = TableModelConsts.TABLE_RENAME;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, oldValue);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, newValue);
    return sql;
  }

  /**
   * 根据建模字段信息，构造新增表字段脚本
   *
   * @param tableColumn 建模表字段信息
   * @return 新增表字段脚本
   */
  public String getAddTableColumnSql(DataTableColumnDTO tableColumn) {
    String sql = TableModelConsts.ADD_TABLE_COLUMN;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, getTableColumnSql(tableColumn));
    return sql;
  }

  /**
   * 根据建模字段信息，构造修改表字段脚本
   *
   * @param tableColumn 本次变更的表字段
   * @param isModifyCode 是否调整字段基本信息
   * @param isPrimaryKey 是否设置主键（为空则不调整）
   * @param isModifyComment 是否调整备注
   * @param oldColumnCode 旧字段编码（为空则不调整）
   * @param isUniqueKey 是否设置唯一索引（为空则不调整）
   * @param oldUniqueIndexCode 旧唯一索引编码（用于删除唯一索引）
   * @param isModNullable 是否调整可为空（用于 oracle 数据库修改字段的必填）
   * @return 修改表字段脚本
   */
  public String getModifyTableColumnSql(DataTableColumnDTO tableColumn, boolean isModifyCode, @Nullable Boolean isPrimaryKey,
    boolean isModifyComment, @Nullable String oldColumnCode, @Nullable Boolean isUniqueKey, @Nullable String oldUniqueIndexCode,
    boolean isModNullable) {
    StringBuilder sql = new StringBuilder();
    if (oldColumnCode != null) {
      sql.append(getRenameColumnCodeSql(tableColumn, oldColumnCode, tableColumn.getColumnCode()));
    }
    if (isModifyCode) {
      sql.append(getModifyColumnSql(tableColumn, isModNullable));
    }
    if (isPrimaryKey != null) {
      sql.append(getModifyPrimaryKeySql(tableColumn, isPrimaryKey));
    }
    if (isUniqueKey != null) {
      sql.append(getModifyUniqueKeySql(tableColumn, isUniqueKey, oldUniqueIndexCode));
    }
    if (isModifyComment) {
      sql.append(getModifyColumnCommonSql(tableColumn));
    }
    return sql.toString();
  }

  /**
   * 获取主键变动SQL
   *
   * @param tableColumn 本次变更的表字段
   * @param isPrimaryKey 是否设置为主键
   * @return 主键变动SQL
   */
  protected String getModifyPrimaryKeySql(DataTableColumnDTO tableColumn, boolean isPrimaryKey) {
    if (isPrimaryKey) {
      String sql = TableModelConsts.ADD_PRIMARY_KEY;
      sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
      sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getColumnCode());
      return sql;
    }
    else {
      String sql = TableModelConsts.DROP_PRIMARY_KEY_OM;
      sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
      return sql;
    }
  }

  /**
   * 获取唯一索引变动SQL
   *
   * @param tableColumn 本次变更的表字段
   * @param isUniqueKey 是否设置唯一索引（为null则不调整）
   * @param oldUniqueIndexCode 旧唯一索引编码（用于删除唯一索引）
   * @return 主键变动SQL
   */
  private String getModifyUniqueKeySql(DataTableColumnDTO tableColumn, boolean isUniqueKey, @Nullable String oldUniqueIndexCode) {
    if (isUniqueKey) {
      return parseCreateUniqueIndexSql(tableColumn.getUniqueIndexCode(), tableColumn.getTableCode(), tableColumn.getColumnCode());
    }
    else if (oldUniqueIndexCode != null) {
      return parseDropIndexSql(oldUniqueIndexCode, tableColumn.getTableCode());
    }
    else {
      return "";
    }
  }

  /**
   * 根据建模字段信息，构造删表字段脚本
   *
   * @param tableCode 表名
   * @param tableColumnCode 表字段名
   * @return 删表脚本
   */
  public String getDropTableColumnSql(String tableCode, String tableColumnCode) {
    String sql = TableModelConsts.DROP_TABLE_COLUMN;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumnCode);
    return sql;
  }

  /**
   * 获取表字段SQL（建表）
   *
   * @param column 表字段
   * @return 建表时的表字段sql
   */
  private String getTableColumnSql(DataTableColumnDTO column) {
    StringBuilder columnStr = new StringBuilder();
    columnStr.append(column.getColumnCode());
    columnStr.append(getDefinitionForDataTypeAndLength(column.getDataType(), column.getDataLength(), column.getDataScale()));
    columnStr.append(getDefinitionForDefaultValue(column.getDataType(), column.getDefaultValue()));
    columnStr.append(BaseConsts.TRUE.equals(column.getNullable()) ? "" : " not null");
    return columnStr.toString();
  }

  /**
   * 根据序列信息，构造新增序列脚本
   *
   * @param sequenceCode 序列编码
   * @param startBy 初始值
   * @param incrementBy 步长
   * @param maxValue 最大值
   * @return 新增序列脚本
   */
  public String parseAddSequenceSql(String sequenceCode, Long startBy, Long incrementBy, Long maxValue) {
    return getAddSequenceSql(sequenceCode, startBy, incrementBy, maxValue);
  }

  /**
   * 根据序列信息，构造修改序列脚本
   *
   * @param sequenceCode 序列编码
   * @param isIncrementBy 是否修改步长
   * @param incrementBy 步长
   * @param isMaxValue 是否修改最大值
   * @param maxValue 最大值
   * @return 修改序列脚本
   */
  public String parseModifySequenceSql(String sequenceCode, boolean isIncrementBy, Long incrementBy, boolean isMaxValue, Long maxValue) {
    return getModifySequenceSql(sequenceCode, isIncrementBy, incrementBy, isMaxValue, maxValue);
  }

  /**
   * 根据序列信息，构造删除序列脚本
   *
   * @param sequenceCode 序列编码
   * @return 删除序列脚本
   */
  public String parseDropSequenceSql(String sequenceCode) {
    return getDropSequenceSql(sequenceCode);
  }

  /**
   * 根据序列信息，构造改序列名脚本
   *
   * @param oldValue 旧值
   * @param newValue 新值
   * @param dataSourceProperties 数据源配置
   * @return 建表脚本
   */
  public String parseRenameSequenceSql(String oldValue, String newValue, DataSourceProperties dataSourceProperties) {
    return getRenameSequenceSql(oldValue, newValue, dataSourceProperties);
  }

  /**
   * 根据索引数据，构造建索引脚本
   *
   * @param indexName 索引编码
   * @param tabelCode 表编码
   * @param columns 索引表字段
   * @return 建索引脚本
   */
  public String parseCreateIndexSql(String indexName, String tabelCode, String columns) {
    String sql = TableModelConsts.CREATE_INDEX;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, indexName);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tabelCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, columns);
    return sql;
  }

  /**
   * 构造唯一索引脚本
   *
   * @param indexName 索引编码
   * @param tabelCode 表编码
   * @param columnCode 索引表字段
   * @return 建索引脚本
   */
  public String parseCreateUniqueIndexSql(String indexName, String tabelCode, String columnCode) {
    String sql = TableModelConsts.CREATE_UNIQUE_INDEX;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, indexName);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tabelCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, columnCode);
    return sql;
  }

  /**
   * 获取删除索引脚本
   *
   * @param indexCode 索引编码
   * @param tableCode 表编码
   * @return 删除索引脚本
   */
  public String parseDropIndexSql(String indexCode, String tableCode) {
    return getDropIndexSql(indexCode, tableCode);
  }

  /**
   * 解析表字段类型 sql
   * @param column 建模字段信息
   * @return 建表字段sql
   */
  public String parseTableColumnType(DataTableColumnDTO column) {
    if (column == null) {
      return "";
    }
    return getDefinitionForDataTypeAndLength(column.getDataType(), column.getDataLength(), column.getDataScale());
  }

  /**
   * 获取删除索引脚本，默认逻辑，可子类实现
   *
   * @param indexCode 索引编码
   * @param tableCode 表编码
   * @return 删除索引脚本
   */
  protected String getDropIndexSql(String indexCode, String tableCode) {
    String sql = TableModelConsts.DROP_INDEX;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, indexCode);
    return sql;
  }

  /**
   * 获取字段类型和长度定义，默认逻辑，可子类实现
   *
   * @param dataType 字段类型
   * @param dataLength 字段长度
   * @param dataScale 字段精度
   * @return 字段类型和长度定义
   */
  protected String getDefinitionForDataTypeAndLength(String dataType, @Nullable Long dataLength, @Nullable Long dataScale) {
    return "";
  }

  /**
   * 获取默认值定义，默认逻辑，可子类实现
   *
   * @param dataType 字段类型
   * @param defaultValue 默认值
   * @return 默认值定义
   */
  protected String getDefinitionForDefaultValue(String dataType, String defaultValue) {
    return "";
  }

  /**
   * 获取更改字段编码 SQL，默认逻辑，可子类实现
   *
   * @param tableColumn 字段信息
   * @param oldColumnCode 旧字段编码
   * @param newColumnCode 新字段编码
   * @return 更改字段编码 SQL
   */
  protected String getRenameColumnCodeSql(DataTableColumnDTO tableColumn, String oldColumnCode, String newColumnCode) {
    String sql = TableModelConsts.TABLE_COLUMN_RENAME;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, oldColumnCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, newColumnCode);
    return sql;
  }

  /**
   * 获取更改字段定义 SQL，默认逻辑，可子类实现
   *
   * @param tableColumn 字段信息
   * @return 更改字段编码 SQL
   */
  protected String getModifyColumnSql(DataTableColumnDTO tableColumn, boolean isModNullable) {
    return "";
  }

  /**
   * 获取更改字段备注 SQL，默认逻辑，可子类实现
   *
   * @param tableColumn 字段信息
   * @return 更改字段备注 SQL
   */
  protected String getModifyColumnCommonSql(DataTableColumnDTO tableColumn) {
    String sql = TableModelConsts.TABLE_COLUMN_REMARK;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getTableCode());
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getColumnCode());
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableColumn.getColumnName());
    return sql.toLowerCase();
  }

  /**
   * 获取更改表名备注 SQL，默认逻辑，可子类实现
   *
   * @param tableCode 表编码
   * @param tableName 表备注
   * @return 更改表名备注 SQL
   */
  protected String getModifyTableCommentSql(String tableCode, String tableName) {
    String sql = TableModelConsts.TABLE_COMMENT;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableCode);
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, tableName);
    return sql.toLowerCase();
  }

  /**
   * 根据序列信息，构造新增序列脚本，默认逻辑，可子类实现
   *
   * @param sequenceCode 序列编码
   * @param startBy 初始值
   * @param incrementBy 步长
   * @param maxValue 最大值
   * @return 新增序列脚本
   */
  protected String getAddSequenceSql(String sequenceCode, Long startBy, Long incrementBy, Long maxValue) {
    return "";
  }

  /**
   * 根据序列信息，构造删除序列脚本，默认逻辑，可子类实现
   *
   * @param sequenceCode 序列编码
   * @return 删除序列脚本
   */
  protected String getDropSequenceSql(String sequenceCode) {
    String sql = TableModelConsts.DROP_SEQUENCE;
    sql = sql.replaceFirst(TableModelConsts.REPLACE_CHAR, sequenceCode);
    return sql;
  }

  /**
   * 根据序列信息，构造修改序列脚本，默认逻辑，可子类实现
   *
   * @param sequenceCode 序列编码
   * @param isIncrementBy 是否修改步长
   * @param incrementBy 步长
   * @param isMaxValue 是否修改最大值
   * @param maxValue 最大值
   * @return 修改序列脚本
   */
  protected String getModifySequenceSql(String sequenceCode, boolean isIncrementBy, Long incrementBy, boolean isMaxValue, Long maxValue) {
    String sql = "";
    if (isIncrementBy) {
      String sql2 = TableModelConsts.ALTER_SEQUENCE;
      sql2 = sql2.replaceFirst(TableModelConsts.REPLACE_CHAR, sequenceCode);
      sql2 = sql2.replaceFirst(TableModelConsts.REPLACE_CHAR, "INCREMENT BY");
      sql2 = sql2.replaceFirst(TableModelConsts.REPLACE_CHAR, incrementBy.toString());
      sql = sql + sql2;
    }
    if (isMaxValue) {
      String sql2 = TableModelConsts.ALTER_SEQUENCE;
      sql2 = sql2.replaceFirst(TableModelConsts.REPLACE_CHAR, sequenceCode);
      sql2 = sql2.replaceFirst(TableModelConsts.REPLACE_CHAR, "MAXVALUE");
      sql2 = sql2.replaceFirst(TableModelConsts.REPLACE_CHAR, maxValue.toString());
      sql = sql + sql2;
    }
    return sql;
  }

  /**
   * 根据序列信息，构造改序列名脚本，默认逻辑，可子类实现
   *
   * @param oldSequenceCode 序列编码旧值
   * @param newSequenceCode 序列编码新值
   * @return 修改序列名脚本
   */
  protected String getRenameSequenceSql(String oldSequenceCode, String newSequenceCode, DataSourceProperties dataSourceProperties) {
    return "";
  }
}
