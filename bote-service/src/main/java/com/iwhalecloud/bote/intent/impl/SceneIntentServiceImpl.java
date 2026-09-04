package com.iwhalecloud.bote.intent.impl;

import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.chat.SceneIntentResultDTO;
import com.iwhalecloud.bote.dto.chat.query.SimpleUserMessageDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO.SimplePlanStepDTO;
import com.iwhalecloud.bote.intent.ISceneIntentService;
import com.iwhalecloud.bote.intent.adapter.AgentMatchAdapter;
import com.iwhalecloud.bote.intent.adapter.EmbeddingMatchAdapter;
import com.iwhalecloud.bote.intent.adapter.LargeModelMatchAdapter;
import com.iwhalecloud.bote.intent.adapter.SmallModelMatchAdapter;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.service.bot.IBotQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author chen.linfa
 * @since 2024-10-08
 */
@Service
@RequiredArgsConstructor
public class SceneIntentServiceImpl implements ISceneIntentService {

  // @formatter:off
  private final EmbeddingMatchAdapter embeddingMatchAdapter;
  private final SmallModelMatchAdapter smallModelMatchAdapter;
  private final LargeModelMatchAdapter modelMatchAdapter;
  private final AgentMatchAdapter agentMatchAdapter;
  private final BotQueryMapper botQueryMapper;
  private final IBotQueryService botQueryService;
  // @formatter:on

  /**
   * @param tenantId 租户 ID
   * @param botId 当前的机器人 ID
   * @param message 用户消息
   */
  @Nullable
  @Override
  public SceneIntentResultDTO recognize(Long tenantId, @Nullable Long botId, SimpleUserMessageDTO message, Optional<ChatTraceLogBuilder> log) {
    // 向量匹配
    SceneIntentResultDTO result = embeddingMatchAdapter.execute(tenantId, botId, message.getContent(), log);
    // 小模型匹配
    if (result == null) {
      result = smallModelMatchAdapter.execute(tenantId, botId, message.getContent(), log);
    }
    // 大模型匹配
    if (result == null) {
      result = modelMatchAdapter.execute(tenantId, botId, message.getContent(), log);
    }
    if (result == null) {
      return null;
    }

    // 根据智能体规划策略，调整计划
    SimplePlanDTO plan = new SimplePlanDTO();
    plan.setPlanId(Sequences.PLAN_RECORD_ID.next());
    plan.setTenantId(tenantId);
    plan.setBotId(botId);
    plan.setUserMessage(message.getContent());
    plan.setUserParams(message.getParams());
    List<SimplePlanStepDTO> steps = new ArrayList<>();
    plan.setSteps(steps);
    if (result.getSceneId() != null) {
      SimplePlanStepDTO step = new SimplePlanStepDTO();
      step.setAgentId(result.getSceneId());
      step.setAgentName(result.getSceneName());
      step.setAgentRequest(result.getQuestion());
      steps.add(step);
    }
    if (result.getPlan() != null) {
      steps.addAll(result.getPlan().getSteps());
    }
    if (BooleanUtils.isTrue(message.getPlanable())) {
      agentMatchAdapter.execute(plan, log);
    }
    // 调整识别结果
    return wrapResult(plan, false);
  }

  @Override
  public SceneIntentResultDTO recognizeBot(Long tenantId, String message) {
    Optional<ChatTraceLogBuilder> log = Optional.empty();
    // 向量匹配
    SceneIntentResultDTO result = embeddingMatchAdapter.execute(tenantId, null, message, log);
    // 小模型匹配
    if (result == null) {
      result = smallModelMatchAdapter.execute(tenantId, null, message, log);
    }
    // 大模型匹配
    if (result == null) {
      result = modelMatchAdapter.execute(tenantId, null, message, log);
    }
    if (result == null) {
      // 未识别到智能体，需要返回租户下默认的应用
      SimpleBotDTO bot = botQueryService.getDefaultBot(tenantId);
      Assert.notNull(bot, "当前租户未配置应用，无法使用对话功能。");
      result = new SceneIntentResultDTO();
      result.setBotId(bot.getBotId());
      result.setBotName(bot.getBotName());
      result.setQuestion(message);
    }
    if (result.getBotId() == null) {
      // 尝试根据匹配到的智能体，找到最佳的关联应用
      SimpleBotDTO bot = botQueryMapper.selectBotListBySceneId(tenantId, result.getSceneId()).get(0);
      result.setBotId(bot.getBotId());
      result.setBotName(bot.getBotName());
    }
    return result;
  }

  @Override
  @Nullable
  public SceneIntentResultDTO recognizePlan(Long tenantId, Long botId, SimpleUserMessageDTO message, Optional<ChatTraceLogBuilder> log) {
    SimplePlanDTO plan = new SimplePlanDTO();
    plan.setPlanId(Sequences.PLAN_RECORD_ID.next());
    plan.setTenantId(tenantId);
    plan.setBotId(botId);
    plan.setUserMessage(message.getContent());
    plan.setUserParams(message.getParams());
    plan.setSteps(message.getBusiInfos());
    agentMatchAdapter.execute(plan, log);
    return wrapResult(plan, true);
  }

  /**
   * @param plan 计划
   * @param showPlan 标识严格按照计划方式输出，即使只匹配到一个智能体
   */
  @Nullable
  private SceneIntentResultDTO wrapResult(SimplePlanDTO plan, boolean showPlan) {
    // 移除信息不全的步骤
    List<SimplePlanStepDTO> steps = CollectionUtils.emptyIfNull(plan.getSteps()).stream()
      .filter(p -> p.getAgentId() != null && StringUtils.isNotEmpty(p.getAgentName())).collect(Collectors.toList());
    if (CollectionUtils.isEmpty(steps)) {
      return null;
    }
    for (SimplePlanStepDTO step : steps) {
      if (StringUtils.isEmpty(step.getAgentRequest())) {
        step.setAgentRequest(plan.getUserMessage());
      }
    }
    SceneIntentResultDTO result = new SceneIntentResultDTO();
    if (steps.size() == 1 && !showPlan) {
      result.setSceneId(steps.get(0).getAgentId());
      result.setSceneName(steps.get(0).getAgentName());
      result.setQuestion(steps.get(0).getAgentRequest());
      result.setPlan(null);
    }
    else {
      plan.setSteps(steps);
      result.setPlan(plan);
    }
    return result;
  }
}
