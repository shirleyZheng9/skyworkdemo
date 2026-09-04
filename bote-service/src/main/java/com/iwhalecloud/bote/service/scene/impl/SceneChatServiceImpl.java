package com.iwhalecloud.bote.service.scene.impl;

import com.iwhalecloud.bote.cache.PlanContextCache;
import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.PlanConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.chat.SceneProcessDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bote.dto.scene.FlowSceneProcessDTO;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bote.mapper.chat.SceneProcessMapper;
import com.iwhalecloud.bote.mapper.scene.SceneChatMessageMapper;
import com.iwhalecloud.bote.service.a2a.helper.A2aAgentEngine;
import com.iwhalecloud.bote.service.orchestration.IOrchestrationEngine;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.ChatReplyHandler;
import com.iwhalecloud.bote.service.planning.hepler.PlanRecorder;
import com.iwhalecloud.bote.service.scene.ISceneChatService;
import com.iwhalecloud.bote.service.scene.runner.impl.ClawSceneRunner;
import com.iwhalecloud.bote.service.scene.runner.impl.KnowledgeSceneRunner;
import com.iwhalecloud.bote.service.scene.runner.impl.SimpleSceneRunner;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 场景运行服务
 *
 * @author bianjp
 * @since 2024-08-05
 */
@Service
@RequiredArgsConstructor
public class SceneChatServiceImpl implements ISceneChatService {
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final SceneCache sceneCache;
  private final SceneChatMessageMapper sceneChatMessageMapper;
  private final IOrchestrationEngine orchestrationEngine;
  private final PlanContextCache planContextCache;
  private final PlanRecorder planRecorder;
  private final SceneProcessMapper sceneProcessMapper;
  private final A2aAgentEngine a2aAgentEngine;
  private final SimpleSceneRunner simpleSceneRunner;
  private final KnowledgeSceneRunner knowledgeSceneRunner;
  private final ClawSceneRunner clawSceneRunner;

  @Override
  public OrchestrationEngineResponse run(SceneChatParamsDTO sceneChatParams) {
    Date startTime = new Date();
    // 捕获所有异常，方便调用方使用
    try {
      OrchestrationEngineResponse response = doRun(sceneChatParams);
      if (Boolean.TRUE.equals(response.getSuccess())) {
        if (Boolean.TRUE.equals(response.getSceneFinished())) {
          FlowSceneProcessDTO process = getFlowSceneProcess(sceneChatParams.getContextId());
          if (process != null) {
            // 嵌套智能体结束，发起主流程
            SceneChatParamsDTO params = buildSceneChatParams(sceneChatParams, process);
            OrchestrationEngineResponse result = doRun(params);
            if (Boolean.TRUE.equals(result.getSceneFinished())) {
              processMainResponse(params);
            }
            else {
              response.setSceneFinished(false);
            }
          }
        }
      }
      return response;
    }
    catch (Exception e) {
      return OrchestrationEngineResponse.fail(startTime, e);
    }
  }

  @Override
  @Nullable
  public FlowSceneProcessDTO getFlowSceneProcess(String contextId) {
    return sceneChatMessageMapper.getProcess(contextId);
  }

