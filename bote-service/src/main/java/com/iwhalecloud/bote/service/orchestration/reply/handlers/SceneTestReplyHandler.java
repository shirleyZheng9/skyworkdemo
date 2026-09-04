package com.iwhalecloud.bote.service.orchestration.reply.handlers;

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
 * 复杂场景、对话型工作流调试的回复处理器
 *
 * @author bianjp
 * @since 2025-03-10
 */
public class SceneTestReplyHandler extends AbstractReplyHandler {
  public SceneTestReplyHandler(SseEmitter emitter, @Nullable String clientId) {
    super(emitter, clientId);
  }

  @Override
  public AnswerDTO stream(SseInvoker invoker, @Nullable String code, @Nullable String name, @Nullable String msgId) {
    if (StringUtils.isEmpty(msgId)) {
      msgId = Sequences.BOT_SESSION_MSG_ID.next() + "";
    }
    AnswerDTO answer = collectStream(invoker, msgId, code, name);
    // TODO 暂时特殊处理，后续需要优化判断方式
    // 向前端发送 Agent 节点的记忆内容（与发给前端的 text 事件内容有些差异），以保持调试和聊天窗口中的记忆内容一致（聊天窗口中从会话表获取记忆内容）
    if (answer.getMemoryContent() != null && emitter != null) {
      SseUtil.sendJson(emitter, ChatMessageType.AGENT_REPLY, answer.getMsgId(), answer.getMemoryContent());
    }
    return answer;
  }
}
