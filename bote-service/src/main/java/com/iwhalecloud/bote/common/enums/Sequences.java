package com.iwhalecloud.bote.common.enums;

import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 序列枚举类
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@RequiredArgsConstructor
@Getter
public enum Sequences {
  /** 目录主键 */
  CATALOG_ID("bt_catalog", "catalog_id", "seq_catalog_id"),
  /** 标签主键 */
  LABEL_ID("bt_label", "label_id", "seq_label_id"),
  /** 关联标签主键 */
  LABEL_OBJECT_REL_ID("bt_label_object_rel", "rel_id", "seq_label_object_relid"),
  /** 租户主键 */
  TENANT_ID("bt_tenant", "tenant_id", "seq_tenant_id"),
  /** 租户成员主键 */
  TENANT_USER_ID("bt_tenant_user", "tenant_user_id", "seq_tenant_user_id"),
  /** 权限 */
  PRIV_ID("bt_priv", "priv_id", "seq_priv_id"),
  /** 角色权限 */
  ROLE_PRIV_ID("bt_role_priv", "rela_id", "seq_role_priv_id"),
  /** 外部门户 ID */
  EXTERNAL_PORTAL_ID("bt_external_portal", "id", "seq_external_portal_id"),
  /** 敏感词 ID */
  SENSITIVE_WORD_ID("bt_sensitive_word", "sensitive_word_id", "seq_sensitive_word_id"),

  /** 机器人主键 */
  BOT_ID("bt_bot", "bot_id", "seq_bot_id"),
  /** 机器人收藏 */
  BOT_FAVOR_ID("bt_bot_favor", "favor_id", "seq_bot_favor_id"),
  /** 机器人授权 */
  BOT_AUTH_ID("bt_bot_auth", "auth_id", "seq_bot_auth_id"),
  /** 机器人关联场景 */
  BOT_SCENE_REL_ID("bt_bot_scene_rel", "rel_id", "seq_bot_scene_rel_id"),

  /** 鉴权 */
  API_AUTH_ID("bt_api_auth", "auth_id", "seq_api_auth_id"),
  /** 鉴权指令 */
  API_AUTH_POINT_ID("bt_api_auth_point", "point_id", "seq_api_auth_point_id"),
  /** 发布 */
  APP_PUBLISH_ID("bt_app_publish", "publish_id", "seq_app_publish_id"),

  /** 用户会话辅助信息 */
  BOT_USER_EXPERIENCE_ID("bt_bot_user_experience", "experience_id", "seq_bot_user_experience_id"),

  /** 联想术语 */
  SUGGESTION_TERM_ID("bt_suggestion_term", "term_id", "seq_suggestion_term_id"),

  /** 场景 */
  BOT_SCENE_ID("bt_bot_scene", "scene_id", "seq_bot_scene_id"),
  /** 场景变量 */
  BOT_SCENE_PARAM_ID("bt_bot_scene_param", "param_id", "seq_bot_scene_param_id"),
  /** 场景提示词 */
  BOT_SCENE_PROMPT_ID("bt_bot_scene_prompt", "scene_prompt_id", "seq_bot_scene_prompt_id"),
  /** 场景关联的技能 */
  BOT_SCENE_SKILL_ID("bt_bot_scene_skill", "scene_skill_id", "seq_bot_scene_skill_id"),
  /** 场景关联的环境变量 */
  BOT_CLAW_ENV_VARIABLE_ID("bt_claw_env_variable", "id", "seq_claw_env_variable_id"),
  /** 场景关联的工作空间 */
  BOT_CLAW_WORKSPACE_ID("bt_claw_workspace", "id", "seq_claw_workspace_id"),

  /** 会话 */
  BOT_SESSION_ID("bt_bot_session", "session_id", "seq_bot_session_id"),
  /** 会话消息 */
  BOT_SESSION_MSG_ID("bt_bot_session_msg", "msg_id", "seq_bot_session_msg_id"),
  /** 聊天主题 */
  CHAT_THEME_ID("bt_chat_theme", "theme_id", "seq_bt_chat_theme_id"),
  /** 聊天回复主题 */
  CHAT_REPLY_THEME_ID("bt_chat_reply_theme", "reply_theme_id", "seq_bt_chat_reply_theme_id"),
  /** 意图识别记录 */
  CHAT_INTENT_LOG_ID("bt_chat_intent_log", "log_id", "seq_chat_intent_log_id"),
  /** 会话场景进度 */
  CHAT_SCENE_PROCESS_ID("bt_chat_scene_process", "id", "seq_chat_scene_process_id"),
  /** 计划 */
  PLAN_RECORD_ID("bt_plan_record", "record_id", "seq_plan_record_id"),
  /** 计划步骤 */
  PLAN_RECORD_STEP_ID("bt_plan_step", "step_id", "seq_plan_step_id"),
  /** 流程智能体进度 */
  FLOW_SCENE_PROCESS_ID("bt_flow_scene_process", "id", "seq_flow_scene_process_id"),