  /**
   * 执行场景
   */
  private OrchestrationEngineResponse doRun(SceneChatParamsDTO sceneChatParams) {
    Assert.notNull(sceneChatParams.getTenantId(), "tenantId 不能为空");
    Assert.notNull(sceneChatParams.getReplyHandler(), "replyHandler 不能为空");
    Long sceneId = sceneChatParams.getSceneId();
    Long flowId = sceneChatParams.getFlowId();
    Assert.isTrue(sceneId != null || flowId != null, "sceneId, flowId 不能同时为空");

    // 检查是否需要记录流程日志
    sceneChatParams.setLogEnabled(shouldEnableLogging(sceneChatParams));

    if (sceneId != null && sceneChatParams.getDynamicDsl() == null) {
      SimpleBotSceneDTO scene;
      // 调试时场景可能未上架，不会加载到缓存中，需要查询数据库
      if (Boolean.TRUE.equals(sceneChatParams.getDebug())) {
        scene = sceneCache.getSceneFromDb(sceneChatParams.getTenantId(), sceneId);
        Assert.notNull(scene, () -> "智能体不存在: " + sceneId);
      }
      else {
        scene = sceneCache.getScene(sceneChatParams.getTenantId(), sceneId);
      }
      Assert.isTrue(SceneConsts.SCENE_TYPES.contains(scene.getSceneType()), () -> "未知的智能体类型: " + scene.getSceneType());
      sceneChatParams.setModelId(scene.getModelId());
      // 解析自定义模型参数
      if (StringUtils.isNotBlank(scene.getModelConfigJson())) {
        sceneChatParams.setCustomModelConfig(JsonUtil.parseJson(scene.getModelConfigJson(), CustomModelConfig.class));
      }
      // 简单场景
      if (SceneConsts.SCENE_TYPE_SCENE.equals(scene.getSceneType())) {
        sceneChatParams.setLongTermMemoryEnabled(scene.getLongTermMemoryEnabled());
        return simpleSceneRunner.run(scene, sceneChatParams);
      }
      // 知识问答场景
      if (SceneConsts.SCENE_TYPE_KNOWLEDGE.equals(scene.getSceneType())) {
        return knowledgeSceneRunner.run(scene, sceneChatParams);
      }
      if (SceneConsts.SCENE_TYPE_CLAW.equals(scene.getSceneType())) {
        return clawSceneRunner.run(scene, sceneChatParams);
      }
      // A2A 服务
      if (SceneConsts.SCENE_TYPE_A2A.equals(scene.getSceneType())) {
        return a2aAgentEngine.execute(sceneChatParams);
      }
    }
    // 流程编排智能体、对话型工作流
    OrchestrationEngineRequest request = new OrchestrationEngineRequest(sceneChatParams, sceneId, flowId, sceneChatParams.getParams());
    return orchestrationEngine.run(request);
  }

  /**
   * 检查是否需要记录流程日志
   */
  private boolean shouldEnableLogging(SceneChatParamsDTO sceneChatParams) {
    // 只有通过正常的会话触发时才记录日志，调试时不记录
    return sceneChatParams.getConversationId() != null
      && !SceneConsts.TEST_CONVERSATION_ID.equals(sceneChatParams.getConversationId())
      && tenantSettingInfoCache.isFlowLogEnabled(sceneChatParams.getTenantId());
  }

  private SceneChatParamsDTO buildSceneChatParams(SceneChatParamsDTO params, FlowSceneProcessDTO process) {
    SceneChatParamsDTO sceneChatParams = new SceneChatParamsDTO();
    sceneChatParams.setTenantId(params.getTenantId());
    sceneChatParams.setBotId(params.getBotId());
    sceneChatParams.setSceneId(process.getMainSceneId());
    sceneChatParams.setContextId(process.getMainContextId());
    sceneChatParams.setConversationId(process.getMainConversationId());

    sceneChatParams.setCompletedNodeCode(process.getNodeCode());
    sceneChatParams.setCompletedContextId(params.getContextId());

    sceneChatParams.setReplyHandler(params.getReplyHandler());
    sceneChatParams.setDebug(params.getDebug());

    if (params.getPlanId() != null) {
      PlanRecordDTO record = planContextCache.get(params.getPlanId());
      sceneChatParams.setPlanId(params.getPlanId());
      sceneChatParams.setDynamicDsl(record != null ? record.getDsl() : null);
    }
    return sceneChatParams;
  }

  /**
   * 处理主流程的执行结果
   */
  private void processMainResponse(SceneChatParamsDTO params) {
    if (params.getReplyHandler() instanceof ChatReplyHandler) {
      // 标记智能体已完成
      SceneProcessDTO log = new SceneProcessDTO();
      log.setSessionId(params.getConversationId());
      log.setSceneId(params.getSceneId());
      log.setContextId(params.getContextId());
      log.setChatStatus(ChatConsts.CHAT_SCENE_STATUS_FINISH);
      log.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
      sceneProcessMapper.updateChatSceneStatus(log);

      // 标记计划已完成
      if (params.getPlanId() != null) {
        PlanRecordDTO record = planContextCache.get(params.getPlanId());
        if (record != null) {
          record.setStatus(PlanConsts.STATUS_SUCCESS);
          planRecorder.updateRecord(record, null, params.getReplyHandler());
        }
      }
    }
  }
}
