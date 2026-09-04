package com.iwhalecloud.bote.intent.vector.adapter;

import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.dto.intent.IntentionEmbeddingDTO;
import com.iwhalecloud.bote.dto.intent.IntentionMatchItemDTO;
import com.iwhalecloud.bote.intent.vector.IntentionVectorStore;
import com.iwhalecloud.bote.mapper.intent.IntentionEmbeddingMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

/**
 * PostgreSQL 意图向量存储实现，作废
 *
 * <p>使用 <a href="https://github.com/pgvector/pgvector">pgvector</a> 插件</p>
 *
 * @author bianjp
 * @since 2024-12-23
 */
//@Component
@RequiredArgsConstructor
public class PostgresIntentionVectorStore implements IntentionVectorStore {
  private final IntentionEmbeddingMapper intentionEmbeddingMapper;

  @Override
  @Transactional
  public void add(IntentionEmbeddingDTO intentionEmbedding) {
    intentionEmbedding.setId(Sequences.INTENTION_EMBEDDING_ID.next());
    intentionEmbeddingMapper.insert(intentionEmbedding);
  }

  @Override
  public void batchAdd(List<IntentionEmbeddingDTO> intentionEmbeddings) {
    for (IntentionEmbeddingDTO embedding : intentionEmbeddings) {
      embedding.setId(Sequences.INTENTION_EMBEDDING_ID.next());
    }
    // 分批插入，避免数据量过大
    for (List<IntentionEmbeddingDTO> partition : ListUtils.partition(intentionEmbeddings, 50)) {
      intentionEmbeddingMapper.batchInsert(partition);
    }
  }

  @Override
  @Transactional
  public void save(IntentionEmbeddingDTO intentionEmbedding) {
    Long id = intentionEmbeddingMapper.selectIdByQuestionId(intentionEmbedding.getQuestionId());
    if (id == null) {
      add(intentionEmbedding);
    }
    else {
      intentionEmbeddingMapper.update(id, intentionEmbedding.getQuestion());
    }
  }

  @Override
  public void batchSave(List<IntentionEmbeddingDTO> intentionEmbeddings) {
    List<Long> questionIds = intentionEmbeddings.stream().map(IntentionEmbeddingDTO::getQuestionId).collect(Collectors.toList());
    // question_id -> id 映射
    Map<Long, Long> questionIdToIdMap = intentionEmbeddingMapper.selectIdsByQuestionIds(questionIds).stream()
      .collect(Collectors.toMap(IntentionEmbeddingDTO::getQuestionId, IntentionEmbeddingDTO::getId));

    // 找出新增的向量
    List<IntentionEmbeddingDTO> addedEmbeddings = new ArrayList<>();
    for (IntentionEmbeddingDTO embedding : intentionEmbeddings) {
      Long id = questionIdToIdMap.get(embedding.getQuestionId());
      if (id == null) {
        addedEmbeddings.add(embedding);
      }
      else {
        intentionEmbeddingMapper.update(id, embedding.getQuestion());
      }
    }
    // 批量新增
    batchAdd(addedEmbeddings);
  }

  @Override
  @Transactional
  public void delete(Long tenantId, Long questionId) {
    intentionEmbeddingMapper.deleteByQuestionId(questionId);
  }

  @Override
  @Transactional
  public void deleteByTenant(Long tenantId) {
    intentionEmbeddingMapper.deleteByTenantId(tenantId);
  }

  @Override
  @Nullable
  public Long bestMatch(Long tenantId, @Nullable List<Long> sceneIds, float[] question, float scoreThreshold) {
    List<IntentionMatchItemDTO> items = intentionEmbeddingMapper.selectTopMatches(tenantId, null, question, scoreThreshold, 1);
    return items.isEmpty() ? null : items.get(0).getQuestionId();
  }

  @Override
  public List<IntentionMatchItemDTO> topMatches(Long tenantId, @Nullable List<Long> sceneIds, float[] question, float scoreThreshold, int topNum) {
    return intentionEmbeddingMapper.selectTopMatches(tenantId, null, question, scoreThreshold, topNum);
  }

}
