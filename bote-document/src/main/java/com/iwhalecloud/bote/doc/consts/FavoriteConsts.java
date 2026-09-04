package com.iwhalecloud.bote.doc.consts;

/**
 * 收藏模块常量类
 * 包含目标类型、权限、错误消息等常量
 *
 * @author lizuyin
 * @since 2025-08-21
 */
public final class FavoriteConsts {
  /** 目标类型：文档 */
  public static final String TARGET_TYPE_DOCUMENT = "DOCUMENT";

  // ==================== 目标类型常量 ====================
  /** 目标类型：文件夹 */
  public static final String TARGET_TYPE_FOLDER = "FOLDER";
  /** 目标类型：文档库 */
  public static final String TARGET_TYPE_LIBRARY = "LIBRARY";
  /** 目标类型：知识库 */
  public static final String TARGET_TYPE_KNOWLEDGE = "KNOWLEDGE";
  /** 文档权限：编辑 */
  public static final String PERMISSION_EDIT = "EDIT";

  // ==================== 权限常量 ====================
  /** 置顶标识：是 */
  public static final String IS_PINNED_TRUE = "T";

  // ==================== 置顶标识常量 ====================
  /** 页码验证错误消息 */
  public static final String ERROR_MSG_PAGE_NUM_INVALID = "页码必须大于0";

  // ==================== 错误消息常量 ====================
  /** 每页大小验证错误消息 */
  public static final String ERROR_MSG_PAGE_SIZE_INVALID = "每页大小必须大于0";
  /** 收藏成功消息 */
  public static final String SUCCESS_MSG_FAVORITE_ADDED = "收藏成功";

  // ==================== 添加收藏相关常量 ====================
  /** 资源已收藏消息 */
  public static final String SUCCESS_MSG_ALREADY_FAVORITED = "资源已收藏";
  /** 取消收藏成功消息 */
  public static final String SUCCESS_MSG_FAVORITE_REMOVED = "取消收藏成功";
  /** 设置置顶成功消息 */
  public static final String SUCCESS_MSG_FAVORITE_PINNED = "设置置顶成功";
  /** 取消置顶成功消息 */
  public static final String SUCCESS_MSG_FAVORITE_UNPINNED = "取消置顶成功";
  private FavoriteConsts() {
  }
}
