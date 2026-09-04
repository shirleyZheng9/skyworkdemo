package com.iwhalecloud.bote.common.consts;

import com.google.common.collect.ImmutableList;
import java.sql.Types;
import java.util.List;

/**
 * 常量类
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
public final class BaseConsts {
  private BaseConsts() {
  }

  /** 请求地址前缀 */
  public static final String API_PREFIX = "bote/";

  /** 数据状态: 有效 */
  public static final String STATUS_CD_VALID = "00A";
  /** 数据状态: 无效 */
  public static final String STATUS_CD_INVALID = "00X";
  /** 数据状态: 禁用 */
  public static final String STATUS_CD_DISABLED = "00D";


  /** True 缩写 */
  public static final String TRUE = "T";
  /** False 缩写 */
  public static final String FALSE = "F";

  /** 状态: 成功 */
  public static final String STATE_SUCCESS = "S";
  /** 状态: 失败 */
  public static final String STATE_FAIL = "F";

  /** 平台级租户 ID */
  public static final Long PLATFORM_TENANT_ID = -1L;
  /** 默认租户 ID */
  public static final Long DEFAULT_TENANT_ID = 0L;
  /** 平台助手租户 ID */
  public static final Long COPILOT_TENANT_ID = 1L;
  /** 资产租户 ID */
  public static final Long ASSET_TENANT_ID = 2L;
  /**
   * BoteClaw 默认的租户ID
   */
  public static final Long BOTE_CLAW_TENANT_ID = -1L;

  /** 用户 ID: API, 表示外系统调用接口，且未关联具体用户 */
  public static final Long USER_ID_API = -1L;

  /** 博特 AI ID */
  public static final Long BOTE_AI_ID = -1L;
  /** 博特 AI 名称 */
  public static final String BOTE_AI_NAME = "博特 AI";

  /** 参数根节点 */
  public static final String PARAMETER_NODE_ROOT = "root";
  /** 参数根节点描述 */
  public static final String PARAMETER_NODE_ROOT_DESCRIPTION = "根节点";

  /** SQL 结果类型: 单值 */
  public static final String SQL_RESULT_TYPE_VALUE = "1";
  /** SQL 结果类型: 单对象 */
  public static final String SQL_RESULT_TYPE_MAP = "2";
  /** SQL 结果类型: 列表 */
  public static final String SQL_RESULT_TYPE_LIST = "3";
  /** SQL 结果类型: 分页列表 */
  public static final String SQL_RESULT_TYPE_PAGE = "4";

  /** 字典配置数据类型 ：开关 */
  public static final String DC_CFG_TYPE_1 = "1";
  /** 字典配置数据类型 ：参数配置 */
  public static final String DC_CFG_TYPE_2 = "2";
  /** 字典配置数据类型 ：模型配置 */
  public static final String DC_CFG_TYPE_3 = "3";

  /** 页面来源类型：平台 */
  public static final String PAGE_SOURCE_PLATFORM = "PLATFORM";
  /** 页面来源类型：高代码 */
  public static final String PAGE_SOURCE_HIGH_CODE = "HIGH_CODE";

  /** 页面模板类型：通用表单 */
  public static final String PAGE_TEMPLATE_FORM = "form";
  /** 页面模板类型：表格 */
  public static final String PAGE_TEMPLATE_TABLE = "table";
  /** 页面模板类型：卡片列表 */
  public static final String PAGE_TEMPLATE_CARD = "card";
  /** 页面模板类型：描述列表 */
  public static final String PAGE_TEMPLATE_DESCRIPTIONS = "descriptions";
  /** 页面模板类型: 列表 */
  public static final String PAGE_TEMPLATE_LIST = "list";

  /** 静态词典：条件表达式操作符 */
  public static final String STATIC_CONDITIONAL_OPERATOR = "CONDITIONAL_OPERATOR";

  /** 标签类型：场景 */
  public static final String LABEL_TYPE_SCENE = "scene";
  /** 标签类型：MCP */
  public static final String LABEL_TYPE_MCP = "mcp";

  /** 辅助功能类型：指令 */
  public static final String USER_EXPERIENCE_TYPE_POINT = "point";
  /** 辅助功能类型：提问 */
  public static final String USER_EXPERIENCE_TYPE_QUESTION = "question";

  public static final String REQUEST_TYPE_GET = "get";
  /** 请求类型: POST */
  public static final String REQUEST_TYPE_POST = "post";

  /** 发布状态: 未开始 */
  public static final int PUBLISH_STATUS_NOT_STARTED = 0;
  /** 发布状态: 运行中 */
  public static final int PUBLISH_STATUS_RUNNING = 1;
  /** 发布状态: 成功 */
  public static final int PUBLISH_STATUS_SUCCESS = 10;
  /** 发布状态: 失败 */
  public static final int PUBLISH_STATUS_FAILED = -1;

  /** 机器人状态 未上架：0 已上架：1 */
  public static final String BOT_STATUS_PUBLISH = "1";
  public static final String BOT_STATUS_UNPUBLISH = "0";

  /** 时间格式数据类型 */
  public static final List<Integer> DATE_TIME_DATA_TYPE = ImmutableList.of(Types.DATE, Types.TIME, Types.TIMESTAMP, Types.TIME_WITH_TIMEZONE, Types.TIMESTAMP_WITH_TIMEZONE);

  /** 分页查询参数 - 每页数量 */
  public static final String PAGE_SIZE = "pageSize";
  /** 分页查询参数 - 页数 */
  public static final String PAGE_NUM = "pageNum";
  /** 分页信息参数 - 页数 */
  public static final String PAGE_LIST = "list";

  /** 门户类型: uportal */
  public static final String PORTAL_TYPE_UPORTAL = "uportal";
  /** 门户类型: ngportal */
  public static final String PORTAL_TYPE_NGPORTAL = "ngportal";
  /** 门户类型: basiccenter */
  public static final String PORTAL_TYPE_BASIC_CENTER = "basiccenter";
  /** 门户类型: 单点登录 */
  public static final String PORTAL_TYPE_SSO = "sso";
  /** 门户类型: cas */
  public static final String PORTAL_TYPE_CAS = "cas";
  /** 门户类型: oauth2 */
  public static final String PORTAL_TYPE_OAUTH2 = "oauth2";
  /** 门户类型: 无 */
  public static final String PORTAL_TYPE_NONE = "none";
  /** 门户系统编码: 默认（自带门户） */
  public static final String PORTAL_SYSTEM_CODE_DEFAULT = "default";
  /** 外部门户类型列表 */
  public static final List<String> EXTERNAL_PORTAL_TYPES = ImmutableList.of(PORTAL_TYPE_UPORTAL, PORTAL_TYPE_NGPORTAL, PORTAL_TYPE_BASIC_CENTER, PORTAL_TYPE_NONE, PORTAL_TYPE_SSO, PORTAL_TYPE_OAUTH2);

  /** 租户用户角色: 管理 */
  public static final String ROLE_MANAGE = "MANAGE";
  /** 租户用户角色: 编辑 */
  public static final String ROLE_EDIT = "EDIT";
  /** 租户用户角色: 只读 */
  public static final String ROLE_READONLY = "READONLY";
  /** 租户用户角色: 使用 */
  public static final String ROLE_USE = "USE";
  /** 用户角色列表 */
  public static final List<String> ROLES = ImmutableList.of(ROLE_MANAGE, ROLE_EDIT, ROLE_READONLY, ROLE_USE);

  /** 权限类型: 菜单 */
  public static final String PRIV_TYPE_MENU = "menu";
  /** 权限类型: 组件 */
  public static final String PRIV_TYPE_COMPONENT = "component";

  /** 脚本类型：groovy */
  public static final String SCRIPT_TYPE_GROOVY = "Groovy";
  /** 脚本类型：python3 */
  public static final String SCRIPT_TYPE_PYTHON3 = "Python3";

  /** 数据库类型: oracle */
  public static final String DB_TYPE_ORACLE = "ORACLE";
  /** 数据库类型: postgresql */
  public static final String DB_TYPE_PG = "postgresql";
  /** 数据库类型: MYSQL */
  public static final String DB_TYPE_MYSQL = "MYSQL";

  /** 环境编码：开发 */
  public static final String ENV_CODE_DEV = "dev";
  /** 环境类型：配置态 */
  public static final String ENV_TYPE_DEV = "Develop";

  /** 租户设置信息类型：意图识别 */
  public static final String FUNC_TYPE_INTENT_ANNOTATION = "annotation";
  /** 租户设置信息类型：对话上下文 */
  public static final String FUNC_TYPE_CHAT_CONTEXT = "chat_context";
  /** 租户设置信息类型：知识库 */
  public static final String FUNC_TYPE_KNOWLEDGE = "knowledge";
  /** 租户设置信息类型：知识库 */
  public static final String FUNC_TYPE_KNOWLEDGE_OTHER = "knowledge_other";
  /** 租户设置信息类型: 流程日志 */
  public static final String FUNC_TYPE_FLOW_LOG = "flow_log";
  /** 租户设置信息类型：大模型 */
  public static final String FUNC_DEFAULT_TYPE_LARGE_MODEL = "default_large_model";
  /** 租户设置信息类型：功能开关 */
  public static final String FUNC_TYPE_FUNC_SWITCH = "func_switch";
  /** 租户设置信息类型：扩展插件 */
  public static final String FUNC_TYPE_CHAT_PLUGINS = "chat_plugins";
  /** 租户设置信息类型：对话窗口扩展 - 顶部内容 */
  public static final String FUNC_TYPE_PIN_SETTING = "pin_setting";
  /** 租户设置信息类型：对话窗口扩展 - 底部内容 */
  public static final String FUNC_TYPE_PIN_SETTING_BOTTOM = "pin_setting_bottom";
  /** 租户设置信息类型：联想术语设置 */
  public static final String FUNC_TYPE_SUGGESTION = "suggestion";
  /** 租户设置信息类型：对话主题 */
  public static final String FUNC_TYPE_CHAT_THEME = "chat_theme";
  /** 租户设置信息类型：回复消息主题 */
  public static final String FUNC_TYPE_REPLY_CHAT_THEME = "reply_chat_theme";

  /** 模型来源：平台 */
  public static final String MODEL_SOURCE_PLATFORM = "platform";
  /** 模型来源：租户 */
  public static final String MODEL_SOURCE_TENANT = "tenant";
  /** 模型来源：天工 AI 网关 */
  public static final String MODEL_SOURCE_GATEWAY = "gateway";

  /** 对话模式 - 平台 */
  public static final String CHAT_MODE_TYPE_PLATFORM = "platform";
  /** 对话模式 - 多机器人模式 */
  public static final String CHAT_MODE_TYPE_MULTIPLE = "multiple";
  /** 对话模式 - 单机器人模式 */
  public static final String CHAT_MODE_TYPE_SINGLE = "single";

  /** 默认 excel 工作簿名称 */
  public static final String DEFAULT_SHEET_NAME = "sheet1";

  /** 发布类型 - 模型微调 */
  public static final String PUBLISH_TYPE_FINETUNE = "finetune";
  /** 发布类型 - 微调评测 */
  public static final String PUBLISH_TYPE_EVAL = "eval";
  /** 发布类型 - 导入 */
  public static final String PUBLISH_TYPE_IMPORT = "import";
  /** 发布类型 - 导出 */
  public static final String PUBLISH_TYPE_EXPORT = "export";
  /** 发布类型 - 复制 */
  public static final String PUBLISH_TYPE_COPY = "copy";
  /** 发布类型 - 在线发布 */
  public static final String PUBLISH_TYPE_ONLINE = "online";
  /** 发布类型 - 备份 */
  public static final String PUBLISH_TYPE_BACKUP = "backup";
  /** 发布类型 - 回退 */
  public static final String PUBLISH_TYPE_RETURN = "return";

  /** 文件业务类型 - 知识文档 */
  public static final String FILE_BUSI_TYPE_DOCUMENT = "document";
  /** 文件业务类型 - 页面 */
  public static final String FILE_BUSI_TYPE_PAGE = "page";
  /** 文件业务类型 - 评测 */
  public static final String FILE_BUSI_TYPE_EVAL = "eval";
  /** 文件业务类型 - 训练 */
  public static final String FILE_BUSI_TYPE_FINETUNE = "finetune";
  /** 文件业务类型 - Agent Skill */
  public static final String FILE_BUSI_TYPE_AGENT_SKILL = "agentSkill";
  /** 文件业务类型 - Agent Skill 编排内二进制文件 */
  public static final String FILE_BUSI_TYPE_AGENT_SKILL_FILE = "agentSkillFile";

  /** 微调评测数据，文件存储路径前缀 */
  public static final String FINETUNE_FILE_PATH = "bote-fine-tuner/datas/";

  /** 微调用途 - 场景意图 */
  public static final String FINETUNE_USE_TYPE_INTENT = "intent";
  /** 微调用途 - 语料 */
  public static final String FINETUNE_USE_TYPE_CORPUS = "corpus";

  /** 微调状态 - 发布中 */
  public static final String FINETUNE_STATUS_WAIT = "-1";
  /** 微调状态 - 未上架 */
  public static final String FINETUNE_STATUS_UNPUBLISH = "0";
  /** 微调状态 - 已上架 */
  public static final String FINETUNE_STATUS_PUBLISH = "1";

  /** 评测状态 - 评测中 */
  public static final String EVAL_STATUS_RUNNING = "0";
  /** 评测状态 - 完成 */
  public static final String EVAL_STATUS_FINISH = "1";
  /** 评测状态 - 失败 */
  public static final String EVAL_STATUS_FAILED = "-1";

  /** 机器人授权类型 - 所有人可见 */
  public static final String BOT_AUTH_TYPE_ALL = "all";
  /** 机器人授权类型 - 自定义 */
  public static final String BOT_AUTH_TYPE_CUSTOM = "custom";
  /** 机器人授权类型 - 部分人可见 */
  public static final String BOT_AUTH_TYPE_PART_USER = "partUser";
  /** 机器人授权类型 - 部分租户可见 */
  public static final String BOT_AUTH_TYPE_PART_TENANT = "partTenant";
  /** 机器人授权类型 - 部分组织可见 */
  public static final String BOT_AUTH_TYPE_PART_ORG = "partOrg";

  /** 提示类型的异常编码 */
  public static final String RESULT_CODE_WARNING = "1";

  /** 联网搜索策略 - bocha */
  public static final String WEB_SEARCH_BOCHA = "bocha";
  /** 联网搜索策略 - tavily */
  public static final String WEB_SEARCH_TAVILY = "tavily";
  /** 联网搜索策略 - brave */
  public static final String WEB_SEARCH_BRAVE = "brave";
  /** 联网搜索策略 - custom */
  public static final String WEB_SEARCH_CUSTOM = "custom";
  /** 联网搜索策略 - 内部联网搜索 */
  public static final String WEB_SEARCH_INTERNAL = "internal";

  /** 默认参数名称: 签名 */
  public static final String HEADER_KEY_SIGN = "X-SIGN";
  /** 默认参数名称: 签名模式 */
  public static final String HEADER_KEY_SIGN_SECURITY_MODE = "XA-TYPE";
  /** 默认签名模式: */
  public static final String DEFAULT_SIGN_SECURITY_MODE = "1.1";
  /** 签名 hash */
  public static final String HASH = "#";
  /** 安全模式: 请求参数 AES 加解密 */
  public static final String AES = "3.0";
  /** 安全模式: 请求参数 DES 加解密 */
  public static final String DES = "4.0";
  /** 请求头: 系统编码 */
  public static final String HEADER_SYSTEM_CODE = "System-Code";
  /** cookie名称：用户ID */
  public static final String COOKIE_NAME_USER_ID = "X-BT-N-ID";
  /** 请求头: API鉴权令牌 */
  public static final String HEADER_AUTHORIZATION = "Authorization";
  /** 属性编码: 是否为 SSE 接口 */
  public static final String ATTRIBUTE_KEY_IS_SSE = "ATTR-IS-SSE";

  /** 用户状态-被锁定 */
  public static final String IS_LOCKED_TRUE = "Y";
  /** 用户状态-未锁定 */
  public static final String IS_LOCKED_FALSE = "N";
  /** 用户状态-启用 */
  public static final String USER_STATE_ENABLE = "A";
  /** 用户状态-禁用 */
  public static final String USER_STATE_DISABLE = "X";

  /** 异常栈信息开启配置项 */
  public static final String EXCEPTION_STACK = "exception.stack";

  /** 智能体规划策略 - 自主规划 */
  public static final String AGENT_STRATEGY_AUTO = "auto";
  /** 智能体规划策略 - 动态规划 */
  public static final String AGENT_STRATEGY_PLAN = "plan";
  /** 智能体规划策略 - 自定义 */
  public static final String AGENT_STRATEGY_CUSTOM = "custom";
  /** 智能体规划策略 - 无 */
  public static final String AGENT_STRATEGY_NONE = "none";

  /** 回复输出内容格式 - 默认 */
  public static final String REPLY_CONTENT_TYPE_MARKDOWN = "markdown";
  /** 回复输出内容格式 - 文档 */
  public static final String REPLY_CONTENT_TYPE_WORD = "word";
  /** 回复输出内容格式 - 大纲 */
  public static final String REPLY_CONTENT_TYPE_OUTLINE = "outline";
  /** 回复输出内容格式 - 段落 */
  public static final String REPLY_CONTENT_TYPE_PARAGRAPH = "paragraph";

  /** 对话主题级别：应用 */
  public static final String CHAT_THEME_SCOPE_APP = "app";
  /** 对话主题级别：租户 */
  public static final String CHAT_THEME_SCOPE_TENATN = "tenant";
  /** 对话主题级别：平台 */
  public static final String CHAT_THEME_SCOPE_PLATFORM = "platform";

  /** 门户适配表 */
  public static final String EXTERNAL_PORTAL_TBALE = "bt_external_portal";

  /** 密码策略配置列表 */
  public static final List<String> PASSWORD_STRATEGY_CFG_LIST = ImmutableList.of("PWD_COMPOSITION", "PWD_MIN_LENGTH", "LOGIN_RUN_MODEL",
    "LOGIN_RUN_MODEL_RULE", "PWD_KEYBOARD_NEAR", "PWD_SAME_CHAR");

  /** 用户类型 - 标识使用者注册的用户 */
  public static final String USER_TYPE_USE = "30";

  /** 状态-启用 */
  public static final String STATE_ENABLE = "A";
  /** 状态-禁用 */
  public static final String STATE_DISABLE = "X";

  /** 工作台应用场景-单对话 */
  public static final String WORKBENCH_APP_SCENE_DIALOGUE = "dialogue";
  /** 工作台应用场景-网页应用 */
  public static final String WORKBENCH_APP_SCENE_WEBAPP = "webApp";
  /** 工作台应用场景-群聊 */
  public static final String WORKBENCH_APP_SCENE_GROUPCHAT = "groupChat";
  /** 工作台应用场景-智能应用 */
  public static final String WORKBENCH_APP_SCENE_SMARTAPP = "smartApp";

  /** 工作台应用授权类型-全部成员 */
  public static final String WORKBENCH_APP_AUTH_TYPE_ALL = "all";
  /** 作台应用授权类型-部分成员 */
  public static final String WORKBENCH_APP_AUTH_TYPE_CUSTOM = "custom";

  /** 应用类型-AI助理 */
  public static final String APP_TYPE_AI_BOT = "aiBot";
  /** 应用类型-网页应用 */
  public static final String APP_TYPE_WEP_APP = "webApp";

  /** 广场应用类型-平台 */
  public static final String PLAT_BOT_TYPE_PLATFORM = "platform";
  /** 广场应用类型-第三方 */
  public static final String PLAT_BOT_TYPE_OTHER = "other";

  /** 应用授权申请状态：待审核 */
  public static final Integer BOT_AUTH_APPLY_AUDIT_WAIT = 0;
  /** 应用授权申请状态：通过 */
  public static final Integer BOT_AUTH_APPLY_AUDIT_APPROVE = 1;
  /** 应用授权申请状态：未通过 */
  public static final Integer BOT_PUBLISH_APPLY_AUDIT_REJECT = 2;

  /** 登录短信验证码前缀 */
  public static final String SMS_CODE_PREFIX = "smsCode-";

  /** 组织成员角色：管理员 */
  public static final String ORG_ROLE_ADMIN = "admin";
  /** 组织成员角色：普通成员 */
  public static final String ORG_ROLE_MEMBER = "member";

  /** 外系统编码 - 百应 */
  public static final String SYSTEM_TYPE_BEYOND = "beyond";
  /** 外系统编码 - 灵犀 */
  public static final String SYSTEM_TYPE_LCDP = "lcdp";

  /** 数据库渠道类型：平台数据库 */
  public static final String DATABASE_TUNNEL_PLATFORM = "platform";
  /** 数据库渠道类型：自定义数据库 */
  public static final String DATABASE_TUNNEL_CUSTOM = "custom";
  /** 系统预置字段：数据渠道 */
  public static final String PLATFORM_COLUMN_BOTE_DATA_TUNNEL = "bote_data_tunnel";
  /** 数据渠道：测试数据 */
  public static final String DATA_TUNNEL_TEST = "bote_test";
  /** 数据渠道：正式数据 */
  public static final String DATA_TUNNEL_OFFICIAL = "bote_official";

  /** 数据库操作类型 - 查询 */
  public static final String DATABASE_QUERY = "query";
  /** 数据库操作类型 - 新增 */
  public static final String DATABASE_INSERT = "insert";
  /** 数据库操作类型 - 修改 */
  public static final String DATABASE_UPDATE = "update";
  /** 数据库操作类型 - 删除 */
  public static final String DATABASE_DELETE = "delete";

  /** MQ 类型: Kafka */
  public static final String MQ_TYPE_KAFKA = "kafka";
  /** MQ 类型: ZMQ */
  public static final String MQ_TYPE_ZMQ = "zmq";
  /** MQ 类型: CtgMQ */
  public static final String MQ_TYPE_CTGMQ = "ctgmq";

  /** AI门户预置租户ID */
  public static final Long AI_PORTAL_PRESET_TENANT_ID = -2L;

  public static final String TASK_TYPE_TEXT = "text";
  public static final String TASK_TYPE_AGENT = "agent";

  public static final String CHANNEL_TYPE_DINGTALK = "DingTalk";
  public static final String CHANNEL_TYPE_FEISHU = "Feishu";
  /** 个人微信（ClawBot） */
  public static final String CHANNEL_TYPE_WECLAWBOT = "WECLAWBOT";

  /** Bot数据来源 - AI门户 */
  public static final String DATA_FROM_PORTAL_BOT = "10A";
  /** BoteClaw 系统预置的提示词 */
  public static final String BOTECLAW_SYSTEM_PROMPT = "BOTECLAW_SYSTEM_PROMPT";
  /** 大模型数据来源：个人 */
  public static final String DATA_FROM_MODEL_PERSON = "10A";

  /** 技能发布申请状态：待审核 */
  public static final Integer SKILL_APPLY_STATUS_WAIT = 0;
  /** 技能发布申请状态：通过 */
  public static final Integer SKILL_APPLY_STATUS_APPROVE = 1;
  /** 技能发布申请状态：未通过 */
  public static final Integer SKILL_APPLY_STATUS_REJECT = 2;
  /** 技能发布申请状态：已失效 */
  public static final Integer SKILL_APPLY_STATUS_INVALID = -1;
  /** 技能默认版本 */
  public static final String SKILL_DEFAULT_VERSION = "1.0.0";

  /** 安全围栏流程类型 - 用户输入 */
  public static final String SECURITY_TYPE_USER_INPUT = "USER_INPUT";
  /** 安全围栏流程类型 - 模型输入 */
  public static final String SECURITY_TYPE_LLM_INPUT = "LLM_INPUT";
  /** 安全围栏流程类型 - 模型输出 */
  public static final String SECURITY_TYPE_LLM_OUTPUT = "LLM_OUTPUT";

  /** 记忆类型：长期记忆 */
  public static final String MEMORY_TYPE_LONG_TERM = "LONG_TERM";
  /** 记忆类型：每日记忆 */
  public static final String MEMORY_TYPE_DAILY = "DAILY";

  /** 技能类型：本地技能 */
  public static final String SKILL_TYPE_LOCAL = "local";
  /** 技能类型：在线技能 */
  public static final String SKILL_TYPE_ONLINE = "online";
  /** 技能模板类型：本体场景 */
  public static final String SKILL_TEMPLATE_TYPE_ONTOLOGY_SCENE = "ontologyScene";
  /** 技能模板类型：默认 */
  public static final String SKILL_TEMPLATE_TYPE_DEFAULT = "default";

  /** 平台类型：AI门户 */
  public static final String PLATFORM_AI_PORTAL = "aiPortal";
  /** 平台类型：开发中心 */
  public static final String PLATFORM_DEVELOPER = "developer";
}
