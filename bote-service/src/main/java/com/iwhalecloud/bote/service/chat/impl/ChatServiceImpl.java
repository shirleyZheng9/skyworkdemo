package com.iwhalecloud.bote.service.chat.impl;

import com.iwhalecloud.bote.agent.agents.GeneralAgent;
import com.iwhalecloud.bote.agent.event.handlers.SseChatAgentEventHandler;
import com.iwhalecloud.bote.cache.GeneraAgentIdCache;
import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.cache.SseEmitterCache;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.exception.SecurityFenceException;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestMessageDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.chat.IntentResultDTO;
import com.iwhalecloud.bote.dto.chat.SceneIntentResultDTO;
import com.iwhalecloud.bote.dto.chat.SearchResultDTO;
import com.iwhalecloud.bote.dto.chat.query.SearchQueryParams;
import com.iwhalecloud.bote.dto.planning.PlanRequest;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO;
import com.iwhalecloud.bote.dto.planning.query.PlanParams;
import com.iwhalecloud.bote.service.chat.IChatService;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.chat.helper.CallModelHelper;
import com.iwhalecloud.bote.service.chat.helper.CallSceneHelper;
import com.iwhalecloud.bote.service.chat.helper.IntentRecognizeHelper;
import com.iwhalecloud.bote.service.chat.search.IChatSearchProvider;
import com.iwhalecloud.bote.service.planning.IPlanService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 对话服务
 *
 * @author Admin
 */
