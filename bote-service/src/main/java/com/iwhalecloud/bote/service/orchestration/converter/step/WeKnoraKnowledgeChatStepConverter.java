package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.WeKnoraKnowledgeChatStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * WeKnora 知识问答步骤转换器
 *
 * @author huangyunming
 * @since 2026-04-01
 */
public class WeKnoraKnowledgeChatStepConverter extends AbstractStepConverter<WeKnoraKnowledgeChatStep> {

  public WeKnoraKnowledgeChatStepConverter() {
    super(WeKnoraKnowledgeChatStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, WeKnoraKnowledgeChatStep step, ConverterContext context) {
    step.setModelId(parseJsonAttr(node, "modelId", String.class));
    step.setQuestion(parseRequiredJsonAttr(node, "question", "问题", String.class));
    step.setWeKnoraKnowledgeBaseIds(parseRequiredJsonAttr(node, "weKnoraKnowledgeBaseIds", "WeKnora 知识库 ID", String.class));
    step.setKnowledgeId(parseJsonAttr(node, "knowledgeId", String.class));
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
