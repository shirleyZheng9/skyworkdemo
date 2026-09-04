package com.iwhalecloud.bote.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.bot.SceneIntentDTO;
import com.iwhalecloud.bote.dto.intent.IntentQuestionDTO;
import com.iwhalecloud.bote.dto.intent.query.IntentQueryParams;
import com.iwhalecloud.bote.mapper.intent.IntentQuestionManageMapper;
import com.iwhalecloud.bote.mapper.scene.SceneQueryMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 智能体意图识别缓存
 *
 * @author chen.linfa
 * @since 2024-10-11
 */
@Component
public class SceneIntentCache implements TenantCacheMarker {
  private static final Logger logger = LoggerFactory.getLogger(SceneIntentCache.class);

  private final SceneQueryMapper sceneQueryMapper;
  private final IntentQuestionManageMapper intentQuestionMapper;
  /** 本地缓存，key 为 tenant_id */
  private final LoadingCache<@NonNull Long, @NonNull List<SceneIntentDTO>> cache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(Duration.ofHours(6))
    .build(CacheLoader.from(this::loadScenes));

  public SceneIntentCache(SceneQueryMapper sceneQueryMapper, IntentQuestionManageMapper intentQuestionMapper) {
    this.sceneQueryMapper = sceneQueryMapper;
    this.intentQuestionMapper = intentQuestionMapper;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_SCENE_INTENT;
  }

  /**
   * 获取智能体列表
   *
   * @param tenantId 租户 ID
   * @param botId 指定机器人范围，可选
   * @return 智能体
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  public List<SceneIntentDTO> getScenes(Long tenantId, @Nullable Long botId) {
    List<SceneIntentDTO> scenes;
    try {
      scenes = cache.get(tenantId);
    }
    catch (ExecutionException e) {
      throw new BssException("获取智能体意图缓存失败: " + ExpUtil.getMsg(e.getCause()), e.getCause());
    }
    if (CollectionUtils.isNotEmpty(scenes)) {
      if (botId != null) {
        List<Long> ids = CollectionUtils.emptyIfNull(sceneQueryMapper.selectSceneListByBotId(tenantId, botId, SceneConsts.EXCLUDE_LABELS))
          .stream().map(SceneIntentDTO::getSceneId).collect(Collectors.toList());
        scenes = scenes.stream().filter(p -> ids.contains(p.getSceneId())).collect(Collectors.toList());
      }
      if (CollectionUtils.isNotEmpty(scenes)) {
        // 补充意图问句，考虑数据量问题，这块内容不适合放置到缓存
        IntentQueryParams params = new IntentQueryParams();
        params.setTenantId(tenantId);
        params.setSceneIds(scenes.stream().map(SceneIntentDTO::getSceneId).collect(Collectors.toList()));
        Map<Long, List<IntentQuestionDTO>> group = CollectionUtils.emptyIfNull(intentQuestionMapper.selectIntentQuestionList(params)).stream()
          .collect(Collectors.groupingBy(IntentQuestionDTO::getSceneId));
        for (SceneIntentDTO scene : scenes) {
          if (group.containsKey(scene.getSceneId())) {
            List<String> questions = group.get(scene.getSceneId()).stream().map(IntentQuestionDTO::getQuestion).collect(Collectors.toList());
            // 截取最多 30 条
            scene.setQuestions(questions.size() > 30 ? questions.subList(0, 30) : questions);
          }
        }
      }
    }
    return scenes;
  }

  /**
   * 加载智能体列表
   */
  private List<SceneIntentDTO> loadScenes(Long tenantId) {
    // 只查询上架且未配置副驾标签的智能体
    return sceneQueryMapper.selectSceneIntentListByTenantId(tenantId, SceneConsts.EXCLUDE_LABELS);
  }

  @Override
  public boolean isDistributedCacheEnabled() {
    return false;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void refreshLocalCache() {
    logger.debug("Refresh all: cacheName={}", getCacheName());
    cache.invalidateAll();
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void refreshLocalCache(List<String> keys) {
    logger.debug("Refresh local by keys: cacheName={}, keys={}", getCacheName(), keys);
    // 只清除缓存，不重新加载，以减少耗时
    for (String key : keys) {
      if (StringUtils.isNumeric(key)) {
        cache.invalidate(Long.parseLong(key));
      }
    }
  }

  @Override
  public void refresh() {
    refreshLocalCache();
  }

  @Override
  public void refresh(List<String> keys) {
    refreshLocalCache(keys);
  }
}
