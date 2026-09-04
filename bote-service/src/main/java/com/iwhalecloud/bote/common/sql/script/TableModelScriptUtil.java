package com.iwhalecloud.bote.common.sql.script;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.jdbc.LowerCaseColumnMapRowMapper;
import com.iwhalecloud.bote.common.sql.consts.DatabaseConsts;
import com.iwhalecloud.bote.common.sql.consts.SqlParseConsts;
import com.iwhalecloud.bote.common.sql.consts.TableModelConsts;
import com.iwhalecloud.bote.common.sql.factory.DataDefinitionLanguageFactory;
import com.iwhalecloud.bote.dto.base.DataSourceProperties;
import com.iwhalecloud.bote.dto.database.DataTableColumnDTO;
import com.iwhalecloud.bote.dto.database.DataTableDTO;
import com.iwhalecloud.bote.dto.skill.DataSourceDTO;
import com.iwhalecloud.bote.service.base.IDataSourceProviderService;
import com.iwhalecloud.bote.service.skill.IDataSourceManageService;
import com.iwhalecloud.bss.litchi.database.inspect.DatabaseInspector;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Column;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Table;
import com.iwhalecloud.bss.litchi.diffc.result.DataDiffState;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.diffc.result.FieldDifference;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表模型脚本工具
 *
 * @author wangtingyun
 * @since 2025-11-21
 */
@SuppressWarnings("PMD.UnusedFormalParameter")
public final class TableModelScriptUtil {

  private static final Logger logger = LoggerFactory.getLogger(TableModelScriptUtil.class);

  private TableModelScriptUtil() {
  }

  private static final List<String> MODIFY_COLUMN_FIELD = Arrays.asList("DATA_TYPE", "DATA_LENGTH", "DATA_SCALE", "NULLABLE", "DEFAULT_VALUE");
  private static final IDataSourceManageService dataSourceService = SpringUtil.getBean(IDataSourceManageService.class);
  private static final IDataSourceProviderService dataSourceProvider = SpringUtil.getBean(IDataSourceProviderService.class);

  /**
   * 分析表变更脚本
   *
   * @param difference 变更表
   * @return 变更脚本
   */
  public static String analyseForTable(DataDifference<DataTableDTO> difference) {
    DataTableDTO tableDTO = difference.getToSaveData();
    DataSourceDTO dataSource = dataSourceService.findDataSource(tableDTO.getTenantId(), tableDTO.getDataSourceId());
    Assert.notNull(dataSource, String.format("未找到租户的数据源：tenantId=%s，dataSourceId=%s", tableDTO.getTenantId(), tableDTO.getDataSourceId()));
    String database = dataSource.getDataSourceType().toLowerCase();

    // 判断是否为不执行脚本的数据源类型（如 udal），是则返回空
    String databaseFilter = SystemParameter.UNSUPPORTED_DDL_DATABASE.getValueFromDb();
    if (databaseFilter.toLowerCase().contains(database)) {
      return "";
    }
    StringBuilder sql = new StringBuilder();
    Long dataSourceId = dataSource.getDataSourceId();
    DataDiffState state = difference.getState();
    switch (state.getValue()) {
      case "A":
        sql.append(DataDefinitionLanguageFactory.create(database).parseCreateTableSql(tableDTO));
        addSequence(tableDTO, dataSource, sql);
        break;
      case "M":
        sql.append(analyseTable(difference, database));
        sql.append(analyseTableColumn(difference, database, dataSourceId, tableDTO.getTableCode(), tableDTO.getTenantId()));
        //sql.append(analyseIndex(difference, database, table.getTableCode()));
        break;
      case "D":
        sql.append(DataDefinitionLanguageFactory.create(database).getDropTableSql(tableDTO.getTableCode()));
        break;
      default:
    }
    return sql.toString();
  }

  private static void addSequence(DataTableDTO table, DataSourceDTO dataSource, StringBuilder sql) {
    DataTableColumnDTO column = IterableUtils.find(CollectionUtils.emptyIfNull(table.getTableColumns()),
      p -> BaseConsts.TRUE.equals(p.getCreateSeq()));
    if (column != null) {
      // seq_表名小写_id
      String sequenceCode = ("seq_" + table.getTableCode() + "_id").toLowerCase();
      sql.append(DataDefinitionLanguageFactory.create(dataSource.getDataSourceType().toLowerCase())
        .parseAddSequenceSql(sequenceCode, 10000L, 1L, 9999999999L));
    }
  }