@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements IChatService {
  private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

  private final CallModelHelper callModelHelper;
  private final IPlanService planService;
  private final IntentRecognizeHelper intentRecognize;
  private final CallSceneHelper callScene;
  private final SceneCache sceneCache;
  private final SseEmitterCache sseEmitterCache;
  private final IChatSearchProvider chatSearchProvider;
  private final GeneraAgentIdCache generaAgentIdCache;

  @Override
  public void completions(ChatRequestDTO request, SseEmitter sseEmitter) {
    ChatContext context = new ChatContext(request, sseEmitter, SessionUtil.getOptionalUserId());
    // 通用智能体或自定义BoteClaw
    if (generaAgentIdCache.isBoteClaw(request.getBotId())) {
      try {
        GeneralAgent generalAgent = new GeneralAgent(context, new SseChatAgentEventHandler(context));
        generalAgent.execute();
      }
      finally {
        context.sendDoneMessage();
      }
      return;
    }

    if (request.getPlanParams() != null) {
      // 保存并执行计划
      if (request.getPlanParams().processPlan()) {
        processPlan(context, request.getPlanParams());
        return;
      }
      if (request.getPlanParams().recognizePlan()) {
        // 生成计划
        analyzePlan(context);
        return;
      }
    }

    // 分析意图
    Pair<IntentResultDTO, Boolean> result = analyzeIntent(context);
    if (BooleanUtils.isTrue(result.getRight())) {
      return;
    }

    // 执行意图
    try {
      processIntent(context, result.getLeft(), request.getMessage());
    }
    catch (Exception e) {
      handleException(request, e, context);
    }
  }

  @Override
  public ResultVO<SearchResultDTO> search(SearchQueryParams params) {
    return chatSearchProvider.search(params);
  }

  /**
   * 分析用户意图
   */
  private Pair<IntentResultDTO, Boolean> analyzeIntent(ChatContext context) {
    ChatRequestDTO request = context.getRequest();
    ChatTraceLogBuilder log = context.newLog("智能体识别").input(request);
    IntentResultDTO result = null;
    try {
      result = intentRecognize.recognizeAgent(context, log);
      log.output(result);
      log.end();

      // 场景 ID
      Long sceneId = null;
      if (result.getEnterScene() != null) {
        if (result.getEnterScene().getPlan() != null) {
          context.sendMessage(ChatMessageType.TEXT, SystemParameter.START_PLAN_TIP.getValueFromDb());
          // 输出计划内容，通知前端进行确认
          context.sendMessage(ChatMessageType.CONFIRM_PLAN, result.getEnterScene().wrapPlan());
          context.sendDoneMessage();
          return Pair.of(result, true);
        }
        sceneId = result.getEnterScene().getSceneId();
        context.setContextId(result.getEnterScene().getContextId());
        context.setSceneName(result.getEnterScene().getSceneName());
      }
      if (sceneId == null && request.getSceneId() != null) {
        sceneId = request.getSceneId();
      }
      context.setSceneId(sceneId);
    }
    catch (Exception e) {
      log.failed().addLog(ExpUtil.getMsg(e)).addLog(ExceptionUtils.getStackTrace(e));
      handleException(request, e, context);
      return Pair.of(result, true);
    }
    return Pair.of(result, false);
  }

  /**
   * 通过规划智能体，生成计划
   * <p>特殊场景，满足 BSS 盒子业务</p>
   */
  private void analyzePlan(ChatContext context) {
    ChatTraceLogBuilder log = context.newLog("智能体识别").input(context.getRequest());
    IntentResultDTO result;
    try {
      result = intentRecognize.recognizePlan(context, log);
      log.end();
    }
    catch (Exception e) {
      log.failed().addLog(ExpUtil.getMsg(e)).addLog(ExceptionUtils.getStackTrace(e));
      handleException(context.getRequest(), e, context);
      return;
    }
    if (result.getEnterScene() != null && result.getEnterScene().getPlan() != null) {
      context.sendMessage(ChatMessageType.TEXT, SystemParameter.START_PLAN_TIP.getValueFromDb());
      // 输出计划内容，通知前端进行确认
      SimplePlanDTO plan = result.getEnterScene().wrapPlan();
      plan.setIsAuto(BooleanUtils.isTrue(context.getRequest().getPlanParams().getIsAuto()));
      context.sendMessage(ChatMessageType.CONFIRM_PLAN, plan);
      context.sendDoneMessage();
    }
    else {
      context.sendMessage(ChatMessageType.TEXT, SystemParameter.SELECT_SCENE_TIP.getValueFromDb());
      context.sendDoneMessage();
    }
  }

  /**
   * 处理会话异常
   */
  private void handleException(ChatRequestDTO request, Exception e, ChatContext context) {
    // 用户主动取消时不需要发送错误信息
    if (sseEmitterCache.isCancelled(request.getClientId())) {
      context.addErrorMessage("用户取消");
      context.complete();
      return;
    }
    if (e instanceof SecurityFenceException) {
      context.sendMessage(ChatMessageType.TEXT, ExpUtil.getMsg(e));
      context.sendDoneMessage();
      return;
    }
    logger.error("Failed to process chat: request={}", request, e);
    context.sendMessage(ChatMessageType.ERROR, ExpUtil.getMsg(e));
    // 异常堆栈数据，作为相关数据进行处理
    context.sendMessage(ChatMessageType.EXCEPTION, ExceptionUtils.getStackTrace(e));
    context.sendDoneMessage();
  }

  /**
   * 执行意图
   */
  private void processIntent(ChatContext context, IntentResultDTO result, ChatRequestMessageDTO userMessage) {
    // 进入场景
    if (result.getEnterScene() != null) {
      SceneIntentResultDTO enter = result.getEnterScene();
      // 确认启动智能体
      if (BooleanUtils.isTrue(enter.getHit())) {
        Map<String, Object> params = new HashMap<>();
        params.put("userMessage", userMessage.getContent());
        params.put("tip", String.format(SystemParameter.CONFIRM_SCENE_TIP.getValueFromDb(), enter.getSceneName()));
        params.put("sceneId", enter.getSceneId());
        params.put("sceneName", enter.getSceneName());
        context.sendMessage(ChatMessageType.CONFIRM_SCENE, params);
        context.sendDoneMessage();
        return;
      }

      // 发送上下文和场景信息
      context.sendSceneInfoMessage();
      if (userMessage.isEmpty()) {
        SimpleBotSceneDTO scene = sceneCache.getScene(context.getTenantId(), enter.getSceneId());
        if (BooleanUtils.isNotTrue(scene.getAutoStartEnabled())) {
          if (StringUtils.isNotEmpty(scene.getPrologue())) {
            // 回复场景开场白
            context.sendMessage(ChatMessageType.TEXT, scene.getPrologue());
            context.sendDoneMessage();
          }
          else {
            // 避免报错，直接回复
            context.sendReadyMessage();
          }
          return;
        }
      }
      // 调用场景
      callScene.invoke(context);
      return;
    }

    // 退出场景
    if (result.getExitScene() != null) {
      context.sendExitSceneMessage();
      context.sendDoneMessage();
      return;
    }

    // 识别不到意图时，进行大模型对话
    processModel(context);
  }

  /**
   * 执行大模型对话
   */
  private void processModel(ChatContext context) {
    boolean firstEnter = StringUtils.isEmpty(context.getContextId());
    if (firstEnter) {
      String contextId = SceneContextUtil.newContextId();
      context.setContextId(contextId);
      context.getRequest().setContextId(contextId);
      // 设置当前对话的上下文
      SseUtil.sendText(context.getEmitter(), ChatMessageType.CONTEXT_ID, context.getContextId());
    }
    ChatTraceLogBuilder log = context.newLog("大模型对话");
    try {
      callModelHelper.invoke(context, log, firstEnter);
    }
    catch (Exception e) {
      log.failed().addLog(ExpUtil.getMsg(e)).addLog(ExceptionUtils.getStackTrace(e));
      throw e;
    }
  }

  /**
   * 执行计划
   */
  private void processPlan(ChatContext context, PlanParams params) {
    if (BooleanUtils.isTrue(params.getInterrupted())) {
      // 中断计划
      PlanRequest request = new PlanRequest();
      request.setTenantId(context.getTenantId());
      request.setPlanId(params.getPlanId());
      request.setInterrupted(params.getInterrupted());
      request.setChatContext(context);
      planService.interrupt(request);
    }
    else if (CollectionUtils.isNotEmpty(params.getSteps())) {
      SimplePlanDTO plan = new SimplePlanDTO();
      plan.setTenantId(context.getTenantId());
      plan.setBotId(context.getBotId());
      plan.setPlanId(params.getPlanId());
      plan.setSessionId(context.getSessionId());
      Map<String, Object> message = new HashMap<>();
      if (StringUtils.isNotEmpty(plan.getUserMessage())) {
        message.put("content", plan.getUserMessage());
      }
      if (MapUtils.isNotEmpty(context.getUserMessage().getParams())) {
        message.put("params", context.getUserMessage().getParams());
      }
      if (MapUtils.isNotEmpty(message)) {
        plan.setUserMessage(JsonUtil.toJsonString(message));
      }
      plan.setSteps(params.getSteps());
      planService.saveStartPlan(plan, context);
    }
  }
}
