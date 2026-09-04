package com.iwhalecloud.bote.common.consts;

/**
 * 组织管理相关常量
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
public final class OrganizationConsts {
  // 私有构造函数，防止实例化
  private OrganizationConsts() {
  }
  // ==================== 组织类型 ====================
  /** 组织类型：总部 */
  public static final String ORG_TYPE_HEADQUARTERS = "headquarters";
  /** 组织类型：分公司 */
  public static final String ORG_TYPE_BRANCH = "branch";
  /** 组织类型：办事处 */
  public static final String ORG_TYPE_OFFICE = "office";
  /** 组织类型：部门 */
  public static final String ORG_TYPE_DEPARTMENT = "department";

  // ==================== 成员角色 ====================
  /** 成员角色：管理员 */
  public static final String MEMBER_ROLE_ADMIN = "admin";
  /** 成员角色：普通成员 */
  public static final String MEMBER_ROLE_MEMBER = "member";
  /** 成员角色：访客 */
  public static final String MEMBER_ROLE_GUEST = "guest";

  // ==================== 成员类型 ====================
  /** 成员类型：正式成员 */
  public static final String MEMBER_TYPE_REGULAR = "regular";
  /** 成员类型：临时成员 */
  public static final String MEMBER_TYPE_TEMPORARY = "temporary";

  // ==================== 字段类型 ====================
  /** 字段类型：单行文本 */
  public static final String FIELD_TYPE_TEXT = "text";
  /** 字段类型：多行文本 */
  public static final String FIELD_TYPE_TEXTAREA = "textarea";
  /** 字段类型：数字 */
  public static final String FIELD_TYPE_NUMBER = "number";
  /** 字段类型：日期 */
  public static final String FIELD_TYPE_DATE = "date";
  /** 字段类型：日期时间 */
  public static final String FIELD_TYPE_DATETIME = "datetime";
  /** 字段类型：布尔值 */
  public static final String FIELD_TYPE_BOOLEAN = "boolean";
  /** 字段类型：单选下拉 */
  public static final String FIELD_TYPE_SELECT = "select";
  /** 字段类型：多选 */
  public static final String FIELD_TYPE_MULTI_SELECT = "multi_select";
  /** 字段类型：邮箱 */
  public static final String FIELD_TYPE_EMAIL = "email";
  /** 字段类型：电话 */
  public static final String FIELD_TYPE_PHONE = "phone";
  /** 字段类型：网址 */
  public static final String FIELD_TYPE_URL = "url";
  /** 字段类型：JSON对象 */
  public static final String FIELD_TYPE_JSON = "json";

  // ==================== 默认值 ====================
  /** 组织路径分隔符 */
  public static final String ORG_PATH_SEPARATOR = ".";
  /** 根组织层级 */
  public static final Integer ROOT_ORG_LEVEL = 1;
  /** 最大组织层级 */
  public static final Integer MAX_ORG_LEVEL = 10;

  // ==================== 错误消息 ====================
  /** 错误：组织不存在 */
  public static final String ERROR_ORG_NOT_EXISTS = "组织不存在";
  /** 错误：组织编码已存在 */
  public static final String ERROR_ORG_CODE_EXISTS = "组织编码已存在";
  /** 错误：组织下存在子组织 */
  public static final String ERROR_ORG_HAS_CHILDREN = "组织下存在子组织，无法删除";
  /** 错误：组织下存在成员 */
  public static final String ERROR_ORG_HAS_MEMBERS = "组织下存在成员，无法删除";
  /** 错误：成员已存在 */
  public static final String ERROR_MEMBER_EXISTS = "用户已是组织成员";
  /** 错误：成员不存在 */
  public static final String ERROR_MEMBER_NOT_EXISTS = "组织成员不存在";
  /** 错误：字段键名已存在 */
  public static final String ERROR_FIELD_KEY_EXISTS = "字段键名已存在";
  /** 错误：字段配置不存在 */
  public static final String ERROR_FIELD_CONFIG_NOT_EXISTS = "字段配置不存在";
  /** 错误：扩展字段验证失败 */
  public static final String ERROR_EXT_FIELD_VALIDATION = "扩展字段验证失败";

  // ==================== 静态值 ====================
  /** 静态值：组织成员类型 */
  public static final String ATTR_CODE_ORG_MEMBER_TYPE = "ORG_MEMBER_TYPE";
  /** 静态值：组织成员角色 */
  public static final String ATTR_CODE_ORG_MEMBER_ROLE = "ORG_MEMBER_ROLE";
  /** 静态值：组织扩展字段类型 */
  public static final String ATTR_CODE_ORG_EXT_FILED_TYPE = "ORG_EXT_FILED_TYPE";
  /** 静态值：组织类型 */
  public static final String ATTR_CODE_ORG_TYPE = "ORG_TYPE";
}
