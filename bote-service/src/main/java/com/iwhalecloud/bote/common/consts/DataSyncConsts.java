package com.iwhalecloud.bote.common.consts;

import java.util.List;

/**
 * 数据同步常量类
 *
 * @author yangran
 * @since 2025-8-15
 */
public final class DataSyncConsts {

  // 表名常量
  public static final String TABLE_BT_FILE_INFO = "bt_file_info";
  public static final String TABLE_BT_CATALOG = "bt_catalog";
  public static final String TABLE_BT_LIBRARY_LARGE_MODEL = "bt_library_large_model";
  public static final String TABLE_BT_LABEL = "bt_label";
  public static final String TABLE_BT_SERVICE_PLATFORM = "bt_service_platform";
  public static final String TABLE_BT_SERVICE_GATEWAY = "bt_service_gateway";
  public static final String TABLE_BT_DATA_SOURCE_INST = "bt_data_source_inst";
  public static final String TABLE_BT_DATA_SOURCE = "bt_data_source";
  public static final String TABLE_BT_BOT_SCENE_SKILL = "bt_bot_scene_skill";
  public static final String TABLE_BT_BOT_SCENE = "bt_bot_scene";
  public static final String TABLE_BT_SKILL_FLOW = "bt_skill_flow";
  public static final String TABLE_BT_SKILL_PAGE = "bt_skill_page";
  public static final String TABLE_BT_SKILL_SERVICE = "bt_skill_service";
  public static final String TABLE_BT_SKILL_SQL = "bt_skill_sql";
  public static final String TABLE_BT_SKILL_PAGE_FUNC = "bt_skill_page_func";
  public static final String TABLE_BT_SKILL_FUNCTION = "bt_skill_function";
  public static final String TABLE_BT_SKILL_PLUGIN = "bt_skill_plugin";
  public static final String TABLE_BT_MCP_SERVER = "bt_mcp_server";
  public static final String TABLE_BT_SKILL_ATTR_SPEC = "bt_skill_attr_spec";
  public static final String TABLE_BT_BOT = "bt_bot";
  public static final String TABLE_BT_PROMPT = "bt_prompt";
  public static final String TABLE_BT_KNOWLEDGE_BASE = "bt_knowledge_base";
  public static final String TABLE_BT_CORPUS_INFO = "bt_corpus_info";
  public static final String TABLE_BT_JOB = "bt_job";
  // 文档中心相关表
  public static final String TABLE_BT_DC_DOCUMENT = "bt_dc_document";
  public static final String TABLE_BT_DC_DOCUMENT_LIBRARY = "bt_dc_document_library";
  public static final String TABLE_BT_DC_DOC_CONTENT = "bt_dc_doc_content";
  public static final String TABLE_BT_DC_WORKBOOK_CONTENT = "bt_dc_workbook_content";
  public static final String TABLE_BT_DOCUMENT = "bt_document";

  // 字段名常量
  public static final String FIELD_FILE_ID = "file_id";
  public static final String FIELD_CATALOG_NAME = "catalog_name";
  public static final String FIELD_CATALOG_TYPE = "catalog_type";
  public static final String FIELD_PAR_CATALOG_ID = "par_catalog_id";
  public static final String FIELD_CATALOG_ID = "catalog_id";
  public static final String FIELD_TO_REMOVE = "_to_remove";
  public static final String FIELD_SKILL_TYPE = "skill_type";
  public static final String FIELD_SKILL_ID = "skill_id";
  public static final String FIELD_DATA_SOURCE_ID = "data_source_id";
  public static final String FIELD_MODEL_CODE = "model_code";
  public static final String FIELD_MODEL_ID = "model_id";
  public static final String FIELD_LABEL_NAME = "label_name";
  public static final String FIELD_LABEL_TYPE = "label_type";
  public static final String FIELD_LABEL_ID = "label_id";
  public static final String FIELD_JOB_ID = "bss_job_id";

  // 文档中心相关字段
  public static final String FIELD_DOCUMENT_ID = "document_id";
  public static final String FIELD_LIBRARY_ID = "library_id";
  public static final String FIELD_DC_DOCUMENT_ID = "dc_document_id";

  // JSON字段名常量
  public static final String JSON_FIELD_SCENE_GRAPH_JSON = "scene_graph_json";
  public static final String JSON_FIELD_SCENE_DSL = "scene_dsl";
  public static final String JSON_FIELD_SKILL_JSON = "skill_json";
  public static final String JSON_FIELD_FLOW_GRAPH_JSON = "flow_graph_json";
  public static final String JSON_FIELD_FLOW_DSL = "flow_dsl";
  public static final String JSON_FIELD_PAGE_TEMPLATE_JSON = "page_template_json";

  // 技能类型常量
  public static final String SKILL_TYPE_WORKFLOW = "workflow";
  public static final String SKILL_TYPE_SERVICE = "service";
  public static final String SKILL_TYPE_SQL = "sql";
  public static final String SKILL_TYPE_PAGE = "page";
  public static final String SKILL_TYPE_PAGE_FUNC = "pageFunc";
  public static final String SKILL_TYPE_TOOLBOX = "toolbox";
  public static final String SKILL_TYPE_LLM_SKILL = "llmSkill";
  public static final String SKILL_TYPE_AGENT_SKILL = "agentSkill";
  public static final String SKILL_TYPE_MCP = "mcp";
  public static final String SKILL_TYPE_KNOWLEDGE_CHAT = "knowledgeChat";

  // 特殊值常量
  public static final Long ROOT_CATALOG_PARENT_ID = -1L;
  public static final String PRESET_CATALOG_NAME = "预置分组";
  public static final String PATH_SEPARATOR = "|";
  public static final int RANDOM_SUFFIX_LENGTH = 8;
  public static final String PUBLISH_TYPE_ALL = "ALL";

  private DataSyncConsts() {
  }

  public static List<String> getDocumentCenterTables() {
    return List.of(TABLE_BT_DC_DOCUMENT, TABLE_BT_DC_DOCUMENT_LIBRARY, TABLE_BT_DC_DOC_CONTENT,
        TABLE_BT_DC_WORKBOOK_CONTENT, TABLE_BT_KNOWLEDGE_BASE, TABLE_BT_DOCUMENT);
  }
}
