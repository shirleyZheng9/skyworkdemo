package com.iwhalecloud.bote.common.consts;

/**
 * 会话相关常量
 *
 * @author chen.linfa
 * @since 2024-11-22
 */
public final class ChatConsts {
  private ChatConsts() {
  }

  /** 对话完成标识 */
  public static final String COMPLETIONS_DONE = "[DONE]";
  /** 对话就绪标识 */
  public static final String COMPLETIONS_READY = "[READY]";
  /** 退出场景标识 */
  public static final String COMPLETIONS_EXIT = "[EXIT]";

  /** 点赞类型：点赞 */
  public static final String LIKE_TYPE_1 = "1";
  /** 点赞类型：点踩 */
  public static final String LIKE_TYPE_2 = "2";
  /** 点赞类型：不予置评 */
  public static final String LIKE_TYPE_3 = "3";

  /** 默认会话标题 */
  public static final String DEFAULT_SESSION_TITLE = "新对话";
  /** 默认会话 ID */
  public static final Long DEFAULT_SESSION_ID = -1L;
  /** 百应会话 ID */
  public static final Long BEYOND_SESSION_ID = -2L;
  /** 微信公众号会话 ID */
  public static final Long WECHAT_SESSION_ID = -3L;
  /** A2A 会话 ID */
  public static final Long A2A_SESSION_ID = -4L;
  /** 外部渠道消息监听器 会话 ID (飞书 钉钉 企业微信) */
  public static final Long SDK_SESSION_ID = -5L;
  /** 评测 会话 ID */
  public static final Long EVAL_SESSION_ID = -6L;
  /** 退出场景标识 ID */
  public static final Long EXIT_SCENE_ID = -1L;

  /** 会话场景状态 - 运行中 */
  public static final String CHAT_SCENE_STATUS_RUNNING = "1";
  /** 会话场景状态 - 结束 */
  public static final String CHAT_SCENE_STATUS_FINISH = "2";

  /** 会话上传附件 */
  public static final String PARAM_FILE_ID = "fileIds";

  /** 对话应用属性 - 置顶 */
  public static final String CHAT_BOT_ATTR_TOP = "top";
  /** 对话应用属性 - 标记 */
  public static final String CHAT_BOT_ATTR_MARK = "mark";
  /** 对话应用属性 - 完成 */
  public static final String CHAT_BOT_ATTR_CLOSE = "close";

  /** 对话预置的平台应用列表 */
  public static final String CHAT_DEFAULT_PLAT_BOT = "CHAT_DEFAULT_PLAT_BOT";

  /** AI 门户通用搜索类型 - 综合 */
  public static final String AI_SEARCH_TYPE_ALL = "all";
  /** AI 门户通用搜索类型 - AI 助理 */
  public static final String AI_SEARCH_TYPE_BOT = "bot";
  /** AI 门户通用搜索类型 - 网页应用 */
  public static final String AI_SEARCH_TYPE_WEB = "web";
  /** AI 门户通用搜索类型 - 文档 */
  public static final String AI_SEARCH_TYPE_DOC = "document";
}
