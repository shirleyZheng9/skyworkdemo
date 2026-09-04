package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.ContinueStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 继续循环步骤转换器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class ContinueStepConverter extends AbstractStepConverter<ContinueStep> {
  public ContinueStepConverter() {
    super(ContinueStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, ContinueStep step, ConverterContext context) {
    // 不需要处理
  }
}
