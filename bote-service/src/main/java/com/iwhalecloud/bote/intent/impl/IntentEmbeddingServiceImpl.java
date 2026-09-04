package com.iwhalecloud.bote.intent.impl;

import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.cache.SceneIntentCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.dto.bot.SceneIntentDTO;
import com.iwhalecloud.bote.dto.intent.IntentQuestionDTO;
import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import com.iwhalecloud.bote.dto.intent.IntentionEmbeddingDTO;
import com.iwhalecloud.bote.dto.intent.IntentionMatchItemDTO;
import com.iwhalecloud.bote.dto.intent.IntentionMatchItemWithSceneDTO;
import com.iwhalecloud.bote.intent.IIntentEmbeddingService;
import com.iwhalecloud.bote.intent.vector.IntentionVectorStore;
import com.iwhalecloud.bote.llm.client.EmbeddingClient;
import com.iwhalecloud.bote.mapper.intent.IntentQuestionManageMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * 意图问句向量服务
 *
 * @author bianjp
 * @since 2024-12-23
 */
@Service
@RequiredArgsConstructor
public class IntentEmbeddingServiceImpl implements IIntentEmbeddingService {
  /** 默认评分阈值 */
  private static final float DEFAULT_SCORE_THRESHOLD = 0.8F;

  private final IntentionVectorStore intentionVectorStore;
  private final ModelClientCache modelClientCache;
  private final IntentQuestionManageMapper intentQuestionManageMapper;
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final SceneIntentCache sceneIntentCache;

  @Override
  public void save(IntentQuestionDTO question) {
    IntentStrategyDTO setting = tenantSettingInfoCache.getIntentStrategy(question.getTenantId());
    if (!setting.isEmbedding()) {
      return;
    }
    EmbeddingClient client = modelClientCache.getEmbeddingClient(question.getTenantId(), setting.getEmbeddingModelId());
    float[] embedding = client.embedding(question.getQuestion());
    IntentionEmbeddingDTO dto = new IntentionEmbeddingDTO();
    dto.setTenantId(question.getTenantId());
    dto.setSceneId(question.getSceneId());
    dto.setQuestionId(question.getQuestionId());
    dto.setQuestion(embedding);
    intentionVectorStore.save(dto);
  }

  @Override
  public void batchSave(List<IntentQuestionDTO> questions) {
    Long tenantId = questions.get(0).getTenantId();
    IntentStrategyDTO setting = tenantSettingInfoCache.getIntentStrategy(tenantId);
    if (!setting.isEmbedding()) {
      return;
    }
    List<IntentionEmbeddingDTO> intentionEmbeddings = buildIntentionEmbeddings(tenantId, setting, questions);
    intentionVectorStore.batchSave(intentionEmbeddings);
  }

  @Override
  public void delete(Long tenantId, Long questionId) {
    IntentStrategyDTO setting = tenantSettingInfoCache.getIntentStrategy(tenantId);
    if (!setting.isEmbedding()) {
      return;
    }
    intentionVectorStore.delete(tenantId, questionId);
  }

  @Override
  public void rebuild(Long tenantId) {
    IntentStrategyDTO setting = tenantSettingInfoCache.getRequiredIntentStrategy(tenantId);
    List<IntentQuestionDTO> annotations = intentQuestionManageMapper.selectQuestionsByTenantId(tenantId);
    if (CollectionUtils.isEmpty(annotations)) {
      intentionVectorStore.deleteByTenant(tenantId);
      return;
    }

    List<IntentionEmbeddingDTO> intentionEmbeddings = buildIntentionEmbeddings(tenantId, setting, annotations);
    intentionVectorStore.deleteByTenant(tenantId);
    intentionVectorStore.batchAdd(intentionEmbeddings);
  }

