package com.iwhalecloud.bote.embedding;

import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.IKnowledgeQuestionEmbeddingService;
import com.iwhalecloud.bote.llm.client.EmbeddingClient;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 知识库提问向量嵌入
 *
 * @author qian.sisheng
 * @since 2026/04/02
 */
@Service
@RequiredArgsConstructor
public class IKnowledgeQuestionEmbeddingServiceImpl implements IKnowledgeQuestionEmbeddingService {

  private final ModelClientCache modelClientCache;

  @Override
  public float[] embed(Long tenantId, String text) {
    String embeddingModelId = BaseSystemParameter.KNOWLEDGE_QUESTION_EMBEDDING_MODEL_ID.getValueFromDb();
    if (StringUtils.isBlank(embeddingModelId)) {
      return null;
    }
    EmbeddingClient client = modelClientCache.getEmbeddingClient(tenantId, Long.valueOf(embeddingModelId));
    return client.embedding(text);
  }
}
