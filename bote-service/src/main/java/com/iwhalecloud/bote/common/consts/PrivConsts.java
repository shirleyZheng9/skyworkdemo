package com.iwhalecloud.bote.common.consts;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * 权限相关常量
 *
 * @author chen.linfa
 * @since 2025-10-09
 */
public final class PrivConsts {
  private PrivConsts() {
  }

  /** 角色: 系统管理 */
  public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
  /** 角色: 企业管理 */
  public static final String ROLE_SPACE_ADMIN = "SPACE_ADMIN";
  /** 角色: 租户管理 */
  public static final String ROLE_MANAGE = "MANAGE";
  /** 角色: 租户编辑 */
  public static final String ROLE_EDIT = "EDIT";
  /** 角色: 租户只读 */
  public static final String ROLE_READONLY = "READONLY";
  /** 角色: 使用 */
  public static final String ROLE_USE = "USE";

  /** 权限类型: 菜单 */
  public static final String PRIV_TYPE_MENU = "menu";
  /** 权限类型: 组件 */
  public static final String PRIV_TYPE_COMPONENT = "component";

  /** 门户目录根节点 - 使用者平台 */
  public static final Long PORTAL_DIR_USE = 1000L;
  /** 门户目录根节点 - 管理平台 */
  public static final Long PORTAL_DIR_MANAGE = 2000L;
  /** 门户目录根节点 - 开发平台 */
  public static final Long PORTAL_DIR_DEVELOP = 3000L;

  /** 门户菜单打开方式 - 内部打开 */
  public static final String PORTAL_MENU_TYPE_PAGE = "page";
  /** 门户菜单打开方式 - iframe */
  public static final String PORTAL_MENU_TYPE_IFRAME = "iframe";
  /** 门户菜单打开方式 - 新窗口打开 */
  public static final String PORTAL_MENU_TYPE_OPEN = "open";

  /** 菜单 - 文档中心（旧） */
  public static final Long MENU_ID_KNOWLEDGE = 1301L;
  /** 高阶功能菜单 - 微调 */
  public static final Long MENU_ID_FINETUNE = 1521L;
  /** 高阶功能菜单 - 语料 */
  public static final Long MENU_ID_CORPUS = 1321L;
  /** 高阶功能菜单 - 联想话术 */
  public static final Long MENU_ID_SUGGESTIONTERM = 2025101804L;

  /** 功能菜单 - 插件管理 */
  public static final Long MENU_ID_PLUGIN = 3040L;
  /** 功能菜单 - 插件市场广场 */
  public static final Long MENU_ID_PLUGIN_SQUARE = 4002L;
  /** 功能菜单 - 插件市场广场 */
  public static final String MENU_URL_PLUGIN_SQUARE = "/pluginSquare";

  /** 运行态菜单 - 工作台应用 */
  public static final Long MENU_ID_WORKBENCH = 2025101811L;
  /** 运行态菜单 - AI 助理 */
  public static final Long MENU_ID_AI_ASSISTANT = 2025101812L;
  /** 运行态菜单 - 网页应用 */
  public static final Long MENU_ID_WEB_APP = 2025101813L;

  public static final List<Long> ADVANCED_MENU_IDS = ImmutableList.of(MENU_ID_FINETUNE, MENU_ID_CORPUS, MENU_ID_SUGGESTIONTERM);

  public static final List<Long> RUNTIME_MENU_IDS = ImmutableList.of(MENU_ID_WORKBENCH, MENU_ID_AI_ASSISTANT, MENU_ID_WEB_APP);
}
