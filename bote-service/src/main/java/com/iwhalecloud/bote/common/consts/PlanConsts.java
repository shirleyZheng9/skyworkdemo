package com.iwhalecloud.bote.common.consts;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * 计划相关常量
 *
 * @author chen.linfa
 * @since 2025-05-20
 */
public final class PlanConsts {
  private PlanConsts() {
  }

  /** 规划智能体类型 - 业务 */
  public static final String AGENT_TYPE_REACT = "react";
  /** 执行状态: 未开始 */
  public static final int STATUS_NOT_STARTED = 0;
  /** 执行状态: 运行中 */
  public static final int STATUS_RUNNING = 1;
  /** 执行状态: 成功 */
  public static final int STATUS_SUCCESS = 10;
  /** 执行状态: 失败 */
  public static final int STATUS_FAILED = -1;

  /** 规划智能体 - 计划 ID KEY */
  public static final String AGENT_PLAN_ID = "planId";
  /** 规划智能体 - 用户消息 KEY */
  public static final String AGENT_PLAN_MESSAGE_KEY = "userMessage";
  /** 规划智能体 - 步骤列表 KEY */
  public static final String AGENT_PLAN_STEP_KEY = "steps";


  public static final List<Integer> STATUS_RUN = ImmutableList.of(STATUS_NOT_STARTED, STATUS_RUNNING);
  public static final List<Integer> STATUS_FINISH = ImmutableList.of(STATUS_SUCCESS, STATUS_FAILED);
}