  /**
   * 解析表变更脚本
   */
  private static String analyseTable(DataDifference<DataTableDTO> difference, String database) {
    FieldDifference field = IterableUtils.find(CollectionUtils.emptyIfNull(difference.getFieldDifferences()), p -> "TABLE_CODE".equalsIgnoreCase(p.getName()));
    if (field == null) {
      return "";
    }
    return DataDefinitionLanguageFactory.create(database).getRenameTableSql(field.getOldValue(), field.getNewValue());
  }

  /**
   * 解析表字段变更脚本
   */
  private static String analyseTableColumn(DataDifference<DataTableDTO> difference, String database, Long dataSourceId, String tableCode, Long tenantId) {
    // 探测表的所有列
    List<String> columnNames = inspectTableColumn(difference, dataSourceId, tableCode, tenantId);

    // 过滤出应用模型-表字段的差异列表
    List<DataDifference<DataTableColumnDTO>> tableColumnDataDifferences = filterAppModelTableColumnDataDifferences(difference);

    // 解析出应用模型-表字段列表中需要删除的字段编码列表
    List<String> removeColumnCodes = parseAppModelTableToRemoveColumnCodes(tableColumnDataDifferences);
    // 解析出应用模型-表字段列表中做了修改的原字段编码列表
    List<String> updateColumnCodes = parseAppModelTableToUpdateColumnCodes(tableColumnDataDifferences);

    StringBuilder sql = new StringBuilder();
    for (DataDifference<DataTableColumnDTO> columnDifference : CollectionUtils.emptyIfNull(tableColumnDataDifferences)) {
      DataTableColumnDTO column = columnDifference.getToSaveData();
      column.setTableCode(tableCode);
      DataDiffState state = columnDifference.getState();

      String columnCode = column.getColumnCode();

      if (state.getValue().equals(DataDiffState.REMOVE.getValue())) {
        sql.append(DataDefinitionLanguageFactory.create(database).getDropTableColumnSql(column.getTableCode(), columnCode));
      }
      // 新增属性且探测到表中没有这个字段（新增属性但表中存在字段的情况出现的原因是在MySQL环境中多个DDL语句是没有事务去保证的，导致两个DDL语句前面一条执行成功无法回滚）
      // 新增属性且探测到表中存在这个字段但是对字段做了删除操作
      // 新增属性探测到表中存在这个字段但是对且字段做了更新操作
      else if (state.getValue().equals(DataDiffState.ADD.getValue()) && (!columnNames.contains(columnCode) || removeColumnCodes.contains(columnCode) || updateColumnCodes.contains(columnCode))) {
        sql.append(DataDefinitionLanguageFactory.create(database).getAddTableColumnSql(column));
        if (BaseConsts.TRUE.equals(column.getCreateSeq())) {
          // 自动生成序列
          String sequenceCode = ("seq_" + tableCode + "_id").toLowerCase();
          sql.append(DataDefinitionLanguageFactory.create(database).parseAddSequenceSql(sequenceCode, 10000L, 1L, 9999999999L));
        }
        if (isUniqueKey(column)) {
          // 建立唯一索引
          sql.append(DataDefinitionLanguageFactory.create(database).parseCreateUniqueIndexSql(column.getUniqueIndexCode(), tableCode, columnCode));
        }
      }
      else {
        analyseTableColumnForModify(sql, columnDifference, column, database, dataSourceId, tableCode);
      }
    }
    return sql.toString();
  }

  private static List<String> parseAppModelTableToUpdateColumnCodes(List<DataDifference<DataTableColumnDTO>> tableColumnDataDifferences) {
    List<String> toUpdateColumnCodes = new ArrayList<>();

    for (DataDifference<DataTableColumnDTO> columnDataDifference : CollectionUtils.emptyIfNull(tableColumnDataDifferences)) {
      // 如果不是修改操作
      if (!columnDataDifference.getState().getValue().equals(DataDiffState.MODIFY.getValue())) {
        continue;
      }
      // 如果未找到业务对象字段编码差异
      FieldDifference columnCodeField = IterableUtils.find(columnDataDifference.getFieldDifferences(), p -> "COLUMN_CODE".equalsIgnoreCase(p.getName()));
      if (columnCodeField == null) {
        continue;
      }
      String oldColumnCode = columnCodeField.getOldValue();
      String newColumnCode = columnCodeField.getNewValue();
      // 业务对象字段编码未发生改变
      if (Objects.equals(oldColumnCode, newColumnCode)) {
        continue;
      }
      // 业务对象字段编码发生改变, 添加到结果列表
      toUpdateColumnCodes.add(oldColumnCode);
    }
    return toUpdateColumnCodes;
  }

