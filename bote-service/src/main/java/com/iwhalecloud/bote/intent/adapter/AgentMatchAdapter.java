package com.iwhalecloud.bote.intent.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.ChatflowContextCache;
import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.PlanConsts;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.dto.bot.SimpleAgentStrategyDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO.SimplePlanStepDTO;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.NonStreamFlowReplyHandler;
import com.iwhalecloud.bote.service.scene.ISceneChatService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

/**
 * 基于规划智能体匹配
 *
 * @author chen.linfa
 * @since 2025-05-29
 */
@Component
@RequiredArgsConstructor
public class AgentMatchAdapter {

  private final BotQueryMapper botQueryMapper;
  private final SceneCache sceneCache;
  private final ChatflowContextCache chatflowContextCache;
  private final ISceneChatService sceneChatService;

  public void execute(SimplePlanDTO plan, Optional<ChatTraceLogBuilder> log) {
    if (plan.getBotId() == null) {
      return;
    }
    // 按照应用维护，调整智能体规划
    SimpleBotDTO bot = botQueryMapper.selectAgentStrategy(plan.getTenantId(), plan.getBotId());
    if (bot.hasAgentStrategy()) {
      SimpleAgentStrategyDTO strategy = JsonUtil.parseJson(bot.getAgentStrategy(), SimpleAgentStrategyDTO.class);
      if (strategy == null) {
        return;
      }
      if (BaseConsts.AGENT_STRATEGY_PLAN.equals(strategy.getType()) && strategy.getPlanAgent() != null) {
        callPlanAgent(plan, strategy.getPlanAgent(), log);
      }
      else if (BaseConsts.AGENT_STRATEGY_CUSTOM.equals(strategy.getType()) && CollectionUtils.isNotEmpty(strategy.getAgents())) {
        // 按照指定的执行步骤，重置计划
        List<SimplePlanStepDTO> steps = new ArrayList<>();
        for (SimpleBotSceneDTO scene : strategy.getAgents()) {
          SimplePlanStepDTO step = new SimplePlanStepDTO();
          step.setAgentId(scene.getSceneId());
          step.setAgentName(scene.getSceneName());
          step.setAgentRequest(plan.getUserMessage());
          steps.add(step);
        }
        plan.setSteps(steps);
        String agents = steps.stream().map(SimplePlanStepDTO::getAgentName).collect(Collectors.joining(","));
        log.ifPresent(l -> l.addLog("智能体规划: 顺序执行。规划：%s", agents));
      }
    }
    // 规划后的步骤，补充每个智能体具体的流程步骤
    wrapPlan(plan);
  }

  /**
   * 通过规划智能体，微调计划
   */
  private void callPlanAgent(SimplePlanDTO plan, SimpleBotSceneDTO agent, Optional<ChatTraceLogBuilder> log) {
    String contextId = SceneContextUtil.newContextId();
    SceneChatParamsDTO sceneChatParams = new SceneChatParamsDTO();
    sceneChatParams.setTenantId(plan.getTenantId());
    sceneChatParams.setBotId(plan.getBotId());
    sceneChatParams.setSceneId(agent.getSceneId());
    sceneChatParams.setConversationId(plan.getPlanId());
    sceneChatParams.setContextId(contextId);
    sceneChatParams.setMessageContent(plan.getUserMessage());
    sceneChatParams.setParams(plan.getUserParams());
    sceneChatParams.setReplyHandler(new NonStreamFlowReplyHandler(contextId));

    Map<String, Object> params = new HashMap<>();
    params.put(PlanConsts.AGENT_PLAN_ID, plan.getPlanId());
    params.put(PlanConsts.AGENT_PLAN_MESSAGE_KEY, plan.getUserMessage());
    params.put(PlanConsts.AGENT_PLAN_STEP_KEY, plan.getSteps());
    chatflowContextCache.put(false, agent.getSceneId(), null, contextId, params);
    OrchestrationEngineResponse response = sceneChatService.run(sceneChatParams);
    if (Boolean.TRUE.equals(response.getSuccess())) {
      Map<String, Object> cacheContext = chatflowContextCache.get(agent.getSceneId(), null, contextId);
      if (MapUtils.isNotEmpty(cacheContext) && cacheContext.containsKey(PlanConsts.AGENT_PLAN_STEP_KEY)) {
        List<SimplePlanStepDTO> steps = JsonUtil.parseJson(JsonUtil.toJsonString(cacheContext.get(PlanConsts.AGENT_PLAN_STEP_KEY)),
          new TypeReference<List<SimplePlanStepDTO>>() {
          });
        String agents = CollectionUtils.emptyIfNull(steps).stream().map(SimplePlanStepDTO::getAgentName).collect(Collectors.joining(","));
        log.ifPresent(l -> l.addLog("智能体规划: 指定智能体。规划后：%s", agents));
        if (CollectionUtils.isNotEmpty(steps)) {
          plan.setSteps(steps);
        }
      }
    }
    else {
      log.ifPresent(l -> l.addLog("智能体规划: 指定智能体。规划出现异常：%s", response.getFailMsg()));
    }
  }

  /**
   * 补充每个智能体具体的执行步骤
   * <p>目前，只有流程模式的智能体，具备定义流程步骤信息 </p>
   */
  private void wrapPlan(SimplePlanDTO plan) {
    for (SimplePlanStepDTO step : CollectionUtils.emptyIfNull(plan.getSteps())) {
      if (step.getAgentId() == null || CollectionUtils.isNotEmpty(step.getFlowSteps())) {
        continue;
      }
      step.setFlowSteps(sceneCache.getFlowSteps(plan.getTenantId(), step.getAgentId()));
    }
  }
}