  /** 大模型 */
  LARGE_MODEL_ID("bt_library_large_model", "model_id", "seq_large_model_id"),
  /** 模型微调 */
  MODEL_FINETUNE_ID("bt_model_finetune", "id", "bt_model_finetune_id"),
  /** 模型评测 */
  MODEL_EVAL_ID("bt_model_eval", "id", "bt_model_eval_id"),
  /** 场景会话消息 ID */
  CHAT_MESSAGE_ID("bt_chat_message", "message_id", "seq_chat_message_id"),
  /** 场景会话消息附件 ID */
  CHAT_MESSAGE_FILE_ID("bt_bot_session_msg_file", "rela_id", "seq_bot_session_msg_file_id"),

  /** 用户 */
  USER_ID("bt_user", "user_id", "seq_user_id"),

  /** 组织 */
  ORGANIZATION_ID("bt_organization", "org_id", "seq_organization_id"),
  /** 组织成员 */
  ORGANIZATION_MEMBER_ID("bt_organization_member", "member_id", "seq_organization_member_id"),
  /** 组织字段配置 */
  ORGANIZATION_FIELD_CONFIG_ID("bt_organization_field_config", "config_id", "seq_organization_field_config_id"),
  /** 组织用户角色 */
  ORG_USER_ROLE_ID("bt_org_user_role", "org_role_id", "seq_org_user_role_id"),

  /** 技能：属性主键 */
  SKILL_ATTR_SPEC_ID("bt_skill_attr_spec", "attr_id", "seq_skill_attr_id"),
  /** 技能：属性值主键 */
  SKILL_ATTR_VALUE_ID("bt_skill_attr_value", "attr_value_id", "seq_skill_attr_value_id"),
  /** 技能：属性值关系 */
  SKILL_ATTR_REL_ID("bt_skill_attr_value_rel", "rel_id", "seq_skill_attr_value_rel_id"),
  /** 技能：页面 */
  SKILL_PAGE_ID("bt_skill_page", "page_id", "seq_skill_page_id"),
  /** 技能：页面组件 */
  SKILL_PAGE_COMP_ID("bt_skill_page_comp", "page_comp_id", "seq_skill_page_comp_id"),
  /** 技能：页面函数 */
  SKILL_PAGE_FUNC_ID("bt_skill_page_func", "page_func_id", "seq_skill_page_func_id"),
  /** 技能：服务函数 */
  SKILL_FUNCTION_ID("bt_skill_function", "func_id", "seq_skill_function_id"),
  /** 技能：插件 */
  SKILL_PLUGIN_ID("bt_skill_plugin", "api_id", "seq_skill_plugin_api_id"),
  /** 技能：API */
  SKILL_SERVICE_ID("bt_skill_service", "service_id", "seq_skill_service_id"),
  /** 技能：API 平台 */
  SERVICE_PLATFORM_ID("bt_service_platform", "platform_id", "seq_service_platform_id"),
  /** 技能：API 网关 */
  SERVICE_GATEWAY_ID("bt_service_gateway", "gateway_id", "seq_service_gateway_id"),
  /** 技能：API 模拟响应报文 */
  SERVICE_MOCK_RSP_ID("bt_skill_service_mock", "rsp_id", "seq_service_mock_rsp_id"),
  /** 技能：SQL */
  SKILL_SQL_ID("bt_skill_sql", "service_id", "seq_skill_sql_id"),
  /** 技能：对象 */
  SKILL_OBJECT_ID("bt_skill_object", "busi_object_id", "seq_skill_object_id"),
  /** 技能：文本 */
  SKILL_TEXT_ID("bt_skill_text", "text_id", "seq_skill_text_id"),
  /** 技能：数据源 */
  DATA_SOURCE_ID("bt_data_source", "data_source_id", "seq_data_source_id"),
  /** 技能：数据源实例 */
  DATA_SOURCE_INST_ID("bt_data_source_inst", "data_source_inst_id", "seq_data_source_inst_id"),
  /** 技能：提示词 */
  PROMPT_ID("bt_prompt", "prompt_id", "seq_prompt_id"),
  /** 技能：提示词内容 */
  CONTENT_ID("bt_prompt_content", "content_id", "seq_content_id"),
  /** 技能：流程 */
  SKILL_FLOW_ID("bt_skill_flow", "flow_id", "seq_skill_flow_id"),
  /** 技能：流程参数 */
  SKILL_FLOW_PARAM_ID("bt_skill_flow_param", "param_id", "seq_skill_flow_param_id"),

