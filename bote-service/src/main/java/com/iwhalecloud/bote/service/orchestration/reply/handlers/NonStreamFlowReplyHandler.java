package com.iwhalecloud.bote.service.orchestration.reply.handlers;

import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.service.orchestration.reply.AbstractReplyHandler;
import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * 工作流执行接口的非流式回复处理器
 *
 * @author bianjp
 * @since 2025-03-10
 */
@Getter
public class NonStreamFlowReplyHandler extends AbstractReplyHandler {
  public NonStreamFlowReplyHandler(@Nullable String clientId) {
    super(null, clientId);
  }

  @Override
  public AnswerDTO stream(SseInvoker invoker, @Nullable String code, @Nullable String name, @Nullable String msgId) {
    return collectStream(invoker, msgId, code, name);
  }
}
