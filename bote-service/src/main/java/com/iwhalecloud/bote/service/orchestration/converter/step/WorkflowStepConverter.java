package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.WorkflowStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 工作流步骤转换器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class WorkflowStepConverter extends AbstractStepConverter<WorkflowStep> {
  public WorkflowStepConverter() {
    super(WorkflowStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, WorkflowStep step, ConverterContext context) {
    step.setFlowId(parseRequiredJsonAttr(node, "flowId", "工作流", Long.class));
    step.setParameters(getInputParams(node));
    step.setResponse(getOutputParams(node));
  }

}