  /**
   * 构造意图问句向量列表
   */
  private List<IntentionEmbeddingDTO> buildIntentionEmbeddings(Long tenantId, IntentStrategyDTO setting, List<IntentQuestionDTO> questions) {
    EmbeddingClient client = modelClientCache.getEmbeddingClient(tenantId, setting.getEmbeddingModelId());
    List<String> contents = questions.stream().map(IntentQuestionDTO::getQuestion).collect(Collectors.toList());
    List<float[]> embeddings = client.embedding(contents);
    List<IntentionEmbeddingDTO> intentionEmbeddings = new ArrayList<>(questions.size());
    for (int i = 0; i < questions.size(); i++) {
      IntentQuestionDTO annotation = questions.get(i);
      IntentionEmbeddingDTO dto = new IntentionEmbeddingDTO();
      dto.setTenantId(tenantId);
      dto.setSceneId(annotation.getSceneId());
      dto.setQuestionId(annotation.getQuestionId());
      dto.setQuestion(embeddings.get(i));
      intentionEmbeddings.add(dto);
    }
    return intentionEmbeddings;
  }

  @Override
  @Nullable
  public Long bestMatch(Long tenantId, @Nullable Long botId, String question) {
    IntentStrategyDTO setting = tenantSettingInfoCache.getIntentStrategy(tenantId);
    // 未开启时直接返回 null
    if (!setting.isEmbedding()) {
      return null;
    }
    float[] embedding = modelClientCache.getEmbeddingClient(tenantId, setting.getEmbeddingModelId()).embedding(question);
    float scoreThreshold = ObjectUtils.getIfNull(setting.getEmbeddingScoreThreshold(), DEFAULT_SCORE_THRESHOLD);
    List<Long> sceneIds = botId == null ? Collections.emptyList()
      : CollectionUtils.emptyIfNull(sceneIntentCache.getScenes(tenantId, botId)).stream().map(SceneIntentDTO::getSceneId).collect(Collectors.toList());
    Long questionId = intentionVectorStore.bestMatch(tenantId, sceneIds, embedding, scoreThreshold);
    if (questionId != null) {
      return intentQuestionManageMapper.selectSceneIdByQuestionId(tenantId, questionId);
    }
    return null;
  }

  @Override
  public List<IntentionMatchItemWithSceneDTO> topMatches(Long tenantId, @Nullable Long botId, String question, @Nullable Float scoreThreshold,
    int topNum) {
    IntentStrategyDTO setting = tenantSettingInfoCache.getRequiredIntentStrategy(tenantId);
    float[] embedding = modelClientCache.getEmbeddingClient(tenantId, setting.getEmbeddingModelId()).embedding(question);
    float finalScoreThreshold = ObjectUtils.firstNonNull(scoreThreshold, setting.getEmbeddingScoreThreshold(), DEFAULT_SCORE_THRESHOLD);
    List<Long> sceneIds = botId == null ? Collections.emptyList()
      : CollectionUtils.emptyIfNull(sceneIntentCache.getScenes(tenantId, botId)).stream().map(SceneIntentDTO::getSceneId).collect(Collectors.toList());
    List<IntentionMatchItemDTO> intentionMatchItems = intentionVectorStore.topMatches(tenantId, sceneIds, embedding, finalScoreThreshold, topNum);
    if (intentionMatchItems.isEmpty()) {
      return Collections.emptyList();
    }

    // 查询关联的机器人、场景、问句
    List<Long> questionIds = intentionMatchItems.stream().map(IntentionMatchItemDTO::getQuestionId).collect(Collectors.toList());
    List<IntentionMatchItemWithSceneDTO> newIntentionMatchItems = intentQuestionManageMapper.selectSceneAndQuestionsByIds(tenantId, questionIds);

    return intentionMatchItems.stream().map(item -> {
      IntentionMatchItemWithSceneDTO dto = IterableUtils.find(newIntentionMatchItems, i -> item.getQuestionId().equals(i.getQuestionId()));
      if (dto == null) {
        return null;
      }
      dto.setScore(item.getScore());
      return dto;
    }).filter(Objects::nonNull).collect(Collectors.toList());
  }
}
