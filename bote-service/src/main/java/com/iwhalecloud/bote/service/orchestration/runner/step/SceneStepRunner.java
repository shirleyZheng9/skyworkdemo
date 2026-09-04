package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.cache.ChatflowContextCache;
import com.iwhalecloud.bote.cache.PlanContextCache;
import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.PlanConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TemplateUtil;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.chat.SceneProcessDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.SceneStep;
import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bote.dto.planning.PlanStepDTO;
import com.iwhalecloud.bote.dto.scene.FlowSceneProcessDTO;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.mapper.chat.SceneProcessMapper;
import com.iwhalecloud.bote.mapper.scene.SceneChatMessageMapper;
import com.iwhalecloud.bote.service.a2a.helper.A2aAgentEngine;
import com.iwhalecloud.bote.service.orchestration.IOrchestrationEngine;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.ChatReplyHandler;
import com.iwhalecloud.bote.service.planning.hepler.PlanRecorder;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 智能体 步骤执行器
 *
 * @author chen.linfa
 * @since 2025-07-01
 */
public class SceneStepRunner extends AbstractLlmStepRunner<SceneStep> {

  private static final SceneProcessMapper sceneProcessMapper = SpringUtil.getBean(SceneProcessMapper.class);
  private static final SceneChatMessageMapper sceneChatMessageMapper = SpringUtil.getBean(SceneChatMessageMapper.class);
  private static final PlanRecorder planRecorder = SpringUtil.getBean(PlanRecorder.class);
  private static final IOrchestrationEngine engine = SpringUtil.getBean(IOrchestrationEngine.class);
  private static final A2aAgentEngine a2aAgentEngine = SpringUtil.getBean(A2aAgentEngine.class);
  private static final ChatflowContextCache chatflowContextCache = SpringUtil.getBean(ChatflowContextCache.class);
  private static final SceneCache sceneCache = SpringUtil.getBean(SceneCache.class);
  private static final PlanContextCache planContextCache = SpringUtil.getBean(PlanContextCache.class);

  @Override
  protected void doRun(SceneOrchestrationContext orchestrationContext, SceneStep step) {
    OrchestrationEngineRequest request = orchestrationContext.getRequest();
    SimpleBotSceneDTO scene = sceneCache.getScene(request.getTenantId(), step.getSceneId());
    Assert.isTrue(!SceneConsts.SCENE_TYPE_SCENE.equals(scene.getSceneType()), () -> "多智能体调度不支持自主规划模式的智能体: " + scene.getSceneName());
    Assert.isTrue(!SceneConsts.SCENE_TYPE_KNOWLEDGE.equals(scene.getSceneType()), () -> "多智能体调度不支持知识问答模式的智能体: " + scene.getSceneName());
    step.setMessageContent(TemplateUtil.resolveTemplate(step.getMessageContent(), this::resolveTemplateParam));
    if (StringUtils.isEmpty(request.getCompletedNodeCode())) {
      String currentContextId = SceneContextUtil.newContextId();
      // 记录当前智能体进度
      recordProcess(request, step, scene, currentContextId);
      // 发送上下文和场景信息
      sendSceneInfoMessage(request, scene.getSceneId(), scene.getSceneName(), currentContextId);

      // 初始化当前智能体的缓存
      Map<String, Object> contextParams = MapUtils.isEmpty(step.getContextParams()) ? new HashMap<>() : step.getContextParams();
      Map<String, Object> cacheContent = chatflowContextCache.get(request.getSceneId(), null, request.getContextId());
      if (MapUtils.isNotEmpty(cacheContent)) {
        // 主流程的上下文，全量透传到当前智能体
        contextParams.putAll(cacheContent);
      }
      if (MapUtils.isNotEmpty(request.getContextParams())) {
        contextParams.putAll(request.getContextParams());
      }
      if (request.getPlanId() != null) {
        contextParams.put("planId", request.getPlanId());
      }
      chatflowContextCache.put(false, scene.getSceneId(), null, currentContextId, contextParams);

      // 执行当前智能体
      SceneChatParamsDTO sceneChatParams = new SceneChatParamsDTO(request);
      sceneChatParams.setSceneId(scene.getSceneId());
      sceneChatParams.setMessageContent(step.getMessageContent());
      sceneChatParams.setContextParams(contextParams);
      sceneChatParams.setContextId(currentContextId);
      sceneChatParams.setReplyHandler(request.getReplyHandler());
      Optional<OrchestrationStepRunLog> logOptional = orchestrationContext.getLastStepRunLogOptional();
      logOptional.ifPresent(l -> {
        Map<String, Object> input = new HashMap<>();
        input.put("messageContent", step.getMessageContent());
        input.put("params", step.getParams());
        input.put("contextParams", contextParams);
        l.setInput(input);
      });
      OrchestrationEngineResponse response = invokeScene(step, sceneChatParams, scene);
      logOptional.ifPresent(log -> log.setInnerServiceLogs(response.getStepLogs()));
      if (Boolean.TRUE.equals(response.getSceneFinished())) {
        // 标记当前智能体已执行成功
        setCacheContent(request.getSceneId(), request.getContextId(), scene.getSceneId(), currentContextId);
        finishProcess(request, step);
      }
      else {
        // 停留在当前智能体
        orchestrationContext.setReturned();
      }
    }
    else {
      // 标记当前智能体已执行成功
      setCacheContent(request.getSceneId(), request.getContextId(), step.getSceneId(), request.getCompletedContextId());
      updateProcess(request, step, request.getCompletedContextId());
      // 清理中间变量
      request.setCompletedNodeCode(null);
      request.setCompletedContextId(null);
    }
  }

