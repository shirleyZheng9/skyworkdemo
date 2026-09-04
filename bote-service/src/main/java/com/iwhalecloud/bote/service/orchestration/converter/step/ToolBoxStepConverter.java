package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.ToolboxStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 工具箱函数步骤转换器
 *
 * @author chen.linfa
 * @since 2024-08-06
 */
public class ToolBoxStepConverter extends AbstractStepConverter<ToolboxStep> {
  public ToolBoxStepConverter() {
    super(ToolboxStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, ToolboxStep step, ConverterContext context) {
    step.setFuncId(parseRequiredJsonAttr(node, "funcId", "服务函数", Long.class));
    step.setParameters(getInputParams(node));
    step.setResponse(getOutputParams(node));
  }
}
