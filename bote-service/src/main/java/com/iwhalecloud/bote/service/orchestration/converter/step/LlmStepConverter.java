package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.model.VisionConfig;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.LlmStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;

/**
 * 大模型步骤转换器
 *
 * @author chen.linfa
 * @since 2024-08-06
 */
public class LlmStepConverter extends AbstractLlmStepConverter<LlmStep> {
  public LlmStepConverter() {
    super(LlmStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, LlmStep step, ConverterContext context) {
    step.setModelId(parseRequiredJsonAttr(node, "modelId", "大模型", String.class));
    step.setMessages(parseMessages(node));
    parsePrompt(node, step::setPromptId, step::setPromptParameters, step::setUserMessage, "userMessage", "消息内容");
    step.setVision(parseJsonAttr(node, "vision", VisionConfig.class));
    step.setStream(parseJsonAttr(node, "stream", Boolean.class));
    step.setMemory(parseJsonAttr(node, "memory", MemoryConfig.class));
    step.setThinkingStrategy(parseJsonAttr(node, "thinkingStrategy", ThinkingStrategy.class));
    step.setCustomModelConfig(parseJsonAttr(node, "customModelConfig", CustomModelConfig.class));
  }

}
