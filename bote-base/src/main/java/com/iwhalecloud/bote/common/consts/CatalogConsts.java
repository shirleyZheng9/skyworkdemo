package com.iwhalecloud.bote.common.consts;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * 目录相关常量
 *
 * @author chen.linfa
 * @since 2025-10-11
 */
public final class CatalogConsts {
  private CatalogConsts() {
  }

  /** 默认目录节点父 ID */
  public static final Long DEFAULT_CATALOG_ITEM_PARENT_ID = -1L;
  /** 应用预置的目录节点 ID */
  public static final Long BOT_CATALOG_ITEM_ID = 1261931035110359041L;

  /** 目录类型：场景 */
  public static final String TYPE_SCENE = "scene";
  /** 目录类型：技能 */
  public static final String TYPE_SKILL = "skill";
  /** 目录类型：知识库 */
  public static final String TYPE_KNOWLEDGE = "knowledge";
  /** 目录类型：语料 */
  public static final String TYPE_CORPUS = "corpus";
  /** 目录类型：页面资源 */
  public static final String TYPE_PAGE_FILE = "pageFileResource";
  /** 目录类型：提示词 */
  public static final String TYPE_PROMPT = "prompt";
  /** 目录类型：鉴权指令 */
  public static final String TYPE_API_AUTH = "apiAuth";
  /** 目录类型：菜单类型权限 */
  public static final String TYPE_PRIV_MENU = "privMenu";
  /** 目录类型：组件类型权限 */
  public static final String TYPE_PRIV_COMPONENT = "privComponent";
  /** 目录类型：运行态菜单类型权限 */
  public static final String TYPE_RUN_PRIV_MENU = "runPrivMenu";
  /** 目录类型：组件类型权限 */
  public static final String TYPE_RUN_PRIV_COMPONENT = "runPrivComponent";
  /** 目录类型：应用 */
  public static final String TYPE_BOT = "bot";
  /** 目录类型：A2A */
  public static final String TYPE_A2A = "a2a";
  /** 目录类型：插件 */
  public static final String TYPE_PLUGIN = "plugin";
  /** 目录类型：评测集 */
  public static final String EVALUATION_SET = "evaluationSet";
  /** 目录类型：评估器 */
  public static final String EVALUATOR = "evaluator";
  /** 目录类型：实验 */
  public static final String EXPERIMENT = "experiment";
  /** 新增租户，需要预置的目录类型 */
  public static final List<String> CATALOG_TYPES = ImmutableList.of(TYPE_SKILL, TYPE_KNOWLEDGE, TYPE_CORPUS, TYPE_PAGE_FILE, TYPE_API_AUTH, TYPE_A2A, EVALUATION_SET, EXPERIMENT, EVALUATOR);
}
