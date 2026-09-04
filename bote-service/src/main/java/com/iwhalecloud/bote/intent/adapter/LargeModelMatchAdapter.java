package com.iwhalecloud.bote.intent.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.SceneIntentCache;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.FreemarkerUtil;
import com.iwhalecloud.bote.dto.bot.SceneIntentDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.chat.SceneIntentResultDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO.SimplePlanStepDTO;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.service.model.helper.LargeModelAnswerHelper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 基于大模型匹配
 *
 * @author chen.linfa
 * @since 2024-12-05
 */
@Component
@RequiredArgsConstructor
public class LargeModelMatchAdapter {

  private final BotQueryMapper botQueryMapper;

  private final SceneIntentCache sceneIntentCache;

  private final LargeModelAnswerHelper modelAnswerHelper;

  @Nullable
  public SceneIntentResultDTO execute(Long tenantId, @Nullable Long botId, String message, Optional<ChatTraceLogBuilder> log) {
    if (botId != null) {
      SimpleBotDTO bot = botQueryMapper.selectAgentStrategy(tenantId, botId);
      if (bot.hasAgentStrategy()) {
        return matchPlan(tenantId, botId, message, log);
      }
    }
    // 只匹配最佳的一个智能体
    return matchSingleAgent(tenantId, botId, message, log);
  }

  private SceneIntentResultDTO matchSingleAgent(Long tenantId, Long botId, String message, Optional<ChatTraceLogBuilder> log) {
    List<SceneIntentDTO> scenes = sceneIntentCache.getScenes(tenantId, botId);
    if (CollectionUtils.isEmpty(scenes)) {
      return null;
    }
    String template = SystemParameter.SINGLE_AGENT_INTENT_PROMPT.getValueFromDb();
    Map<String, Object> params = new HashMap<>();
    params.put("scenes", scenes);
    // 生成识别单智能体提示词
    String planPrompt = FreemarkerUtil.process(template, params);
    // 使用 LLM 生成
    Map<String, Object> result = modelAnswerHelper.chat(tenantId, planPrompt, message, new TypeReference<Map<String, Object>>() {
    });
    if (MapUtils.isNotEmpty(result) && result.containsKey("intentNumber")) {
      SceneIntentDTO scene = scenes.get(MapUtils.getInteger(result, "intentNumber") - 1);
      if (scene != null) {
        log.ifPresent(l -> l.addLog("大模型匹配: 命中。智能体：%s", scene.getSceneName()));
        String question = MapUtils.getString(result, "question");
        SceneIntentResultDTO intent = new SceneIntentResultDTO();
        intent.setSceneId(scene.getSceneId());
        intent.setSceneName(scene.getSceneName());
        intent.setQuestion(StringUtils.isEmpty(question) ? message : question);
        return intent;
      }
    }
    log.ifPresent(l -> l.addLog("大模型匹配: 未命中"));
    return null;
  }

  private SceneIntentResultDTO matchPlan(Long tenantId, @Nullable Long botId, String message, Optional<ChatTraceLogBuilder> log) {
    String template = SystemParameter.GENERATE_PLAN_PROMPT.getValueFromDb();
    Map<String, Object> params = new HashMap<>();
    params.put("agents", sceneIntentCache.getScenes(tenantId, botId));
    params.put("userMessage", message);
    // 生成计划提示词
    String planPrompt = FreemarkerUtil.process(template, params);
    // 使用 LLM 生成计划
    Map<String, Object> result = modelAnswerHelper.chat(tenantId, planPrompt, message, new TypeReference<Map<String, Object>>() {
    });
    if (MapUtils.isNotEmpty(result) && result.containsKey("code")) {
      List<SimplePlanStepDTO> steps = JsonUtil.parseJson(JsonUtil.toJsonString(result.get("code")), new TypeReference<List<SimplePlanStepDTO>>() {
      });
      if (CollectionUtils.isNotEmpty(steps)) {
        String agents = steps.stream().map(SimplePlanStepDTO::getAgentName).collect(Collectors.joining(","));
        log.ifPresent(l -> l.addLog("大模型匹配: 命中。智能体：%s", agents));
        SceneIntentResultDTO intent = new SceneIntentResultDTO();
        SimplePlanDTO plan = new SimplePlanDTO();
        plan.setSteps(steps);
        intent.setPlan(plan);
        return intent;
      }
    }
    log.ifPresent(l -> l.addLog("大模型匹配: 未命中"));
    return null;
  }
}
