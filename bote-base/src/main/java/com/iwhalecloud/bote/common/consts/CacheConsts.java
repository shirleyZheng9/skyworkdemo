package com.iwhalecloud.bote.common.consts;

/**
 * 缓存相关常量
 *
 * @author bianjp
 * @since 2024-11-15
 */
public final class CacheConsts {
  private CacheConsts() {
  }

  /** 缓存分组: 平台 */
  public static final String GROUP_PLATFORM = "bote";

  /** 缓存 key 前缀: 登录信息 */
  public static final String KEY_PREFIX_LOGIN = "login:";
  /** 缓存 key 前缀: oauth2 client */
  public static final String KEY_PREFIX_OAUTH2CLIENT = "oauth2client:";
  public static final String KEY_PREFIX_OAUTH2TOKEN = "oauth2token:";
  /** 缓存 key 前缀: 静态数据 */
  public static final String KEY_PREFIX_ATTR_SPEC = "attr:";
  /** 缓存 key 前缀: 系统参数 */
  public static final String KEY_PREFIX_DC_PARAM = "param:";
  /** 缓存 key 前缀: 配置词典 */
  public static final String KEY_PREFIX_DC_PUBLIC = "public:";
  /** 缓存 key 前缀: 网关 */
  public static final String KEY_PREFIX_GATEWAY = "gateway:";
  /** 缓存 key 前缀: API 技能 */
  public static final String KEY_PREFIX_API = "api:";
  /** 缓存 key 前缀: SQL 技能 */
  public static final String KEY_PREFIX_SQL = "sql:";
  /** 缓存 key 前缀: 大模型插件 */
  public static final String KEY_PREFIX_LLM_PLUGIN = "llmPlugin:";
  /** 缓存 key 前缀: 页面 */
  public static final String KEY_PREFIX_PAGE = "page:";
  /** 缓存 key 前缀: Agent Skill */
  public static final String KEY_PREFIX_AGENT_SKILL = "agentSkill:";
  /** 缓存 key 前缀: 页面函数 */
  public static final String KEY_PREFIX_PAGE_FUNC = "pageFunc:";
  /** 缓存 key 前缀: 工具箱（服务函数） */
  public static final String KEY_PREFIX_TOOLBOX = "toolbox:";
  /** 缓存 key 前缀: 对话流上下文 */
  public static final String KEY_PREFIX_CHATFLOW_CONTEXT = "chatflow:";
  /** 缓存 key 前缀: 场景 */
  public static final String KEY_PREFIX_SCENE = "scene:";
  /** 缓存 key 前缀: 场景意图识别 */
  public static final String KEY_PREFIX_SCENE_INTENT = "sceneIntent:";
  /** 缓存 key 前缀: 复杂场景 DSL */
  public static final String KEY_PREFIX_SCENE_DSL = "sceneDsl:";
  /** 缓存 key 前缀: 工作流 DSL */
  public static final String KEY_PREFIX_FLOW_DSL = "flowDsl:";
  /** 缓存 key 前缀: 租户设置 */
  public static final String KEY_PREFIX_TENANT_SETTING = "tenantSetting:";
  /** 缓存 key 前缀: API 鉴权 */
  public static final String KEY_PREFIX_API_AUTH = "apiAuth:";
  /** 缓存 key 前缀: 知识库 */
  public static final String KEY_PREFIX_KNOWLEDGE = "knowledge:";
  /** 缓存 key 前缀: 回复下载 */
  public static final String KEY_PREFIX_REPLY_DOWNLOAD = "replyDownload:";
  /** 缓存 key 前缀: 用户角色权限 */
  public static final String KEY_PREFIX_ROLE_PRIV = "rolePriv:";
  /** 缓存 key 前缀：签名数据 */
  public static final String KEY_PREFIX_SING_REPLAY = "signReplay:";
  /** 缓存key 前缀：钉钉 */
  public static final String KEY_PREFIX_DINGDING = "dingding:";
  /** 缓存key 前缀: 编辑锁 */
  public static final String KEY_PREFIX_EDIT_LOCK = "editLock:";
  /** 缓存 key 前缀: 微信认证 */
  public static final String KEY_PREFIX_WECHAT_AUTH = "wechatAuth:";
  /** 缓存 key 前缀: 计划上下文 */
  public static final String KEY_PREFIX_PLAN_CONTEXT = "plan:";
  /** 缓存 key 前缀: 工作流执行状态 */
  public static final String KEY_PREFIX_FLOW_EXECUTION_STATUS = "flowExecutionStatus:";
  /** 缓存 key 前缀: 知识中台令牌 */
  public static final String KEY_PREFIX_ZHXY_TOKEN = "zhxyToken:";
  /** 缓存 key 前缀: 知识图谱 SSO 登录信息 */
  public static final String KEY_PREFIX_KNOWLEDGE_GRAPH_LOGIN_INFO = "knowledgeGraphLoginInfo:";
  /** 缓存 key 前缀: CAS票据 */
  public static final String KEY_PREFIX_SESSION_TICKET = "sessionTicket:";
  /** 缓存key 前缀：飞书userAccessToken */
  public static final String KEY_PREFIX_LARK_USER_ACCESS_TOKEN = "larkUserAccessToken:";
  /** 缓存 key 前缀: A2A 任务(外系统调用博特 A2A 服务生成的任务) */
  public static final String KEY_PREFIX_A2A_TASK = "a2aTask:";
  /** 缓存 key 前缀: A2A 任务信息(博特调用外系统 A2A 生成的任务) */
  public static final String KEY_PREFIX_A2A_TASK_INFO = "a2aTaskInfo:";
  /** 缓存 key 前缀: A2A 通知上下文 */
  public static final String KEY_PREFIX_A2A_NOTIFICATION = "a2aNotificationContext:";
  /** 缓存 key 前缀: A2A 推送通知配置 */
  public static final String KEY_PREFIX_A2A_PUSH_CONFIG = "a2aPushConfig:";
  /** 缓存 key 前缀: 大模型 token 数量统计 */
  public static final String KEY_PREFIX_LLM_TOKEN_COUNT = "llmTokenCount:";
  /** 缓存 key 前缀: TTS 音色 ID */
  public static final String KEY_PREFIX_TTS_VOICE_ID = "ttsVoiceId:";
  /** 缓存 key 前缀: 表定义 */
  public static final String KEY_PREFIX_DATA_TABLE = "dataTable:";
  /** 缓存 key 前缀: 插件市场 */
  public static final String KEY_PREFIX_PLUGIN_HUB = "pluginHub:";
  /** 缓存 key 前缀: 沙箱用户绑定（分布式下 userId -> 远程沙箱 ID） */
  public static final String KEY_PREFIX_SANDBOX_USER_BINDING = "sandboxUserBinding:";
  /** 缓存 key 前缀: 技能广场大批量异步导出任务（进度与分片下载路径） */
  public static final String KEY_PREFIX_SKILL_SQUARE_BULK_EXPORT = "skillSquareBulkExport:";
  /** 缓存 key 前缀: 技能广场异步导出「当前任务」映射（userId -> jobId，与任务详情 TTL 一致） */
  public static final String KEY_PREFIX_SKILL_SQUARE_BULK_EXPORT_USER_JOB = "skillSquareBulkExportUserJob:";

