package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeRetrievalStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import org.springframework.util.Assert;

/**
 * 知识库步骤转换器
 *
 * @author chen.linfa
 * @since 2024-08-06
 */
public class KnowledgeRetrievalStepConverter extends AbstractStepConverter<KnowledgeRetrievalStep> {
  public KnowledgeRetrievalStepConverter() {
    super(KnowledgeRetrievalStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, KnowledgeRetrievalStep step, ConverterContext context) {
    step.setQuestion(parseRequiredJsonAttr(node, "question", "问题", String.class));
    step.setKnowledgeId(parseRequiredJsonAttr(node, "knowledgeId", "知识库", String.class));
    step.setDocumentId(parseJsonAttr(node, "documentId", String.class));
    step.setTopK(parseJsonAttr(node, "topK", Integer.class));
    Assert.isTrue(step.getTopK() == null || step.getTopK() > 0, "召回数量必须是正数");
    step.setMinScore(parseJsonAttr(node, "minScore", Float.class));
    step.setKnowledgeExt(parseJsonAttr(node, "knowledgeExt", String.class));
    step.setResourceExt(parseJsonAttr(node, "resourceExt", String.class));
  }
}
