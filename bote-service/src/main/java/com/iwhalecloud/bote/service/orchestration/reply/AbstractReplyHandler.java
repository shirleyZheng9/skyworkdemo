package com.iwhalecloud.bote.service.orchestration.reply;

import com.iwhalecloud.bote.cache.PlanContextCache;
import com.iwhalecloud.bote.cache.ReplyDownloadCache;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.KnowledgeChatLogSseEvent;
import com.iwhalecloud.bote.common.sse.event.QuestionsSseEvent;
import com.iwhalecloud.bote.common.sse.event.ReasoningSseEvent;
import com.iwhalecloud.bote.common.sse.event.ReferencesSseEvent;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.sse.event.TextSseEvent;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.dto.chat.ReplyDTO;
import com.iwhalecloud.bote.dto.chat.ReplyDownloadInfoDTO;
import com.iwhalecloud.bote.dto.chat.ReplyDownloadMessage;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep.ContentTypeConfig;
import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bote.dto.search.response.BochaSearchResponse.WebPageInfoGroup;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner.ExecuteExceptionBranchException;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner.FallbackOutputException;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 回复处理器抽象类
 *
 * @author bianjp
 * @since 2025-03-07
 */
public abstract class AbstractReplyHandler implements ReplyHandler {
  private static final ReplyDownloadCache replyDownloadCache = SpringUtil.getBean(ReplyDownloadCache.class);
  private static final PlanContextCache planContextCache = SpringUtil.getBean(PlanContextCache.class);

  /** SSE 触发器 */
  @Nullable
  protected final SseEmitter emitter;

  /** 客户端 ID */
  @Nullable
  protected final String clientId;

  /** 回复列表 */
  protected final List<ReplyDTO> replies = new CopyOnWriteArrayList<>();

  public AbstractReplyHandler(@Nullable SseEmitter emitter, @Nullable String clientId) {
    this.emitter = emitter;
    this.clientId = clientId;
  }

  @Override
  @Nullable
  public String getClientId() {
    return clientId;
  }

  @Override
  public List<ReplyDTO> getReplies() {
    return replies;
  }

  @Override
  public final void reply(ChatMessageType type, Object content, @Nullable String code, @Nullable String name) {
    // 只记录需要持久化的回复
    if (type.isPersist()) {
      replies.add(new ReplyDTO(type, content, code, name));
    }
    sendReply(type, content, code, name, null);
  }

