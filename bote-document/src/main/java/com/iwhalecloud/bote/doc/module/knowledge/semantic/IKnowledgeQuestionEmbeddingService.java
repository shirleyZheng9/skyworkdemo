package com.iwhalecloud.bote.doc.module.knowledge.semantic;

/**
 * 知识库提问向量化
 *
 * @author qian.sisheng
 * @since 2026/04/02
 */
public interface IKnowledgeQuestionEmbeddingService {

  /**
   * 对文本做 embedding
   *
   * @param tenantId 租户 ID
   * @param text     文本
   */
  float[] embed(Long tenantId, String text);
}
