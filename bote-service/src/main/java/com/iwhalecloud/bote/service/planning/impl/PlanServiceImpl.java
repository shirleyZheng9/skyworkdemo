package com.iwhalecloud.bote.service.planning.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.PlanConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bote.dto.planning.PlanRequest;
import com.iwhalecloud.bote.dto.planning.PlanResponse;
import com.iwhalecloud.bote.dto.planning.PlanStepDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO.SimplePlanStepDTO;
import com.iwhalecloud.bote.mapper.planning.PlanManageMapper;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.planning.IPlanService;
import com.iwhalecloud.bote.service.planning.hepler.PlanCreator;
import com.iwhalecloud.bote.service.planning.hepler.PlanExecutor;
import com.iwhalecloud.bote.service.planning.hepler.PlanRecorder;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 计划服务管理实现
 *
 * @author chen.linfa
 * @since 2025-05-14
 */
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements IPlanService {

  // @formatter:off
  private final PlanCreator planCreator;
  private final PlanExecutor planExecutor;
  private final PlanRecorder planRecorder;
  private final PlanManageMapper planManageMapper;
  private final SceneCache sceneCache;
  // @formatter:on

  @Transactional
  @Override
  public PlanRecordDTO savePlan(SimplePlanDTO plan) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Long tenantId = plan.getTenantId();
    PlanRecordDTO record = new PlanRecordDTO();
    record.setPlanId(plan.getPlanId());
    record.setPlanTitle("");
    record.setUserRequest(plan.getUserMessage());
    record.setBotId(plan.getBotId());
    record.setTenantId(tenantId);
    record.setStatus(PlanConsts.STATUS_RUNNING);
    record.setStartTime(new Date());
    record.setCreatorId(userId);
    record.setUpdatorId(userId);
    record.setStatusCd(BaseConsts.STATUS_CD_VALID);

    List<PlanStepDTO> steps = new ArrayList<>();
    record.setSteps(steps);
    for (int i = 0; i < plan.getSteps().size(); i++) {
      SimplePlanStepDTO step = plan.getSteps().get(i);
      Long stepId = StringUtils.isNumeric(step.getStepId()) ? Long.parseLong(step.getStepId()) : Sequences.PLAN_RECORD_STEP_ID.next();
      step.setStepId(stepId.toString());

      PlanStepDTO dto = new PlanStepDTO();
      dto.setStepId(stepId);
      dto.setPlanId(record.getPlanId());
      dto.setAgentId(step.getAgentId());
      dto.setAgentName(step.getAgentName());
      dto.setAgentType(PlanConsts.AGENT_TYPE_REACT);
      dto.setIsAutoRun(BaseConsts.FALSE);
      dto.setAgentRequest(step.getAgentRequest());
      dto.setAgentParam(MapUtils.isEmpty(step.getAgentParam()) ? null : JsonUtil.toJsonString(step.getAgentParam()));
      dto.setStepStatus(PlanConsts.STATUS_NOT_STARTED);
      dto.setStepIndex(i + 1);
      dto.setTenantId(tenantId);
      dto.setBotId(plan.getBotId());
      dto.setSessionId(plan.getSessionId());
      dto.setCreatorId(userId);
      dto.setUpdatorId(userId);
      dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
      // 补充步骤信息
      fillFlowStepInfo(dto);
      steps.add(dto);
    }
    record.setSceneDsl(planCreator.generateDsl(plan));

    planManageMapper.insertPlanRecord(record);
    planManageMapper.batchInsertPlanStep(steps);
    return record;
  }

  @Override
  public PlanResponse startPlan(Long planId, ChatContext chatContext) {
    PlanRequest request = new PlanRequest();
    request.setPlanId(planId);
    request.setTenantId(chatContext.getTenantId());
    request.setChatContext(chatContext);
    return planExecutor.run(request);
  }

  @Override
  public PlanResponse saveStartPlan(SimplePlanDTO plan, ChatContext chatContext) {
    // 单独事务保存计划
    //noinspection SpringTransactionalMethodCallsInspection
    TransactionUtil.executeNew(() -> savePlan(plan));
    return startPlan(plan.getPlanId(), chatContext);
  }

  @Override
  public ResultVO<PlanResponse> jumpPlan(PlanRequest request) {
    Assert.notNull(request.getTenantId(), "租户 ID 不能为空");
    Assert.notNull(request.getPlanId(), "计划 ID 不能为空");
    PlanRecordDTO record = planManageMapper.getPlanRecord(request.getTenantId(), request.getPlanId());
    if (PlanConsts.STATUS_RUNNING != record.getStatus()) {
      // 异常情况，退出流程
      return ResultVO.success();
    }
    if (StringUtils.isNotEmpty(request.getCompletedContextId())) {
      PlanStepDTO step = planManageMapper.getPlanStepByContextId(request.getTenantId(), request.getPlanId(), request.getCompletedContextId());
      if (step != null) {
        request.setCompletedStepId(step.getStepId());
        request.setCompletedStepIndex(step.getStepIndex());
      }
    }
    Assert.notNull(request.getCompletedStepId(), "标记已完成的步骤 ID 不能为空");
    return ResultVO.success(planExecutor.run(request));
  }

  @Transactional
  @Override
  public ResultVO<PlanResponse> interrupt(PlanRequest request) {
    PlanRecordDTO record = planManageMapper.getPlanRecord(request.getTenantId(), request.getPlanId());
    if (record != null) {
      record.setSteps(planManageMapper.selectPlanStepList(request.getTenantId(), request.getPlanId()));
      for (PlanStepDTO step : record.getSteps()) {
        if (StringUtils.isNotEmpty(step.getFlowStepJson())) {
          step.setFlowSteps(JsonUtil.parseJson(step.getFlowStepJson(), new TypeReference<List<SimpleFlowStepDTO>>() {
          }));
        }
      }
      record.setFailReason("主动中断");
      record.setStatus(PlanConsts.STATUS_FAILED);
      record.setEndTime(new Date());
      record.setSpentTime((int) (record.getEndTime().getTime() - record.getStartTime().getTime()));
      planRecorder.updateRecord(record, request.getChatContext(), null);
    }
    else {
      record = new PlanRecordDTO();
      record.setPlanId(request.getPlanId());
      record.setStatus(PlanConsts.STATUS_FAILED);
    }
    request.getChatContext().sendMessage(ChatMessageType.UPDATE_PLAN_STATE, record.toMap());
    request.getChatContext().sendDoneMessage();
    return ResultVO.success();
  }

  private void fillFlowStepInfo(PlanStepDTO step) {
    List<SimpleFlowStepDTO> flowSteps = step.getFlowSteps();
    if (CollectionUtils.isEmpty(step.getFlowSteps())) {
      flowSteps = sceneCache.getFlowSteps(step.getTenantId(), step.getAgentId());
    }
    if (CollectionUtils.isNotEmpty(flowSteps)) {
      for (SimpleFlowStepDTO flow : flowSteps) {
        flow.setStepStatus(PlanConsts.STATUS_NOT_STARTED);
        for (SimpleFlowStepDTO child : CollectionUtils.emptyIfNull(flow.getChildren())) {
          child.setStepStatus(PlanConsts.STATUS_NOT_STARTED);
        }
      }
      step.setFlowStepJson(JsonUtil.toJsonStringCompact(flowSteps));
      step.setFlowSteps(flowSteps);
    }
  }
}