  /**
   * 调用智能体
   */
  private OrchestrationEngineResponse invokeScene(SceneStep step, SceneChatParamsDTO sceneChatParams, SimpleBotSceneDTO scene) {
    if (SceneConsts.SCENE_TYPE_CHATFLOW.equals(scene.getSceneType())) {
      OrchestrationEngineRequest engineRequest = new OrchestrationEngineRequest(sceneChatParams, scene.getSceneId(), null, step.getParams());
      return engine.run(engineRequest);
    }
    else if (SceneConsts.SCENE_TYPE_A2A.equals(scene.getSceneType())) {
      Date startTime = new Date();
      try {
        return a2aAgentEngine.execute(sceneChatParams);
      }
      catch (Exception e) {
        return OrchestrationEngineResponse.fail(startTime, e);
      }
    }
    else {
      throw new BssException("多智能体调度不支持 " + scene.getSceneType() + " 类型的智能体: " + scene.getSceneName());
    }
  }

  /**
   * 新增当前智能体进度
   */
  private void recordProcess(OrchestrationEngineRequest request, SceneStep step, SimpleBotSceneDTO scene, String currentContextId) {
    // 记录流程进度
    FlowSceneProcessDTO flowLog = new FlowSceneProcessDTO();
    flowLog.setId(Sequences.FLOW_SCENE_PROCESS_ID.next());
    flowLog.setTenantId(request.getTenantId());
    flowLog.setSceneId(scene.getSceneId());
    flowLog.setContextId(currentContextId);
    flowLog.setNodeCode(step.getCode());
    flowLog.setFlowStatus(ChatConsts.CHAT_SCENE_STATUS_RUNNING);
    flowLog.setMainSceneId(request.getSceneId());
    flowLog.setMainContextId(request.getContextId());
    flowLog.setMainConversationId(request.getConversationId());
    flowLog.setStatusCd(BaseConsts.STATUS_CD_VALID);
    flowLog.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    TransactionUtil.executeNew(() -> sceneChatMessageMapper.insertProcess(flowLog));

    if (request.getReplyHandler() instanceof ChatReplyHandler) {
      // 记录会话进度
      SceneProcessDTO chatLog = new SceneProcessDTO();
      chatLog.setId(Sequences.CHAT_SCENE_PROCESS_ID.next());
      chatLog.setTenantId(request.getTenantId());
      chatLog.setBotId(request.getBotId());
      chatLog.setSessionId(request.getConversationId());
      chatLog.setSceneId(scene.getSceneId());
      chatLog.setSceneName(scene.getSceneName());
      chatLog.setContextId(currentContextId);
      chatLog.setChatStatus(ChatConsts.CHAT_SCENE_STATUS_RUNNING);
      chatLog.setCreatorId(SessionUtil.getLoginInfo().getUserId());
      chatLog.setStatusCd(BaseConsts.STATUS_CD_VALID);
      TransactionUtil.executeNew(() -> sceneProcessMapper.insertSceneProcess(chatLog));
    }

    if (request.getPlanId() != null) {
      // 更新计划进度
      PlanRecordDTO record = planContextCache.get(request.getPlanId());
      Assert.notNull(record, "执行记录不存在");
      PlanStepDTO planStep = IterableUtils.find(record.getSteps(),
        p -> Objects.equals(p.getAgentId(), step.getSceneId()) && Objects.equals(p.getStepId(), step.getStepId()));
      planStep.setContextId(currentContextId);
      planStep.setStepStatus(PlanConsts.STATUS_RUNNING);
      planRecorder.updateStep(record, planStep, request.getReplyHandler());
    }
  }

