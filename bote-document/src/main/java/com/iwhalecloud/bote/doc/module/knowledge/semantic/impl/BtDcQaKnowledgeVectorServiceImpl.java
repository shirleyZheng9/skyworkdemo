package com.iwhalecloud.bote.doc.module.knowledge.semantic.impl;

import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.BtDcQaRecordManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.IBtDcQaKnowledgeVectorService;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.dto.IndexDocDTO;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.dto.KnowledgeQuestionDTO;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.dto.PendingKnowledgeQuestionDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 知识库问答记录标准化
 *
 * @author qian.sisheng
 * @since 2026/04/02
 */
@Service
@RequiredArgsConstructor
public class BtDcQaKnowledgeVectorServiceImpl implements IBtDcQaKnowledgeVectorService {

  private static final Logger logger = LoggerFactory.getLogger(BtDcQaKnowledgeVectorServiceImpl.class);
  /** 相似度阈值 */
  private static final float SIMILARITY_THRESHOLD = 0.85f;
  /** 邻近向量数量 */
  private static final int NEIGHBOR_TOP_K = 5;
  /** 存量回填读取分片大小（按库表批次抓取） */
  private static final int BACKFILL_CHUNK_SIZE = 50;

  private final KnowledgeQuestionVectorHelper questionVectorHelper;
  private final BtDcQaRecordManageMapper btDcQaRecordManageMapper;


  @Override
  @Transactional
  public void applyStandardQuestion(Long tenantId, Long qaId, String msgId, String question) {
    if (StringUtils.isBlank(question)) {
      return;
    }
    // 检查Elasticsearch和向量化模型是否可用
    if (!questionVectorHelper.isQuestionEmbeddingAvailable()) {
      return;
    }
    try {
      // 向量化
      float[] vector = questionVectorHelper.embed(tenantId, question);
      if (vector == null || vector.length == 0) {
        if (logger.isWarnEnabled()) {
          logger.warn("问答记录标准问法向量化失败, qaId={}, tenantId={}, question={}", qaId, tenantId, question);
        }
        return;
      }
      // 获取标准问题
      CanonicalQuestionResult result = getCanonicalQuestion(tenantId, question, vector);
      // 写入Elasticsearch
      IndexDocDTO indexDoc = new IndexDocDTO(msgId, question, result.getCanonical(), vector, tenantId, result.getScore());
      questionVectorHelper.indexDocument(indexDoc);
      // 更新bt_dc_qa_record标准问题
      btDcQaRecordManageMapper.updateStandardQuestionByQaId(tenantId, qaId, result.getCanonical());
    }
    catch (Exception e) {
      logger.error("问答记录标准问法向量化失败qaId={}, tenantId={}", qaId, tenantId, e);
      throw new BssException("问答记录标准问法向量化失败", e);
    }
  }

  @Override
  @Transactional
  public void backfillPendingStandardQuestions(int batchSize) {
    // 检查Elasticsearch和向量化模型是否可用
    if (!questionVectorHelper.isQuestionEmbeddingAvailable()) {
      return;
    }
    // 获取待处理的存量数据
    List<PendingKnowledgeQuestionDTO> pendingQuestions = btDcQaRecordManageMapper.selectPendingForStandardQuestion(batchSize);
    if (CollectionUtils.isEmpty(pendingQuestions)) {
      return;
    }
    // 过滤掉问题为空的记录
    List<PendingKnowledgeQuestionDTO> questions = pendingQuestions.stream()
      .filter(row -> StringUtils.isNotBlank(row.getQuestion()) && StringUtils.isNotBlank(row.getSessionId()) && row.getTenantId() != null)
      .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(questions)) {
      return;
    }
    int chunkSize = Math.min(BACKFILL_CHUNK_SIZE, batchSize);
    for (int i = 0; i < questions.size(); i += chunkSize) {
      processBackfillChunk(questions.subList(i, Math.min(i + chunkSize, questions.size())));
    }
  }

  /**
   * 处理批量存量数据
   */
  private void processBackfillChunk(List<PendingKnowledgeQuestionDTO> chunk) {
    Map<Long, List<PendingKnowledgeQuestionDTO>> byTenant = chunk.stream()
      .filter(row -> row.getTenantId() != null)
      .collect(Collectors.groupingBy(PendingKnowledgeQuestionDTO::getTenantId, LinkedHashMap::new, Collectors.toList()));
    for (Map.Entry<Long, List<PendingKnowledgeQuestionDTO>> entry : byTenant.entrySet()) {
      Long tenantId = entry.getKey();
      for (PendingKnowledgeQuestionDTO row : entry.getValue()) {
        try {
          String question = row.getQuestion();
          float[] vector = questionVectorHelper.embed(tenantId, question);
          if (vector == null || vector.length == 0) {
            continue;
          }
          // 逐条检索+逐条写入，让同批后续数据可命中前序新增向量
          CanonicalQuestionResult result = getCanonicalQuestion(tenantId, question, vector);
          questionVectorHelper.indexDocument(
            new IndexDocDTO(row.getSessionId(), question, result.getCanonical(), vector, tenantId, result.getScore()));
          // 更新bt_dc_qa_record标准问题
          btDcQaRecordManageMapper.updateStandardQuestionByQaId(tenantId, row.getQaId(), result.getCanonical());
        }
        catch (Exception e) {
          if (logger.isErrorEnabled()) {
            logger.error("问答记录标准问法回填失败 qaId={}", row.getQaId(), e);
          }
          throw new BssException("问答记录标准问法回填失败", e);
        }
      }
    }
  }

  /**
   * 获取标准问题
   */
  private CanonicalQuestionResult getCanonicalQuestion(Long tenantId, String question, float[] vector) {
    String scoreThreshold = BaseSystemParameter.KNOWLEDGE_DOCUMENT_EMBEDDING_SCORE.getValueFromDb();
    float scoreThresholdValue = StringUtils.isNotBlank(scoreThreshold) ? Float.parseFloat(scoreThreshold) : SIMILARITY_THRESHOLD;
    List<KnowledgeQuestionDTO> neighbors = questionVectorHelper.topMatches(tenantId, vector, scoreThresholdValue, NEIGHBOR_TOP_K);
    if (CollectionUtils.isEmpty(neighbors)) {
      return new CanonicalQuestionResult(question, 0.0f);
    }
    // 获取第一个相似问题，如果存在，则返回其标准问题
    KnowledgeQuestionDTO first = neighbors.getFirst();
    float score = first.getScore();
    String standard = first.getStandardQuestion();
    if (StringUtils.isNotBlank(standard)) {
      return new CanonicalQuestionResult(standard, score);
    }
    // 获取第一个相似问题，如果存在，则返回其问题内容
    String neighborQuestion = first.getQuestion();
    if (StringUtils.isNotBlank(neighborQuestion)) {
      return new CanonicalQuestionResult(neighborQuestion, score);
    }
    return new CanonicalQuestionResult(question, score);
  }


  @Setter
  @Getter
  @AllArgsConstructor
  private static final class CanonicalQuestionResult {
    /** 标准问题 */
    private final String canonical;
    /** 相似度得分 */
    private final Float score;
  }
}
