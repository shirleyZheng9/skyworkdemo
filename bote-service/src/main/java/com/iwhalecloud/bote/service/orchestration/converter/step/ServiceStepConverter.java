package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.ServiceStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 服务 API 步骤转换器
 *
 * @author chen.linfa
 * @since 2024-08-06
 */
public class ServiceStepConverter extends AbstractStepConverter<ServiceStep> {
  public ServiceStepConverter() {
    super(ServiceStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, ServiceStep step, ConverterContext context) {
    step.setServiceId(parseRequiredJsonAttr(node, "serviceId", "服务", Long.class));
    step.setParameters(getInputParams(node));
    step.setResponse(getOutputParams(node));
  }
}
