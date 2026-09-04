package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.ParamExtractorStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import java.util.List;

/**
 * 参数提取步骤转换器
 *
 * @author bianjp
 * @since 2024-09-04
 */
public class ParamExtractorStepConverter extends AbstractStepConverter<ParamExtractorStep> {
  public ParamExtractorStepConverter() {
    super(ParamExtractorStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, ParamExtractorStep step, ConverterContext context) {
    step.setModelId(parseRequiredJsonAttr(node, "modelId", "大模型", String.class));
    step.setInput(parseRequiredJsonAttr(node, "input", "输入", String.class));
    Long promptId = parseJsonAttr(node, "promptId", Long.class);
    if (promptId != null) {
      step.setPromptId(promptId);
      step.setPromptParameters(parseJsonAttr(node, "promptParameters", new TypeReference<List<ParameterSpec>>() {
      }));
    }
    else {
      step.setInstruction(parseJsonAttr(node, "instruction", String.class));
    }
    step.setParameters(parseRequiredJsonAttr(node, "parameters", "提取参数", ParameterSpec.class));
    step.setMemory(parseJsonAttr(node, "memory", MemoryConfig.class));
    step.setCustomModelConfig(parseJsonAttr(node, "customModelConfig", CustomModelConfig.class));
  }
}
