package com.iwhalecloud.bote.intent.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.chat.SceneIntentResultDTO;
import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import com.iwhalecloud.bote.dto.model.request.PredictRequest;
import com.iwhalecloud.bote.dto.model.response.PredictResponse.PredictInfo;
import com.iwhalecloud.bote.mapper.bot.BotRelaManageMapper;
import com.iwhalecloud.bote.service.model.helper.SmallModelAnswerHelper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 基于微调模型匹配
 *
 * @author chen.linfa
 * @since 2025-03-10
 */
@Component
@RequiredArgsConstructor
public class SmallModelMatchAdapter {

  private final BotRelaManageMapper botRelaManageMapper;
  private final SceneCache sceneCache;
  private final SmallModelAnswerHelper helper;
  private final TenantSettingInfoCache tenantSettingInfoCache;

  @Nullable
  public SceneIntentResultDTO execute(Long tenantId, @Nullable Long botId, String message, Optional<ChatTraceLogBuilder> log) {
    IntentStrategyDTO setting = tenantSettingInfoCache.getIntentStrategy(tenantId);
    if (!setting.isSlm()) {
      log.ifPresent(l -> l.addLog("小模型匹配: 租户未开启"));
      return null;
    }
    PredictRequest request = new PredictRequest();
    request.setTenantId(tenantId.toString());
    request.setBaseModelName(setting.getFinetune().getBaseModelName());
    request.setFineTuningModelId(setting.getFinetune().getModelId().toString());
    request.setThreshold(setting.getFinetune().getThreshold());
    request.setMaxLength(setting.getFinetune().getMaxLength());
    request.setText(message);

    ResultVO<PredictInfo> chatResult = helper.chat(request);
    if (!chatResult.isSuccess() || StringUtils.isEmpty(chatResult.getResultObject().getLabel())) {
      log.ifPresent(l -> l.addLog("向量匹配: 未命中"));
      return null;
    }
    List<String> keys = Arrays.asList(chatResult.getResultObject().getLabel().split("-"));
    Long sceneId = Long.parseLong(keys.get(0));
    // 根据 botId 判断智能体的合法性
    if (botId != null && !botRelaManageMapper.existsBotSceneRelByScene(tenantId, botId, sceneId)) {
      log.ifPresent(l -> l.addLog("向量匹配: 未命中"));
      return null;
    }
    String sceneName = sceneCache.getSceneName(tenantId, sceneId);
    if (sceneName == null) {
      log.ifPresent(l -> l.addLog("向量匹配: 未命中"));
      return null;
    }

    log.ifPresent(l -> l.addLog("向量匹配: 命中。智能体: %s，问句: %s", sceneName, message));
    SceneIntentResultDTO result = new SceneIntentResultDTO();
    result.setSceneId(sceneId);
    result.setSceneName(sceneName);
    result.setQuestion(message);
    if (StringUtils.isNotEmpty(keys.get(1))) {
      String attribute = CollectionUtils.emptyIfNull(JsonUtil.parseJson(keys.get(1), new TypeReference<List<ParameterSpec>>() {
      })).stream().map(p -> p.getKey() + ":" + p.getValue()).collect(Collectors.joining(","));
      result.setQuestion(message + "(" + attribute + ")");
    }
    return result;
  }
}
