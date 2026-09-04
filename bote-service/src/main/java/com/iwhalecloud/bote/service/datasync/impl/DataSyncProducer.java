package com.iwhalecloud.bote.service.datasync.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.DataSyncConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.jdbc.LowerCaseColumnMapRowMapper;
import com.iwhalecloud.bote.common.sql.convert.SqlPageConvert;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.dto.base.SimpleElementDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncNodeDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.mapper.base.DataSyncRecordManageMapper;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDateUtil;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDirUtil;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDocumentUtil;
import com.iwhalecloud.bote.service.datasync.util.DataSyncFileUtil;
import com.iwhalecloud.bote.service.datasync.util.DatasyncScriptUtil;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.database.util.DbUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据同步 - 数据收集
 *
 * @author chen.linfa
 * @since 2024-10-22
 */
@Component
@RequiredArgsConstructor
@SuppressFBWarnings("SECSQLISPRJDBC")
@SuppressWarnings("PMD.GuardLogStatement")
public class DataSyncProducer {
  private static final Logger logger = LoggerFactory.getLogger(DataSyncProducer.class);

  // @formatter:off
  private final DataSyncRecordManageMapper mapper;
  private final IResourceElementService resourceElementService;
  private final JdbcTemplate jdbcTemplate;
  // @formatter:on

  public ResultVO<Void> execute(DataSyncParams params) {
    try {
      // 获取需要同步的模块
      createTableDefinition(params);
      // 收集需要同步的数据
      collectTableRecord(params);
      // 收集文件资源
      DataSyncFileUtil.collectFileResource(params);
      // 收集建模脚本
      DatasyncScriptUtil.export(params);
      return ResultVO.success();
    }
    catch (Exception e) {
      logger.error("Failed to collect data. error={}", e.getMessage(), e);
      return ResultVO.fail("收集应用数据出现异常: " + e.getMessage());
    }
  }

  private void createTableDefinition(DataSyncParams params) {
    computeResourceElement(params);
    // 自定义模式，需要同步租户数据
    if (BooleanUtils.isNotTrue(params.getSyncAll()) && !params.getCodeAndIds()
      .containsKey(DataSyncCodeEnum.TENANT.getCode()) && params.getResetTenantId() == null) {
      params.getCodeAndIds().put(DataSyncCodeEnum.TENANT.getCode(), String.valueOf(params.getTenantId()));
    }
    List<String> paths = Arrays.asList("TENANT", "BOT");
    Map<String, List<DataSyncTableDefinition>> nodes = new HashMap<>();
    Map<String, List<DataSyncTableDefinition>> group = params.getDefinitions().stream().filter(p -> paths.contains(p.getDataConfigPath()))
      .collect(Collectors.groupingBy(DataSyncTableDefinition::getDataConfigCode));
    for (Entry<String, List<DataSyncTableDefinition>> entry : group.entrySet()) {
      // 节点排序
      List<DataSyncTableDefinition> definitions = entry.getValue();
      definitions.sort(Comparator.comparing(DataSyncTableDefinition::getSortby));

      String code = entry.getKey();
      // 按照 sort 字段排序，第一个属于模块主表
      DataSyncTableDefinition mainTable = definitions.get(0);
      mainTable.setMainTable(true);
      if (BooleanUtils.isNotTrue(params.getSyncAll())) {
        // 自定义模式，剔除无关定义
        if (!params.getCodeAndIds().containsKey(code)) {
          continue;
        }
        mainTable.setValues(params.getCodeAndIds().get(code));
      }
      nodes.put(entry.getKey(), definitions);
    }
    params.setNodes(nodes);
  }

  /**
   * 收集数据
   * <p>1. 采用专门的线程池并发处理</p>
   * <p>2. 执行粒度：一个同步节点一个任务</p>
   *
   * @param params 收集条件
   */
  private void collectTableRecord(DataSyncParams params) {
    DataSyncDirUtil.createRootJsonFile(params);
    // 异步处理模式
    List<Runnable> tasks = new ArrayList<>();
    for (Entry<String, List<DataSyncTableDefinition>> entry : params.getNodes().entrySet()) {
      tasks.add(() -> collectDataRecord(params, entry.getValue()));
    }
    ThreadPools.invokeTasks(ThreadPools.getDatasync(), tasks);
    // 收集写入文档中心的数据
    createDocumentTableFile(params, params.getDocumentCenterDefinitions());
  }

