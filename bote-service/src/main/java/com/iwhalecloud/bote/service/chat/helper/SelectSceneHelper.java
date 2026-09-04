package com.iwhalecloud.bote.service.chat.helper;

import com.iwhalecloud.bote.cache.SceneIntentCache;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.bot.RecommendedSceneDTO;
import com.iwhalecloud.bote.dto.bot.SceneIntentDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.chat.SceneProcessDTO;
import com.iwhalecloud.bote.mapper.chat.SceneProcessMapper;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * 推荐场景
 *
 * @author chen.linfa
 * @since 2024-12-18
 */
@Component
@RequiredArgsConstructor
public class SelectSceneHelper {

  private final SceneProcessMapper sceneProcessMapper;

  private final SceneIntentCache sceneIntentCache;

  /**
   * 根据用户历史对话，进行场景推荐
   * <p>1. 减少对大模型的依赖，不建议使用提示词方式提取推荐场景 </p>
   * <p>2. 如果没有历史消息，随机推荐场景 </p>
   */
  public List<RecommendedSceneDTO> invoke(ChatContext context, ChatTraceLogBuilder log) {
    log.addLog("未识别到意图，开始推荐智能体");
    Long tenantId = context.getTenantId();
    Long botId = context.getBotId();
    List<RecommendedSceneDTO> scenes = new ArrayList<>();
    // 从会话历史中获取 top 3 场景
    collectFromLog(tenantId, botId, context.getUserId(), scenes);
    int sceneCountFromLog = scenes.size();
    log.addLog("从会话历史查出 %s 个智能体", sceneCountFromLog);
    // 推荐场景数量
    int limit = SystemParameter.SELECT_SCENE_LIMIT.getRequiredIntegerValueFromDb();
    // 数量不足时从缓存中补充
    if (sceneCountFromLog < limit) {
      collectFromCache(tenantId, botId, scenes, limit - sceneCountFromLog);
      if (scenes.size() > sceneCountFromLog) {
        log.addLog("从缓存补充 %s 个智能体", scenes.size() - sceneCountFromLog);
      }
    }
    return scenes;
  }

  private void collectFromLog(Long tenantId, Long botId, Long userId, List<RecommendedSceneDTO> recommendedScenes) {
    List<SceneProcessDTO> logs = sceneProcessMapper.selectTop3Scene(tenantId, botId, userId);
    for (SceneProcessDTO log : CollectionUtils.emptyIfNull(logs)) {
      if (recommendedScenes.stream().noneMatch(s -> s.getSceneId().equals(log.getSceneId()))) {
        RecommendedSceneDTO dto = new RecommendedSceneDTO();
        dto.setBotId(log.getBotId());
        dto.setSceneId(log.getSceneId());
        dto.setSceneName(log.getSceneName());
        recommendedScenes.add(dto);
      }
    }
  }

  private void collectFromCache(Long tenantId, Long botId, List<RecommendedSceneDTO> recommendedScenes, int count) {
    List<SceneIntentDTO> scenes = sceneIntentCache.getScenes(tenantId, botId);
    int added = 0;
    for (SceneIntentDTO scene : scenes) {
      if (recommendedScenes.stream().noneMatch(s -> s.getSceneId().equals(scene.getSceneId()))) {
        RecommendedSceneDTO dto = new RecommendedSceneDTO();
        dto.setBotId(botId);
        dto.setSceneId(scene.getSceneId());
        dto.setSceneName(scene.getSceneName());
        recommendedScenes.add(dto);
        added++;
        if (added >= count) {
          break;
        }
      }
    }
  }
}
