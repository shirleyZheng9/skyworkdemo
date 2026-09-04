package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import com.iwhalecloud.bote.dto.intent.IntentionMatchItemWithSceneDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.AgentSwitchStep;
import com.iwhalecloud.bote.intent.IIntentEmbeddingService;
import com.iwhalecloud.bote.mapper.bot.BotRelaManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 场景切换 步骤执行器
 *
 * @author zyt 11472359
 * @since 2025-05-28
 */
public class AgentSwitchStepRunner extends AbstractLlmStepRunner<AgentSwitchStep> {

  private final SceneCache sceneCache = SpringUtil.getBean(SceneCache.class);

  private final IIntentEmbeddingService intentEmbeddingService = SpringUtil.getBean(IIntentEmbeddingService.class);

  private final TenantSettingInfoCache tenantSettingInfoCache = SpringUtil.getBean(TenantSettingInfoCache.class);

  private final BotRelaManageMapper botRelaManageMapper = SpringUtil.getBean(BotRelaManageMapper.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, AgentSwitchStep step) {
    //获取用户输入的内容
    String userMessageText = resolveTemplate(step.getUserMessage());
    // 根据切换模式执行不同的逻辑
    String switchMode = step.getSwitchMode();
    switch (switchMode) {
      case "auto":
        handleAutoSwitch(context, step, userMessageText);
        break;
      case "manual":
        handleManualSwitch(context, step);
        break;
      default:
        throw new BssException("不支持的切换模式：" + switchMode);
    }
  }

  /**
   * 处理自动切换
   */
  private void handleAutoSwitch(SceneOrchestrationContext context, AgentSwitchStep step, @Nullable String userMessageText) {
    Map<String, Object> parameters = buildRequestParametersToMap(step.getParameters());
    context.setStepInputLog(parameters);

    Map<String, Object> result = new HashMap<>();
    result.put("sceneAppId", step.getSceneAppId());
    result.put("sceneId", step.getSceneId());
    // 对话内容非必填
    if (StringUtils.isNotEmpty(userMessageText)) {
      result.put("userMessage", userMessageText);
    }
    result.put("switchMode", step.getSwitchMode());
    result.put("parameters", parameters);

    context.setStepOutput(step, result);
    context.getReplyHandler().reply(ChatMessageType.AGENT_SWITCH, result, step.getCode(), step.getName());
  }

  /**
   * 处理用户选择切换
   */
  private void handleManualSwitch(SceneOrchestrationContext context, AgentSwitchStep step) {
    Map<String, Object> parameters = buildRequestParametersToMap(step.getParameters());
    context.setStepInputLog(parameters);
    Map<String, Object> result = new HashMap<>();
    // 转译表达式
    result.put("userPrompt", resolveTemplate(step.getUserPrompt()));
    result.put("agentSource", step.getAgentSource());
    result.put("switchMode", step.getSwitchMode());

    String agentSource = step.getAgentSource();
    List<Map<String, Object>> sceneList = new ArrayList<>();
    Long tenantId = context.getTenantId();
    switch (agentSource) {
      case "customize":
        // 自定义场景
        List<AgentSwitchStep.SceneInfo> sceneInfList = step.getScenes();
        for (AgentSwitchStep.SceneInfo sceneIdMap : sceneInfList) {
          String id = sceneIdMap.getSceneId();
          String sceneAppId = sceneIdMap.getSceneAppId();
          String sceneName = sceneCache.getSceneName(tenantId, Long.parseLong(id));
          Map<String, Object> scene = new HashMap<>();
          scene.put("sceneId", id);
          scene.put("sceneAppId", sceneAppId);
          scene.put("sceneName", sceneName);
          sceneList.add(scene);
        }
        result.put("sceneList", sceneList);
        break;
      case "reference":
        // 引用参数 scenes
        String referenceScenes = step.getReferenceScenes();
        sceneList = handelReferenceScenes(referenceScenes);
        result.put("sceneList", sceneList);
        break;
      case "intentRecognition":
        // 意图识别
        Long botId = step.getBotId();
        String intentRecognitionMessage = resolveTemplate(step.getIntentRecognitionMessage());
        Assert.notNull(intentRecognitionMessage, "意图识别消息不能为空");
        List<IntentionMatchItemWithSceneDTO> finalResult = handelIntentRecognitionMessage(intentRecognitionMessage, tenantId, botId);
        for (IntentionMatchItemWithSceneDTO item : finalResult) {
          Map<String, Object> scene = new HashMap<>();
          scene.put("sceneId", item.getSceneId());
          scene.put("sceneName", item.getSceneName());
          // 根据sceneId查询智能应用Id列表
          List<Long> sceneAppIdList = botRelaManageMapper.querytBotIdBySceneId(tenantId, item.getSceneId());
          Long originalSceneAppId = context.getBotId();
          // 如果sceneAppIdList中有originalSceneAppId，把originalSceneAppId赋值给sceneAppId，如果没有则取出sceneAppIdList第一个值给sceneAppId
          if (originalSceneAppId != null && sceneAppIdList.contains(originalSceneAppId)) {
            scene.put("sceneAppId", originalSceneAppId);
          }
          else {
            scene.put("sceneAppId", sceneAppIdList == null || sceneAppIdList.isEmpty() ? null : sceneAppIdList.get(0));
          }

          sceneList.add(scene);
        }
        result.put("sceneList", sceneList);
        break;

      default:
        throw new BssException("不是正确的智能体来源：" + agentSource);
    }
    context.setStepOutput(step, result);
    context.getReplyHandler().reply(ChatMessageType.AGENT_SWITCH, result, step.getCode(), step.getName());

  }

  /**
   * 处理意图识别
   */
  private List<IntentionMatchItemWithSceneDTO> handelIntentRecognitionMessage(String intentRecognitionMessage, Long tenantId, Long botId) {
    if (!SystemParameter.VECTOR_MATCH_ENABLED.getBooleanValueFromDb()) {
      throw new BssException("向量匹配: 平台未开启");
    }
    IntentStrategyDTO setting = tenantSettingInfoCache.getIntentStrategy(tenantId);
    if (!setting.isEmbedding()) {
      throw new BssException("向量匹配: 租户未开启");
    }

    return intentEmbeddingService.topMatches(tenantId, botId, intentRecognitionMessage, null, 5);
  }

  /**
   * 处理引用参数scenes
   * @param referenceScenes 引用参数
   * @return 处理后的场景列表
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> handelReferenceScenes(String referenceScenes) {
    Object scenes = SceneParamUtil.getParamValue(referenceScenes);
    if (!(scenes instanceof List)) {
      throw new BssException("scenes参数不是一个列表");
    }

    return ((List<?>) scenes).stream()
        .filter(item -> item instanceof Map)
        .map(item -> (Map<?, ?>) item)
        .filter(this::isValidScene)
        .map(item -> (Map<String, Object>) item)
        .collect(Collectors.toList());
  }

  /**
   * 验证场景数据是否有效
   * @param sceneMap 场景数据
   * @return 是否有效
   */
  private boolean isValidScene(Map<?, ?> sceneMap) {
    Object sceneName = sceneMap.get("sceneName");
    Object sceneId = sceneMap.get("sceneId");
    Object sceneAppId = sceneMap.get("sceneAppId");

    boolean hasSceneName = sceneName != null && !String.valueOf(sceneName).isEmpty();
    boolean hasSceneId = sceneId != null && !String.valueOf(sceneId).isEmpty();
    boolean hasSceneAppId = sceneAppId != null && !String.valueOf(sceneAppId).isEmpty();

    return hasSceneName && hasSceneId && hasSceneAppId;
  }

}
