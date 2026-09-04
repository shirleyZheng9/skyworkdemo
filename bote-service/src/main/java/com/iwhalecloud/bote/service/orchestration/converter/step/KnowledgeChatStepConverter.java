package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeChatStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * 知识问答步骤转换器
 *
 * @author bianjp
 * @since 2024-12-18
 */
public class KnowledgeChatStepConverter extends AbstractStepConverter<KnowledgeChatStep> {
  public KnowledgeChatStepConverter() {
    super(KnowledgeChatStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, KnowledgeChatStep step, ConverterContext context) {
    step.setModelId(parseJsonAttr(node, "modelId", String.class));
    step.setQuestion(parseRequiredJsonAttr(node, "question", "问题", String.class));
    step.setKnowledgeId(parseRequiredJsonAttr(node, "knowledgeId", "知识库", String.class));
    step.setDocumentId(parseJsonAttr(node, "documentId", String.class));
    String promptContent = StringUtils.trim(parseJsonAttr(node, "promptContent", String.class));
    if (StringUtils.isNotEmpty(promptContent)) {
      Assert.isTrue(promptContent.contains("{context}"), "提示词必须包含 {context}");
      Assert.isTrue(promptContent.contains("{question}"), "提示词必须包含 {question}");
    }
    step.setPromptContent(promptContent);
    step.setMemory(parseJsonAttr(node, "memory", MemoryConfig.class));
    step.setStream(parseJsonAttr(node, "stream", Boolean.class));
    step.setWithReferences(parseJsonAttr(node, "withReferences", Boolean.class));
    step.setWithQuestions(parseJsonAttr(node, "withQuestions", Boolean.class));
    step.setWithChatLog(parseJsonAttr(node, "withChatLog", Boolean.class));
    step.setThinkingStrategy(parseJsonAttr(node, "thinkingStrategy", ThinkingStrategy.class));
    step.setKnowledgeExt(parseJsonAttr(node, "knowledgeExt", String.class));
    step.setResourceExt(parseJsonAttr(node, "resourceExt", String.class));
    step.setCustomModelConfig(parseJsonAttr(node, "customModelConfig", CustomModelConfig.class));
  }
}
