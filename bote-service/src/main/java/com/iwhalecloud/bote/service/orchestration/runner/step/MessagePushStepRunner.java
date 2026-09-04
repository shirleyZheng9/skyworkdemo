package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.MessagePushStep;

/**
 * 指令步骤执行器
 *
 * @author chen.linfa
 * @since 2025-06-16
 */
public class MessagePushStepRunner extends AbstractLlmStepRunner<MessagePushStep> {

  @Override
  protected void doRun(SceneOrchestrationContext context, MessagePushStep step) {
    if (ChatMessageType.CONTEXT_ID.getCode().equals(step.getEventType())) {
      // 重置上下文 ID
      context.getReplyHandler().pushMessage(ChatMessageType.CONTEXT_ID, SceneContextUtil.newContextId());
    }
  }
}
