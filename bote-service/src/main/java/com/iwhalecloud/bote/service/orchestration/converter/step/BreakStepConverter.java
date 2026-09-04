package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.BreakStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 中断循环步骤转换器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class BreakStepConverter extends AbstractStepConverter<BreakStep> {
  public BreakStepConverter() {
    super(BreakStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, BreakStep step, ConverterContext context) {
    // 不需要处理
  }
}
