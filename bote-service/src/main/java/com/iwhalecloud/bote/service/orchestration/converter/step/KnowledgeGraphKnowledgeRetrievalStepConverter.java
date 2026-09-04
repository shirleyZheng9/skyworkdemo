package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeGraphKnowledgeRetrievalStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import org.springframework.util.Assert;

/**
 * knowledgeGraph 知识检索步骤转换器
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
public class KnowledgeGraphKnowledgeRetrievalStepConverter extends AbstractStepConverter<KnowledgeGraphKnowledgeRetrievalStep> {
  public KnowledgeGraphKnowledgeRetrievalStepConverter() {
    super(KnowledgeGraphKnowledgeRetrievalStep::new);
  }

  /**
   * 将 DSL 节点转换为 knowledgeGraph 检索步骤
   */
  @Override
  protected void convertStep(SceneGraphNodeDTO node, KnowledgeGraphKnowledgeRetrievalStep step, ConverterContext context) {
    step.setQuestion(parseRequiredJsonAttr(node, "question", "问题", String.class));
    step.setKnowledgeGraphName(parseRequiredJsonAttr(node, "knowledgeGraphName", "knowledgeGraph知识库名称", String.class));
    step.setTopK(parseJsonAttr(node, "topK", Integer.class));
    step.setMinScore(parseJsonAttr(node, "minScore", Float.class));
    Assert.isTrue(step.getTopK() == null || step.getTopK() > 0, "召回数量必须是正数");
  }
}
