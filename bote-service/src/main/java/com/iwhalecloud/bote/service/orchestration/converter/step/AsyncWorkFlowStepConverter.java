package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.AsyncWorkFlowStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 异步工作流步骤转换器
 *
 * @author wangtingyun
 * @since 2026-02-25
 */
public class AsyncWorkFlowStepConverter extends AbstractStepConverter<AsyncWorkFlowStep> {
  public AsyncWorkFlowStepConverter() {
    super(AsyncWorkFlowStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, AsyncWorkFlowStep step, ConverterContext context) {
    step.setFlowId(parseRequiredJsonAttr(node, "flowId", "工作流", Long.class));
    step.setParameters(getInputParams(node));
    step.setResponse(getOutputParams(node));
  }
}
