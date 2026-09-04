package com.iwhalecloud.bote.service.planning.hepler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.PlanContextCache;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.PlanConsts;
import com.iwhalecloud.bote.common.util.SceneDslUtil;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bote.dto.planning.PlanRequest;
import com.iwhalecloud.bote.dto.planning.PlanResponse;
import com.iwhalecloud.bote.dto.planning.PlanStepDTO;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.mapper.planning.PlanManageMapper;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.ChatReplyHandler;
import com.iwhalecloud.bote.service.scene.ISceneChatService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 负责执行计划
 *
 * @author chen.linfa
 * @since 2025-05-14
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class PlanExecutor {

  private static final Logger logger = LoggerFactory.getLogger(PlanExecutor.class);

  private final PlanManageMapper planManageMapper;
  private final PlanRecorder planRecorder;
  private final PlanContextCache planContextCache;
  private final ISceneChatService sceneChatService;

  public PlanResponse run(PlanRequest request) {
    Date startTime = new Date();
    // 初始化
    PlanResponse response = initialize(request, startTime);
    if (response != null) {
      return response;
    }

    // 执行服务
    OrchestrationEngineResponse result = doRun(request, startTime);
    if (Boolean.TRUE.equals(result.getSuccess())) {
      response = PlanResponse.success(startTime);
    }
    else {
      response = PlanResponse.fail(startTime, result.getFailMsg());
    }
    return response;
  }

  @Nullable
  private PlanResponse initialize(PlanRequest request, Date startTime) {
    PlanResponse response = null;
    try {
      // 获取计划定义
      PlanRecordDTO record = planManageMapper.getPlanRecord(request.getTenantId(), request.getPlanId());
      Assert.notNull(record, "查询不到有效的计划定义");
      Assert.isTrue(!PlanConsts.STATUS_FINISH.contains(record.getStatus()), "计划已执行完毕，无需重复操作");

      List<PlanStepDTO> steps = planManageMapper.selectPlanStepList(request.getTenantId(), request.getPlanId());
      record.setSteps(steps);
      Assert.notEmpty(record.getSteps(), "查询不到有效的计划步骤定义");
      boolean exists = IterableUtils.matchesAny(steps, p -> PlanConsts.STATUS_RUN.contains(p.getStepStatus()));
      Assert.isTrue(exists, "查询不到待处理的计划步骤定义");
      for (PlanStepDTO step : record.getSteps()) {
        if (StringUtils.isNotEmpty(step.getFlowStepJson())) {
          step.setFlowSteps(JsonUtil.parseJson(step.getFlowStepJson(), new TypeReference<List<SimpleFlowStepDTO>>() {
          }));
        }
      }

      Assert.hasText(record.getSceneDsl(), "编排 DSL 不能为空");
      SceneDslDTO dsl = SceneDslUtil.parse(record.getSceneDsl());
      dsl.setId(record.getPlanId());
      dsl.setCode(record.getPlanTitle());
      dsl.setName(record.getPlanTitle());
      dsl.setChatflow(true);
      record.setDsl(dsl);
      request.setRecord(record);
      planContextCache.put(record.getPlanId(), record);
    }
    catch (Exception e) {
      response = PlanResponse.fail(startTime, e.getMessage());
      logger.error("Failed to execute plan. planId={}", request.getPlanId(), e);
    }
    return response;
  }

  private OrchestrationEngineResponse doRun(PlanRequest planRequest, Date startTime) {
    PlanRecordDTO record = planRequest.getRecord();
    try {
      SceneChatParamsDTO sceneChatParams = buildSceneChatParams(planRequest);
      OrchestrationEngineResponse response = sceneChatService.run(sceneChatParams);
      if (!Boolean.TRUE.equals(response.getSuccess())) {
        sendMessage(response, planRequest.getChatContext());
        // 失败情况下，才需要更新计划状态
        record.setStatus(PlanConsts.STATUS_FAILED);
        record.setFailReason(response.getFailMsg());
        planRecorder.updateRecord(record, planRequest.getChatContext(), null);
      }
      else if (Boolean.TRUE.equals(response.getSceneFinished())) {
        record.setStatus(PlanConsts.STATUS_SUCCESS);
        planRecorder.updateRecord(record, planRequest.getChatContext(), null);
      }
      else {
        planRequest.getChatContext().sendDoneMessage();
      }
      return response;
    }
    catch (Exception e) {
      logger.error("Failed to execute plan. planId={}", record.getPlanId(), e);
      OrchestrationEngineResponse response = OrchestrationEngineResponse.fail(startTime, e);
      sendMessage(response, planRequest.getChatContext());
      record.setStatus(PlanConsts.STATUS_FAILED);
      record.setFailReason(e.getMessage());
      planRecorder.updateRecord(record, planRequest.getChatContext(), null);
      return response;
    }
  }

  private void sendMessage(OrchestrationEngineResponse response, ChatContext chatContext) {
    chatContext.sendMessage(ChatMessageType.TEXT, response.getFailMsg());
    String stackTrace;
    if (StringUtils.isNotEmpty(response.getFailStack())) {
      stackTrace = response.getFailMsg() + "\n" + response.getFailStack();
    }
    else if (response.getException() != null) {
      stackTrace = response.getFailMsg() + "\n" + ExceptionUtils.getStackTrace(response.getException());
    }
    else {
      stackTrace = response.getFailMsg();
    }
    chatContext.sendMessage(ChatMessageType.EXCEPTION, stackTrace);
  }

  /**
   * 构造场景会话请求场景
   */
  @SuppressWarnings("unchecked")
  private SceneChatParamsDTO buildSceneChatParams(PlanRequest request) {
    ChatContext context = request.getChatContext();
    PlanRecordDTO record = request.getRecord();
    SceneChatParamsDTO sceneChatParams = new SceneChatParamsDTO();
    sceneChatParams.setTenantId(request.getTenantId());
    sceneChatParams.setSceneId(request.getPlanId());
    sceneChatParams.setConversationId(context.getSessionId());
    sceneChatParams.setTransactionId(context.getTransactionId());
    sceneChatParams.setContextId(request.getPlanId().toString());
    // 组装入参
    if (StringUtils.isNotEmpty(record.getUserRequest())) {
      Map<String, Object> userMessage = JsonUtil.parseJson(record.getUserRequest(), new TypeReference<Map<String, Object>>() {
      });
      if (MapUtils.isNotEmpty(userMessage)) {
        sceneChatParams.setMessageContent(MapUtils.getString(userMessage, "content"));
        sceneChatParams.setParams(userMessage.containsKey("params") ? (Map<String, Object>) userMessage.get("params") : null);
      }
    }
    sceneChatParams.setContextParams(context.getRequest().getContextParams());
    sceneChatParams.setReplyHandler(new ChatReplyHandler(context));
    sceneChatParams.setPlanId(request.getPlanId());
    sceneChatParams.setDynamicDsl(record.getDsl());
    sceneChatParams.setLogEnabled(false);
    return sceneChatParams;
  }
}
