package com.iwhalecloud.bote.common.consts;

/**
 * 基础相关常量
 *
 * @author chen.linfa
 * @since 2025-11-07
 */
public final class CommonConsts {
  private CommonConsts() {
  }

  /** 请求地址前缀 */
  public static final String API_PREFIX = "bote/";

  /** 数据状态: 有效 */
  public static final String STATUS_CD_VALID = "00A";
  /** 数据状态: 无效 */
  public static final String STATUS_CD_INVALID = "00X";
  /** 数据状态: 禁用 */
  public static final String STATUS_CD_DISABLED = "00D";
  /** 数据状态: 等待 */
  public static final String STATUS_CD_WAITE = "00W";

  /** True 缩写 */
  public static final String TRUE = "T";
  /** False 缩写 */
  public static final String FALSE = "F";

  /** 平台级租户 ID */
  public static final Long PLATFORM_TENANT_ID = -1L;
  /** 默认租户 ID */
  public static final Long DEFAULT_TENANT_ID = 0L;
  /** 平台助手租户 ID */
  public static final Long COPILOT_TENANT_ID = 1L;
  /** 资产租户 ID */
  public static final Long ASSET_TENANT_ID = 2L;

  /** 字典配置数据类型 ：开关 */
  public static final String DC_CFG_TYPE_1 = "1";
  /** 字典配置数据类型 ：参数配置 */
  public static final String DC_CFG_TYPE_2 = "2";
  /** 字典配置数据类型 ：模型配置 */
  public static final String DC_CFG_TYPE_3 = "3";

  /** 租户设置信息类型：意图识别 */
  public static final String FUNC_TYPE_INTENT_ANNOTATION = "annotation";
  /** 租户设置信息类型：对话上下文 */
  public static final String FUNC_TYPE_CHAT_CONTEXT = "chat_context";
  /** 租户设置信息类型：知识库 */
  public static final String FUNC_TYPE_KNOWLEDGE = "knowledge";
  /** bt_tenant_setting_info 中 WeKnora 账号的 func_type */
  public static final String FUNC_TYPE_WEKNORA = "weknora";
  /** bt_tenant_setting_info 中 knowledgeGraph SSO 登录载荷的 func_type */
  public static final String FUNC_TYPE_KNOWLEDGE_GRAPH = "knowledge_graph";
  /** 租户设置信息类型：知识库 */
  public static final String FUNC_TYPE_KNOWLEDGE_OTHER = "knowledge_other";
  /** 租户设置信息类型：安全围栏 */
  public static final String FUNC_TYPE_SECURITY = "security";
  /** 租户设置信息类型: 流程日志 */
  public static final String FUNC_TYPE_FLOW_LOG = "flow_log";
  /** 租户设置信息类型：大模型 */
  public static final String FUNC_DEFAULT_TYPE_LARGE_MODEL = "default_large_model";
  /** 租户设置信息类型：功能开关 */
  public static final String FUNC_TYPE_FUNC_SWITCH = "func_switch";
  /** 租户设置信息类型：扩展插件 */
  public static final String FUNC_TYPE_CHAT_PLUGINS = "chat_plugins";
  /** 租户设置信息类型：联想术语设置 */
  public static final String FUNC_TYPE_SUGGESTION = "suggestion";
  /** 租户设置信息类型：对话主题 */
  public static final String FUNC_TYPE_CHAT_THEME = "chat_theme";
  /** 租户设置信息类型：插件市场 */
  public static final String FUNC_TYPE_PLUGIN_HUB = "plugin_hub";
  /** 租户设置信息类型：水印 */
  public static final String FUNC_TYPE_WATERMARK = "watermark";
  /** 门户类型: uportal */
  public static final String PORTAL_TYPE_UPORTAL = "uportal";
  /** 门户类型: ngportal */
  public static final String PORTAL_TYPE_NGPORTAL = "ngportal";
  /** 门户类型: 无 */
  public static final String PORTAL_TYPE_NONE = "none-portal";

  /** 外系统编码 - 企业级租户 */
  public static final String SYSTEM_TYPE_WORKSPACE = "workspace";

}
