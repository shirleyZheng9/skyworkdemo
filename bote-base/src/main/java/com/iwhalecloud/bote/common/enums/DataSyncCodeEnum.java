package com.iwhalecloud.bote.common.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 数据同步节点枚举类
 *
 * @author chen.linfa
 * @since 2025-04-12
 */
@Getter
@RequiredArgsConstructor
public enum DataSyncCodeEnum {
  /** 机器人 */
  BOT("bot_base", "bt_bot", "智能应用"),
  /** 场景 */
  SCENE("bot_scene", "bt_bot_scene", "智能体"),

  /** 知识库 */
  KNOWLEDGE("knowledge_base", "bt_knowledge_base", "知识库"),
  /** 文档库 */
  LIBRARY("library", "bt_dc_document_library", "文档库"),
  /** 文档管理 */
  DOCUMENT_FILE("document_file_info", "bt_file_info", "文档"),
  /** 语料 */
  CORPUS("corpus", "bt_corpus_info", "语料"),

  /** 技能：工作流 */
  SKILL_FLOW("skill_flow", "bt_skill_flow", "工作流"),
  /** 技能：页面 */
  SKILL_PAGE("skill_page", "bt_skill_page", "页面"),
  /** 技能：API */
  SKILL_SERVICE("skill_service", "bt_skill_service", "API"),
  /** 技能：服务函数 */
  SKILL_FUNCTION("skill_function", "bt_skill_function", "服务函数"),
  /** 技能：数据 */
  SKILL_ATTR("skill_attr_spec", "bt_skill_attr_spec", "数据"),
  /** 技能: 大模型服务 */
  SKILL_PLUGIN("skill_plugin", "bt_skill_plugin", "大模型服务"),
  /** 技能：页面函数 */
  SKILL_PAGE_FUNC("skill_page_func", "bt_skill_page_func", "页面函数"),
  /** 技能：SQL */
  SKILL_SQL("skill_sql", "bt_skill_sql", "SQL服务"),
  /** 技能：MCP */
  SKILL_MCP("skill_mcp", "bt_mcp_server", "MCP"),
  /** Agent Skill */
  AGENT_SKILL("agent_skill", "bt_agent_skill", "Agent Skill"),
  /** Agent Skill 文件 */
  AGENT_SKILL_FILE("agent_skill_file", "bt_file_info", "Agent Skill 文件"),
  /** A2A 服务 */
  A2A_AGENT("a2a_agent", "bt_a2a_agent", "A2A 服务"),
  /** A2A 平台 */
  A2A_PLATFORM("a2a_platform", "bt_a2a_platform", "A2A 平台"),
  /** 数据表 */
  DATA_TABLE("data_table", "bt_data_table", "数据表"),
  /** 页面资源 */
  PAGE_FILE("page_file_info", "bt_file_info", "页面资源"),
  /** 提示词（新版表 bt_prompt_basic，与 bt_prompt_commit 配套） */
  PROMPT("prompt", "bt_prompt_basic", "提示词"),
  /** 副驾指令 */
  COPILOT_POINT("copilot_point", "bt_copilot_point", "副驾指令"),

  /** 模型 */
  MODEL("library_large_model", "bt_library_large_model", "模型"),

  /** 插件 */
  PLUGIN("plugin", "bt_plugin", "插件"),

  /** 网关 */
  SERVICE_PLATFORM("service_platform", "bt_service_platform", "网关"),
  /** 数据源 */
  DATA_SOURCE("data_source", "bt_data_source", "数据源"),

  /** 目录 */
  CATALOG("catalog", "bt_catalog", "目录"),
  /** 标签 */
  LABEL("bt_label", "bt_label", "标签"),

  /** 常见问题、指令 */
  USER_EXPERIENCE("bot_user_experience", "bot_user_experience", "常见问题、指令"),

  /** 会话主题 */
  CHAT_THEME("chat_theme", "bt_chat_theme", "会话主题"),

  /** 环境变量  */
  ENV_VAR("env_variable", "bt_env_variable", "环境变量"),

  /** 租户 */
  TENANT("tenant_base", "bt_tenant", "租户"),
  /** 租户设置 */
  TENANT_SETTING_INFO("tenant_setting_info", "bt_tenant_setting_info", "租户设置"),
  /** API 密钥 */
  API_AUTH("api_auth", "bt_api_auth", "API密钥"),
  /** 应用发布 */
  APP_PUBLISH("app_publish", "bt_app_publish", "应用发布"),

  /** 定时任务 */
  JOB("job", "", "定时任务");

  private final String code;
  /** 主表编码 */
  private final String tableCode;
  /** 名称 */
  private final String name;

  /**
   * 倒序输出节点编码列表，辅助按序数据导入
   */
  public static List<String> getReverseCodes() {
    List<String> codes = new ArrayList<>();
    for (DataSyncCodeEnum value : values()) {
      codes.add(value.getCode());
    }
    Collections.reverse(codes);
    return codes;
  }

  public static String getTableCode(String code) {
    for (DataSyncCodeEnum value : values()) {
      if (code.equals(value.getCode())) {
        return value.getTableCode().toLowerCase();
      }
    }
    return null;
  }

  /**
   * 根据编码获取名称
   */
  public static String getName(String code) {
    for (DataSyncCodeEnum value : values()) {
      if (code.equals(value.getCode())) {
        return value.getName();
      }
    }
    return null;
  }

  /**
   * 根据编码获取枚举
   */
  public static DataSyncCodeEnum getCodeEnum(String code) {
    for (DataSyncCodeEnum enumValue : values()) {
      if (enumValue.getCode().equals(code)) {
        return enumValue;
      }
    }
    return null;
  }


  public static List<String> ignoreForCopy() {
    return Arrays.asList(TENANT.getCode(), TENANT_SETTING_INFO.getCode(), API_AUTH.getCode(), APP_PUBLISH.getCode());
  }

  public static List<String> ignoreForSceneCopy() {
    return Arrays.asList(KNOWLEDGE.getCode(), LIBRARY.getCode());
  }

}

