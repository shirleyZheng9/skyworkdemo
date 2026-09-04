package com.iwhalecloud.bote.service.datasync.util;

import static com.iwhalecloud.bote.common.consts.DataSyncConsts.SKILL_TYPE_LLM_SKILL;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.SKILL_TYPE_MCP;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.SKILL_TYPE_PAGE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.SKILL_TYPE_PAGE_FUNC;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.SKILL_TYPE_SERVICE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.SKILL_TYPE_SQL;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.SKILL_TYPE_TOOLBOX;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.SKILL_TYPE_WORKFLOW;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_BOT;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_BOT_SCENE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_CORPUS_INFO;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_KNOWLEDGE_BASE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_MCP_SERVER;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_PROMPT;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_ATTR_SPEC;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_FLOW;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_FUNCTION;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_PAGE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_PAGE_FUNC;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_PLUGIN;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_SERVICE;
import static com.iwhalecloud.bote.common.consts.DataSyncConsts.TABLE_BT_SKILL_SQL;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 表字段映射工具类
 *
 * @author yangran
 * @since 2025-8-15
 */
public final class TableFieldMappingUtil {

  // @formatter:off
  private static final Map<String, String> NAME_FIELD_MAP = new HashMap<>();
  private static final Map<String, String> CODE_FIELD_MAP = new HashMap<>();
  private static final Map<String, String> SKILL_TYPE_TABLE_MAP = new HashMap<>();
  // @formatter:on

  static {
    initNameFieldMap();
    initCodeFieldMap();
    initSkillTypeTableMap();
  }

  private static void initNameFieldMap() {
    NAME_FIELD_MAP.put(TABLE_BT_BOT, "bot_name");
    NAME_FIELD_MAP.put(TABLE_BT_BOT_SCENE, "scene_name");
    NAME_FIELD_MAP.put(TABLE_BT_SKILL_SERVICE, "service_name");
    NAME_FIELD_MAP.put(TABLE_BT_SKILL_SQL, "service_name");
    NAME_FIELD_MAP.put(TABLE_BT_SKILL_FLOW, "flow_name");
    NAME_FIELD_MAP.put(TABLE_BT_SKILL_PAGE, "page_name");
    NAME_FIELD_MAP.put(TABLE_BT_SKILL_FUNCTION, "func_name");
    NAME_FIELD_MAP.put(TABLE_BT_SKILL_PAGE_FUNC, "func_name");
    NAME_FIELD_MAP.put(TABLE_BT_SKILL_PLUGIN, "api_name");
    NAME_FIELD_MAP.put(TABLE_BT_MCP_SERVER, "server_name");
    NAME_FIELD_MAP.put(TABLE_BT_PROMPT, "prompt_title");
    NAME_FIELD_MAP.put(TABLE_BT_KNOWLEDGE_BASE, "knowledge_name");
    NAME_FIELD_MAP.put(TABLE_BT_CORPUS_INFO, "corpus_name");
    NAME_FIELD_MAP.put(TABLE_BT_SKILL_ATTR_SPEC, "attr_name");
  }

  private static void initCodeFieldMap() {
    CODE_FIELD_MAP.put(TABLE_BT_BOT, "bot_code");
    CODE_FIELD_MAP.put(TABLE_BT_BOT_SCENE, "scene_code");
    CODE_FIELD_MAP.put(TABLE_BT_SKILL_SERVICE, "service_code");
    CODE_FIELD_MAP.put(TABLE_BT_SKILL_SQL, "service_code");
    CODE_FIELD_MAP.put(TABLE_BT_SKILL_FLOW, "flow_code");
    CODE_FIELD_MAP.put(TABLE_BT_SKILL_PAGE, "page_code");
    CODE_FIELD_MAP.put(TABLE_BT_SKILL_FUNCTION, "func_code");
    CODE_FIELD_MAP.put(TABLE_BT_SKILL_PAGE_FUNC, "func_code");
    CODE_FIELD_MAP.put(TABLE_BT_SKILL_PLUGIN, "api_code");
    CODE_FIELD_MAP.put(TABLE_BT_MCP_SERVER, "server_code");
    CODE_FIELD_MAP.put(TABLE_BT_KNOWLEDGE_BASE, "knowledge_code");
    CODE_FIELD_MAP.put(TABLE_BT_CORPUS_INFO, "corpus_code");
    CODE_FIELD_MAP.put(TABLE_BT_SKILL_ATTR_SPEC, "attr_nbr");
  }

  private static void initSkillTypeTableMap() {
    SKILL_TYPE_TABLE_MAP.put(SKILL_TYPE_WORKFLOW, TABLE_BT_SKILL_FLOW);
    SKILL_TYPE_TABLE_MAP.put(SKILL_TYPE_SERVICE, TABLE_BT_SKILL_SERVICE);
    SKILL_TYPE_TABLE_MAP.put(SKILL_TYPE_SQL, TABLE_BT_SKILL_SQL);
    SKILL_TYPE_TABLE_MAP.put(SKILL_TYPE_PAGE, TABLE_BT_SKILL_PAGE);
    SKILL_TYPE_TABLE_MAP.put(SKILL_TYPE_PAGE_FUNC, TABLE_BT_SKILL_PAGE_FUNC);
    SKILL_TYPE_TABLE_MAP.put(SKILL_TYPE_TOOLBOX, TABLE_BT_SKILL_FUNCTION);
    SKILL_TYPE_TABLE_MAP.put(SKILL_TYPE_LLM_SKILL, TABLE_BT_SKILL_PLUGIN);
    SKILL_TYPE_TABLE_MAP.put(SKILL_TYPE_MCP, TABLE_BT_MCP_SERVER);
  }

  /**
   * 获取表的名称字段
   */
  public static String getNameField(String tableCode) {
    return NAME_FIELD_MAP.get(tableCode);
  }

  /**
   * 获取表的编码字段
   */
  public static String getCodeField(String tableCode) {
    return CODE_FIELD_MAP.get(tableCode);
  }

  /**
   * 根据技能类型获取目标表名
   */
  public static String getTargetTableBySkillType(String skillType) {
    if (StringUtils.isBlank(skillType)) {
      return null;
    }
    return SKILL_TYPE_TABLE_MAP.get(skillType);
  }

  private TableFieldMappingUtil() {
  }
}
