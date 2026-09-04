package com.iwhalecloud.bote.service.orchestration.reply.handlers;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.dto.chat.ReplyDTO;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep.ContentTypeConfig;
import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bote.service.orchestration.reply.ReplyHandler;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.lang.Nullable;

/**
 * 子智能体专用回复处理器：仅收集子智能体输出作为工具结果，不向主 handler 推送回复/流式内容，避免与主智能体总结重复展示。
 *
 * @author chen.linfa
 * @since 2026-03-16
 */
public class SubSceneReplyHandler implements ReplyHandler {

  private final ReplyHandler delegate;
  private final List<ReplyDTO> subReplies = new CopyOnWriteArrayList<>();

  public SubSceneReplyHandler(ReplyHandler delegate) {
    this.delegate = delegate;
  }

  @Override
  public List<ReplyDTO> getReplies() {
    return subReplies;
  }

  @Override
  @Nullable
  public String getClientId() {
    return delegate.getClientId();
  }

  @Override
  public void reply(ChatMessageType type, Object content, @Nullable String code, @Nullable String name) {
    if (type.isPersist()) {
      subReplies.add(new ReplyDTO(type, content, code, name));
    }
    // 不委托：子智能体完整输出已作为工具结果由主智能体总结，避免重复展示
  }

  @Override
  public void replyText(ChatMessageType type, Object content, String code, String name, @Nullable String msgId) {
    subReplies.add(new ReplyDTO(type, content, code, name));
    // 不委托，避免重复展示
  }

  @Override
  public void replyRelated(ChatMessageType type, Object content, String code, String name) {
    delegate.replyRelated(type, content, code, name);
  }

  @Override
  public void download(Long tenantId, String msgId, String fileType, String content) {
    delegate.download(tenantId, msgId, fileType, content);
  }

  @Override
  public String sentContentType(ContentTypeConfig contentType) {
    return delegate.sentContentType(contentType);
  }

  @Override
  public void setContentType(String msgId, ContentTypeConfig contentType) {
    delegate.setContentType(msgId, contentType);
  }

  @Override
  public void addMessageWhenException(Date startTime, @Nullable String reasoning, @Nullable String text) {
    // 不委托：异常由工具结果与主智能体统一呈现
  }

  @Override
  public void pushMessage(ChatMessageType type, Object content) {
    // 不委托：避免子场景 CONTEXT_ID/SCENE 等推送到前端造成重复或切换
  }

  @Override
  public void updatePlanState(PlanRecordDTO record) {
    delegate.updatePlanState(record);
  }

  @Override
  public String newMsgId() {
    return delegate.newMsgId();
  }

  @Override
  public void sendStreamText(ChatMessageType type, String msgId, String partialText) {
    // 不委托：流式内容仅用于收集，由工具结果交给主智能体总结后一次性展示
  }

  @Override
  public void sendMessage(ChatMessageType type, String msgId, Object data) {
    // 不委托，避免子智能体中间事件推送到前端
  }

  @Override
  public void addStreamMessage(ChatMessageType type, String msgId, Date startTime, String text) {
    subReplies.add(new ReplyDTO(type, text, null, null));
    // 不委托，避免重复展示
  }

  @Override
  public AnswerDTO stream(SseInvoker invoker, @Nullable String code, @Nullable String name, @Nullable String msgId) {
    // 使用仅收集不推送的 handler 执行，避免子智能体流式内容推送到前端造成重复
    AnswerDTO answer = new NonStreamChatReplyHandler(null).stream(invoker, code, name, msgId);
    subReplies.add(new ReplyDTO(answer));
    return answer;
  }
}