  @Override
  public final void replyText(ChatMessageType type, Object content, String code, String name, @Nullable String msgId) {
    // 记录回复
    replies.add(new ReplyDTO(type, content, code, name));
    sendReply(type, content, code, name, msgId);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void replyRelated(ChatMessageType type, Object content, String code, String name) {
    // 关联数据追加在对应的文本消息
    ReplyDTO reply = IterableUtils.find(replies, p -> Objects.equals(code, p.getCode()) && Objects.equals(name, p.getName()));
    if (reply != null) {
      if (ChatMessageType.WEB_PAGE_INFO == type) {
        reply.setPageInfos((List<WebPageInfoGroup>) content);
      }
      if (ChatMessageType.FILE_INFO == type) {
        reply.setFileInfos((List<FileInfoDTO>) content);
      }
    }
    sendReply(type, content, code, name, null);
  }

  /**
   * 发送回复
   */
  @Nullable
  protected String sendReply(ChatMessageType type, Object content, @Nullable String code, @Nullable String name, @Nullable String msgId) {
    if (emitter != null) {
      if (StringUtils.isEmpty(msgId)) {
        msgId = Sequences.BOT_SESSION_MSG_ID.next() + "";
      }
      SseUtil.sendJson(emitter, type, msgId, content);
    }
    return msgId;
  }

  @Override
  public String sentContentType(ContentTypeConfig contentType) {
    String id = Sequences.BOT_SESSION_MSG_ID.next() + "";
    if (emitter != null) {
      SseUtil.sendJson(emitter, ChatMessageType.TEXT_CONTENT_TYPE, id, contentType);
    }
    return id;
  }

  @Override
  public void setContentType(String msgId, ContentTypeConfig contentType) {
    // 对话接口的回复才需处理
  }

  @Override
  public void addMessageWhenException(Date startTime, @Nullable String reasoning, @Nullable String text) {
    // 对话接口的回复才需处理
  }

  @Override
  public void pushMessage(ChatMessageType type, Object content) {
    if (emitter != null) {
      if (content instanceof String) {
        SseUtil.sendText(emitter, type, content.toString());
      }
      else {
        SseUtil.sendJson(emitter, type, content);
      }
    }
  }

  @Override
  public void updatePlanState(PlanRecordDTO record) {
    planContextCache.put(record.getPlanId(), record);
    if (emitter != null) {
      SseUtil.sendJson(emitter, ChatMessageType.UPDATE_PLAN_STATE, record.toMap());
    }
  }

  @Override
  public void download(Long tenantId, String msgId, String fileType, String content) {
    if (emitter != null) {
      SseUtil.sendJson(emitter, ChatMessageType.DOWNLOAD, msgId, new ReplyDownloadMessage(fileType));
      replyDownloadCache.set(tenantId, msgId, new ReplyDownloadInfoDTO());
    }
  }

  /**
   * 收集流式输出
   */
  @SuppressWarnings({"PMD.AvoidCatchingThrowable", "PMD.PreserveStackTrace"})
  protected AnswerDTO collectStream(SseInvoker invoker, @Nullable String msgId, @Nullable String code, @Nullable String name) {
    try {
      return doCollectStream(invoker, msgId, code, name);
    }
    catch (ExecuteExceptionBranchException e) {
      throw e;
    }
    // 捕获异常，用于执行节点配置的异常处理策略
    catch (Throwable throwable) {
      // 回复节点组合了固定文本、大模型回复时，如果大模型调用报错会触发这里
      // ExecuteExceptionBranchException 会被 CompositeSseInvoker 封装为 BssException
      if (throwable.getCause() instanceof ExecuteExceptionBranchException) {
        throw (ExecuteExceptionBranchException) throwable.getCause();
      }
      Consumer<Throwable> errorCallback = invoker.getErrorCallback();
      if (errorCallback != null) {
        try {
          errorCallback.accept(throwable);
        }
        catch (FallbackOutputException e) {
          if (emitter != null) {
            SseUtil.sendJson(emitter, ChatMessageType.TEXT, msgId, e.getFallbackText());
          }
          AnswerDTO answer = new AnswerDTO(msgId, code, name);
          answer.setText(e.getFallbackText());
          return answer;
        }
      }
      throw throwable;
    }
  }

  /**
   * 收集流式输出
   */
  private AnswerDTO doCollectStream(SseInvoker invoker, @Nullable String msgId, @Nullable String code, @Nullable String name) {
    Date startTime = new Date();
    AnswerDTO answer = new AnswerDTO(msgId, code, name);
    // 推理内容
    StringBuilder reasoning = new StringBuilder();
    // 文本内容
    StringBuilder content = new StringBuilder();
    Consumer<SseEvent> eventHandler = event -> {
      switch (event) {
        case ReasoningSseEvent reasoningSseEvent -> reasoning.append(StringUtils.defaultString(reasoningSseEvent.getText()));
        case TextSseEvent textSseEvent -> content.append(StringUtils.defaultString(textSseEvent.getText()));
        case ReferencesSseEvent referencesSseEvent -> answer.setReferences(referencesSseEvent.getReferences());
        case QuestionsSseEvent questionsSseEvent -> answer.setQuestions(questionsSseEvent.getQuestions());
        case KnowledgeChatLogSseEvent knowledgeChatLogSseEvent -> answer.setChatLogId(knowledgeChatLogSseEvent.getChatLogId());
        default -> {
          // 忽略不需要关注的事件
        }
      }
      sendStreamEvent(event, msgId, name);
    };

    try {
      invoker.invoke(eventHandler);
    }
    catch (BssException e) {
      // 出现异常，也需要记录已输出的文本
      addMessageWhenException(startTime, reasoning.toString(), content.toString());
      throw e;
    }
    catch (Exception e) {
      // 出现异常，也需要记录已输出的文本
      addMessageWhenException(startTime, reasoning.toString(), content.toString());
      throw new BssException(e);
    }

    answer.setReasoning(reasoning.toString());
    answer.setText(content.toString());
    invoker.finish(answer);
    replies.add(new ReplyDTO(answer));
    return answer;
  }

  /**
   * 发送流式输出事件
   */
  protected void sendStreamEvent(SseEvent event, @Nullable String msgId, @Nullable String stepName) {
    if (emitter != null) {
      SseUtil.sendJson(emitter, event.getMsgType(), msgId, event.getMsgContent());
    }
  }

  @Override
  public String newMsgId() {
    return UUID.randomUUID().toString();
  }

  @Override
  public void sendStreamText(ChatMessageType type, String msgId, String partialText) {
    if (emitter != null) {
      SseUtil.sendJson(emitter, type, msgId, partialText);
    }
  }

  @Override
  public void sendMessage(ChatMessageType type, String msgId, Object data) {
    if (emitter != null) {
      SseUtil.sendJson(emitter, type, msgId, data);
    }
  }

  @Override
  public void addStreamMessage(ChatMessageType type, String msgId, Date startTime, String text) {
    replies.add(new ReplyDTO(type, text, null, null));
  }

}