  // /** 知识库 */
  // KNOWLEDGE_BASE_KNOWLEDGE_ID("bt_knowledge_base", "knowledge_id", "seq_knowledge_base_knowledge_id"),
  /** 文档 */
  DOCUMENT_DOC_ID("bt_document", "doc_id", "seq_document_doc_id"),
  /** 文件信息 */
  FILE_INFO_ID("bt_file_info", "file_info_id", "seq_file_info_id"),
  DOCUMENT_DOCUMENT_ID("bt_document", "document_id", "seq_document_document_id"),
  /** 文档分片 */
  DOCUMENT_SEGMENT_ID("bt_document_segment", "segment_id", "seq_document_segment_doc_seqment_id"),
  /** 文档关键字 */
  DOCUMENT_KEYWORD_ID("bt_document_keyword", "keyword_id", "seq_document_keyword_keyword_id"),
  /** 分片关键字关系 */
  SEGMENT_KEYWORD_REL_ID("bt_segment_keyword_rel", "rel_id", "seq_segment_keyword_rel_rel_id"),
  /** 文档同义词 */
  DOCUMENT_SYNONYM_ID("bt_document_synonym", "synonym_id", "seq_document_synonym_id"),
  /** 文档关键字与同义词关系 */
  KEYWORD_SYNONYM_REL_ID("bt_keyword_synonym_rel", "rel_id", "seq_keyword_synonym_rel_id"),
  /** 知识库查询记录 */
  KNOWLEDGE_QUERY_RECORD_ID("bt_knowledge_query_record", "id", "seq_knowledge_query_record_id"),
  /** 语料基本信息 */
  CORPUS_ID("bt_corpus_info", "corpus_id", "seq_corpus_info_corpus_id"),
  /** 文档参数 */
  DOCUMENT_PARAMETER_ID("bt_document_parameter", "parameter_id", "seq_document_parameter_id"),
  /** 文档内容 */
  DOCUMENT_CONTENT_ID("bt_document_content", "content_id", "seq_document_content_id"),

  /** 意图问句 */
  INTENTION_QUESTION_ID("bt_intention_question", "question_id", "seq_intent_question_id"),
  /** 意图日志 */
  CHAT_INTENTION_LOG_ID("bt_chat_intention_log", "log_id", "SEQ_BT_CHAT_INTENTION_LOG_ID"),
  /** 意图标注向量 */
  INTENTION_EMBEDDING_ID("bt_intention_embedding", "id", "seq_bt_intention_embedding_id"),
  /** 租户设置信息 */
  TENANT_SETTING_INFO_SETTING_ID("bt_tenant_setting_info", "setting_id", "tenant_setting_info_setting_id"),
  /** 意图识别策略 */
  INTENTION_STRATEGY_ID("bt_intention_strategy", "id", "bt_intention_strategy_id"),

  /** 资源关联 */
  RESOURCE_ELEMENT_ID("bt_resource_element", "resource_element_id", "seq_resource_element_id"),

  /** 插件 */
  PLUGIN_ID("bt_plugin", "plugin_id", "seq_plugin_id"),

  /** MCP 服务 */
  SEQ_BT_MCP_SERVER_SERVER_ID("bt_mcp_server", "server_id", "seq_server_id"),

  /** 应用平台 */
  SEQ_BT_PLAT_BOT_INFO_PLAT_BOT_ID("bt_plat_bot_info", "plat_bot_id", "seq_plat_bot_id"),

  /** oauth2客户端 */
  OAUTH2_CLIENT_ID("bt_oauth2_client", "id", "seq_bt_oauth2_client_id"),

