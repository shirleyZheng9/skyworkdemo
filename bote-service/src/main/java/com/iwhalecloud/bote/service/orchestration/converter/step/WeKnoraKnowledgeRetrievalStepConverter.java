package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.WeKnoraKnowledgeRetrievalStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import org.springframework.util.Assert;

/**
 * WeKnora 知识检索步骤转换器
 *
 * @author huangyunming
 * @since 2026-04-01
 */
public class WeKnoraKnowledgeRetrievalStepConverter extends AbstractStepConverter<WeKnoraKnowledgeRetrievalStep> {

  public WeKnoraKnowledgeRetrievalStepConverter() {
    super(WeKnoraKnowledgeRetrievalStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, WeKnoraKnowledgeRetrievalStep step, ConverterContext context) {
    step.setQuestion(parseRequiredJsonAttr(node, "question", "问题", String.class));
    step.setWeKnoraKnowledgeBaseIds(parseRequiredJsonAttr(node, "weKnoraKnowledgeBaseIds", "WeKnora 知识库 ID", String.class));
    step.setKnowledgeId(parseJsonAttr(node, "knowledgeId", String.class));
    step.setDocumentId(parseJsonAttr(node, "documentId", String.class));
    step.setTopK(parseJsonAttr(node, "topK", Integer.class));
    Assert.isTrue(step.getTopK() == null || step.getTopK() > 0, "召回数量必须是正数");
    step.setMinScore(parseJsonAttr(node, "minScore", Float.class));
    step.setKnowledgeExt(parseJsonAttr(node, "knowledgeExt", String.class));
    step.setResourceExt(parseJsonAttr(node, "resourceExt", String.class));
  }
}
