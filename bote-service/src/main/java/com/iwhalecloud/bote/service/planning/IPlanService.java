package com.iwhalecloud.bote.service.planning;

import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bote.dto.planning.PlanRequest;
import com.iwhalecloud.bote.dto.planning.PlanResponse;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 计划服务管理
 *
 * @author chen.linfa
 * @since 2025-05-14
 */
public interface IPlanService {

  /**
   * 保存计划
   *
   * @param plan 计划
   * @return 结果
   */
  PlanRecordDTO savePlan(SimplePlanDTO plan);

  /**
   * 执行计划
   *
   * @param planId 计划 ID
   * @param chatContext 会话上下文
   * @return 结果
   */
  PlanResponse startPlan(Long planId, ChatContext chatContext);

  /**
   * 保存并执行计划
   *
   * @param plan 计划 ID
   * @param chatContext 会话上下文
   * @return 结果
   */
  PlanResponse saveStartPlan(SimplePlanDTO plan, ChatContext chatContext);

  /**
   * 流转计划
   *
   * @param request 执行计划入参
   * @return 结果
   */
  ResultVO<PlanResponse> jumpPlan(PlanRequest request);

  /**
   * 中断计划
   *
   * @param request 执行计划入参
   * @return 结果
   */
  ResultVO<PlanResponse> interrupt(PlanRequest request);
}
