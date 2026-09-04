package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.McpToolStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * MCP 工具步骤转换器
 *
 * @author bianjp
 * @since 2025-07-14
 */
public class McpToolStepConverter extends AbstractStepConverter<McpToolStep> {
  public McpToolStepConverter() {
    super(McpToolStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, McpToolStep step, ConverterContext context) {
    step.setPlatform(parseJsonAttr(node, "platform", Boolean.class));
    step.setServerId(parseRequiredJsonAttr(node, "serverId", "工具", Long.class));
    step.setToolName(parseRequiredJsonAttr(node, "toolName", "工具名称", String.class));
    step.setParameters(getInputParams(node));
  }

}
