package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.EndStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 结束步骤转换器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class EndStepConverter extends AbstractStepConverter<EndStep> {
  public EndStepConverter() {
    super(EndStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, EndStep step, ConverterContext context) {
    step.setParameters(getInputParams(node));
  }
}
