package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.PluginStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 插件步骤转换器
 *
 * @author qian.sisheng
 * @since 2025-04-22
 */
public class PluginStepConverter extends AbstractStepConverter<PluginStep> {
  public PluginStepConverter() {
    super(PluginStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, PluginStep step, ConverterContext context) {
    step.setPluginId(parseRequiredJsonAttr(node, "pluginId", "插件", Long.class));
    step.setModelId(parseJsonAttr(node, "modelId", String.class));
    step.setIsPluginHub(parseJsonAttr(node, "isPluginHub", Boolean.class));
    step.setToolName(parseJsonAttr(node, "toolName", String.class));
    step.setParameters(getInputParams(node));
    step.setResponse(getOutputParams(node));
  }

}