  /**
   * 按照节点模块收集数据
   */
  private void collectDataRecord(DataSyncParams params, List<DataSyncTableDefinition> definitions) {
    // 外键数据缓存，用于自定义模式
    String foreignKey = definitions.get(0).getTableCode() + "-" + definitions.get(0).getPrimaryKey();
    Map<String, List<Object>> foreignKeyValues = new HashMap<>(16);
    for (DataSyncTableDefinition definition : definitions) {

      StringBuilder querySql = new StringBuilder();
      List<Object> args = new ArrayList<>();
      // 构造查询 SQL
      if (buildQuerySql(params, definition, foreignKey, foreignKeyValues, querySql, args)) {
        continue;
      }
      List<Map<String, Object>> records = queryTableRecord(querySql.toString(), args, definition);
      if (CollectionUtils.isNotEmpty(records)) {
        // 存在收集数据，才需要生成节点 json 文件，减少不必要的开销
        // 性能优化，剔除 records 中失效的数据，只记录失效数据主键值集合 invalidValues
        collectInvalidRecord(definition, records);
        // 收集文档表定义
        DataSyncDocumentUtil.collectDocumentTables(definition, params);
        // 文档中心的数据后面统一写入文件
        if ("library".equals(definition.getDataConfigCode())) {
          continue;
        }

        if (BooleanUtils.isFalse(params.getSyncAll()) && BooleanUtils.isTrue(definition.getMainTable())) {
          // 增量场景，主表当前的主键值维护在 foreignKeyValues
          List<Object> values = records.stream().map(m -> MapUtils.getObject(m, definition.getPrimaryKey())).collect(Collectors.toList());
          foreignKeyValues.put(foreignKey, values);
        }
        if (filterDefinition(definition, params)) {
          continue;
        }
        // 日期数据格式转换
        DataSyncDateUtil.toString(definition);
        // 按照表粒度存储表数据
        DataSyncDirUtil.createTableJsonFile(params, definition);
        DataSyncDirUtil.createNodeJsonFile(params, definition);
      }
    }
  }

  /**
   *
   * 将知识库和知识库文档置换处理
   */
  private boolean filterDefinition(DataSyncTableDefinition definition, DataSyncParams params) {
    if (DataSyncCodeEnum.KNOWLEDGE.getCode().equals(definition.getDataConfigCode())
      && DataSyncConsts.TABLE_BT_KNOWLEDGE_BASE.equals(definition.getTableCode())
      || DataSyncCodeEnum.KNOWLEDGE.getCode().equals(definition.getDataConfigCode())
        && DataSyncConsts.TABLE_BT_DOCUMENT.equals(definition.getTableCode())) {
      List<DataSyncTableDefinition> documentCenterDefinitions = params.getDocumentCenterDefinitions();
      if (CollectionUtils.isEmpty(documentCenterDefinitions)) {
        params.setDocumentCenterDefinitions(new ArrayList<>());
      }
      params.getDocumentCenterDefinitions().add(definition);
      return true;
    }
    return false;
  }

  /**
   * 创建文档中心的文件
   */
  private void createDocumentTableFile(DataSyncParams params, List<DataSyncTableDefinition> definitions) {
    List<Map<String, Object>> documentDataRecords = collectKnowledgeDocumentRecords(definitions);
    Map<Object, List<Map<String, Object>>> documentDataMap = buildDocumentDataMap(documentDataRecords);

    for (DataSyncTableDefinition definition : definitions) {
      enrichKnowledgeFileCounts(definition, documentDataMap);
      writeDocumentCenterFiles(params, definition);
    }
  }

  /**
   * 获取知识库文档表数据
   */
  private List<Map<String, Object>> collectKnowledgeDocumentRecords(List<DataSyncTableDefinition> definitions) {
    List<Map<String, Object>> records = new ArrayList<>();
    definitions.stream()
      .filter(def -> DataSyncConsts.TABLE_BT_DOCUMENT.equals(def.getTableCode()) && DataSyncCodeEnum.KNOWLEDGE.getCode().equals(def.getDataConfigCode()))
      .map(DataSyncTableDefinition::getDataRecords)
      .filter(CollectionUtils::isNotEmpty)
      .forEach(records::addAll);
    return records;
  }

