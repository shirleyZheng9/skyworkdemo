package com.iwhalecloud.bote.service.datasync.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.datasync.ContentReplaceRule;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.mapper.base.DataSyncRecordManageMapper;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_DATA_SOURCE_ID;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_FILE_ID;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_PAR_CATALOG_ID;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_SKILL_ID;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_SKILL_TYPE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.FIELD_TO_REMOVE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.RANDOM_SUFFIX_LENGTH;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.ROOT_CATALOG_PARENT_ID;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.SKILL_TYPE_KNOWLEDGE_CHAT;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_BOT_SCENE_SKILL;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_CATALOG;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_DATA_SOURCE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_DATA_SOURCE_INST;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_FILE_INFO;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_LABEL;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_LIBRARY_LARGE_MODEL;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SERVICE_GATEWAY;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SERVICE_PLATFORM;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_SQL;

/**
 * 数据同步辅助工具类 - 值替换（重构后的精简版本）
 *
 * @author chen.linfa
 * @since 2024-10-25
 */
@SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.GuardLogStatement"})
public final class DataSyncReplaceUtil {
  private static final Logger logger = LoggerFactory.getLogger(DataSyncReplaceUtil.class);

  private static final DataSyncRecordManageMapper dataSyncRecordManageMapper = SpringUtil.getBean(DataSyncRecordManageMapper.class);

  private DataSyncReplaceUtil() {
  }

  /**
   * 重置文件 ID
   */
  public static void replaceFileId(DataSyncTableDefinition definition, Map<Long, Long> fileIdMapper) {
    if (MapUtils.isEmpty(fileIdMapper) || !TABLE_BT_FILE_INFO.equalsIgnoreCase(definition.getTableCode())) {
      return;
    }

    for (Map<String, Object> record : CollectionUtils.emptyIfNull(definition.getDataRecords())) {
      Long fileId = MapUtils.getLong(record, FIELD_FILE_ID);
      if (fileId != null && fileIdMapper.get(fileId) != null) {
        record.replace(FIELD_FILE_ID, fileIdMapper.get(fileId));
      }
    }
  }

  /**
   * 替换主键ID并处理特殊字段
   */
  public static void replacePrimaryId(DataSyncTableDefinition definition, Map<String, Map<Long, Long>> mappings,
    Map<String, Map<String, String>> codeMappings, String mainTableCode, boolean resetPrimaryKey) {
    if (!resetPrimaryKey) {
      return;
    }
    if (CollectionUtils.isEmpty(definition.getDataRecords())) {
      return;
    }

    String tableCode = definition.getTableCode();
    Map<Long, Long> map = mappings.get(tableCode);
    if (MapUtils.isEmpty(map)) {
      map = new HashMap<>();
      mappings.put(tableCode, map);
    }

    // 处理特殊表
    if (processSpecialTables(definition, mappings, codeMappings)) {
      return;
    }

    // 处理普通表
    processNormalTable(definition, mappings, mainTableCode, tableCode);

    // 后处理
    postProcessTable(definition, mappings, codeMappings, tableCode);
  }

  private static boolean processSpecialTables(DataSyncTableDefinition definition, Map<String, Map<Long, Long>> mappings,
    Map<String, Map<String, String>> codeMappings) {
    String tableCode = definition.getTableCode();
    List<Map<String, Object>> records = definition.getDataRecords();

    switch (tableCode) {
      case TABLE_BT_CATALOG:
        SpecialTableProcessor.processCatalogRecords(records, definition.getResetTenantId(), mappings);
        generateNewIdsForUnmatched(definition, mappings.computeIfAbsent(tableCode, k -> new HashMap<>()));
        removeMarkedRecords(records);
        return true;

      case TABLE_BT_LIBRARY_LARGE_MODEL:
        SpecialTableProcessor.processModelRecords(records, definition.getResetTenantId(), mappings);
        generateNewIdsForUnmatched(definition, mappings.computeIfAbsent(tableCode, k -> new HashMap<>()));
        removeMarkedRecords(records);
        return true;

      case TABLE_BT_LABEL:
        SpecialTableProcessor.processLabelRecords(records, definition.getResetTenantId(), mappings);
        generateNewIdsForUnmatched(definition, mappings.computeIfAbsent(tableCode, k -> new HashMap<>()));
        removeMarkedRecords(records);
        return true;

      case TABLE_BT_SERVICE_PLATFORM:
      case TABLE_BT_SERVICE_GATEWAY:
      case TABLE_BT_DATA_SOURCE_INST:
      case TABLE_BT_DATA_SOURCE:
        SpecialTableProcessor.processApiGatewayAndDataSourceRecords(records, tableCode);
        removeMarkedRecords(records);
        return true;

      default:
        return false;
    }
  }

