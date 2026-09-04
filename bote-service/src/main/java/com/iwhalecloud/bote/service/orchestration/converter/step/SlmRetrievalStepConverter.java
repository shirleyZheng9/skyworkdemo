package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.SlmRetrievalStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 微调模型检索步骤转换器
 *
 * @author chen.linfa
 * @since 2025-04-19
 */
public class SlmRetrievalStepConverter extends AbstractStepConverter<SlmRetrievalStep> {
  public SlmRetrievalStepConverter() {
    super(SlmRetrievalStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, SlmRetrievalStep step, ConverterContext context) {
    step.setModelId(parseRequiredJsonAttr(node, "modelId", "大模型", Long.class));
    step.setQuestion(parseRequiredJsonAttr(node, "question", "问题", String.class));
  }
}