  /** oauth2客户端关联url */
  OAUTH2_CLIENT_URL_ID("bt_oauth2_client_redirect_uri", "id", "seq_bt_oauth2_client_redirect_uri_id"),

  /** 模板智能体 */
  SEQ_BT_PLAT_SCENE_INFO_ID("bt_plat_scene_info", "plat_scene_id", "seq_plat_scene_info_id"),

  /** 网页应用 */
  WEB_APP_ID("bt_web_app", "web_app_id", "seq_web_app_id"),
  /** 工作台应用 */
  WORKBENCH_APP_ID("bt_workbench_app", "workbench_app_id", "seq_workbench_app_id"),
  /** 工作台应用关联 */
  WORKBENCH_APP_REL_ID("bt_workbench_app_rel", "rel_id", "seq_workbench_app_rel_id"),
  /** 工作台应用授权 */
  WORKBENCH_APP_AUTH_ID("bt_workbench_app_auth", "auth_id", "seq_workbench_app_auth_id"),
  /** 用户常用应用 */
  USER_FAV_APP_ID("bt_user_fav_app", "fav_id", "seq_user_fav_app_id"),
  /** 网页应用访问记录 */
  WEB_APP_RECORD_ID("bt_web_app_record", "record_id", "seq_web_app_record_id"),

  /** 应用发布申请 */
  SEQ_BOT_AUTH_APPLY_ID("BT_BOT_AUTH_APPLY", "apply_id", "seq_bot_auth_apply_id"),
  /** 用户密码历史 */
  USER_PWD_HIST_ID("bt_user_pwd_his", "his_id", "seq_user_pwd_his_id"),

  /** 环境变量 */
  ENV_VARIABLE_ID("bt_env_variable", "variable_id", "seq_env_variable_id"),
  /** 环境变量值 */
  ENV_VARIABLE_VAL_ID("bt_env_variable_val", "variable_val_id", "seq_env_variable_val_id"),

  /** ai环境变量 */
  AI_ENV_VARIABLE_ID("bt_ai_env_variable", "id", "seq_bt_ai_env_variable_id"),
  /** ai模型维护 */
  AI_MODEL_ID("bt_ai_model", "id", "seq_bt_ai_model_id"),
  /** ai skill维护 */
  AI_SKILL_ID("bt_ai_skill", "id", "seq_bt_ai_skill_id"),
  /** ai mcp维护 */
  AI_MCP_ID("bt_ai_mcp", "id", "seq_bt_ai_mcp_id"),
  /** ai 工作空间维护 */
  AI_WORKSPACE_ID("bt_ai_workspace", "id", "seq_bt_ai_workspace_id"),
  /** claw 环境变量关联 */
  CLAW_ENV_VARIABLE_ID("bt_claw_env_variable", "id", "seq_bt_claw_env_variable_id"),
  /** claw 工作区 */
  CLAW_WORKSPACE_ID("bt_claw_workspace", "id", "seq_bt_claw_workspace_id"),

  /** 业务数据表 */
  DATA_TABLE_ID("bt_data_table", "table_id", "seq_data_table_id"),
  /** 业务数据表字段 */
  DATA_TABLE_COLUMN_ID("bt_data_table_column", "table_column_id", "seq_table_column_id"),
  /** 表建模记录 */
  TABLE_MODEL_ITEM_ID("bt_table_model_item", "table_model_item_id", "seq_table_model_item_id"),

  /** 在线环境维护 */
  PUBLISH_GATEWAY_ID("bt_publish_gateway", "gateway_id", "seq_publish_gateway_id"),

  /** 渠道 */
  AI_CHANNEL_ID("bt_ai_channel", "id", "seq_ai_channel_id"),

  /** 技能发布申请 */
  SEQ_SKILL_PUBLISH_APPLY_ID("bt_skill_publish_apply", "apply_id", "seq_skill_publish_apply_id"),

  /** 不要使用，只放在最后方便维护枚举类 */
  DO_NOT_USE(null, null, null);

  /** 表名 */
  private final String table;

  /** 列名 */
  private final String column;

  /** 序列名 */
  private final String name;

  /**
   * 获取下个序列值
   *
   * @return 序列值
   */
  public long next() {
    // return SeqUtil.next(name);
    return IDUtils.nextId();
  }

}
