package com.iwhalecloud.bote.service.orchestration.reply.handlers;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.event.ReasoningSseEvent;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.sse.event.TextSseEvent;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.service.orchestration.reply.AbstractReplyHandler;
import io.a2a.A2A;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.TextPart;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A2A 流式回复处理器
 *
 * @author bianjp
 * @since 2025-09-13
 */
public class A2aStreamReplyHandler extends AbstractReplyHandler {
  private final TaskUpdater taskUpdater;
  /** 上一个流式输出的消息 ID */
  private String lastMsgId;
  /** 上一个流式输出的制品名称 */
  private String lastArtifactName;
  /** 思考内容收集器 */
  private final StringBuilder reasoningContentCollector = new StringBuilder();

  public A2aStreamReplyHandler(TaskUpdater taskUpdater, String clientId) {
    super(null, clientId);
    this.taskUpdater = taskUpdater;
  }

  @Override
  @Nullable
  protected String sendReply(ChatMessageType type, Object content, @Nullable String code, @Nullable String name, @Nullable String msgId) {
    // 只处理文本消息
    if (type != ChatMessageType.TEXT) {
      return null;
    }
    // 发送非流式输出的回复
    if (content instanceof String str) {
      if (!str.isEmpty()) {
        taskUpdater.addArtifact(List.of(new TextPart(str)), msgId, name, null);
      }
    }
    return null;
  }

  @Override
  public AnswerDTO stream(SseInvoker invoker, @Nullable String code, @Nullable String name, @Nullable String msgId) {
    // 确保消息 ID 不为空，否则会影响 sendStreamEvent 中的逻辑
    String finalMsgId = StringUtils.isNotEmpty(msgId) ? msgId : newMsgId();
    return collectStream(invoker, finalMsgId, code, name);
  }

  @Override
  protected void sendStreamEvent(SseEvent event, @Nullable String msgId, @Nullable String stepName) {
    // 思考内容无法流式输出，先收集起来，按段落发送，每个段落作为一条 working 状态的消息
    if (event instanceof ReasoningSseEvent reasoningSseEvent) {
      reasoningContentCollector.append(StringUtils.defaultString(reasoningSseEvent.getText()));
      int pos = reasoningContentCollector.lastIndexOf("\n\n");
      if (pos > 0) {
        taskUpdater.startWork(A2A.toAgentMessage(reasoningContentCollector.substring(0, pos)));
        reasoningContentCollector.delete(0, pos + 2);
      }
      return;
    }
    // 只处理文本消息
    if (!(event instanceof TextSseEvent)) {
      return;
    }
    Assert.notNull(msgId, "消息 ID 不能为空");
    // 发送正文前发送剩余的思考内容
    if (!reasoningContentCollector.isEmpty()) {
      taskUpdater.startWork(A2A.toAgentMessage(reasoningContentCollector.toString()));
      reasoningContentCollector.setLength(0);
    }

    // 智能体可能会生成多条流式回复，接收到新回复时需要结束前一条流式回复（A2A 协议要求发送 lastChunk=true）
    if (lastMsgId != null && !msgId.equals(lastMsgId)) {
      sendTaskArtifactUpdateEvent("", lastMsgId, lastArtifactName, true);
    }
    sendTaskArtifactUpdateEvent(((TextSseEvent) event).getText(), msgId, stepName, false);
    lastMsgId = msgId;
    lastArtifactName = stepName;
  }

  @Override
  public void sendStreamText(ChatMessageType type, String msgId, String partialText) {
    sendStreamEvent(SseEvent.ofText(partialText), msgId, null);
  }

  /**
   * 智能体执行结束，给最后一条流式回复发送 lastChunk=true 事件
   */
  public void finish() {
    if (lastMsgId == null) {
      return;
    }
    sendTaskArtifactUpdateEvent("", lastMsgId, lastArtifactName, true);
  }

  /**
   * 发送任务制品更新事件
   */
  private void sendTaskArtifactUpdateEvent(String text, String artifactId, @Nullable String artifactName, boolean lastChunk) {
    // 第一个片段必须是 append=false, 否则无法成功添加制品
    boolean append = artifactId.equals(lastMsgId);
    taskUpdater.addArtifact(List.of(new TextPart(text)), artifactId, artifactName, null, append, lastChunk);
  }
}
