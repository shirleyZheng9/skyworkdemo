package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.ScriptStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 代码块步骤转换器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class ScriptStepConverter extends AbstractStepConverter<ScriptStep> {
  public ScriptStepConverter() {
    super(ScriptStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, ScriptStep step, ConverterContext context) {
    step.setScriptType(parseRequiredJsonAttr(node, "scriptType", "脚本类型", String.class));
    step.setScriptContent(parseRequiredJsonAttr(node, "scriptContent", "脚本内容", String.class));
    step.setPyPackageList(parseJsonAttr(node, "pyPackageList", new TypeReference<>() { }));
    step.setParameters(getInputParams(node));
    step.setOutData(getOutputParams(node));
  }
}
