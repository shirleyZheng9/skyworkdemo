package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.MessagePushStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 指令步骤转换器
 *
 * @author chen.linfa
 * @since 2025-06-16
 */
public class MessagePushStepConverter extends AbstractStepConverter<MessagePushStep> {
  public MessagePushStepConverter() {
    super(MessagePushStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, MessagePushStep step, ConverterContext context) {
    step.setEventType(parseRequiredJsonAttr(node, "eventType", "指令类型", String.class));
    step.setMessageContent(parseJsonAttr(node, "messageContent", String.class));
  }
}
