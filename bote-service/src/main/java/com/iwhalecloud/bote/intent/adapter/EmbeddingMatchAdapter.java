package com.iwhalecloud.bote.intent.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.chat.SceneIntentResultDTO;
import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import com.iwhalecloud.bote.dto.intent.IntentionMatchItemWithSceneDTO;
import com.iwhalecloud.bote.intent.IIntentEmbeddingService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 基于向量化匹配
 *
 * @author chen.linfa
 * @since 2024-12-05
 */
@Component
@RequiredArgsConstructor
public class EmbeddingMatchAdapter {

  private final IIntentEmbeddingService intentEmbeddingService;

  private final TenantSettingInfoCache tenantSettingInfoCache;

  @Nullable
  public SceneIntentResultDTO execute(Long tenantId, @Nullable Long botId, String message, Optional<ChatTraceLogBuilder> log) {
    if (!SystemParameter.VECTOR_MATCH_ENABLED.getBooleanValueFromDb()) {
      log.ifPresent(l -> l.addLog("向量匹配: 平台未开启"));
      return null;
    }
    IntentStrategyDTO setting = tenantSettingInfoCache.getIntentStrategy(tenantId);
    if (!setting.isEmbedding()) {
      log.ifPresent(l -> l.addLog("向量匹配: 租户未开启"));
      return null;
    }

    List<IntentionMatchItemWithSceneDTO> scenes = intentEmbeddingService.topMatches(tenantId, botId, message, null, 1);
    if (CollectionUtils.isEmpty(scenes)) {
      log.ifPresent(l -> l.addLog("向量匹配: 未命中"));
      return null;
    }
    IntentionMatchItemWithSceneDTO matchScene = scenes.get(0);
    log.ifPresent(
      l -> l.addLog("向量匹配: 命中智能体。评分: %s, 智能体: %s，问句: %s", matchScene.getScore(), matchScene.getSceneName(), matchScene.getQuestion()));

    SceneIntentResultDTO result = new SceneIntentResultDTO();
    result.setSceneId(matchScene.getSceneId());
    result.setSceneName(matchScene.getSceneName());
    result.setQuestion(message);
    if (StringUtils.isNotEmpty(matchScene.getAttribute())) {
      String attribute = CollectionUtils.emptyIfNull(JsonUtil.parseJson(matchScene.getAttribute(), new TypeReference<List<ParameterSpec>>() {
      })).stream().map(p -> p.getKey() + ":" + p.getValue()).collect(Collectors.joining(","));
      result.setQuestion(message + "(" + attribute + ")");
    }
    return result;
  }
}