  /**
   * 按 knowledge_id 分组文档记录
   */
  private Map<Object, List<Map<String, Object>>> buildDocumentDataMap(List<Map<String, Object>> documentDataRecords) {
    if (CollectionUtils.isEmpty(documentDataRecords)) {
      return new HashMap<>();
    }
    return documentDataRecords.stream()
      .filter(record -> record.get("knowledge_id") != null)
      .collect(Collectors.groupingBy(record -> record.get("knowledge_id")));
  }

  /**
   * 填充知识库的 file_counts
   */
  private void enrichKnowledgeFileCounts(DataSyncTableDefinition definition, Map<Object, List<Map<String, Object>>> documentDataMap) {
    if (!(DataSyncCodeEnum.KNOWLEDGE.getCode().equals(definition.getDataConfigCode())
      && DataSyncConsts.TABLE_BT_KNOWLEDGE_BASE.equals(definition.getTableCode()))) {
      return;
    }

    List<Map<String, Object>> dataRecords = definition.getDataRecords();
    if (CollectionUtils.isEmpty(dataRecords) || MapUtils.isEmpty(documentDataMap)) {
      return;
    }

    dataRecords.forEach(record -> {
      Object knowledgeId = record.get("knowledge_id");
      if (knowledgeId != null) {
        List<Map<String, Object>> dataList = documentDataMap.get(knowledgeId);
        if (CollectionUtils.isNotEmpty(dataList)) {
          record.put("file_counts", dataList.size());
        }
        else {
          record.put("file_counts", 0);
        }
      }
    });
  }

  /**
   * 写入文档中心定义对应的文件
   */
  private void writeDocumentCenterFiles(DataSyncParams params, DataSyncTableDefinition definition) {
    List<Map<String, Object>> records = definition.getDataRecords();
    if (CollectionUtils.isEmpty(records)) {
      return;
    }
    // 存在收集数据，才需要生成节点 json 文件，减少不必要的开销
    DataSyncDateUtil.toString(definition);
    DataSyncDirUtil.createTableJsonFile(params, definition);
    DataSyncDirUtil.createNodeJsonFile(params, definition);
  }

  /**
   * 根据表定义组装查询 SQL
   *
   * @return 是否跳过
   */
  private boolean buildQuerySql(DataSyncParams params, DataSyncTableDefinition definition, String foreignKey,
    Map<String, List<Object>> foreignKeyValues, StringBuilder querySql, List<Object> args) {
    querySql.append("SELECT * FROM ").append(definition.getTableCode()).append(" WHERE 1 = 1 ");
    if (StringUtils.isNotEmpty(definition.getSyncAllQueryCondition())) {
      String condition = definition.getSyncAllQueryCondition().replace(":tenantId", params.getTenantId().toString());
      querySql.append(" AND ").append(condition);
    }
    return BooleanUtils.isFalse(params.getSyncAll()) && buildWhereCondition(definition, foreignKey, foreignKeyValues, querySql, args);
  }

  private boolean buildWhereCondition(DataSyncTableDefinition definition, String foreignKey, Map<String, List<Object>> foreignKeyValues,
    StringBuilder querySql, List<Object> args) {
    if (BooleanUtils.isTrue(definition.getMainTable())) {
      if (StringUtils.isEmpty(definition.getValues())) {
        return true;
      }
      return buildMainTableWhereCondition(definition, querySql, args);
    }
    return buildOtherTableWhereCondition(definition, foreignKey, foreignKeyValues, querySql, args);
  }

