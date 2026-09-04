package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.SceneStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 智能体 步骤转换器
 *
 * @author chen.linfa
 * @since 2025-07-01
 */
public class SceneStepConverter extends AbstractStepConverter<SceneStep> {
  public SceneStepConverter() {
    super(SceneStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, SceneStep step, ConverterContext context) {
    step.setSceneId(parseRequiredJsonAttr(node, "sceneId", "智能体", Long.class));
    step.setMessageContent(parseRequiredJsonAttr(node, "messageContent", "消息内容", String.class));
  }
}
