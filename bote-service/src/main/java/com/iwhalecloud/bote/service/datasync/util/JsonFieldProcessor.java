package com.iwhalecloud.bote.service.datasync.util;

import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.iwhalecloud.bote.common.consts.DataSyncConsts.JSON_FIELD_FLOW_DSL;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.JSON_FIELD_FLOW_GRAPH_JSON;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.JSON_FIELD_PAGE_TEMPLATE_JSON;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.JSON_FIELD_SCENE_DSL;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.JSON_FIELD_SCENE_GRAPH_JSON;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.JSON_FIELD_SKILL_JSON;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.SKILL_TYPE_PAGE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.SKILL_TYPE_SERVICE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_BOT_SCENE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_BOT_SCENE_SKILL;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_ATTR_SPEC;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_FLOW;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_PAGE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_PAGE_FUNC;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_SERVICE;

/**
 * JSON字段处理器 - 处理JSON字段中的ID和编码替换
 *
 * @author yangran
 * @since 2025-8-15
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class JsonFieldProcessor {
  private static final Logger logger = LoggerFactory.getLogger(JsonFieldProcessor.class);

  /**
   * 处理JSON字段中的主键替换
   */
  public static void processJsonFields(Map<String, Object> record, String tableCode, Map<String, Map<Long, Long>> mappings,
    Map<String, Map<String, String>> codeMappings) {
    if (TABLE_BT_BOT_SCENE.equals(tableCode)) {
      processJsonField(record, JSON_FIELD_SCENE_GRAPH_JSON, mappings, codeMappings);
      processJsonField(record, JSON_FIELD_SCENE_DSL, mappings, codeMappings);
    }

    if (TABLE_BT_BOT_SCENE_SKILL.equals(tableCode)) {
      processJsonField(record, JSON_FIELD_SKILL_JSON, mappings, codeMappings);
    }

    if (TABLE_BT_SKILL_FLOW.equals(tableCode)) {
      processJsonField(record, JSON_FIELD_FLOW_GRAPH_JSON, mappings, codeMappings);
      processJsonField(record, JSON_FIELD_FLOW_DSL, mappings, codeMappings);
    }

    if (TABLE_BT_SKILL_PAGE.equals(tableCode)) {
      processJsonField(record, JSON_FIELD_PAGE_TEMPLATE_JSON, mappings, codeMappings);
    }
  }

  /**
   * 处理单个JSON字段中的主键替换
   */
  private static void processJsonField(Map<String, Object> record, String fieldName, Map<String, Map<Long, Long>> mappings,
    Map<String, Map<String, String>> codeMappings) {
    Object fieldValue = record.get(fieldName);
    if (fieldValue instanceof String && StringUtils.isNotEmpty((String) fieldValue)) {
      String jsonContent = (String) fieldValue;
      String updatedContent = replaceIdsInJsonContent(jsonContent, mappings, codeMappings);
      if (!jsonContent.equals(updatedContent)) {
        record.put(fieldName, updatedContent);
      }
    }
  }

  /**
   * 在JSON内容中替换ID和编码
   */
  private static String replaceIdsInJsonContent(String jsonContent, Map<String, Map<Long, Long>> mappings,
    Map<String, Map<String, String>> codeMappings) {
    String result = jsonContent;

    // 替换直接ID字段
    result = replaceIdFields(result, mappings);

    // 替换编码映射
    result = replaceCodesInJsonContent(result, codeMappings);

    return result;
  }

  private static String replaceIdFields(String jsonContent, Map<String, Map<Long, Long>> mappings) {
    String result = jsonContent;

    for (Map.Entry<String, Map<Long, Long>> tableEntry : mappings.entrySet()) {
      String tableName = tableEntry.getKey();
      Map<Long, Long> tableMappings = tableEntry.getValue();

      for (Map.Entry<Long, Long> mapping : tableMappings.entrySet()) {
        Long oldId = mapping.getKey();
        Long newId = mapping.getValue();

        result = replaceCommonIdFields(result, oldId, newId);
        result = replaceContextualContentIdsByTable(result, tableName, oldId, newId);
      }
    }

    return result;
  }

  private static String replaceCommonIdFields(String jsonContent, Long oldId, Long newId) {
    String result = jsonContent;

    // 替换各种ID字段
    String[] idFields = {"flowId", "skillId", "serviceId", "sqlId", "funcId", "apiId", "serverId", "modelId", "promptId", "knowledgeId", "pageId",
      "sceneId", "pluginId", "pageFuncId"
    };

    for (String fieldName : idFields) {
      result = replaceIdInJson(result, fieldName, oldId, newId);
    }

    return result;
  }

  /**
   * 在JSON中替换指定字段的ID值
   */
  private static String replaceIdInJson(String jsonContent, String idField, Long oldId, Long newId) {
    String result = jsonContent;

    // 替换数字格式的ID
    result = result.replaceAll("\"" + idField + "\"\\s*:\\s*" + oldId + "\\b", "\"" + idField + "\": " + newId);

    // 替换字符串格式的ID
    result = result.replaceAll("\"" + idField + "\"\\s*:\\s*\"" + oldId + "\"", "\"" + idField + "\": \"" + newId + "\"");

    return result;
  }

  /**
   * 根据表名替换上下文相关的content字段
   */
  private static String replaceContextualContentIdsByTable(String jsonContent, String tableName, Long oldId, Long newId) {
    String result = jsonContent;

    switch (tableName) {
      case TABLE_BT_SKILL_SERVICE:
        result = replaceContentByCode(result, SKILL_TYPE_SERVICE, oldId, newId);
        break;
      case TABLE_BT_SKILL_PAGE:
        result = replaceContentByCode(result, SKILL_TYPE_PAGE, oldId, newId);
        break;
      default:
        break;
    }

    return result;
  }

  /**
   * 替换特定code类型上下文中的content字段
   */
  private static String replaceContentByCode(String jsonContent, String codeType, Long oldId, Long newId) {
    try {
      Object jsonObj = JsonUtil.parseJson(jsonContent, Object.class);
      Object processedObj = processJsonObject(jsonObj, codeType, oldId, newId);
      if (processedObj == null) {
        return jsonContent;
      }
      return JsonUtil.toJsonString(processedObj);
    }
    catch (Exception e) {
      logger.debug("JSON处理失败: {}", e.getMessage());
      return jsonContent;
    }
  }

  /**
   * 递归处理JSON对象，替换指定code和content的匹配项
   */
  @SuppressWarnings("unchecked")
  private static Object processJsonObject(Object obj, String codeType, Long oldId, Long newId) {
    if (obj instanceof Map) {
      return processJsonMap((Map<String, Object>) obj, codeType, oldId, newId);
    }
    else if (obj instanceof List) {
      return processJsonList((List<Object>) obj, codeType, oldId, newId);
    }
    else {
      return obj;
    }
  }

  private static Map<String, Object> processJsonMap(Map<String, Object> map, String codeType, Long oldId, Long newId) {
    Map<String, Object> result = new HashMap<>(map);

    // 检查并更新匹配的code和content字段
    if (shouldUpdateContent(map, codeType, oldId)) {
      result.put("content", newId.toString());
      logger.debug("找到匹配项，code: {}, oldContent: {}, newContent: {}", codeType, oldId, newId);
    }

    // 处理children字段
    processChildrenField(result, map, codeType, oldId, newId);

    // 处理其他嵌套字段
    processOtherNestedFields(result, map, codeType, oldId, newId);

    return result;
  }

  private static boolean shouldUpdateContent(Map<String, Object> map, String codeType, Long oldId) {
    Object codeValue = map.get("code");
    Object contentValue = map.get("content");
    return codeType.equals(codeValue) && oldId.toString().equals(contentValue);
  }

  @SuppressWarnings("unchecked")
  private static void processChildrenField(Map<String, Object> result, Map<String, Object> map, String codeType, Long oldId,
    Long newId) {
    Object children = map.get("children");
    if (children instanceof List) {
      List<Object> childrenList = (List<Object>) children;
      List<Object> processedChildren = new ArrayList<>();
      for (Object child : childrenList) {
        processedChildren.add(processJsonObject(child, codeType, oldId, newId));
      }
      result.put("children", processedChildren);
    }
  }

  private static void processOtherNestedFields(Map<String, Object> result, Map<String, Object> map, String codeType, Long oldId,
    Long newId) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();

      if (shouldSkipField(key)) {
        continue;
      }

      if (value instanceof Map || value instanceof List) {
        result.put(key, processJsonObject(value, codeType, oldId, newId));
      }
    }
  }

  private static boolean shouldSkipField(String key) {
    return "code".equals(key) || "content".equals(key) || "children".equals(key);
  }

  private static List<Object> processJsonList(List<Object> list, String codeType, Long oldId, Long newId) {
    List<Object> result = new ArrayList<>();
    for (Object item : list) {
      result.add(processJsonObject(item, codeType, oldId, newId));
    }
    return result;
  }

  /**
   * 替换JSON内容中的编码引用
   */
  private static String replaceCodesInJsonContent(String jsonContent, Map<String, Map<String, String>> codeMappings) {
    String result = jsonContent;

    for (Map.Entry<String, Map<String, String>> tableEntry : codeMappings.entrySet()) {
      String tableName = tableEntry.getKey();
      Map<String, String> tableCodeMappings = tableEntry.getValue();

      if (TABLE_BT_SKILL_ATTR_SPEC.equals(tableName)) {
        result = replaceStaticCodes(result, tableCodeMappings);
      }

      if (TABLE_BT_SKILL_PAGE_FUNC.equals(tableName)) {
        result = replacePageFuncCodes(result, tableCodeMappings);
      }
    }

    return result;
  }

  private static String replaceStaticCodes(String jsonContent, Map<String, String> codeMap) {
    String result = jsonContent;
    for (Map.Entry<String, String> mapping : codeMap.entrySet()) {
      String oldCode = mapping.getKey();
      String newCode = mapping.getValue();
      result = replaceStaticCodeInJson(result, oldCode, newCode);
    }
    return result;
  }

  private static String replacePageFuncCodes(String jsonContent, Map<String, String> codeMap) {
    String result = jsonContent;
    for (Map.Entry<String, String> mapping : codeMap.entrySet()) {
      String oldCode = mapping.getKey();
      String newCode = mapping.getValue();
      result = replacePageFuncCodeInJson(result, oldCode, newCode);
    }
    return result;
  }

  /**
   * 在JSON中替换staticCode字段的值
   */
  private static String replaceStaticCodeInJson(String jsonContent, String oldCode, String newCode) {
    String result = jsonContent;
    String escapedOldCode = escapeRegexChars(oldCode);
    result = result.replaceAll("\"staticCode\"\\s*:\\s*\"" + escapedOldCode + "\"", "\"staticCode\": \"" + newCode + "\"");
    return result;
  }

  /**
   * 在JSON中替换pageFunc编码
   */
  private static String replacePageFuncCodeInJson(String jsonContent, String oldCode, String newCode) {
    try {
      Object jsonObj = JsonUtil.parseJson(jsonContent, Object.class);
      Object processedObj = processPageFuncJsonObject(jsonObj, oldCode, newCode);
      if (processedObj == null) {
        return jsonContent;
      }
      return JsonUtil.toJsonString(processedObj);
    }
    catch (Exception e) {
      logger.debug("pageFunc JSON处理失败: {}", e.getMessage());
      return jsonContent;
    }
  }

  /**
   * 递归处理JSON对象，替换pageFunc的content字段
   */
  @SuppressWarnings("unchecked")
  private static Object processPageFuncJsonObject(Object obj, String oldCode, String newCode) {
    if (obj instanceof Map) {
      return processPageFuncJsonMap((Map<String, Object>) obj, oldCode, newCode);
    }
    else if (obj instanceof List) {
      return processPageFuncJsonList((List<Object>) obj, oldCode, newCode);
    }
    else {
      return obj;
    }
  }

  private static Map<String, Object> processPageFuncJsonMap(Map<String, Object> map, String oldCode, String newCode) {
    Map<String, Object> result = new HashMap<>(map);

    // 检查并更新匹配的pageFunc code和content字段
    if (shouldUpdatePageFuncContent(map, oldCode)) {
      result.put("content", newCode);
      logger.debug("找到pageFunc匹配项，oldContent: {}, newContent: {}", oldCode, newCode);
    }

    // 处理children字段
    processPageFuncChildrenField(result, map, oldCode, newCode);

    // 处理其他嵌套字段
    processPageFuncOtherNestedFields(result, map, oldCode, newCode);

    return result;
  }

  private static boolean shouldUpdatePageFuncContent(Map<String, Object> map, String oldCode) {
    Object codeValue = map.get("code");
    Object contentValue = map.get("content");
    return "pageFunc".equals(codeValue) && oldCode.equals(contentValue);
  }

  @SuppressWarnings("unchecked")
  private static void processPageFuncChildrenField(Map<String, Object> result, Map<String, Object> map, String oldCode,
    String newCode) {
    Object children = map.get("children");
    if (children instanceof List) {
      List<Object> childrenList = (List<Object>) children;
      List<Object> processedChildren = new ArrayList<>();
      for (Object child : childrenList) {
        processedChildren.add(processPageFuncJsonObject(child, oldCode, newCode));
      }
      result.put("children", processedChildren);
    }
  }

  private static void processPageFuncOtherNestedFields(Map<String, Object> result, Map<String, Object> map, String oldCode,
    String newCode) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();

      if (shouldSkipPageFuncField(key)) {
        continue;
      }

      if (value instanceof Map || value instanceof List) {
        result.put(key, processPageFuncJsonObject(value, oldCode, newCode));
      }
    }
  }

  private static boolean shouldSkipPageFuncField(String key) {
    return "code".equals(key) || "content".equals(key) || "children".equals(key);
  }

  private static List<Object> processPageFuncJsonList(List<Object> list, String oldCode, String newCode) {
    List<Object> result = new ArrayList<>();
    for (Object item : list) {
      result.add(processPageFuncJsonObject(item, oldCode, newCode));
    }
    return result;
  }

  /**
   * 转义正则表达式特殊字符
   */
  private static String escapeRegexChars(String input) {
    if (input == null) {
      return "";
    }
    return input.replaceAll("[\\\\\\[\\]{}()*+?.^$|]", "\\\\$0");
  }

  private JsonFieldProcessor() {
  }
}
