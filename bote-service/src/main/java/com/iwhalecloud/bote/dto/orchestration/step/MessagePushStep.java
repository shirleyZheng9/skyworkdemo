package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * 指令步骤
 *
 * @author chen.linfa
 * @since 2025-06-16
 */
@Getter
@Setter
public class MessagePushStep extends AbstractStep {
  /** 消息内容 */
  private String messageContent;
  /** 事件类型 */
  private String eventType;

  public MessagePushStep() {
    super(StepType.MESSAGE_PUSH);
  }
}