  /**
   * 更新当前智能体进度
   */
  private void updateProcess(OrchestrationEngineRequest request, SceneStep step, String contextId) {
    Long sceneId = step.getSceneId();
    TransactionUtil.executeNew(() -> {
      sceneChatMessageMapper.updateProcess(sceneId, contextId, ChatConsts.CHAT_SCENE_STATUS_FINISH);
      if (request.getReplyHandler() instanceof ChatReplyHandler) {
        sceneProcessMapper.updateSceneProcess(sceneId, contextId, ChatConsts.CHAT_SCENE_STATUS_FINISH);
      }
    });

    if (request.getPlanId() != null) {
      // 更新计划进度
      PlanRecordDTO record = planContextCache.get(request.getPlanId());
      Assert.notNull(record, "执行记录不存在");
      PlanStepDTO planStep = IterableUtils.find(record.getSteps(),
        p -> Objects.equals(p.getAgentId(), sceneId) && Objects.equals(p.getStepId(), step.getStepId()));

      planStep.setStepStatus(PlanConsts.STATUS_SUCCESS);
      for (SimpleFlowStepDTO flowStep : CollectionUtils.emptyIfNull(planStep.getFlowSteps())) {
        flowStep.setStepStatus(PlanConsts.STATUS_SUCCESS);
      }
      planRecorder.updateStep(record, planStep, request.getReplyHandler());
    }
  }

  /**
   * 发送上下文和智能体信息
   */
  private void sendSceneInfoMessage(OrchestrationEngineRequest request, @Nullable Long sceneId, @Nullable String sceneName, String contextId) {
    if (sceneId == null) {
      // 计划模式，主流程没有智能体信息，无需发送消息
      return;
    }
    if (StringUtils.isEmpty(sceneName)) {
      sceneName = sceneCache.getSceneName(request.getTenantId(), sceneId);
    }
    request.getReplyHandler().pushMessage(ChatMessageType.CONTEXT_ID, contextId);
    Map<String, Object> sceneInfo = new HashMap<>();
    sceneInfo.put("sceneId", sceneId);
    sceneInfo.put("sceneName", sceneName);
    request.getReplyHandler().pushMessage(ChatMessageType.SCENE, sceneInfo);
  }

  /**
   * 标记当前智能体进度已完成
   */
  private void finishProcess(OrchestrationEngineRequest request, SceneStep step) {
    // 更新当前智能体执行状态
    updateProcess(request, step, request.getCompletedContextId());
    // 发送退出当前智能体消息
    request.getReplyHandler().pushMessage(ChatMessageType.EXIT_SCENE, ChatConsts.COMPLETIONS_EXIT);
    // 重新进入主智能体
    sendSceneInfoMessage(request, request.getSceneId(), null, request.getContextId());
  }

  /**
   * 当前智能体支持结束，将智能体变量赋值到主流程，方便在多智能间传输
   */
  private void setCacheContent(Long mainSceneId, String mainContextId, Long sceneId, String contextId) {
    Map<String, Object> content = chatflowContextCache.get(sceneId, null, contextId);
    if (MapUtils.isEmpty(content)) {
      return;
    }
    Map<String, Object> mainContent = chatflowContextCache.get(mainSceneId, null, mainContextId);
    if (MapUtils.isEmpty(mainContent)) {
      mainContent = new HashMap<>();
    }
    mainContent.putAll(content);
    chatflowContextCache.put(false, mainSceneId, null, mainContextId, mainContent);
  }
}
