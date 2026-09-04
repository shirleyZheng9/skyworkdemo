package com.iwhalecloud.bote.doc.module.knowledge.semantic.impl;

import com.iwhalecloud.bote.doc.module.knowledge.semantic.IKnowledgeQuestionEmbeddingService;

/**
 * 文档中心独立部署时的 embedding 占位实现。
 * <p>完整实现见 bote-service 模块 {@code IKnowledgeQuestionEmbeddingServiceImpl}，
 * 集成部署主博特服务时会自动替换本 Bean。</p>
 */
public class FallbackKnowledgeQuestionEmbeddingService implements IKnowledgeQuestionEmbeddingService {

  @Override
  public float[] embed(Long tenantId, String text) {
    return null;
  }
}
