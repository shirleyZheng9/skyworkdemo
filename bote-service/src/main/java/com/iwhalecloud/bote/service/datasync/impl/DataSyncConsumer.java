package com.iwhalecloud.bote.service.datasync.impl;

import com.google.common.collect.Lists;
import com.iwhalecloud.bote.cache.TableDefinitionCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.DataSyncConsts;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.jdbc.LowerCaseColumnMapRowMapper;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.utils.DcIdUtils;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import com.iwhalecloud.bote.dto.datasync.ScriptDefinition;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.service.datasync.util.DataSyncClobUtil;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDateUtil;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDirUtil;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDocumentUtil;
import com.iwhalecloud.bote.service.datasync.util.DataSyncFileUtil;
import com.iwhalecloud.bote.service.datasync.util.DataSyncReplaceUtil;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Table;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.SqlCharacterValue;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * 数据同步 - 数据保存
 *
 * @author chen.linfa
 * @since 2024-10-22
 */
@Component
@RequiredArgsConstructor
@SuppressFBWarnings("SECSQLISPRJDBC")
@SuppressWarnings("PMD.GuardLogStatement")
public class DataSyncConsumer {
  private static final Logger logger = LoggerFactory.getLogger(DataSyncConsumer.class);

  private final TableDefinitionCache tableDefinitionCache;

  private final JdbcTemplate jdbcTemplate;

