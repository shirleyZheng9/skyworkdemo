package com.iwhalecloud.bote.service.orchestration.reply.handlers;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.service.orchestration.reply.AbstractReplyHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 工作流执行接口的流式回复处理器
 *
 * @author bianjp
 * @since 2025-03-10
 */
public class StreamFlowReplyHandler extends AbstractReplyHandler {
  public StreamFlowReplyHandler(SseEmitter emitter, @Nullable String clientId) {
    super(emitter, clientId);
  }

  @Override
  public AnswerDTO stream(SseInvoker invoker, @Nullable String code, @Nullable String name, @Nullable String msgId) {
    if (StringUtils.isEmpty(msgId)) {
      msgId = Sequences.BOT_SESSION_MSG_ID.next() + "";
    }
    // 发送步骤信息
    if (emitter != null && StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(name)) {
      SseUtil.sendJson(emitter, ChatMessageType.CURRENT_STEP_INFO, msgId, ImmutableMap.of("code", code, "name", name));
    }
    return collectStream(invoker, msgId, code, name);
  }
}
