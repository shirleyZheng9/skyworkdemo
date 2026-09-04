package com.iwhalecloud.bote.common.enums;

import com.iwhalecloud.bss.litchi.base.error.IErrorConstant;
import lombok.Getter;

/**
 * 应用基础错误编码
 * <p>错误编码使用范围: 000000 ~ 999999 </p>
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Getter
public enum BaseErrorConstant implements IErrorConstant {

  /** 配置数据没有发生变化 */
  NO_DIFFERENCE("000001", "配置数据没有发生变化", "请检查是否未修改表单数据"),
  CHECK_ATTR_NBR("000002", "属性编码+取值类型不唯一：AttrNbr==%s", "请修改属性编码为不重复值"),
  CHECK_ATTR_VALUE("000003", "属性值不唯一：AttrValue== %s", "请修改属性值为不重复值"),
  CHECK_CODE("000004", "该编码【%s】已被使用，请尝试其他编码", "请修改编码为不重复值"),
  CHECK_NAME("000005", "该名称【%s】已被使用，请尝试其他名称", "请修改名称为不重复值"),
  CHECK_TENANT_USER("000006", "用户已加入租户，无需重复操作", "请检查租户成员配置"),
  /** 未登录 */
  NO_LOGIN("000007", "未登录", "请重新登录"),
  /** 加密失败 */
  ENCRYPT_ERROR("000008", "加密失败"),
  UNKNOWN_STEP_TYPE("000009", "未知的步骤类型: %s", "请联系管理员核查该节点的DSL数据"),
  /** 数据源连接失败 */
  CONNECT_DATASOURCE_FAILED("000010", "连接数据源失败", "检查应用数据源配置是否正确"),
  /** 获取数据源失败 */
  GET_DATASOURCE_FAIL("000011", "获取数据源失败", "检查应用数据源配置是否正确"),
  /** SQL执行失败 */
  SQL_ERROR("000012", "SQL执行失败，请检查SQL语句是否正确", "请检查SQL语句是否正确"),
  /** 缺少数据源配置 */
  FIND_DATASOURCE_FAIL("000012", "缺少数据源配置: %s", "根据数据源ID或数据源编码无法查询到数据源配置，请检查数据是否存在"),
  /** 账号或密码错误 */
  LOGIN_FAIL("000013", "登录账号或密码错误", "请检查登录账号或密码"),
  /** 账号不存在 */
  USER_NAME_NOT_EXISTS("000014", "用户[%s]不存在", "请检查登录账号"),
  /** 用户名密码校验失败 */
  USER_PASSWORD_FAIL("000015", "用户名密码校验失败", "请检查密码"),
  /** 用户名密码必须包含大写字母 */
  USER_PASSWORD_CHECK_FAIL("000016", "用户名密码必须包含大写字母", "请检查密码"),
  /** 数据不存在 */
  NOT_EXIST("000017", "数据不存在：id=%s", "请检查数据是否存在"),
  /** 用户账号已被锁定 */
  USER_LOCKED("000018", "用户账号已被锁定"),
  /** 找不到外系统用户ID */
  EXT_USER_NOT_FOUND("000019", "找不到外系统用户ID"),
  /** 密码修改：新旧密码不能一样 */
  USER_MODIFY_PWD_SAME("000020", "新密码不能和旧密码相同", "请检查密码"),
  /** 密码修改：历史密码重复 */
  USER_MODIFY_PWD_USED("000021", "修改密码失败，因为此密码之前已经被使用过", "请检查密码"),

  /** 文件不存在 */
  FILE_ID_NOT_EXIST("000101", "文件不存在,fileId = %s", "请检查文件是否被删除"),
  /**  文件转换失败 */
  FILE_CONVERT_FAIL("000102", "文件转化失败", "请检查文件"),
  /**  智能应用设置默认失败 */
  BOT_DEFAULT_FAIL("000103", "该名称【%s】未启用，请先启用再设置默认", "请应用是否启用"),

  /** 缓存刷新错误 */
  CACHE_REFRESH_ERROR("000201", "%s"),

  REQUEST_UPORTAL_LOGGED_API_FAIL("000301", "调用 uportal 登录状态检查接口失败"),
  REQUEST_NGPORTAL_LOGGED_API_FAIL("000302", "调用 ngportal 登录状态检查接口失败"),
  REQUEST_BASIC_CENTER_LOGGED_API_FAIL("000303", "调用 basiccenter 登录状态检查接口失败"),

  /** HTTP 请求失败 */
  REQUEST_FAIL("000401", "HTTP 请求失败: %s", "请检查请求路径、请求参数、请求方式、头部信息等是否正确"),
  /** 不支持的协议 */
  INVALID_PROTOCOL("000402", "不支持的协议: %s", "只支持 http 和 https 协议"),
  /** 请求地址不合法 */
  INVALID_URL("000403", "请求地址不合法: %s", "请检查请求地址是否正确"),
  /** 校验 ip+host 连通性错误 */
  URI_CONNECTION_ERROR("000404", "%s 无法访问！", "请检查地址是否正确"),
  /** SSL上下文构建失败 */
  SSL_CONTEXT_BUILD_ERROR("000405", "SSL上下文构建失败"),

  /** 服务平台 */
  SERVICE_PLAT_DEF_NOT_EXIST("000501", "服务平台不存在：platDefId=%s", "请检查服务平台是否存在"),
  BOT_SKILL_SERVICE_NOT_EXIST("000502", "API服务不存在， serviceId=%s", "请检查API服务是否存在"),

  /** 页面实例 */
  BOT_SKILL_PAGE_NOT_EXIST("000520", "页面不存在：pageId=%s", "请检查页面是否存在"),
  /** 工作流 */
  BOT_SKILL_WORKFLOW_NOT_EXISTS("000540", "工作流不存在：flowId=%s", "请检查工作流是否存在"),
  /** 提示语 */
  BOT_PROMPT_NOT_EXISTS("000560", "提示语不存在：promptId=%s", "请检查提示语流是否存在"),
  /** 业务数据 */
  BOT_SKILL_ATTR_SPEC_NOT_EXISTS("000580", "静态数据不存在：attrId=%s", "请检查静态数据是否存在"),
  /** 业务对象 */
  BOT_SKILL_OBJECT_NOT_EXISTS("000600", "业务对象不存在：busiObjectId=%s", "请检查业务对象是否存在"),
  /** SQL服务 */
  BOT_SKILL_SQL_NOT_EXISTS("000620", "SQL服务不存在：serviceId=%s", "请检查SQL服务是否存在"),
  /** 找不到 SQL 语句 */
  GET_SQL_SOURCE_ERROR("000621", "找不到 SQL 语句", "请检查 SQL 语句是否存在语法错误"),
  /** SQL过于复杂 */
  SQL_COMPLEX_ERROR("000622", "SQL 过于复杂，暂时不支持", "SQL 语句仅支持一层节点的增删改查"),
  /** 暂不支持该数据库类型 */
  UNSUPPORTED_DATABASE_TYPE("000623", "暂不支持该数据库类型: %s", "目前只支持 Oracle、Mysql、Postgresql 数据库类型"),
  /** SQL解析异常 */
  PARSE_SQL_FAILED("000624", "SQL解析异常，请重新检查定义。", "请检查 SQL 语句是否存在语法错误"),
  /** 函数 */
  BOT_SKILL_FUNCTION_NOT_EXISTS("000640", "函数不存在：funcId=%s", "请检查函数是否存在"),
  /** 文本 */
  BOT_SKILL_TEXT_NOT_EXISTS("000660", "文本不存在：textId=%s", "请检查文本是否存在"),
  /** 数据源 */
  BOT_SKILL_DATASOURCE_INST_NOT_EXISTS("000680", "数据源不存在，请检查数据源", "请检查数据源是否存在"),
  /** 大模型 */
  LARGE_MODEL_NOT_EXISTS("000700", "大模型不存在：modelId=%s", "请检查大模型是否存在"),
  /** 未配置默认模型 */
  NO_DEFAULT_MODEL("000710", "当前租户未配置默认模型", "当前租户未配置默认模型，请在模型菜单中选择一个模型设置为默认模型"),
  /** 页面函数 */
  BOT_SKILL_PAGE_FUNC_NOT_EXISTS("000720", "页面函数不存在：pageFunId=%s", "请检查页面函数是否存在"),
  /** 用户名称重复 */
  USER_NAME_EXISTS("000740", "用户名已存在", "请修改用户名为不重复值"),
  /** 用户不存在 */
  USER_NOT_EXISTS("000741", "用户不存在", "请检查用户是否存在"),
  /** 根据文件流的头部信息获得文件类型 */
  FILE_GET_TYPE_ERROR("000742", "根据文件流的头部信息获得文件类型失败"),
  /** 大模型API不存在 */
  SKILL_PLUGIN_NOT_EXISTS("000760", "插件不存在：apiId=%s", "请检查插件是否存在"),
  /** 文件 OCR 提取文字异常 */
  OCR_IDENTIFY_WORDS_ERROR("000770", "文件 OCR 提取文字异常，%s"),
  /** 参数值类型解析异常 */
  PARAM_TYPE_ERROR("000780", "参数值类型解析异常", "请检查参数值"),
  /** 参数格式解析异常 */
  PARAM_FORMAT_ERROR("000781", "参数格式解析异常", "请检查参数格式"),
  /** 不支持的参数类型 */
  SERVICEBODY_BUILDBYOBJECT_UNSUPPOER_TYPE("000782", "不支持的参数类型: %s", "请检查参数"),
  /** 不支持的页面模板类型 */
  PAGE_TEMPLATE_UNSUPPOER_TYPE("000790", "不支持的页面模板类型: %s"),
  /** 平台数据不支持修改 */
  UNSUPPOER_MODIFY("000800", "平台数据不支持修改: %s"),
  /** 系统参数更新不到数据 */
  DC_CFG_NO_UPDATE_DATA_ERROR("000810", "以下编码保存失败，找不到对应的静态数据：%s", "根据参数编码找到查询到配置数据，请检查数据是否存在"),
  /** 系统参数配置查询异常，找不到数据 */
  DC_CFG_DATA_NOT_FOUND_ERROR("000811", "配置数据不存在: configId=%s"),
  /** 系统参数更新请求参数错误 */
  DC_CFG_UPDATE_PARAM_ERROR("000812", "参数错误，需要更新的编码不能为空！", "请配置需要修改的系统参数"),
  /** 系统参数不存在 */
  DC_PARAM_NOT_FOUND_ERROR("000813", "系统参数不存在: paramCode=%s"),

  /** 创建代码生成工作空间异常 */
  CREATE_CODE_GENERATE_WORKSPACE_ERROR("000801", "创建代码生成工作空间异常"),
  /** 清空数据同步工作空间异常 */
  CLEAR_CODE_GENERATE_WORKSPACE_ERROR("000802", "清空数据同步工作空间异常"),
  /** 未找到类路径文件 */
  GET_RESOURCES_ERROR("000803", "未找到类路径文件: %s"),
  /** 创建压缩包失败 */
  ZIP_FILES_ERROR("000804", "创建压缩包失败"),
  /** 解压缩文件失败 */
  UNZIP_FILES_ERROR("000805", "解压缩文件失败"),
  /** 执行解压缩失败 */
  DO_UNZIP_FILES_ERROR("000806", "非法的 ZIP 文件，尝试解压到目标目录之外: %s"),

  CHECK_LABEL_NAME("000900", "标签名称不唯一：LabelName==%s"),
  /** API swagger导入接口异常 */
  GET_OPEN_API_FAILED("0000905", "读取 api-docs 内容异常", "请检查 api-docs 地址是否正确"),
  /** API swagger文件导入接口异常 */
  GET_OPEN_API_FILE_FAILED("0000906", "读取 api-docs 文件内容异常", "请检查 api-docs 文件内容是否正确"),

  /** 探测表失败 */
  INSPECT_TABLE_SCHEMA_FAIL("000910", "无法检查表 Schema: table=%s"),
  /** 数据源测试失败 */
  TEST_APP_DATASOURCE_FAILED("000911", "测试不通过，请检查数据库连接地址、用户名、密码是否正确：%s", "请检查数据库连接地址、用户名、密码是否正确"),
  /** 连接数据库失败 */
  GET_DATABASE_CONNECTION_FAILED("000912", "连接数据库失败"),
  /** 执行sql脚本失败 */
  EXECUTE_SQL_SCRIPT_FAILED("000913", "执行 SQL 脚本失败"),
  /** 检测数据库/schema名称失败 */
  CHECK_DATABASE_NAME_ERROR("000914", "检测 %s 数据库/schema名称失败: %s"),
  /** 创建数据库失败 */
  CREATE_DATABASE_ERROR("000915", "创建 %s 数据库失败: %s"),

  /** 不允许上传的文件类型 */
  NOT_ALLOWED_UPLOAD_FILE_TYPE("000920", "不允许上传的文件类型: %s", "请检查文件类型"),
  /** 不允许上传空文件 */
  EMPTY_FILE_ERROR("000921", "不允许上传空文件, name=%s", "请检查文件内容"),

  /** 意图问句已存在 */
  QUESTION_EXIST("000930", "意图问句已存在: %s", "请检查意图问句是否重复"),

  /** 属性值导入数量超出最大值 **/
  ATTR_IMPORT_LIMIT_ERROR("000940", "属性值数量不能超过 %s 条, 当前数量为 %s", "请将属性值分多次导入"),

  /** 安全校验：登录用户非法请求参数 */
  SECURITY_1("000941", "登录用户非法请求参数"),
  /** 安全校验：非法请求参数 */
  SECURITY_2("000942", "非法请求参数"),
  /** 安全校验：请求签名已失效 */
  SECURITY_3("000943", "请求签名已失效"),
  /** 安全校验：请求签名中的时间参数错误 */
  SECURITY_4("000944", "当前签名安全模式为 1.1，请求签名中的时间参数错误"),
  NOT_ALLOWED_UPLOAD_FILE_CONVERT_TO_ONLINE_TYPE("000945", "不允许转在线文档的文件类型: %s", "请检查文件类型"),

  /** 业务数据表：表名重复 */
  DATA_TABLE_CODE_EXIST("000946", "数据表名 %s 已存在", "请勿创建重复的数据表名"),
  /** 业务数据表：表不存在 */
  DATA_TABLE_NOT_EXIST("000947", "数据表不存在: %s", "无法查询到数据表，请检查表数据是否已被删除"),

  /** 以下为数据库相关的的错误类型 */
  NO_SUPPORT_DATABASE("001000", "暂时不支持适配数据库类型：%s", ""),
  SQL_SCRIPT_NOT_EXIST("001001", "SQL 脚本文件不存在: %s"),
  SQL_SCRIPT_NOT_EMPTY("001002", "SQL 脚本不能为空"),
  SQL_SCRIPT_LIST_IS_EMPTY("001003", "SQL 脚本列表为空"),
  SQL_SCRIPT_FILE_NOT_EXIST("001004", "SQL 脚本文件不存在: %s"),
  READ_SQL_SCRIPT_FAILED("001005", "读取脚本资源失败"),
  PARSE_SQL_SCRIPT_FAILED("001006", "SQL 脚本错误"),
  DATABASE_URL_NOT_EMPTY("001007", "数据库连接地址不能为空", "请检查数据源中的数据库地址"),
  INVALID_DATABASE_URL_NOT_EMPTY("001008", "非法的数据库连接地址: %s", "请检查数据源中的数据库地址是否正确"),
  DDL_CONVERTER_NOT_FOUND("001009", "没有找到 %s 数据库的脚本生成器", "请检查系统配置表中是否包含相关配置"),
  SQL_EXECUTE_FAILED("001010", "SQL 脚本执行失败: %s"),
  ILLEGAL_DATA_TYPE("001011", "非法数据类型：%s", "数据类型仅支持字符型、数字型、时间、浮点数"),

  /** 不要使用，只放在最后方便维护枚举类 */
  DO_NOT_USE("", "");

  /** 错误编码 */
  private final transient ErrorConstant errorConstant;

  BaseErrorConstant(String code, String message) {
    this(code, message, null);
  }

  BaseErrorConstant(String code, String message, String guide) {
    this.errorConstant = new ErrorConstant("0000" + code, message, guide);
  }
}
