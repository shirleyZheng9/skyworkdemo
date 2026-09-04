package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.PlaywrightStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * PlayWright 自动化步骤转换器
 *
 * @author bianjp
 * @since 2026-01-14
 */
public class PlaywrightStepConverter extends AbstractStepConverter<PlaywrightStep> {
  public PlaywrightStepConverter() {
    super(PlaywrightStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, PlaywrightStep step, ConverterContext context) {
    step.setUseCustomChatId(parseJsonAttr(node, "useCustomChatId", Boolean.class));
    if (Boolean.TRUE.equals(step.getUseCustomChatId())) {
      step.setChatId(parseJsonAttr(node, "chatId", String.class));
    }
    step.setAutoRelease(parseJsonAttr(node, "autoRelease", Boolean.class));
    //noinspection SpellCheckingInspection
    step.setVncEnabled(parseJsonAttr(node, "vncEnabled", Boolean.class));
    //noinspection SpellCheckingInspection
    step.setVncReadonly(parseJsonAttr(node, "vncReadonly", Boolean.class));
    step.setScriptContent(parseRequiredJsonAttr(node, "scriptContent", "脚本内容", String.class));
    step.setFiles(parseJsonAttr(node, "files", String.class));
    step.setParameters(getInputParams(node));
    step.setOutData(getOutputParams(node));
  }
}
