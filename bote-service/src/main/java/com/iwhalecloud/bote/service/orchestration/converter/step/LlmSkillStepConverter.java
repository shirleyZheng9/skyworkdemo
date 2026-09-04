package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.LlmSkillStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 大模型能力步骤转换器
 *
 * @author chen.linfa
 * @since 2024-08-06
 */
public class LlmSkillStepConverter extends AbstractStepConverter<LlmSkillStep> {
  public LlmSkillStepConverter() {
    super(LlmSkillStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, LlmSkillStep step, ConverterContext context) {
    step.setApiId(parseRequiredJsonAttr(node, "apiId", "模型能力", Long.class));
    step.setParameters(getInputParams(node));
  }
}
