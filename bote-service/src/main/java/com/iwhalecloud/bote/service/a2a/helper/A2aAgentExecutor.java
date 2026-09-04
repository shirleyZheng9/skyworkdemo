package com.iwhalecloud.bote.service.a2a.helper;

import com.google.common.base.Suppliers;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.util.A2aUtil;
import com.iwhalecloud.bote.dto.chat.ReplyDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.service.orchestration.reply.ReplyHandler;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.A2aStreamReplyHandler;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.NonStreamFlowReplyHandler;
import com.iwhalecloud.bote.service.scene.ISceneChatService;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.a2a.A2A;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.Message;
import io.a2a.spec.Task;
import io.a2a.spec.TaskNotCancelableError;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TaskStatusUpdateEvent;
import io.a2a.spec.TextPart;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * A2A 智能体执行器
 *
 * <p>用于以 A2A 协议调用平台自身的智能体。</p>
 *
 * @author bianjp
 * @since 2025-09-12
 */
@Component
@RequiredArgsConstructor
public class A2aAgentExecutor implements AgentExecutor {
  private final ISceneChatService sceneChatService;
  private final IRefreshCacheService refreshCacheService;

  @Override
  public void execute(RequestContext context, EventQueue eventQueue) {
    InvokeA2aContextParams params = (InvokeA2aContextParams) MapUtils.getObject(context.getCallContext().getState(), InvokeA2aContextParams.STATE_KEY);
    Assert.notNull(params, "非法调用来源");

    TaskUpdater updater = new TaskUpdater(context, eventQueue);
    // 新任务，状态改为 submitted
    if (context.getTask() == null) {
      updater.submit();
    }
    // 状态改为 working
    updater.startWork();

    // 提取消息中的文本、结构化数据、文件
    Message message = context.getMessage();
    Assert.notNull(message, "A2A 消息不能为空");
    String messageText = A2aUtil.extractTextFromMessage(message);
    Map<String, Object> messageData = A2aUtil.extractDataFromMessage(message);
    List<Long> messageFiles = A2aUtil.extractFilesFromMessage(message);

    ReplyHandler replyHandler;
    if (params.streaming()) {
      replyHandler = new A2aStreamReplyHandler(updater, context.getTaskId());
    }
    else {
      replyHandler = new NonStreamFlowReplyHandler(context.getTaskId());
    }

    // 调用智能体
    SceneChatParamsDTO sceneChatParams = new SceneChatParamsDTO();
    sceneChatParams.setTenantId(params.tenantId());
    sceneChatParams.setSceneId(params.sceneId());
    sceneChatParams.setConversationId(ChatConsts.A2A_SESSION_ID);
    sceneChatParams.setContextId(context.getContextId());
    sceneChatParams.setMessageContent(messageText);
    sceneChatParams.setParams(messageData);
    sceneChatParams.setFileIds(messageFiles);
    // 使用 taskId 作为 clientId 以便根据 taskId 中断执行
    sceneChatParams.setClientId(context.getTaskId());
    sceneChatParams.setReplyHandler(replyHandler);
    sceneChatParams.setHistoryMessagesLoader(Suppliers.ofInstance(loadHistoryMessages(context.getTask())));
    OrchestrationEngineResponse engineResponse = sceneChatService.run(sceneChatParams);

    // 执行失败
    if (!engineResponse.getSuccess().equals(Boolean.TRUE)) {
      updater.fail(A2A.toAgentMessage(engineResponse.getFailMsg()));
      return;
    }

    // 提取回复
    String replyText = extractTextFromReplies(replyHandler);
    // 回复消息，随 TaskStatusUpdateEvent 发送
    Message replyMessage = replyText.isEmpty() ? null : updater.newAgentMessage(List.of(new TextPart(replyText)), null);

    // 流式请求，需要结束最后一个流式回复
    if (replyHandler instanceof A2aStreamReplyHandler streamReplyHandler) {
      streamReplyHandler.finish();
      // 流式发送使用了 artifact 的 append 特性, a2a-sdk 不会将 artifact 记录为历史消息，只会记录 TaskStatusUpdateEvent 中的消息: io.a2a.client.ClientTaskManager#saveTaskEvent
      // 为了让 a2a-sdk 把回复记录到历史消息，需要发送一条虚假更新事件
      if (replyMessage != null) {
        eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
          .taskId(context.getTaskId())
          .contextId(context.getContextId())
          .isFinal(false)
          .metadata(Map.of("replyAsHistory", true))
          .status(new TaskStatus(TaskState.WORKING, replyMessage, null))
          .build());
        // 已经流式发送回复，后面的 TaskStatusUpdateEvent 不需要再发送，否则会重复
        replyMessage = null;
      }
    }

    // 对于流程编排模式的智能体，可以根据 sceneFinished 判断状态是 input-required 还是 completed, 其它类型的智能体暂时无法判断
    if (Boolean.TRUE.equals(engineResponse.getSceneFinished())) {
      updater.complete(replyMessage);
    }
    else {
      updater.requiresInput(replyMessage);
    }
  }

  @Override
  public void cancel(RequestContext context, EventQueue eventQueue) {
    TaskState taskState = context.getTask().getStatus().state();
    if (taskState == TaskState.CANCELED || taskState == TaskState.COMPLETED) {
      throw new TaskNotCancelableError();
    }
    // 中断执行
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_SSE_EMITTER, context.getTaskId());
    TaskUpdater taskUpdater = new TaskUpdater(context, eventQueue);
    taskUpdater.cancel();
  }

  /**
   * 提取回复中的文本内容，有多条回复时拼成一条
   */
  private String extractTextFromReplies(ReplyHandler replyHandler) {
    if (replyHandler.getReplies().isEmpty()) {
      return "";
    }
    return replyHandler.getReplies().stream()
      .filter(r -> r.getType() == ChatMessageType.TEXT)
      .map(ReplyDTO::getText)
      .filter(StringUtils::isNotEmpty)
      .collect(Collectors.joining("\n\n"));
  }

  /**
   * 加载任务的历史消息
   */
  private List<com.iwhalecloud.bote.llm.client.dto.message.Message> loadHistoryMessages(@Nullable Task task) {
    if (task == null || CollectionUtils.isEmpty(task.getHistory())) {
      return List.of();
    }
    return task.getHistory().stream()
      .map(event -> {
        String text = A2aUtil.extractTextFromMessage(event);
        if (event.getRole() == Message.Role.USER) {
          return new UserMessage(text);
        }
        return new AssistantMessage(text);
      })
      .collect(Collectors.toList());
  }

  /**
   * 调用 A2A 智能体的上下文参数
   */
  public record InvokeA2aContextParams(Long tenantId, Long sceneId, boolean streaming) {
    /** 存储在 ServerCallContext#state 中使用的 key */
    public static final String STATE_KEY = "contextParams";
  }
}
