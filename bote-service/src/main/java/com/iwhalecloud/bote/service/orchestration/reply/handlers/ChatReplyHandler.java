package com.iwhalecloud.bote.service.orchestration.reply.handlers;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.dto.chat.ReplyDownloadMessage;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep.ContentTypeConfig;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.orchestration.reply.AbstractReplyHandler;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 对话接口的回复处理器
 *
 * @author bianjp
 * @since 2025-03-07
 */
public class ChatReplyHandler extends AbstractReplyHandler {
  /** 对话上下文 */
  private final ChatContext context;

  public ChatReplyHandler(ChatContext context) {
    super(context.getEmitter(), context.getRequest().getClientId());
    this.context = context;
  }

  @Override
  protected String sendReply(ChatMessageType type, Object content, @Nullable String code, @Nullable String name, @Nullable String msgId) {
    // 需要使用上下文对象的发送消息方法，以便记录消息到数据库中
    if (ChatMessageType.TEXT.equals(type)) {
      return context.sendTextMessage(type, content, msgId);
    }
    else {
      return context.sendMessage(type, content);
    }
  }

  @Override
  public void setContentType(String msgId, ContentTypeConfig contentType) {
    context.setContentType(Long.parseLong(msgId), contentType);
  }

  @Override
  public void addMessageWhenException(Date startTime, @Nullable String reasoning, @Nullable String text) {
    if (StringUtils.isEmpty(reasoning) && StringUtils.isEmpty(text)) {
      return;
    }
    AnswerDTO answer = new AnswerDTO();
    answer.setReasoning(reasoning);
    // 补充文本，避免历史会话，前面的推理内容无法展示
    answer.setText(StringUtils.defaultIfEmpty(text, "已停止"));
    context.addStreamMessage(context.newMsgId(), startTime, answer);
  }

  @Override
  public void download(Long tenantId, String msgId, String fileType, String content) {
    Assert.notNull(emitter, "emitter cannot be null");
    SseUtil.sendJson(emitter, ChatMessageType.DOWNLOAD, msgId, new ReplyDownloadMessage(fileType));
    context.setDownloadInfo(Long.parseLong(msgId), fileType, content);
  }

  @Override
  public String newMsgId() {
    return context.newMsgId().toString();
  }

  @Override
  public void addStreamMessage(ChatMessageType type, String msgId, Date startTime, String text) {
    super.addStreamMessage(type, msgId, startTime, text);
    AnswerDTO answer = new AnswerDTO();
    answer.setText(text);
    context.addStreamMessage(Long.parseLong(msgId), startTime, answer);
  }

  @Override
  public AnswerDTO stream(SseInvoker invoker, @Nullable String code, @Nullable String name, @Nullable String msgId) {
    Long id;
    if (StringUtils.isNotEmpty(msgId)) {
      id = Long.valueOf(msgId);
    }
    else {
      id = context.newMsgId();
    }
    Date startTime = new Date();
    AnswerDTO answer = collectStream(invoker, id.toString(), code, name);
    // 记录流式消息
    context.addStreamMessage(id, startTime, answer);
    return answer;
  }

  public AnswerDTO createAnswer(SseInvoker invoker, Long msgId) {
    return collectStream(invoker, msgId.toString(), null, null);
  }

}
