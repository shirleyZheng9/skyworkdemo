package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeGraphKnowledgeChatStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import org.springframework.util.Assert;

/**
 * knowledgeGraph 知识问答步骤转换器
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
public class KnowledgeGraphKnowledgeChatStepConverter extends AbstractStepConverter<KnowledgeGraphKnowledgeChatStep> {
  public KnowledgeGraphKnowledgeChatStepConverter() {
    super(KnowledgeGraphKnowledgeChatStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, KnowledgeGraphKnowledgeChatStep step, ConverterContext context) {
    step.setModelId(parseJsonAttr(node, "modelId", String.class));
    step.setQuestion(parseRequiredJsonAttr(node, "question", "问题", String.class));
    step.setKnowledgeGraphName(parseRequiredJsonAttr(node, "knowledgeGraphName", "knowledgeGraph 知识库名称", String.class));
    step.setPromptContent(parseJsonAttr(node, "promptContent", String.class));
    step.setStream(parseJsonAttr(node, "stream", Boolean.class));
    step.setWithReferences(parseJsonAttr(node, "withReferences", Boolean.class));
    step.setWithQuestions(parseJsonAttr(node, "withQuestions", Boolean.class));
    step.setWithChatLog(parseJsonAttr(node, "withChatLog", Boolean.class));
    Assert.hasText(step.getKnowledgeGraphName(), "knowledgeGraph 知识库名称不能为空");
  }
}