  private boolean buildMainTableWhereCondition(DataSyncTableDefinition definition, StringBuilder querySql, List<Object> args) {
    // 如果是节点主表，带上主键查询条件
    List<Object> primaryKeyValues = Arrays.stream(definition.getValues().split("/")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
    List<List<Object>> partitionPrimaryKeyValues = Lists.partition(primaryKeyValues, 1000);
    // @formatter:off
    querySql.append(" AND ( ");
    for (Iterator<List<Object>> iterator = partitionPrimaryKeyValues.iterator(); iterator.hasNext();) {
      List<Object> partitionPrimaryKeyValue = iterator.next();
      querySql.append(definition.getPrimaryKey()).append(" IN (").append(StringUtils.repeat("?", ", ", partitionPrimaryKeyValue.size()))
        .append(" ) ");
      if (iterator.hasNext()) {
        querySql.append(" OR ");
      }
    }
    querySql.append(" ) ");
    // @formatter:on
    args.addAll(primaryKeyValues);
    // 不跳过查询
    return false;
  }

  private boolean buildOtherTableWhereCondition(DataSyncTableDefinition definition, String foreignKey, Map<String, List<Object>> foreignKeyValues,
    StringBuilder querySql, List<Object> args) {
    if (StringUtils.isEmpty(definition.getForeignKey())) {
      return false;
    }

    List<Object> ids = foreignKeyValues.get(foreignKey);
    if (CollectionUtils.isEmpty(ids)) {
      // 没找到关联外键值时，跳过
      return true;
    }
    ids = ids.stream().distinct().collect(Collectors.toList());
    List<List<Object>> partitionIds = Lists.partition(ids, 1000);
    querySql.append(" AND (");
    // @formatter:off
    for (Iterator<List<Object>> iterator = partitionIds.iterator(); iterator.hasNext();) {
      List<Object> partitionId = iterator.next();
      querySql.append(definition.getForeignKey()).append(" IN (").append(StringUtils.repeat("?", ", ", partitionId.size())).append(") ");
      if (iterator.hasNext()) {
        querySql.append(" OR ");
      }
    }
    // @formatter:on
    querySql.append(" ) ");
    args.addAll(ids);

    return false;
  }

  /**
   * 收集表数据，对于数据量过大的配置表，采用分批处理
   *
   * @param sql 查询 SQL
   * @param args 查询 SQL 条件
   * @param definition 表定义
   * @return 配置数据
   */
  private List<Map<String, Object>> queryTableRecord(String sql, List<Object> args, DataSyncTableDefinition definition) {
    String countSql = "SELECT COUNT(*) FROM (" + sql + ") TEMP ";
    int total = ObjectUtils.getIfNull(jdbcTemplate.queryForObject(countSql, Integer.class, args.toArray(new Object[0])), 0);
    int limit = 10000;
    if (total <= limit) {
      return jdbcTemplate.query(sql, new LowerCaseColumnMapRowMapper(), args.toArray(new Object[0]));
    }
    // 分页查询，补充主键排序条件，避免多批次查询出现数据重复
    String orderBy = " order by " + definition.getPrimaryKey() + " ";
    List<Map<String, Object>> records = new ArrayList<>();
    for (int i = 0; i < (total / limit) + 1; i++) {
      String querySql = SqlPageConvert.convert(DbUtil.getDatabaseType().name().toLowerCase(), sql + orderBy, i + 1, limit);
      records.addAll(jdbcTemplate.query(querySql, new LowerCaseColumnMapRowMapper(), args.toArray(new Object[0])));
    }
    return records;
  }

  /**
   * 性能优化，剔除 records 中失效的数据，只记录失效数据主键值集合 invalidValues
   * <p>1. 目的：减少导出数据包冗余数据</p>
   *
   * @param definition 表定义
   * @param dataRecords 表数据
   */
  private void collectInvalidRecord(DataSyncTableDefinition definition, List<Map<String, Object>> dataRecords) {
    String primaryKey = definition.getPrimaryKey().toLowerCase();
    String invalidStatusCd = BaseConsts.STATUS_CD_INVALID;
    List<String> invalidValues = new ArrayList<>();
    List<Map<String, Object>> datas = new ArrayList<>();
    for (Map<String, Object> data : dataRecords) {
      if (Objects.equals(invalidStatusCd, MapUtils.getString(data, "status_cd"))) {
        invalidValues.add(MapUtils.getObject(data, primaryKey).toString());
      }
      else {
        datas.add(data);
      }
    }
    definition.setDataRecords(datas);

    if (CollectionUtils.isNotEmpty(invalidValues)) {
      definition.setInvalidValues(String.join("/", invalidValues));
    }
  }

  /**
   * 增量导出场景，根据血缘关系，自动处理关联数据
   */
  private void computeResourceElement(DataSyncParams params) {
    Map<String, String> codeAndIds = params.getCodeAndIds();
    if (BooleanUtils.isTrue(params.getSyncAll()) || MapUtils.isEmpty(codeAndIds) || BooleanUtils.isNotTrue(params.getRelatable())) {
      return;
    }
    Map<String, String> newEntries = new HashMap<>();
    // 遍历codeAndIds中的每个节点，调用queryRelatedResource方法获取关联资源
    for (Map.Entry<String, String> entry : codeAndIds.entrySet()) {
      processResourceElementEntry(params, entry, newEntries);
    }
    // 统一添加新收集的键值对
    codeAndIds.putAll(newEntries);
  }

  /**
   * 处理单个资源元素条目
   */
  private void processResourceElementEntry(DataSyncParams params, Map.Entry<String, String> entry, Map<String, String> codeAndIds) {
    String code = entry.getKey();
    String valuesStr = entry.getValue();

    if (StringUtils.isBlank(valuesStr)) {
      return;
    }

    // 将字符串转换为Long列表
    List<Long> values = parseValuesString(valuesStr);

    // 调用queryRelatedResource方法获取关联资源
    List<DataSyncNodeDTO> nodeDTOList = callQueryRelatedResource(params, code, values);

    // 处理关联资源数据，拼接成codeAndIds格式
    processRelatedResourceNodes(nodeDTOList, codeAndIds);
  }

  /**
   * 解析值字符串为Long列表
   */
  private List<Long> parseValuesString(String valuesStr) {
    return Arrays.stream(valuesStr.split("/")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
  }

  /**
   * 调用queryRelatedResource方法
   */
  private List<DataSyncNodeDTO> callQueryRelatedResource(DataSyncParams params, String code, List<Long> values) {
    Map<String, List<SimpleElementDTO>> data = resourceElementService.queryRelatedResource(params.getTenantId(), values, code);

    List<DataSyncNodeDTO> nodes = mapper.selectDataSyncNode();
    List<DataSyncNodeDTO> list = new ArrayList<>();
    for (Entry<String, List<SimpleElementDTO>> entry : data.entrySet()) {
      DataSyncNodeDTO node = IterableUtils.find(nodes, p -> p.getCode().equals(entry.getKey()));
      if (node == null) {
        continue;
      }
      // 根据 id 字段去重
      List<SimpleElementDTO> uniqueElements = new ArrayList<>(
        entry.getValue().stream().collect(Collectors.toMap(SimpleElementDTO::getId, element -> element, (existing, replacement) -> existing))
          .values());
      node.setRecords(JsonUtil.parseJson(JsonUtil.toJsonString(uniqueElements), new TypeReference<List<Map<String, Object>>>() {
      }));
      list.add(node);
    }
    return list;
  }

  /**
   * 处理关联资源节点
   */
  private void processRelatedResourceNodes(List<DataSyncNodeDTO> nodes, Map<String, String> codeAndIds) {
    for (DataSyncNodeDTO node : nodes) {
      String nodeCode = node.getCode();
      List<Map<String, Object>> records = node.getRecords();

      if (CollectionUtils.isEmpty(records)) {
        continue;
      }

      // 提取ID值并拼接
      Set<Long> ids = extractAndMergeIds(nodeCode, records, codeAndIds);

      // 更新codeAndIds
      if (!ids.isEmpty()) {
        codeAndIds.put(nodeCode, StringUtils.join(ids, "/"));
      }
    }
  }

  /**
   * 提取并合并ID值
   */
  private Set<Long> extractAndMergeIds(String nodeCode, List<Map<String, Object>> records, Map<String, String> codeAndIds) {
    Set<Long> ids = new HashSet<>();

    // 如果codeAndIds中已存在该节点，先添加原有数据
    addExistingIds(nodeCode, codeAndIds, ids);

    // 添加新获取的关联资源ID
    addNewIds(records, ids);

    return ids;
  }

  /**
   * 添加已存在的ID
   */
  private void addExistingIds(String nodeCode, Map<String, String> codeAndIds, Set<Long> ids) {
    if (codeAndIds.containsKey(nodeCode)) {
      String existingValues = codeAndIds.get(nodeCode);
      if (StringUtils.isNotBlank(existingValues)) {
        ids.addAll(Arrays.stream(existingValues.split("/")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList()));
      }
    }
  }

  /**
   * 添加新的ID
   */
  private void addNewIds(List<Map<String, Object>> records, Set<Long> ids) {
    for (Map<String, Object> record : records) {
      Object idObj = record.get("id");
      if (idObj != null) {
        if (idObj instanceof Number) {
          ids.add(((Number) idObj).longValue());
        }
        else {
          try {
            ids.add(Long.parseLong(idObj.toString()));
          }
          catch (NumberFormatException e) {
            logger.warn("无法解析ID值: {}", idObj);
          }
        }
      }
    }
  }
}
