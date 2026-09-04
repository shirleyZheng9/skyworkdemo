package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.SetVariableStep;
import com.iwhalecloud.bote.dto.orchestration.step.SetVariableStep.VariableSpec;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import java.util.List;

/**
 * 设置变量步骤转换器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class SetVariableStepConverter extends AbstractStepConverter<SetVariableStep> {
  public SetVariableStepConverter() {
    super(SetVariableStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, SetVariableStep step, ConverterContext context) {
    step.setVariables(parseJsonAttr(node, "variables", new TypeReference<List<VariableSpec>>() {
    }));
  }
}