  private static void processNormalTable(DataSyncTableDefinition definition, Map<String, Map<Long, Long>> mappings, String mainTableCode,
    String tableCode) {
    Map<Long, Long> map = mappings.computeIfAbsent(tableCode, k -> new HashMap<>());
    List<ContentReplaceRule> rules = getRelatedColumn(definition.getContentReplaceRule());

    // 主循环 - 处理ID替换和关联字段
    for (Map<String, Object> record : definition.getDataRecords()) {
      generateNewIdForRecord(definition, record, map);
      processRelatedFields(record, rules, definition, mappings, mainTableCode);
      // 数据源置空
      if (TABLE_BT_SKILL_SQL.equals(tableCode)) {
        record.put(FIELD_DATA_SOURCE_ID, null);
      }
    }
  }

  private static void postProcessTable(DataSyncTableDefinition definition, Map<String, Map<Long, Long>> mappings,
    Map<String, Map<String, String>> codeMappings, String tableCode) {
    List<Map<String, Object>> records = definition.getDataRecords();

    // 处理特殊业务逻辑
    if (TABLE_BT_BOT_SCENE_SKILL.equals(tableCode)) {
      processSceneSkillTable(records, mappings);
    }

    // 处理名称和编码字段
    if (!isSkippedForFieldProcessing(tableCode)) {
      Map<String, String> tableCodeMap = codeMappings.computeIfAbsent(tableCode, k -> new HashMap<>());
      processMainTableFields(records, tableCode, tableCodeMap);
    }

    // 处理JSON字段
    for (Map<String, Object> record : records) {
      JsonFieldProcessor.processJsonFields(record, tableCode, mappings, codeMappings);
    }

    // 处理目录的递归关系
    if (TABLE_BT_CATALOG.equals(tableCode)) {
      processCatalogParentChildRelation(records, mappings);
    }

    // 移除标记为删除的记录
    removeMarkedRecords(records);
  }

  private static boolean isSkippedForFieldProcessing(String tableCode) {
    return TABLE_BT_CATALOG.equals(tableCode) || TABLE_BT_LABEL.equals(tableCode);
  }

  private static void generateNewIdForRecord(DataSyncTableDefinition definition, Map<String, Object> record, Map<Long, Long> map) {
    Long id = MapUtils.getLong(record, definition.getPrimaryKey());
    Long newId = IDUtils.nextId();
    record.put(definition.getPrimaryKey(), newId);
    map.put(id, newId);
  }

  private static void processRelatedFields(Map<String, Object> record, List<ContentReplaceRule> rules, DataSyncTableDefinition definition,
    Map<String, Map<Long, Long>> mappings, String mainTableCode) {
    // 处理关联字段
    for (ContentReplaceRule rule : CollectionUtils.emptyIfNull(rules)) {
      resetRelateId(record, rule.getColumnCode(), rule.getRelatedTable(), mappings);
    }

    // 处理同组配置的外键字段
    if (StringUtils.isNotEmpty(definition.getForeignKey())) {
      resetRelateId(record, definition.getForeignKey(), mainTableCode, mappings);
    }
  }

  private static void processSceneSkillTable(List<Map<String, Object>> records, Map<String, Map<Long, Long>> mappings) {
    for (Map<String, Object> record : records) {
      processSceneSkillRelation(record, mappings);
    }
    processKnowledgeChatSkills(records);
  }

  private static void generateNewIdsForUnmatched(DataSyncTableDefinition definition, Map<Long, Long> map) {
    for (Map<String, Object> record : definition.getDataRecords()) {
      if (!Boolean.TRUE.equals(record.get(FIELD_TO_REMOVE))) {
        Long id = MapUtils.getLong(record, definition.getPrimaryKey());
        Long newId = IDUtils.nextId();
        record.put(definition.getPrimaryKey(), newId);
        map.put(id, newId);
      }
    }
  }

