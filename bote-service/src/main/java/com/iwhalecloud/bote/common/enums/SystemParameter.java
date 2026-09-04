package com.iwhalecloud.bote.common.enums;

import com.iwhalecloud.bote.cache.DcParamCache;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

/**
 * 系统参数相关常量
 * <p>1. 大部分变量，维护在数据库 dc_param 表，通过 DcParamCache 读取 </p>
 * <p>2. 部分变量，维护在配置文件，通过 Environment.class 读取 </p>
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
@RequiredArgsConstructor
@Getter
public enum SystemParameter {

  /** DB：前后端加解密 AES */
  ENCRYPTION_AES("ENCRYPTION_AES", ""),
  /** ENV：自定义 SQL 解析，需要特殊处理的 mybatis 标签 */
  SQL_PARSE_LABEL("sql.parse.label", "if,!--,--,foreach,where,choose,when,otherwise"),
  /** DB：应用数据源的语句超时时间（秒） */
  APP_JDBC_STATEMENT_TIMEOUT("APP_JDBC_STATEMENT_TIMEOUT", ""),
  /** DB：应用数据源的超长事务阈值（秒） */
  APP_JDBC_TRANSACTION_THRESHOLD("APP_JDBC_TRANSACTION_THRESHOLD", ""),
  /** 用户初始密码: 11 */
  USER_INIT_PASSWORD("USER_INIT_PASSWORD", "Aa#123"),
  /** 知识库停用词文件 ID */
  KNOWLEDGE_STOP_FILE_ID("KNOWLEDGE_STOP_FILE_ID", null),

  /** 平台名称 */
  PLATFORM_NAME("PLATFORM_NAME", "博特平台"),
  /** 平台图标 */
  PLATFORM_ICON("PLATFORM_ICON", null),
  /** 登录页背景图 */
  PLATFORM_LOGIN_BG("PLATFORM_LOGIN_BG", null),
  /** 管理平台首页机器人图片 */
  PLATFORM_BOT_IMAGE("PLATFORM_BOT_IMAGE", null),
  /** 管理平台首页平台使用步骤图片 */
  PLATFORM_STEP_IMAGE("PLATFORM_STEP_IMAGE", null),

  /** DB：在线翻译参数 */
  TRANSLATE_STRATEGY("TRANSLATE_STRATEGY", "pinyin"),
  TRANSLATE_LIBRE_URL("TRANSLATE_LIBRE_URL", ""),
  TRANSLATE_LARGE_MODEL_ID("TRANSLATE_LARGE_MODEL_ID", "1"),

  /** DB：超级管理员账号信息 */
  SUPER_ADMIN("SUPER_ADMIN", "1|admin"),

  /** DocChain 账号默认密码 */
  DOC_CHAIN_USER_PASSWORD("DOC_CHAIN_USER_PASSWORD", "MTIzNDU2YUEh"),
  /** DocChain OCR 账号 */
  DOCCHAIN_OCR_USER_NAME("DOCCHAIN_OCR_USER_NAME", ""),
  /** DocChain OCR 账号密码 */
  DOCCHAIN_OCR_PASSWORD("DOCCHAIN_OCR_PASSWORD", ""),
  /** DocChain OCR 账号 API Key */
  DOCCHAIN_OCR_API_KEY("DOCCHAIN_OCR_API_KEY", ""),

  /** 允许上传的文件类型 */
  ALLOW_UPLOAD_FILE_TYPE("ALLOW_UPLOAD_FILE", "doc,docx,pdf,ppt,pptx,csv,md,zip"),
  /** 允许 DocChain 上传的文件类型 */
  ALLOW_DOCCHAIN_UPLOAD_FILE_TYPE("ALLOW_DOCCHAIN_UPLOAD_FILE", "doc,docx,html,jpeg,jpg,md,pdf,png,ppt,pptx,txt,wps,xls,xlsx"),
  /** 平台允许上传的所有文件类型 */
  PLATFORM_ALLOW_UPLOAD_FILE_TYPE("PLATFORM_ALLOW_UPLOAD_FILE_TYPE", "png,jpg,jpeg,gif,svg,bmp,fiff,pdf,txt,md,docx,doc,xls,xlsx,ppt,pptx,html,htm,wav,mp3,acc,mp4,avi,mov,m4a,js,ts,py,java,zip,csv,wps"),

  /** 知识库构建 API 地址 -对接政企 */
  KNOWLEDGE_BUILD_URL("KNOWLEDGE_BUILD_URL", ""),

  /** 大模型最大 token 数限制 */
  LARGE_MODEL_MAX_TOKENS("LARGE_MODEL_MAX_TOKENS", "1000"),

  /** 平台通用对话功能使用的大模型 ID */
  COMMON_CHAT_MODEL_ID("COMMON_CHAT_MODEL_ID", null),

  /** 是否开启工作流的 AI 辅助功能(生成流程、解释流程) */
  FLOW_AI_ENABLED("FLOW_AI_ENABLED", "F"),
  /** 工作流 AI 辅助功能使用的大模型 ID */
  FLOW_AI_MODEL_ID("FLOW_AI_MODEL_ID", null),
  /** 工作流执行时间限制(秒), 0 表示不限制 */
  FLOW_EXECUTION_TIME_LIMIT("FLOW_EXECUTION_TIME_LIMIT", "300"),
  /** 工作流节点执行次数限制（包含嵌套流程），0 表示不限制 */
  FLOW_EXECUTION_NODE_LIMIT("FLOW_EXECUTION_NODE_LIMIT", "10000"),
  /** 调用 A2A 服务超时时间（秒）。注意不是 HTTP 请求超时时间（流式接口没有超时时间，非流式接口的超时时间由 {@link com.iwhalecloud.bote.llm.client.config.ModelHttpClientProperties} 控制） */
  INVOKE_A2A_TIMEOUT("INVOKE_A2A_TIMEOUT", "600"),
  /** 大模型调用工具次数限制 */
  LLM_TOOL_CALL_LIMIT("LLM_TOOL_CALL_LIMIT", "10"),

  /** 历史消息保存天数 */
  ARCHIVE_MESSAGE_TIME("ARCHIVE_MESSAGE_TIME", "30"),
  /** 清除天数 */
  CLEAR_MESSAGE_HIS_TIME("CLEAR_MESSAGE_HIS_TIME", "180"),
  /** 会话日志保留天数 */
  CHAT_LOG_KEEP_DAYS("CHAT_LOG_KEEP_DAYS", "30"),
  /** 流程日志保留天数 */
  FLOW_LOG_KEEP_DAYS("FLOW_LOG_KEEP_DAYS", "3"),
  /** 模型使用日志保留天数 */
  MODEL_USAGE_LOG_KEEP_DAYS("MODEL_USAGE_LOG_KEEP_DAYS", "30"),
  /** 历史模型使用日志保留天数 */
  MODEL_USAGE_LOG_HIS_TIME("MODEL_USAGE_LOG_HIS_TIME", "180"),

  /** 意图识别 - 场景推荐数量 */
  SELECT_SCENE_LIMIT("SELECT_SCENE_LIMIT", "3"),
  /** 意图识别 - 场景推荐提示文本 */
  SELECT_SCENE_TIP("SELECT_SCENE_TIP", "请问您是表达什么意思，或者需要我帮助解答什么问题呢？提供更多背景信息将有助于我更好地为您提供帮助，为您提供以下推荐业务。"),
  /** 意图识别 - 确认启动智能体提示文本 */
  CONFIRM_SCENE_TIP("CONFIRM_SCENE_TIP", "系统识别到你可能需要进入智能体（%s）"),
  /** 向量匹配全局开关（未开启时，租户不显示相关功能开关、菜单） */
  VECTOR_MATCH_ENABLED("VECTOR_MATCH_ENABLED", "F"),
  /** 对话 - 系统异常提示 */
  CHAT_EXCEPTION_TIP("CHAT_EXCEPTION_TIP", "对不起，系统异常，请稍后再试。"),
  /** 敏感词回复内容 */
  SENSITIVE_MSG_REPLY("SENSITIVE_MSG_REPLY", "对不起，您的消息包含违规内容"),
  /** 是否开启敏感词过滤 */
  SENSITIVE_WORD_ENABLED("SENSITIVE_WORD_ENABLED", "F"),

  /** 静态数据批量导入最大数量限制 */
  ATTR_IMPORT_LIMIT_PROPERTY("ATTR_IMPORT_LIMIT_PROPERTY", "100"),

  /** 是否开启联网搜索 */
  WEB_SEARCH_ENABLED("WEB_SEARCH_ENABLED", "F"),
  /** 联网搜索策略 */
  WEB_SEARCH_STRATEGY("WEB_SEARCH_STRATEGY", "bocha"),
  /** 联网搜索脚本 */
  WEB_SEARCH_SCRIPT("WEB_SEARCH_SCRIPT", ""),
  /** 联网搜索 博查参数配置 */
  WEB_SEARCH_BOCHA_PARAMS("WEB_SEARCH_BOCHA_PARAMS", ""),
  /** 联网搜索 Tavily 参数配置 */
  WEB_SEARCH_TAVILY_PARAMS("WEB_SEARCH_TAVILY_PARAMS", ""),
  /** 联网搜索 Brave 参数配置 */
  WEB_SEARCH_BRAVE_PARAMS("WEB_SEARCH_BRAVE_PARAMS", ""),
  /** 联网搜索 Brave API Key（可选；也可设置环境变量 BRAVE_API_KEY）*/
  WEB_SEARCH_BRAVE_API_KEY("WEB_SEARCH_BRAVE_API_KEY", ""),
  /** 内置联网搜索 参数配置 */
  WEB_SEARCH_INTERNAL_PARAMS("WEB_SEARCH_INTERNAL_PARAMS", ""),

  /** 对话大模型记忆限制最大时长（分钟） */
  CHAT_MESSSGE_HIS_LIMIT_MINUTE("CHAT_MESSSGE_HIS_LIMIT_MINUTE", "30"),

  /** DB：签名安全模式 */
  SIGN_SECURITY_MODE("SIGN_SECURITY_MODE", "1.1"),
  /** DB：请求签名有效时间（秒） */
  REQUEST_SIGN_EFFECTIVE_TIME("REQUEST_SIGN_EFFECTIVE_TIME", "10"),
  /** DB: 签名重放校验 */
  SECURITY_SIGN_REUSE_CHECK("SECURITY_SIGN_REUSE_CHECK", "false"),
  /** DB：免签名校验 IP */
  IGNORE_SIGN_IP("IGNORE_SIGN_IP", ""),
  /** DB：环境信息模板 */
  ENV_INFO_TEMPLATE("ENV_INFO_TEMPLATE", ""),
  /** DB: 是否启用 nodejs */
  NODE_JS_ENABLED("NODE_JS_ENABLED", "false"),

  /** DB: 是否开启登录验证码 */
  VERIFICATION_CODE_ENABLE("app_security_enable_verification_code", "false"),
  /** DB: 用户失效时间 */
  USER_INVALID_TIME("USER_INVALID_TIME", ""),
  /** DB: 密码允许错误次数 */
  LOGIN_EXCEED_RETRY_LIMIT("LOGIN_EXCEED_RETRY_LIMIT", "3"),
  /** DB: 是否开启数据存储加密 */
  ENCRYPT_FIELD_ENABLED("ENCRYPT_FIELD_ENABLED", "false"),
  /** DB: 安全校验规则 */
  SECURITY_RULE_LOWER("SECURITY_RULE_LOWER", ""),
  /** DB: 手机号码脱敏规则 */
  MASK_PHONE_RULE("MASK_PHONE_RULE", "1"),
  /** DB: 邮箱脱敏规则 */
  MASK_EMAIL_RULE("MASK_EMAIL_RULE", "1"),
  /** DB: 密码有效期天数 */
  PWD_EXP_DAYS("PWD_EXP_DAYS", "0"),
  /** DB: 密码不重复的历史个数 */
  PWD_HIS_NUM("PWD_HIS_NUM", "0"),
  /** DB: 短信验证码开关 */
  SMS_CODE_ENABLED("SMS_CODE_ENABLED", "F"),
  /** DB: 短信验证码平台类型 */
  SMS_CODE_PLATFORM("SMS_CODE_PLATFORM", ""),
  /** DB: 短信验证码有效时间(分钟) */
  SMS_CODE_TIMEOUT("SMS_CODE_TIMEOUT", "5"),
  /** DB: 短信验证码模板 */
  SMS_CODE_TEMPLATE("SMS_CODE_TEMPLATE", "登录验证码：%s，%d分钟内有效。"),

  /** DB：文本转语音url */
  WORD_TO_AUDIO_URL("WORD_TO_AUDIO_URL", ""),
  /** DB：文本转语音提示词 */
  WORD_TO_AUDIO_PROMPT("WORD_TO_AUDIO_PROMPT", "全天下所有好东西都该属于我，包括你在内"),

  /** DB：SQL 查询数量限制 */
  SQL_QUERY_RECORD_LIMIT("SQL_QUERY_RECORD_LIMIT", "2000"),

  /** 启用插件市场 */
  PLUGIN_ENABLED("PLUGIN_ENABLED", "false"),
  /** 插件市场 API 地址 */
  PLUGIN_API_URL("PLUGIN_API_URL", ""),
  /** 插件市场密钥 */
  PLUGIN_API_KEY("PLUGIN_API_KEY", ""),
  /** 插件市场注册的门户编码 */
  PLUGIN_PORTAL_CODE("PLUGIN_PORTAL_CODE", ""),

  /** 是否开启用户注册 */
  REGISTER_ENABLED("REGISTER_ENABLED", "T"),
  /** 用户注册角色 */
  USER_REGISTER_ROLE("USER_REGISTER_ROLE", "MANAGE"),
  /** 用户注册模式 */
  USER_REGISTER_MODE_ENABLED("USER_REGISTER_MODE_ENABLED", "F"),

  /** 提示词 - 识别单个智能体 */
  SINGLE_AGENT_INTENT_PROMPT("SINGLE_AGENT_INTENT_PROMPT", ""),
  /** 提示词 - 生成计划 */
  GENERATE_PLAN_PROMPT("GENERATE_PLAN_PROMPT", ""),
  /** 提示词 - 分析是否需要二次意图识别 */
  ANALYZE_SECONDARY_RECOGNIZE_PROMPT("ANALYZE_SECONDARY_RECOGNIZE_PROMPT", ""),
  /** 提示词 - 识别智能应用 */
  RECOGNIZE_BOT_PROMPT("RECOGNIZE_BOT_PROMPT", ""),
  /** 提示词 - 分析是否需要退出大模型对话 */
  ANALYZE_EXIT_MODEL_PROMPT("ANALYZE_EXIT_MODEL_PROMPT", ""),
  /** 提示词 - 基于联网搜索内容总结 */
  WEB_SEARCH_PROMPT("WEB_SEARCH_PROMPT", ""),
  /** 提示词 - 分析是否需要联网搜索 */
  ANALYZE_WEB_SEARCH_PROMPT("ANALYZE_WEB_SEARCH_PROMPT", ""),
  /** 提示词 - 基于知识问答提炼问题 */
  QUESTION_PROMPT("QUESTION_PROMPT", ""),
  /** 提示词 - 简单场景提示词模板 */
  SCENE_TEMPLATE_PROMPT("SCENE_TEMPLATE_PROMPT", ""),
  /** 提示词 - 优化场景提示词 */
  SCENE_PERFECT_PROMPT("SCENE_PERFECT_PROMPT", ""),
  /** 提示词 - 生成提示词 */
  GENERATE_PROMPT("GENERATE_PROMPT", ""),
  /** 提示词 - 生成 groovy 脚本 */
  GENERATE_GROOVY_SCRIPT("GENERATE_GROOVY_SCRIPT", ""),
  /** 提示词 - 生成 python3 脚本 */
  GENERATE_PYTHON3_SCRIPT("GENERATE_PYTHON3_SCRIPT", ""),
  /** 提示词 - 生成 PlayWright 节点的 Python 脚本 */
  GENERATE_PLAYWRIGHT_PYTHON_SCRIPT("GENERATE_PLAYWRIGHT_PYTHON_SCRIPT", ""),
  /** 提示词 - 中英翻译 */
  CHINESE_TRANSLATE_PROMPT("CHINESE_TRANSLATE_PROMPT", ""),
  /** 提示词 - 一句话生成智能体提示词 */
  AI_GENERATE_PROMPT("AI_GENERATE_PROMPT", ""),
  /** 提示词 - 生成百应发布说明 */
  GENERATE_PUBLISH_REMARK_PROMPT("GENERATE_PUBLISH_REMARK_PROMPT", ""),
  /** 提示词 - 生成智能体描述 */
  GENERATE_BOT_DESCRIPTION_PROMPT("GENERATE_BOT_DESCRIPTION_PROMPT", ""),
  /** 提示词 - 生成智能体开场白 */
  GENERATE_PROLOGUE_PROMPT("GENERATE_PROLOGUE_PROMPT", ""),
  /** 提示词 - 生成工作流节点系统提示词 */
  GENERATE_LLM_NODE_PROMPT("GENERATE_LLM_NODE_PROMPT", ""),
  /** 提示词 - 生成知识问答节点系统提示词 */
  GENERATE_KNOWLEDGE_QA_NODE_PROMPT("GENERATE_KNOWLEDGE_QA_NODE_PROMPT", ""),
  /** 提示词 - 生成 Agent 节点系统提示词 */
  GENERATE_AGENT_NODE_PROMPT("GENERATE_AGENT_NODE_PROMPT", ""),
  /** 提示词 - 生成 AI 门户 BoteClaw 智能体定义 */
  GENERATE_AI_WORKSPACE_PROMPT("GENERATE_AI_WORKSPACE_PROMPT", ""),
  /** 提示词 - 本体场景 skill 模板 */
  ONTOLOGY_SCENE_TEMPLATE_PROMPT("ONTOLOGY_SCENE_TEMPLATE_PROMPT", ""),

  /** 灵犀平台网关地址 */
  LCDP_GATEWAY_URL("LCDP_GATEWAY_URL", ""),
  /** 灵犀平台API鉴权系统编码，灵犀出厂脚本为当前默认值 */
  LCDP_AUTH_SYSTEM_CODE("LCDP_AUTH_SYSTEM_CODE", "LcdpBote"),
  /** 灵犀平台API鉴权系统密钥，灵犀出厂脚本为当前默认值 */
  LCDP_AUTH_SYSTEM_SECRET("LCDP_AUTH_SYSTEM_SECRET", "wgq7df62fnDwDzPWSd"),

  /** 规划模式策略开关 */
  AGENT_STRATEGY_ENABLED("AGENT_STRATEGY_ENABLED", "T"),
  /** 确认执行计划的提示内容 */
  START_PLAN_TIP("START_PLAN_TIP", "已接收到你的任务，我将开始处理。"),
  /** docchain 主题保存协议   */
  DOCCHAIN_TOPIC_API_REQUEST("DOCCHAIN_TOPIC_API_REQUEST", ""),
  /** docchain使用博特平台LLM开关 */
  DOCCHAIN_USE_BOTE_LLM_ENABLED("DOCCHAIN_USE_BOTE_LLM_ENABLED", "T"),

  /** 聚智OCR开关，默认关闭 */
  JUZHI2_OCR_ENABLED("juzhi2.ocr.enabled", "false"),
  /** 磐智OCR 语音识别开关。默认关闭 */
  PANZHI_VOICE_RECOGNITION_ENABLED("panzhi.voice.recognition.enabled", "false"),

  /** docchain 问答回调博特的密钥 */
  DOCCHAIN_LLM_API_KEY("DOCCHAIN_LLM_API_KEY", ""),

  /** ENV：博特平台接口地址（以 "/" 结尾） */
  BOTE_API_URL("bote.apiUrl", ""),
  /** ENV：博特平台前端地址（以 "/" 结尾） */
  BOTE_WEB_URL("bote.webUrl", ""),
  /** ENV：博特平台公网接口地址（以 "/" 结尾） */
  BOTE_OPEN_API_URL("bote.openApiUrl", ""),
  /** ENV：ffmpeg 安装路径 */
  BOTE_FFMPEG_PATH("bote.ffmpeg.path", "ffmpeg"),

  /** 是否开启聊天窗口语音发送 */
  CHAT_VOICE_ENABLED("CHAT_VOICE_ENABLED", "F"),

  /** 对话窗口消息缩写最大长度 */
  CHAT_MSG_TEXT_ABBREVIATE_SIZE("CHAT_MSG_TEXT_ABBREVIATE_SIZE", "50"),

  /** 应用发布广场审核开关，默认开启 */
  BOT_PUBLISH_SQUARE_AUDIT_ENABLED("BOT_PUBLISH_SQUARE_AUDIT_ENABLED", "T"),

  /** 微调功能开关 */
  MODEL_FINETUNE_ENABLED("MODEL_FINETUNE_ENABLED", "F"),
  /** 语料功能开关 */
  CORPUS_ENABLED("CORPUS_ENABLED", "F"),
  /** 联想话术功能开关 */
  SUGGESTION_TERM_ENABLE("SUGGESTION_TERM_ENABLE", "F"),

  /** 语音识别：语音识别类型 */
  VIDEO_ORC_TYPE("VIDEO_ORC_TYPE", "default"),
  /** 语音识别：视频内容识别 API 地址 (takeAi和 默认) */
  VIDEO_OCR_API_URL("VIDEO_OCR_API_URL", ""),
  /** 语音识别：科大讯飞appId */
  VIDEO_ORC_XFYUN_APP_ID("VIDEO_ORC_XFYUN_APP_ID", ""),
  /** 语音识别：科大讯飞secret */
  VIDEO_ORC_XFYUN_SECRET("VIDEO_ORC_XFYUN_SECRET", ""),
  /** 语音识别：科大讯飞语音识别超时时间(秒) */
  VIDEO_ORC_XFYUN_TIMEOUT("VIDEO_ORC_XFYUN_TIMEOUT", ""),
  /** 语音识别：科大讯飞查询结果接口地址 */
  VIDEO_ORC_XFYUN_QUERY_URL("VIDEO_ORC_XFYUN_QUERY_URL", ""),
  /** 语音识别：识别模式 (normal: 非实时, realtime: 实时) */
  VIDEO_RECOGNITION_MODE("VIDEO_RECOGNITION_MODE", "normal"),
  /** 语音识别：TakeAi appToken */
  TAKE_AI_TOKEN("TAKE_AI_TOKEN", "appToken"),
  /** 语音识别：TakeAi AppKey */
  TAKE_AI_APP_KEY("TAKE_AI_APP_KEY", "appKey"),
  /** 语音识别：TakeAi 参数 */
  TAKE_AI_PRAMS("TAKE_AI_PRAMS", ""),
  /** 自定义语音识别脚本 */
  CUSTOM_VIDEO_RECOGNITION_SCRIPT("CUSTOM_VIDEO_RECOGNITION_SCRIPT", ""),

  /** 豆包语音Url */
  DOUBAO_VOICE_URL("DOUBAO_VOICE_URL", ""),

  /** 是否开启 Agent Pool 对接 */
  AGENT_POOL_ENABLED("AGENT_POOL_ENABLED", "F"),
  /** Agent Pool 接口地址 */
  AGENT_POOL_API_URL("AGENT_POOL_API_URL", null),
  /** Agent Pool 接口密钥，需要是全局密钥而非用户级密钥 */
  AGENT_POOL_API_KEY("AGENT_POOL_API_KEY", null),

  /** 百应：企业空间 ID */
  BEYOND_COMPANY_SPACE_ID("BEYOND_COMPANY_SPACE_ID", ""),
  /** 百应：个人空间 ID */
  BEYOND_DEVELOP_SPACE_ID("BEYOND_DEVELOP_SPACE_ID", ""),

  /** 数据库安全模式开关 */
  DATABASE_SAFE_MODE_ENABLED("DATABASE_SAFE_MODE_ENABLED", "T"),
  /** 限制脚本执行的数据源类型 */
  UNSUPPORTED_DDL_DATABASE("UNSUPPORTED_DDL_DATABASE", ""),

  GAODE_API_URL("bote.amap.api.location", ""),

  BAIDIYUN_API_URL("bote.baidiyun.api", ""),

  /** 导入数据包，只允许更新状态的配置表 */
  ONLY_UPDATE_STATUS_CD_TABLES("ONLY_UPDATE_STATUS_CD_TABLES", "BT_TENANT_SETTING_INFO,BT_KNOWLEDGE_BASE,BT_DOCUMENT,BT_DC_DOCUMENT_LIBRARY"),
  /** 备份清理间隔（月） */
  DATA_SYNC_BACKUP_CLEAR_INTERVAL_MONTHS("DATA_SYNC_BACKUP_CLEAR_INTERVAL_MONTHS", "6"),
  /** MySQL 安全模式开关 */
  MYSQL_SAFE_MODE("MYSQL_SAFE_MODE", "F"),

  /** BoteClaw 对话功能使用的向量化大模型 */
  BOTECLAW_EMBEDDING_MODEL_ID("BOTECLAW_EMBEDDING_MODEL_ID", ""),

  /** LLM 流式输出安全围栏校验，批次字符阈值 */
  PARTIAL_CHECK_CHAR_THRESHOLD("PARTIAL_CHECK_CHAR_THRESHOLD", "120"),

  /** 本体平台 API 地址 */
  ONTOLOGY_BASE_URL("ONTOLOGY_BASE_URL", ""),
  /** 本体平台 API 密钥 */
  ONTOLOGY_API_KEY("ONTOLOGY_API_KEY", ""),

  /** agentSkill 编辑文件类型 */
  AGENT_SKILL_EDIT_FILE_TYPE("AGENT_SKILL_EDIT_FILE_TYPE", "md,txt,json,yaml,yml,py,java,js,ts,xml,properties,sql,sh,bat"),

  DO_NOT_USE("", "");

  /** 系统参数编码 */
  private final String code;

  /** 系统参数默认值 */
  private final String defaultValue;

  /**
   * 获取系统参数值，来源于数据库
   *
   * @return 字符串类型值
   */
  public String getValueFromDb() {
    return SpringUtil.getBean(DcParamCache.class).getDcParamValByCode(this.code, this.defaultValue);
  }

  /**
   * 获取系统参数值，来源于数据库
   *
   * @return 整数类型值
   */
  public Integer getIntegerValueFromDb() {
    String value = getValueFromDb();
    return StringUtils.isEmpty(value) ? null : Integer.valueOf(value);
  }

  /**
   * 获取系统参数值，来源于数据库
   *
   * @return 布尔类型值
   */
  @NonNull
  public Boolean getBooleanValueFromDb() {
    String value = getValueFromDb();
    List<String> flag = Arrays.asList("T", "Y", "TRUE", "1");
    return StringUtils.isEmpty(value) || !flag.contains(value.toUpperCase()) ? Boolean.FALSE : Boolean.TRUE;
  }

  /**
   * 获取系统参数值，来源于数据库（不为空）
   *
   * @return 整数类型值
   */
  @NonNull
  public Integer getRequiredIntegerValueFromDb() {
    String value = getValueFromDb();
    return StringUtils.isEmpty(value) ? 0 : Integer.parseInt(value);
  }

  /**
   * 获取系统参数值（逗号分隔列表），来源于数据库
   *
   * @return 字符串列表
   */
  public List<String> getListValueFromDb() {
    String value = getValueFromDb();
    return StringUtils.isEmpty(value) ? Collections.emptyList() : Arrays.asList(value.split(","));
  }

  /**
   * 获取系统参数值，来源于环境
   *
   * @return 字符串类型值
   */
  public String getValueFromEnv() {
    return SpringUtil.getProperty(this.code, this.defaultValue);
  }

  /**
   * 获取系统参数值，来源于环境
   *
   * @return 整数类型值
   */
  public Integer getIntegerValueFromEnv() {
    String value = getValueFromEnv();
    return StringUtils.isEmpty(value) ? null : Integer.parseInt(value);
  }

  /**
   * 获取系统参数值，来源于环境
   *
   * @return 布尔类型值
   */
  @NonNull
  public Boolean getBooleanValueFromEnv() {
    String value = getValueFromEnv();
    List<String> flag = Arrays.asList("T", "Y", "TRUE", "1");
    return StringUtils.isEmpty(value) || !flag.contains(value.toUpperCase()) ? Boolean.FALSE : Boolean.TRUE;
  }
}

