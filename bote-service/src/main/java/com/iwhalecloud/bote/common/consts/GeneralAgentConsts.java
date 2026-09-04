package com.iwhalecloud.bote.common.consts;

import java.util.List;

/**
 * 通用智能体相关常量
 *
 * @author bianjp
 * @since 2026-04-11
 */
public final class GeneralAgentConsts {
  private GeneralAgentConsts() {
  }

  /** 任务状态: 待处理 */
  public static final String AGENT_TASK_STATUS_PENDING = "pending";
  /** 任务状态: 处理中 */
  public static final String AGENT_TASK_STATUS_PROCESSING = "in_progress";
  /** 任务状态: 已完成 */
  public static final String AGENT_TASK_STATUS_COMPLETED = "completed";
  /** 任务状态: 已删除（虚拟状态，用于更新任务工具中标记要删除） */
  public static final String AGENT_TASK_STATUS_DELETED = "deleted";
  /** 任务状态列表（不包含虚拟的已删除状态） */
  public static final List<String> AGENT_TASK_STATUS_LIST = List.of(AGENT_TASK_STATUS_PENDING, AGENT_TASK_STATUS_PROCESSING, AGENT_TASK_STATUS_COMPLETED);

  /** 子智能体 contextId 前缀 */
  public static final String SUBAGENT_CONTEXT_ID_PREFIX = "sub:";
}