  public ResultVO<Void> execute(DataSyncParams params) {
    try {
      inspectTable(params);
      // 清理数据
      DataSyncReplaceUtil.updateStatusCdForClear(params);
      DataSyncFileUtil.readFileResource(params);
      Map<String, List<DataSyncTableDefinition>> nodes = buildNodesByConfigCode(params);
      List<Pair<String, List<DataSyncTableDefinition>>> definitions = buildOrderedDefinitions(nodes);
      params.setPrimaryIdMappings(new HashMap<>());
      Map<String, Map<Long, Long>> primaryIdMappings = params.getPrimaryIdMappings();
      Map<String, Map<String, String>> codeMappings = new HashMap<>();
      for (Pair<String, List<DataSyncTableDefinition>> children : definitions) {
        prepareAndSaveDefinitionGroup(params, children, primaryIdMappings, codeMappings);
      }
      saveData(DataSyncDocumentUtil.processDocumentCenterData(params));
      return ResultVO.success();
    }
    catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to save app data. error={}", e.getMessage(), e);
      }
      tryRollbackOnly();
      return ResultVO.fail("-1", "保存数据出现异常：" + e.getMessage(), null, e);
    }
  }

  private Map<String, List<DataSyncTableDefinition>> buildNodesByConfigCode(DataSyncParams params) {
    Map<String, List<DataSyncTableDefinition>> nodes = new HashMap<>();
    Map<String, List<DataSyncTableDefinition>> group = params.getDefinitions().stream()
      .collect(Collectors.groupingBy(DataSyncTableDefinition::getDataConfigPath));
    for (Entry<String, List<DataSyncTableDefinition>> entry : group.entrySet()) {
      nodes.putAll(
        entry.getValue().stream().sorted(Comparator.comparing(DataSyncTableDefinition::getSortby, Comparator.nullsLast(Comparator.naturalOrder())))
          .collect(Collectors.groupingBy(DataSyncTableDefinition::getDataConfigCode)));
    }
    return nodes;
  }

  private List<Pair<String, List<DataSyncTableDefinition>>> buildOrderedDefinitions(Map<String, List<DataSyncTableDefinition>> nodes) {
    List<String> dataConfigCodes = DataSyncCodeEnum.getReverseCodes();
    List<Pair<String, List<DataSyncTableDefinition>>> definitions = new ArrayList<>(nodes.size());
    for (String code : dataConfigCodes) {
      if (nodes.containsKey(code)) {
        definitions.add(Pair.of(code, nodes.get(code)));
      }
    }
    for (Entry<String, List<DataSyncTableDefinition>> entry : nodes.entrySet()) {
      if (!dataConfigCodes.contains(entry.getKey())) {
        definitions.add(Pair.of(entry.getKey(), entry.getValue()));
      }
    }
    return definitions;
  }

  private void prepareAndSaveDefinitionGroup(DataSyncParams params, Pair<String, List<DataSyncTableDefinition>> children,
                                             Map<String, Map<Long, Long>> primaryIdMappings, Map<String, Map<String, String>> codeMappings) {
    String mainTableCode = children.getValue().get(0).getTableCode();
    for (DataSyncTableDefinition definition : children.getValue()) {
      definition.setTenantId(params.getTenantId());
      definition.setResetTenantId(params.getResetTenantId());
      definition.setResetPrimaryKey(params.getResetPrimaryKey());
      definition.setSpaceId(params.getSpaceId());
      String code = (definition.getDataConfigCode() + "-" + definition.getTableCode()).toUpperCase();
      definition.setDataRecords(DataSyncDirUtil.getDataRecords(params, code));
      DataSyncReplaceUtil.replaceFileId(definition, params.getFileIdMap());
      DataSyncReplaceUtil.replacePrimaryId(definition, primaryIdMappings, codeMappings, mainTableCode, params.isResetPrimary());
    }
    if ("bt_dc_document_library".equals(mainTableCode)) {
      params.getDocumentCenterDefinitions().addAll(children.getValue());
      return;
    }
    DataSyncDateUtil.toDate(children.getValue());
    saveData(children.getValue());
  }

  /**
   * 重新探测表结构，兼容生产、消费端模型差异场景
   */
  private void inspectTable(DataSyncParams params) {
    if (params.isCopy()) {
      return;
    }
    List<DataSyncTableDefinition> definitions = new ArrayList<>(params.getDefinitions().size());
    for (DataSyncTableDefinition definition : params.getDefinitions()) {
      Table table = tableDefinitionCache.get(definition.getTableCode());
      if (table == null) {
        continue;
      }
      // 表字段可能存在差异，取交集
      List<String> columns = table.getColumns().stream().map(m -> m.getName().toLowerCase()).collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(definition.getTableColumns())) {
        columns.retainAll(definition.getTableColumns());
      }
      definition.setTableColumns(columns);
      definitions.add(definition);
    }
    params.setDefinitions(definitions);
  }

  /**
   * 保存数据
   * <p>1. 采用专门的线程池并发处理</p>
   * <p>2. 执行粒度：一个表定义一个任务</p>
   *
   * @param definitions 一个节点下的表定义
   */
  private void saveData(List<DataSyncTableDefinition> definitions) {
    List<Runnable> tasks = new ArrayList<>(definitions.size());
    for (DataSyncTableDefinition definition : definitions) {
      tasks.add(() -> save(definition));
    }
    ThreadPools.invokeTasks(ThreadPools.getDatasync(), tasks);
  }

  /**
   * 单表的数据保存实现
   *
   * @param definition 表定义
   */
  private void save(DataSyncTableDefinition definition) {
    if (logger.isDebugEnabled()) {
      logger.debug("Start saving data. table={}", definition.getTableCode());
    }

    String primaryKey = definition.getPrimaryKey().toLowerCase();
    String tenantIdKey = StringUtils.isEmpty(definition.getTenantIdAlias()) ? "tenant_id" : definition.getTenantIdAlias();
    List<String> clobColumns = DataSyncClobUtil.getClobColumns(definition.getContentReplaceRule());
    List<String> tableColumns = getColumnsWithoutClob(definition, clobColumns);
    List<String> tableColumnWithoutPrimaryKey = getTableColumnsWithoutPrimaryKey(tableColumns, primaryKey, tenantIdKey);

    String insertSql = getInsertSql(definition, tableColumns);
    String updateSql = getUpdateSql(definition, tableColumnWithoutPrimaryKey);

    processBatchData(definition, primaryKey, insertSql, updateSql);

    // 更新失效数据
    updateForInvalidData(definition);
    // 更新 clob 字段，必须放置到最后执行
    updateForClob(definition);
    if (logger.isDebugEnabled()) {
      logger.debug("Data saved. table={}, record size={}", definition.getTableCode(), definition.getDataRecords().size());
    }
  }

  /**
   * 获取不包含主键和tenant_id的字段列表
   */
  private List<String> getTableColumnsWithoutPrimaryKey(List<String> tableColumns, String primaryKey, String tenantIdKey) {
    return tableColumns.stream().filter(tableColumn -> !Objects.equals(tableColumn, primaryKey))
      .filter(tableColumn -> !Objects.equals(tableColumn, tenantIdKey)).collect(Collectors.toList());
  }

  /**
   * 分批处理数据
   */
  private void processBatchData(DataSyncTableDefinition definition, String primaryKey, String insertSql, String updateSql) {
    int size = 1000;
    List<List<Map<String, Object>>> partitionList = Lists.partition(definition.getDataRecords(), size);
    List<Object[]> insertArgs = new ArrayList<>();
    List<Object[]> updateArgs = new ArrayList<>();

    for (List<Map<String, Object>> dataRecords : partitionList) {
      List<String> exists = queryExists(definition, dataRecords);

      for (Map<String, Object> record : dataRecords) {
        Object primaryValue = record.get(primaryKey);

        if (exists.contains(primaryValue.toString())) {
          processUpdateRecord(definition, record, primaryKey, primaryValue, updateArgs);
        }
        else {
          processInsertRecord(definition, record, insertArgs);
        }

        // 批量执行数据库操作
        executeBatchIfNeeded(size, insertSql, updateSql, insertArgs, updateArgs);
      }
    }

    // 最后一个批次数据入库
    executeFinalBatch(insertSql, updateSql, insertArgs, updateArgs);
  }

  /**
   * 处理更新记录
   */
  private void processUpdateRecord(DataSyncTableDefinition definition, Map<String, Object> record, String primaryKey, Object primaryValue,
                                   List<Object[]> updateArgs) {
    List<Object> args = new ArrayList<>();

    if (getOnlyUpdateStatusTables().contains(definition.getTableCode().toUpperCase())) {
      args.add(MapUtils.getString(record, "status_cd"));
      args.add(primaryValue);
    }
    else {
      record.remove(primaryKey);
      String tenantIdKey = StringUtils.isEmpty(definition.getTenantIdAlias()) ? "tenant_id" : definition.getTenantIdAlias();
      List<String> tableColumnWithoutPrimaryKey = getTableColumnsWithoutPrimaryKey(
        getColumnsWithoutClob(definition, DataSyncClobUtil.getClobColumns(definition.getContentReplaceRule())), primaryKey, tenantIdKey);
      tableColumnWithoutPrimaryKey.forEach(tableColumn -> args.add(record.get(tableColumn)));
      args.add(primaryValue);
      // 重新赋值主键，保证原数据的完整性
      record.put(primaryKey, primaryValue);
    }
    updateArgs.add(args.toArray());
  }

  /**
   * 获取只更新状态字段的表
   */
  private List<String> getOnlyUpdateStatusTables() {
    String tableValue = SystemParameter.ONLY_UPDATE_STATUS_CD_TABLES.getValueFromDb();
    if (StringUtils.isBlank(tableValue)) {
      return List.of();
    }
    return Arrays.asList(tableValue.split(","));
  }

  /**
   * 处理插入记录
   */
  private void processInsertRecord(DataSyncTableDefinition definition, Map<String, Object> record, List<Object[]> insertArgs) {
    List<Object> args = new ArrayList<>();
    boolean isCopy = definition.getResetTenantId() != null;
    boolean isDocumentCenterTable = DataSyncConsts.getDocumentCenterTables().contains(definition.getTableCode());
    boolean isJobTable = DataSyncConsts.TABLE_BT_JOB.equalsIgnoreCase(definition.getTableCode());
    boolean isPromptBasicTable = "bt_prompt_basic".equalsIgnoreCase(definition.getTableCode());
    boolean isPromptCommitTable = "bt_prompt_commit".equalsIgnoreCase(definition.getTableCode());
    definition.getTableColumns().forEach(tableColumn -> {
      Object fieldValue = record.get(tableColumn);
      // 复制场景需要替换租户id
      if (isCopy) {
        if (isPromptBasicTable || isPromptCommitTable) {
          fieldValue = getPromptCopyFieldValue(tableColumn, definition.getResetTenantId(), record);
        }
        else {
          fieldValue = getCopyFieldValue(tableColumn, definition.getResetTenantId(), record);
          if (isJobTable && DataSyncConsts.FIELD_JOB_ID.equalsIgnoreCase(tableColumn)) {
            // 定时任务表，不适合双主键改造，复制场景，需要重置主键值
            fieldValue = IDUtils.nextId();
          }
        }
      }
      // 文档中心表需要替换spaceId
      if (isDocumentCenterTable) {
        Object documentFieldValue = processDocumentFieldValue(tableColumn, definition.getSpaceId());
        fieldValue = documentFieldValue != null ? documentFieldValue : fieldValue;
      }
      args.add(fieldValue);
    });
    insertArgs.add(args.toArray());
  }

  /**
   * 获取复制场景下字段的值
   */
  private Object getCopyFieldValue(String tableColumn, Long resetTenantId, Map<String, Object> record) {
    return switch (tableColumn) {
      case "tenant_id" -> resetTenantId;
      case "created_time", "updated_time" -> new Date();
      case "creator_id", "updator_id" -> SessionUtil.getLoginInfo().getUserId();
      default -> record.get(tableColumn);
    };
  }

  /**
   * 提示词新表复制场景下字段的值
   * <p>bt_prompt_basic / bt_prompt_commit 使用 space_id、created_at/updated_at、created_by/updated_by/committed_by 等非规范字段</p>
   */
  @Nullable
  private Object getPromptCopyFieldValue(String tableColumn, Long resetTenantId, Map<String, Object> record) {
    return switch (tableColumn) {
      case "space_id" -> resetTenantId;
      case "created_at", "updated_at" -> new Date();
      case "created_by", "updated_by", "committed_by" -> {
        Object userId = SessionUtil.getLoginInfo().getUserId();
        yield userId == null ? null : userId.toString();
      }
      default -> record.get(tableColumn);
    };
  }

  /**
   * 文档中心数据复制时需要修改ownerId spaceId
   */
  @Nullable
  private Object processDocumentFieldValue(String tableColumn, Long spaceId) {
    return switch (tableColumn) {
      case "updated_time" -> new Date();
      case "updator_id", "owner_id" -> SessionUtil.getLoginInfo().getUserId();
      case "space_id" -> spaceId;
      case "kb_code" -> DcIdUtils.createKbId();
      case "doc_status" -> KnowledgeConsts.DOCUMENT_STATUS_UNTREATED;
      // 默认返回null 避免替换复制场景的tenantId
      default -> null;
    };
  }

  /**
   * 如果达到批量大小则执行数据库操作
   */
  private void executeBatchIfNeeded(int size, String insertSql, String updateSql, List<Object[]> insertArgs, List<Object[]> updateArgs) {
    if (Objects.equals(size, updateArgs.size())) {
      TransactionUtil.executeNew(() -> jdbcTemplate.batchUpdate(updateSql, updateArgs));
      updateArgs.clear();
    }
    if (Objects.equals(size, insertArgs.size())) {
      TransactionUtil.executeNew(() -> jdbcTemplate.batchUpdate(insertSql, insertArgs));
      insertArgs.clear();
    }
  }

  /**
   * 执行最后一批数据库操作
   */
  private void executeFinalBatch(String insertSql, String updateSql, List<Object[]> insertArgs, List<Object[]> updateArgs) {
    TransactionUtil.executeNew(() -> {
      jdbcTemplate.batchUpdate(updateSql, updateArgs);
      jdbcTemplate.batchUpdate(insertSql, insertArgs);
    });
  }

  /**
   * 获取不带 clob 的字段集
   *
   * @param definition 表定义
   * @param clobColumns clob 字段集合
   * @return 不带 clob 的字段集
   */
  private List<String> getColumnsWithoutClob(DataSyncTableDefinition definition, List<String> clobColumns) {
    List<String> tableColumns = definition.getTableColumns();
    for (String clob : CollectionUtils.emptyIfNull(clobColumns)) {
      tableColumns.remove(clob);
    }
    return tableColumns;
  }

  /**
   * 构造 insert sql
   *
   * @param definition 表定义
   * @param tableColumns 字段集
   * @return insert sql
   */
  private String getInsertSql(DataSyncTableDefinition definition, List<String> tableColumns) {
    return " INSERT INTO " + definition.getTableCode()
      + "(" + String.join(",", tableColumns) + ")"
      + " VALUES (" + StringUtils.repeat("?", ", ", tableColumns.size()) + ")";
  }

  /**
   * 构造 update sql
   *
   * @param definition 表定义
   * @param tableColumnWithoutPrimaryKey 不带主键的字段集
   * @return update sql
   */
  private String getUpdateSql(DataSyncTableDefinition definition, List<String> tableColumnWithoutPrimaryKey) {
    String primaryKey = definition.getPrimaryKey().toLowerCase();
    String tenantIdKey = StringUtils.isEmpty(definition.getTenantIdAlias()) ? "tenant_id" : definition.getTenantIdAlias();
    StringBuilder updateSql = new StringBuilder();
    updateSql.append(" UPDATE ").append(definition.getTableCode()).append(" SET ");
    if (getOnlyUpdateStatusTables().contains(definition.getTableCode().toUpperCase())) {
      updateSql.append(" STATUS_CD=? ");
    }
    else {
      updateSql.append(String.join("=?,", tableColumnWithoutPrimaryKey)).append("=?");
    }
    updateSql.append(" WHERE ").append(primaryKey).append("=?").append(" AND ").append(tenantIdKey).append("=");
    if (definition.getResetTenantId() != null) {
      updateSql.append(definition.getResetTenantId());
    }
    else {
      updateSql.append(definition.getTenantId());
    }
    return updateSql.toString();
  }

  /**
   * IN 查询，查分多次，避免拼接 SQL 过长
   */
  private List<String> queryExists(DataSyncTableDefinition definition, List<Map<String, Object>> dataRecords) {
    String primaryKey = definition.getPrimaryKey().toLowerCase();
    List<Object> primaryValues = dataRecords.stream().map(record -> record.get(primaryKey)).collect(Collectors.toList());
    String tenantIdKey = StringUtils.isEmpty(definition.getTenantIdAlias()) ? "tenant_id" : definition.getTenantIdAlias();
    List<String> exists = new ArrayList<>();
    int limit = 1000;
    for (List<Object> values : ListUtils.partition(primaryValues, limit)) {
      StringBuilder querySql = new StringBuilder();
      // @formatter:off
      querySql.append("SELECT ").append(primaryKey)
        .append(" FROM ").append(definition.getTableCode())
        .append(" WHERE ")
        .append(primaryKey)
        .append(" IN (")
        .append(StringUtils.repeat("?", ", ", values.size())).append(")");
      if (BaseConsts.EXTERNAL_PORTAL_TBALE.equalsIgnoreCase(definition.getTableCode())) {
        if (logger.isInfoEnabled()) {
          logger.info("门户适配表，采用单主键，无需带上租户ID查询条件");
        }
      }
      else if (definition.getResetTenantId() != null) {
        querySql.append(" AND ").append(tenantIdKey).append("=").append(definition.getResetTenantId());
      }
      else {
        querySql.append(" AND ").append(tenantIdKey).append("=").append(definition.getTenantId());
      }
      // @formatter:on
      exists.addAll(jdbcTemplate.query(querySql.toString(), new LowerCaseColumnMapRowMapper(), values.toArray(new Object[0])).stream()
        .map(m -> m.get(primaryKey).toString()).collect(Collectors.toList()));
    }
    return exists;
  }

  /**
   * 更新标记为失效的配置数据
   *
   * @param definition 表定义
   */
  private void updateForInvalidData(DataSyncTableDefinition definition) {
    if (StringUtils.isEmpty(definition.getInvalidValues())) {
      return;
    }
    String tenantIdKey = StringUtils.isEmpty(definition.getTenantIdAlias()) ? "tenant_id" : definition.getTenantIdAlias();
    // 失效数据主键值集合
    // 默认主键字段是 Long 类型，部分配置表，主键值可能记录是非纯数字
    List<Object> values = Arrays.stream(definition.getInvalidValues().split("/")).map(m -> Long.parseLong(m.trim()))
      .collect(Collectors.toList());
    String sql = " UPDATE " + definition.getTableCode() + " SET STATUS_CD='00X' WHERE 1=1";
    doUpdateColumn(sql, values, definition.getPrimaryKey(), tenantIdKey, definition.getTenantId());
  }

  /**
   * update sql 组装主键值条件
   *
   * @param sql update sql
   * @param values 主键值集合
   * @param primaryKey 主键值字段
   * @param tenantId 租户 ID
   */
  private void doUpdateColumn(String sql, List<Object> values, String primaryKey, String tenantIdKey, Long tenantId) {
    int size = 1000;
    List<List<Object>> partitionList = Lists.partition(values, size);
    for (List<Object> ids : partitionList) {
      String condition = " AND " + primaryKey + " IN (" + StringUtils.repeat("?", ", ", ids.size()) + " ) AND " + tenantIdKey + "=" + tenantId;
      List<Object[]> updateArgs = new ArrayList<>();
      updateArgs.add(ids.toArray());
      TransactionUtil.executeNew(() -> jdbcTemplate.batchUpdate(sql + condition, updateArgs));
    }
  }

  /**
   * Clob 字段入库
   *
   * @param definition 表定义
   */
  private void updateForClob(DataSyncTableDefinition definition) {
    List<ScriptDefinition> sqls = new ArrayList<>();
    DataSyncClobUtil.getClobUpdateSql(definition, sqls);
    if (CollectionUtils.isEmpty(sqls)) {
      return;
    }
    TransactionUtil.executeNew(() -> {
      for (ScriptDefinition script : CollectionUtils.emptyIfNull(sqls)) {
        Object[] args = script.getArgs().stream().map(s -> StringUtils.isEmpty(s) ? null : new SqlCharacterValue(s)).toArray();
        jdbcTemplate.update(script.getSql(), args);
      }
    });
  }

  /**
   * 尝试回滚事务
   */
  private void tryRollbackOnly() {
    try {
      // 尝试获取当前事务状态
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
    catch (NoTransactionException e) {
      // 如果抛出 NoTransactionException，说明当前没有事务
      if (logger.isWarnEnabled()) {
        logger.warn("No transaction found, cannot rollback.");
      }
    }
    catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to rollback transaction.", e);
      }
    }
  }
}
