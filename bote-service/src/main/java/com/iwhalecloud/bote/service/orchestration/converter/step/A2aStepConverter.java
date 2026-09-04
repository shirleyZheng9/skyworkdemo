package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.A2aStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * A2A 服务步骤转换器
 *
 * @author bianjp
 * @since 2025-10-21
 */
public class A2aStepConverter extends AbstractStepConverter<A2aStep> {
  public A2aStepConverter() {
    super(A2aStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, A2aStep step, ConverterContext context) {
    step.setAgentId(parseRequiredJsonAttr(node, "agentId", "A2A 服务", Long.class));
    step.setStream(parseJsonAttr(node, "stream", Boolean.class));
    step.setNotificationFlowId(parseJsonAttr(node, "notificationFlowId", Long.class));
    step.setTaskConfig(parseJsonAttr(node, "taskConfig", A2aStep.A2aTaskConfig.class));
    step.setMessageText(parseJsonAttr(node, "messageText", String.class));
    step.setMessageData(parseJsonAttr(node, "messageData", String.class));
    step.setMessageFile(parseJsonAttr(node, "messageFile", String.class));
    step.setMetadata(parseJsonAttr(node, "metadata", String.class));
    Assert.isTrue(!StringUtils.isAllEmpty(step.getMessageText(), step.getMessageData(), step.getMessageFile()), "消息内容、消息数据、消息文件不能同时为空");
  }
}