  private static void processSceneSkillRelation(Map<String, Object> record, Map<String, Map<Long, Long>> mappings) {
    try {
      String skillType = MapUtils.getString(record, FIELD_SKILL_TYPE);
      Object skillIdObj = record.get(FIELD_SKILL_ID);

      if (StringUtils.isBlank(skillType) || skillIdObj == null) {
        return;
      }

      Long oldSkillId = convertToLong(skillIdObj);
      if (oldSkillId == null) {
        return;
      }

      String targetTable = TableFieldMappingUtil.getTargetTableBySkillType(skillType);
      if (targetTable == null) {
        logger.debug("未知技能类型: {}", skillType);
        return;
      }

      updateSkillRelation(record, oldSkillId, targetTable, mappings);
    }
    catch (Exception e) {
      logger.error("处理场景技能关联记录时发生异常: {}", e.getMessage(), e);
    }
  }

  private static void updateSkillRelation(Map<String, Object> record, Long oldSkillId, String targetTable, Map<String, Map<Long, Long>> mappings) {
    Map<Long, Long> tableMappings = mappings.get(targetTable);
    if (MapUtils.isNotEmpty(tableMappings) && tableMappings.containsKey(oldSkillId)) {
      Long newSkillId = tableMappings.get(oldSkillId);
      record.put(FIELD_SKILL_ID, newSkillId);
      logger.debug("更新场景技能关联，targetTable: {}, 原skill_id: {}, 新skill_id: {}", targetTable, oldSkillId, newSkillId);
    }

  }

