package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.StartStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 开始步骤转换器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class StartStepConverter extends AbstractStepConverter<StartStep> {
  public StartStepConverter() {
    super(StartStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, StartStep step, ConverterContext context) {
    // 不需要处理
  }
}
