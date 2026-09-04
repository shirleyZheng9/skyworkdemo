package com.iwhalecloud.bote.doc.module.knowledge.semantic;

/**
 * 知识库问答记录标准化服务
 *
 * @author qian.sisheng
 * @since 2026/04/02
 */
public interface IBtDcQaKnowledgeVectorService {

  /**
   * 提问向量嵌入
   */
  void applyStandardQuestion(Long tenantId, Long qaId, String msgId, String question);

  /**
   * 按批查询 standard_question 为空的记录
   *
   * @param batchSize 批量大小
   */
  void backfillPendingStandardQuestions(int batchSize);
}