  private static Long convertToLong(Object obj) {
    if (obj == null) {
      return null;
    }
    if (obj instanceof Number) {
      return ((Number) obj).longValue();
    }
    try {
      return Long.parseLong(obj.toString());
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  private static void processKnowledgeChatSkills(List<Map<String, Object>> records) {
    if (CollectionUtils.isEmpty(records)) {
      return;
    }

    records.removeIf(record -> {
      String skillType = (String) record.get(FIELD_SKILL_TYPE);
      return SKILL_TYPE_KNOWLEDGE_CHAT.equals(skillType);
    });
  }

  private static void processCatalogParentChildRelation(List<Map<String, Object>> records, Map<String, Map<Long, Long>> mappings) {
    if (CollectionUtils.isEmpty(records)) {
      return;
    }

    Map<Long, Long> catalogMappings = mappings.get(TABLE_BT_CATALOG);
    if (MapUtils.isEmpty(catalogMappings)) {
      return;
    }

    for (Map<String, Object> record : records) {
      if (Boolean.TRUE.equals(record.get(FIELD_TO_REMOVE))) {
        continue;
      }

      Long parCatalogId = MapUtils.getLong(record, FIELD_PAR_CATALOG_ID);
      if (parCatalogId != null && !ROOT_CATALOG_PARENT_ID.equals(parCatalogId)) {
        Long newParCatalogId = catalogMappings.get(parCatalogId);
        if (newParCatalogId != null) {
          record.put(FIELD_PAR_CATALOG_ID, newParCatalogId);
          logger.debug("更新目录父子关系，原父目录ID: {}, 新父目录ID: {}", parCatalogId, newParCatalogId);
        }
      }
    }
  }

  private static void resetRelateId(Map<String, Object> record, String columnCode, String tableCode, Map<String, Map<Long, Long>> mappings) {
    if (!mappings.containsKey(tableCode)) {
      return;
    }
    Long relateId = MapUtils.getLong(record, columnCode);
    if (mappings.get(tableCode).containsKey(relateId)) {
      record.put(columnCode, mappings.get(tableCode).get(relateId));
    }
  }

  private static List<ContentReplaceRule> getRelatedColumn(String contentReplaceRule) {
    if (StringUtils.isEmpty(contentReplaceRule)) {
      return Collections.emptyList();
    }
    return CollectionUtils.emptyIfNull(JsonUtil.parseJson(contentReplaceRule, new TypeReference<List<ContentReplaceRule>>() {
    })).stream().filter(p -> "relate".equals(p.getRule())).collect(Collectors.toList());
  }

  private static void removeMarkedRecords(List<Map<String, Object>> records) {
    if (CollectionUtils.isEmpty(records)) {
      return;
    }

    records.removeIf(record -> Boolean.TRUE.equals(record.get(FIELD_TO_REMOVE)));
  }

  private static void processMainTableFields(List<Map<String, Object>> records, String tableCode, Map<String, String> codeMap) {
    if (CollectionUtils.isEmpty(records)) {
      return;
    }

    for (Map<String, Object> record : records) {
      processNameField(record, tableCode);
      processCodeField(record, tableCode, codeMap);
    }
  }

  private static void processNameField(Map<String, Object> record, String tableCode) {
    String nameField = TableFieldMappingUtil.getNameField(tableCode);
    if (StringUtils.isBlank(nameField)) {
      return;
    }

    Object nameValue = record.get(nameField);
    if (nameValue instanceof String && StringUtils.isNotEmpty((String) nameValue)) {
      String originalName = (String) nameValue;
      String newName = generateNewValue(originalName);
      record.put(nameField, newName);
      logger.debug("表 {} 的名称字段 {} 从 {} 替换为 {}", tableCode, nameField, originalName, newName);
    }
  }

  private static void processCodeField(Map<String, Object> record, String tableCode, Map<String, String> codeMap) {
    String codeField = TableFieldMappingUtil.getCodeField(tableCode);
    if (StringUtils.isBlank(codeField)) {
      return;
    }

    Object codeValue = record.get(codeField);
    if (codeValue instanceof String && StringUtils.isNotEmpty((String) codeValue)) {
      String originalCode = (String) codeValue;
      String newCode = generateNewValue(originalCode);
      record.put(codeField, newCode);

      if (codeMap != null) {
        codeMap.put(originalCode, newCode);
      }

      logger.debug("表 {} 的编码字段 {} 从 {} 替换为 {}", tableCode, codeField, originalCode, newCode);
    }
  }

  private static String generateNewValue(String originalName) {
    if (StringUtils.isBlank(originalName)) {
      return RandomStringUtils.secure().nextAlphanumeric(RANDOM_SUFFIX_LENGTH);
    }
    return originalName + "_" + RandomStringUtils.secure().nextAlphanumeric(RANDOM_SUFFIX_LENGTH);
  }

  /**
   * 全量导入更新状态码为 00M
   */
  public static void updateStatusCdForClear(DataSyncParams params) {
    // 如果不是全量同步或者复制场景，则不更新状态码
    if (BooleanUtils.isNotTrue(params.getSyncAll()) || params.getResetTenantId() != null) {
      return;
    }
    List<DataSyncTableDefinition> dataSyncTableDefinitions = dataSyncRecordManageMapper.selectTableDefinition();
    List<DataSyncTableDefinition> tableDefinitions = ListUtils.emptyIfNull(dataSyncTableDefinitions).stream()
      .filter(p -> StringUtils.isNotEmpty(p.getSyncAllQueryCondition()))
      // 跳过提示词相关的表
      .filter(p -> !Strings.CS.equalsAny(p.getTableCode(), "bt_prompt_basic", "bt_prompt_commit"))
      .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(tableDefinitions)) {
      String updateSql = "UPDATE %s SET %s = '00M' WHERE %s";

      // 如果是mysql的安全模式，则拼接limit语法。否则会提示以下异常
      // You are using safe update mode and you tried to update a table without a WHERE that uses a KEY column.
      if (SystemParameter.MYSQL_SAFE_MODE.getBooleanValueFromDb()) {
        updateSql += " limit 1000000";
      }

      String tenantId = String.valueOf(params.getTenantId());
      String[] updateSqlArray = new String[tableDefinitions.size()];
      for (int i = 0; i < tableDefinitions.size(); i++) {
        String condition = tableDefinitions.get(i).getSyncAllQueryCondition();
        condition = condition.replace(":tenantId", tenantId);
        // 定时任务状态字段特殊处理
        String status = "bt_job".equals(tableDefinitions.get(i).getTableCode()) ? "state" : "status_cd";
        updateSqlArray[i] = String.format(updateSql, tableDefinitions.get(i).getTableCode(), status, condition);
      }
      TransactionUtil.executeNew(() -> SpringUtil.getBean(JdbcTemplate.class).batchUpdate(updateSqlArray));
    }
  }
}
