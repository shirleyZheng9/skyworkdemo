package com.iwhalecloud.bote.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 操作日志枚举类
 *
 * @author chen.linfa
 * @since 2024-10-25
 */
@Getter
@RequiredArgsConstructor
public enum OperClassEnum {

  /** 技能：API */
  SKILL_API,
  /** 技能：工作流 */
  SKILL_FLOW,
  /** 技能：页面 */
  SKILL_PAGE,
  /** 技能：服务函数 */
  SKILL_FUNCTION,
  /** 技能：数据 */
  SKILL_ATTR,
  /** 技能：插件 */
  SKILL_PLUGIN,
  /** 技能：页面函数 */
  SKILL_PAGE_FUNC,
  /** 技能：SQL */
  SKILL_SQL,
  /** 技能：对象 */
  SKILL_OBJECT,
  /** A2A 服务 */
  A2A_AGENT,
  /** A2A 平台 */
  A2A_PLATFORM,
  /** Agent Skill */
  AGENT_SKILL,

  /** 提示词 */
  PROMPT,
  /** 副驾指令 */
  COPILOT_POINT,

  /** 机器人 */
  BOT,
  /** 场景 */
  SCENE,

  /** 知识库 */
  KNOWLEDGE,
  /** 文档库文档 */
  DOCUMENT,
  /** 文档库 */
  LIBRARY,
  /** 语料 */
  CORPUS,

  /** 聊天对话主题 */
  CHAT_THEME,
  /** 回复消息主题 */
  CHAT_REPLY_THEME,

  /** 网页应用 */
  WEB_APP,
  /** 工作台应用 */
  WORKBENCH_APP,

  /** 业务数据表 */
  DATA_TABLE,

  /** 定时任务 */
  JOB,

  /** 不要使用（以上节点编码请按字典排序） */
  DO_NOT_USE
}