  /** 缓存名称: 静态数据 */
  public static final String CACHE_NAME_ATTR_SPEC = "attrSpec";
  /** 缓存名称: 系统参数 */
  public static final String CACHE_NAME_DC_PARAM = "dcParam";
  /** 缓存名称: 公共配置 */
  public static final String CACHE_NAME_DC_PUBLIC = "dcPublic";
  /** 缓存名称: 场景 */
  public static final String CACHE_NAME_SCENE = "scene";
  /** 缓存名称: 场景意图 */
  public static final String CACHE_NAME_SCENE_INTENT = "sceneIntent";
  /** 缓存名称: 复杂场景 DSL */
  public static final String CACHE_NAME_SCENE_DSL = "sceneDsl";
  /** 缓存名称: 工作流 DSL */
  public static final String CACHE_NAME_FLOW_DSL = "flowDsl";
  /** 缓存名称: 角色权限 */
  public static final String CACHE_NAME_ROLE_PRIV = "rolePriv";
  /** 缓存名称: 服务网关 */
  public static final String CACHE_NAME_GATEWAY = "gateway";
  /** 缓存名称: API 技能 */
  public static final String CACHE_NAME_API = "api";
  /** 缓存名称: SQL 技能 */
  public static final String CACHE_NAME_SQL = "sql";
  /** 缓存名称: 大模型插件 */
  public static final String CACHE_NAME_LLM_PLUGIN = "llmPlugin";
  /** 缓存名称: 页面 */
  public static final String CACHE_NAME_PAGE = "page";
  /** 缓存名称: Agent Skill */
  public static final String CACHE_NAME_AGENT_SKILL = "agentSkill";
  /** 缓存名称: 页面函数 */
  public static final String CACHE_NAME_PAGE_FUNC = "pageFunc";
  /** 缓存名称: 工具箱（服务函数） */
  public static final String CACHE_NAME_TOOLBOX = "toolbox";
  /** 缓存名称: 机器人数据源 */
  public static final String CACHE_NAME_BOT_DATA_SOURCE = "botDataSource";
  /** 缓存名称: 大模型客户端 */
  public static final String CACHE_NAME_MODEL_CLIENT = "modelClient";
  /** 缓存名称: MCP 客户端 */
  public static final String CACHE_NAME_MCP_CLIENT = "mcpClient";
  /** 缓存名称: 动态 MCP 客户端 */
  public static final String CACHE_NAME_DYNAMIC_MCP_CLIENT = "dynamicMcpClient";
  /** 缓存名称: 智能应用图标 */
  public static final String CACHE_NAME_BOT_ICON = "botIcon";
  /** 缓存名称: 智能体图标 */
  public static final String CACHE_NAME_SCENE_ICON = "sceneIcon";
  /** 缓存名称: 中译英 */
  public static final String CACHE_NAME_CHINESE_TO_ENGLISH = "chineseToEnglish";
  /** 缓存名称: 表结构 */
  public static final String CACHE_NAME_TABLE_DEFINITION = "tableDefinition";
  /** 缓存名称: 敏感词 */
  public static final String CACHE_NAME_SENSITIVE_WORD = "sensitiveWord";
  /** 缓存名称: SSE 对象 */
  public static final String CACHE_NAME_SSE_EMITTER = "sseEmitter";
  /** 缓存名称: 租户设置 */
  public static final String CACHE_NAME_TENANT_SETTING = "tenantSetting";
  /** 缓存名称: 知识图谱 SSO 登录信息 */
  public static final String CACHE_NAME_KNOWLEDGE_GRAPH_LOGIN_INFO = "knowledgeGraphLoginInfo";
  /** 缓存名称: API 鉴权 */
  public static final String CACHE_NAME_API_AUTH = "apiAuth";
  /** 缓存名称: 门户适配 */
  public static final String CACHE_NAME_PORTAL_ADAPTER = "portalAdapter";
  /** 缓存名称: 知识库 */
  public static final String CACHE_NAME_KNOWLEDGE = "knowledge";
  /** 缓存名称: 租户所有缓存（虚拟） */
  public static final String CACHE_NAME_TENANT_ALL = "tenantAll";
  /** 缓存名称：钉钉 */
  public static final String CACHE_NAME_DINGDING = "dingding";
  /** 缓存名称：微信认证 */
  public static final String CACHE_NAME_WECHAT_AUTH = "wechatAuth";
  /** 缓存名称：OAUTH2 客户端 */
  public static final String CACHE_NAME_OAUTH2CLIENT = "oauth2client";
  /** 缓存名称：TTS 音色 ID */
  public static final String CACHE_NAME_TTS_VOICE_ID = "ttsVoiceId";
  /** 缓存名称: 表定义 */
  public static final String CACHE_NAME_DATA_TABLE = "dataTable";
  /** 横杆连接符 */
  public static final String KEY_PREFIX_HYPHEN = "-";
  /** 文档下载相关的缓存前缀 */
  public static final String CACHE_DOCUMENT_DOWNLOAD_PREFIX = "document_download:";
  /** 文档下载相关的缓存组 */
  public static final String GROUP_DOC = "bote-doc";
  /** 缓存名称：插件市场 */
  public static final String CACHE_NAME_PLUGIN_HUB = "pluginHub";
  /** 缓存名称: 插件市场 MCP 客户端 */
  public static final String CACHE_NAME_PLUGIN_HUB_MCP = "pluginHubMcp";
  /** 缓存名称: 插件图标 */
  public static final String CACHE_NAME_PLUGIN_ICON = "pluginIcon";
  /** 缓存名称: 通用智能体 */
  public static final String CACHE_NAME_GENERAL_AGENT = "generalAgent";
  /** 缓存名称: 通用智能体 ID 列表 */
  public static final String CACHE_NAME_GENERAL_AGENT_ID = "generalAgentId";
  /** 缓存名称: 渠道连接 */
  public static final String CACHE_NAME_CHANNEL_CONNECTION = "channelConnection";

  public static final String COLON = ":";
}