  private static List<String> parseAppModelTableToRemoveColumnCodes(List<DataDifference<DataTableColumnDTO>> tableColumnDataDifferences) {
    return CollectionUtils.emptyIfNull(tableColumnDataDifferences).stream()
      .filter(item -> item.getState().getValue().equals(DataDiffState.REMOVE.getValue()))
      .map(DataDifference::getToSaveData).map(DataTableColumnDTO::getColumnCode).collect(Collectors.toList());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static List<DataDifference<DataTableColumnDTO>> filterAppModelTableColumnDataDifferences(DataDifference<DataTableDTO> difference) {
    List<DataDifference<DataTableColumnDTO>> result = new ArrayList<>();

    for (DataDifference diff : CollectionUtils.emptyIfNull(difference.getChildren())) {
      if (!(diff.getToSaveData() instanceof DataTableColumnDTO)) {
        continue;
      }

      DataDifference<DataTableColumnDTO> columnDiff = (DataDifference<DataTableColumnDTO>) diff;
      DataTableColumnDTO columnModel = columnDiff.getToSaveData();

      // 如果不是主键，直接添加到结果列表
      if (!BaseConsts.TRUE.equals(columnModel.getPrimaryKey())) {
        result.add(columnDiff);
        continue;
      }

      // 如果是主键，则过滤掉试图将其设置为可为空的变更，主键列在数据库中理论上不应为空，但在调用时可以允许使用表字段的自增特性（比如在字段中设置序列或者设置自增字段）
      List<FieldDifference> filteredDifferences = CollectionUtils.emptyIfNull(columnDiff.getFieldDifferences()).stream()
        .filter(fieldDiff -> !(SqlParseConsts.NULLABLE.equalsIgnoreCase(fieldDiff.getName()) && BaseConsts.TRUE.equals(fieldDiff.getNewValue())))
        .collect(Collectors.toList());

      // 如果过滤后仍有其他有效变更，则更新差异并将其添加到结果列表
      if (CollectionUtils.isNotEmpty(filteredDifferences)) {
        columnDiff.setFieldDifferences(filteredDifferences);
        result.add(columnDiff);
      }
    }
    return result;
  }

  private static List<String> inspectTableColumn(DataDifference<DataTableDTO> difference, Long dataSourceId, String tableCode, Long tenantId) {
    // 探测表的所有列
    DataSource dataSourceInfo = dataSourceProvider.getDataSource(tenantId, dataSourceId);
    // 如果表的编码存在变更，则使用变更前的表编码进行探测
    FieldDifference tableCodeFieldDifference = IterableUtils.find(CollectionUtils.emptyIfNull(difference.getFieldDifferences()), p -> "TABLE_CODE".equalsIgnoreCase(p.getName()));
    String inspectTableCode = tableCodeFieldDifference == null ? tableCode : tableCodeFieldDifference.getOldValue();
    Table table = DatabaseInspector.inspectTable(dataSourceInfo, inspectTableCode);
    Assert.notNull(table, inspectTableCode + " 表不存在");
    return CollectionUtils.emptyIfNull(table.getColumns()).stream().map(Column::getName).collect(Collectors.toList());
  }

  private static void analyseTableColumnForModify(StringBuilder sql, DataDifference<DataTableColumnDTO> columnDifference,
                                                  DataTableColumnDTO column, String database, Long dataSourceId, String tableCode) {
    List<FieldDifference> fieldDifferences = ListUtils.emptyIfNull(columnDifference.getFieldDifferences());
    // 是否调整序列
    FieldDifference createSeqField = IterableUtils.find(fieldDifferences, p -> "CREATE_SEQ".equalsIgnoreCase(p.getName()));
    if (createSeqField != null && BaseConsts.TRUE.equals(createSeqField.getNewValue())) {
      String sequenceCode = ("seq_" + tableCode + "_id").toLowerCase();
      sql.append(DataDefinitionLanguageFactory.create(database).parseAddSequenceSql(sequenceCode, 10000L, 1L, 9999999999L));
    }
    // 是否调整字段编码
    FieldDifference columnCodeField = IterableUtils.find(fieldDifferences, p -> "COLUMN_CODE".equalsIgnoreCase(p.getName()));
    String oldColumnCode = columnCodeField == null ? null : columnCodeField.getOldValue();
    // 是否调整字段基本信息
    boolean isModifyCode = IterableUtils.matchesAny(fieldDifferences, p -> MODIFY_COLUMN_FIELD.contains(p.getName()));
    // 是否设置主键
    FieldDifference primaryKeyField = IterableUtils.find(fieldDifferences, p -> "PRIMARY_KEY".equalsIgnoreCase(p.getName()));
    Boolean isPrimaryKey = primaryKeyField == null ? null : BaseConsts.TRUE.equals(primaryKeyField.getNewValue());
    // 是否设置唯一索引
    FieldDifference uniqueIndexCodeField = IterableUtils.find(fieldDifferences, p -> "UNIQUE_INDEX_CODE".equalsIgnoreCase(p.getName()));
    Boolean isUniqueKey = null;
    String oldUniqueIndexCode = null;
    if (uniqueIndexCodeField != null) {
      isUniqueKey = StringUtils.isNotEmpty(uniqueIndexCodeField.getNewValue());
      oldUniqueIndexCode = uniqueIndexCodeField.getOldValue();
    }
    // 是否调整备注: column_name 对应字段备注
    boolean isModComment =
      IterableUtils.matchesAny(fieldDifferences, p -> "COLUMN_NAME".equalsIgnoreCase(p.getName())) && StringUtils.isNotEmpty(column.getColumnName());
    // 是否调整可为空，用于 oracle 数据库修改字段的必填
    boolean isModNullable = false;
    FieldDifference nullableField = IterableUtils.find(fieldDifferences, p -> "NULLABLE".equalsIgnoreCase(p.getName()));

    // 判断字段的必填是否修改
    if (nullableField != null && !Objects.equals(nullableField.getNewValue(), nullableField.getOldValue())) {
      isModNullable = true;
    }

    sql.append(DataDefinitionLanguageFactory.create(database)
      .getModifyTableColumnSql(column, isModifyCode, isPrimaryKey, isModComment, oldColumnCode, isUniqueKey, oldUniqueIndexCode, isModNullable));
  }

  /**
   * 执行业务对象建模脚本，性能优化
   */
  public static void executeScript(Long tenantId, Long dataSourceId, String sql) {
    DataSourceProperties dataSource = dataSourceService.findDataSourceProperties(tenantId, dataSourceId);
    if (dataSource == null) {
      throw BaseErrorConstant.FIND_DATASOURCE_FAIL.toException("tenantId=" + tenantId + ", dataSourceId=" + dataSourceId);
    }
    Resource scriptResource = new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8));
    SqlScriptUtil.ScriptExecuteResult scriptExecuteResult = SqlScriptUtil.executeScript(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword(),
      scriptResource);
    if (!scriptExecuteResult.isSuccess()) {
      throw BaseErrorConstant.SQL_EXECUTE_FAILED.toException(scriptExecuteResult.getFirstError());
    }
  }

  public static void executeScript(Long tenantId, Long dataSourceId, List<String> statements) {
    DataSourceProperties dataSource = dataSourceService.findDataSourceProperties(tenantId, dataSourceId);
    if (dataSource == null) {
      throw BaseErrorConstant.FIND_DATASOURCE_FAIL.toException("tenantId=" + tenantId + ", dataSourceId=" + dataSourceId);
    }
    SqlScriptUtil.executeScript(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword(), statements);
  }

  /**
   * 是否为唯一字段
   *
   * @param tableColumn 表字段
   * @return 结果
   */
  public static boolean isUniqueKey(DataTableColumnDTO tableColumn) {
    return false;
  }

  /**
   * 根据数据库类型修正SQL
   */
  public static String correctSqlByDbType(String sql, String dbType) {
    if (!DatabaseConsts.DATABASE_TYPES_MYSQL.contains(dbType)) {
      return sql.replace("`", "");
    }
    return sql;
  }

  /**
   * 构造达梦数据重命名序列脚本
   */
  public static String getDmRenameSequenceSql(Long tenantId, Long dataSourceId, String username, String oldSequenceCode, String newSequenceCode) {
    // 查询旧序列的信息
    DataSource dataSource;
    try {
      dataSource = dataSourceProvider.getDataSource(tenantId, dataSourceId);
      JdbcTemplate appJdbcTemplate = new JdbcTemplate(dataSource);
      String selectSql = "SELECT sequence_owner, sequence_name,min_value ,max_value ,increment_by,last_number FROM dba_sequences "
        + "WHERE SEQUENCE_NAME = ? AND SEQUENCE_OWNER = ? ";
      List<Object> args = new ArrayList<>();
      args.add(oldSequenceCode.toUpperCase());
      args.add(username);
      // 可能存在多个序列（有多个数据库模式）
      List<Map<String, Object>> records = appJdbcTemplate.query(selectSql, new LowerCaseColumnMapRowMapper(), args.toArray(new Object[0]));
      if (CollectionUtils.isEmpty(records)) {
        return "";
      }
      Map<String, Object> map = records.get(0);
      // 根据旧序列的信息，新建新序列
      String maxValue = MapUtils.getString(map, "MAX_VALUE");
      String incrementBy = MapUtils.getString(map, "INCREMENT_BY");
      String startBy = MapUtils.getString(map, "LAST_NUMBER");

      return generateAddNewSeqAndDropOldSeqSql(newSequenceCode, incrementBy, maxValue, startBy, oldSequenceCode);
    }
    catch (Exception e) {
      logger.error("Failed to rename sequence for DM database", e);
      return "";
    }
  }

  /**
   * 构造以删除名称命名的创建新序列脚本和删除原序列的脚本
   * @param newSequenceCode 序列名称
   * @param incrementBy 递增数
   * @param maxValue 最大值
   * @param startBy 开始值
   * @param oldSequenceCode 原序列名称
   * @return 创建新序列脚本和删除原序列的脚本
   */
  public static String generateAddNewSeqAndDropOldSeqSql(String newSequenceCode, String incrementBy, String maxValue, String startBy, String oldSequenceCode) {
    return generateCreateSeqSql(newSequenceCode, incrementBy, maxValue, startBy) + generateDropSeqSql(oldSequenceCode);
  }

  /**
   * 构造删除序列脚本
   * @param sequenceCode 序列名称
   * @return 删除序列脚本
   */
  private static String generateDropSeqSql(String sequenceCode) {
    // 删除旧序列
    String dropSql = TableModelConsts.DROP_SEQUENCE;
    dropSql = dropSql.replaceFirst(TableModelConsts.REPLACE_CHAR, sequenceCode);
    return dropSql;
  }

  /**
   * 构造创建序列脚本
   * @param newSequenceCode 序列名称
   * @param incrementBy 递增数
   * @param maxValue 最大值
   * @param startBy 开始值
   * @return 创建新序列脚本
   */
  private static String generateCreateSeqSql(String newSequenceCode, String incrementBy, String maxValue, String startBy) {
    String createSql = TableModelConsts.CREATE_SEQUENCE_O;
    createSql = createSql.replaceFirst(TableModelConsts.REPLACE_CHAR, newSequenceCode);
    createSql = createSql.replaceFirst(TableModelConsts.REPLACE_CHAR, incrementBy);
    createSql = createSql.replaceFirst(TableModelConsts.REPLACE_CHAR, maxValue);
    createSql = createSql.replaceFirst(TableModelConsts.REPLACE_CHAR, startBy);
    return createSql;
  }

  /**
   * 获取数据库类型，如果是 pg 数据库，根据 url 再次判断是不是磐维，如果是这返回磐维数据类型
   * @param dataSourceProperties 数据源配置信息
   * @return 数据库类型
   */
  public static String getDatabaseType(DataSourceProperties dataSourceProperties) {

    //String databaseType = dataSourceProperties.getDataSourceType();
    String databaseType = "";

    // 如果是 pg 且 url 包含 panweidb 或者 opengauss，则返回数据库类型为 panweidb
    if (DatabaseConsts.DATABASE_TYPE_PG.equalsIgnoreCase(databaseType) && Strings.CS.containsAny(dataSourceProperties.getUrl(), ":panweidb:", ":opengauss:", "panweidb=1")) {
      return DatabaseConsts.DATABASE_TYPE_PANWEI;
    }
    return databaseType;
  }

}
