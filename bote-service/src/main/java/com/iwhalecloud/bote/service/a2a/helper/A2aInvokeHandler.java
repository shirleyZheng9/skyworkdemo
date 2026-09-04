package com.iwhalecloud.bote.service.a2a.helper;

import com.iwhalecloud.bote.cache.A2aNotificationContextCache;
import com.iwhalecloud.bote.cache.A2aTaskInfoCache;
import com.iwhalecloud.bote.cache.SseEmitterCache;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.A2aUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.a2a.A2aAgentDTO;
import com.iwhalecloud.bote.dto.a2a.A2aNotificationContext;
import com.iwhalecloud.bote.dto.a2a.A2aTaskInfo;
import com.iwhalecloud.bote.service.orchestration.reply.ReplyHandler;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.LogUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import io.a2a.client.Client;
import io.a2a.client.ClientBuilder;
import io.a2a.client.ClientEvent;
import io.a2a.client.MessageEvent;
import io.a2a.client.TaskEvent;
import io.a2a.client.TaskUpdateEvent;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.http.A2AHttpClient;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.client.transport.spi.interceptors.ClientCallContext;
import io.a2a.spec.AgentCard;
import io.a2a.spec.Artifact;
import io.a2a.spec.Message;
import io.a2a.spec.PushNotificationConfig;
import io.a2a.spec.TaskArtifactUpdateEvent;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatusUpdateEvent;
import io.a2a.spec.UpdateEvent;
import java.io.Closeable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * A2A 消息处理器
 *
 * @author bianjp
 * @since 2025-10-28
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class A2aInvokeHandler {
  private static final Logger logger = LoggerFactory.getLogger(A2aInvokeHandler.class);
  private static final A2aTaskInfoCache taskInfoCache = SpringUtil.getBean(A2aTaskInfoCache.class);
  private static final SseEmitterCache sseEmitterCache = SpringUtil.getBean(SseEmitterCache.class);
  private static final A2AHttpClient httpClient = SpringUtil.getBean(A2AHttpClient.class);
  private static final A2aNotificationContextCache notificationContextCache = SpringUtil.getBean(A2aNotificationContextCache.class);
  /** A2A 通知回调接口，用于触发工作流执行 */
  private static final String NOTIFICATION_CALLBACK_URL = StringUtils.stripEnd(SystemParameter.BOTE_API_URL.getValueFromEnv(), "/")
    + "/bote/a2a/notification/triggerFlow";

  /** A2A 服务 */
  private final A2aAgentDTO agent;
  /** A2A 任务信息 */
  private final A2aTaskInfo taskInfo;
  /** 客户端 ID, 用于中断请求 */
  @Nullable
  private final String clientId;
  /** 回复处理器 */
  private final ReplyHandler replyHandler;
  /** 是否不发送 auth-required 状态的消息 */
  private final boolean skipAuthRequiredMessage;
  /** 是否使用流式输出(即使用 A2A 的 message/stream 方法，默认为 true, 关闭时使用 A2A 的 message/send) */
  private final boolean stream;
  /** 非流式输出时接收 A2A 服务器通知的工作流 ID */
  @Nullable
  private final Long notificationFlowId;

  /** 开始时间 */
  private final Date startTime = new Date();
  /** 倒计时锁，用于监听调用结束 */
  private final CountDownLatch countDownLatch = new CountDownLatch(1);
  /** 是否已结束（不论是否成功） */
  private volatile boolean finished = false;
  /** 调用过程中的异常 */
  private Throwable exception;
  /** 思考内容收集器 */
  private final StringBuilder reasoningContentCollector = new StringBuilder();
  /** 回复内容收集器 */
  private final StringBuilder replyContentCollector = new StringBuilder();
  /** 鉴权消息(auth-required 状态伴随的消息) */
  @Getter
  private String authMessage;
  /** 上一个事件的产物 ID, 用来识别流式输出的文本内容 */
  private String lastArtifactId;
  /** 回复消息 ID。暂时只支持一条回复，如果返回了多个回复，或者有流式回复，拼接起来作为一条消息 */
  private String msgId;
  /** A2A 客户端 */
  private Client client;
  /** A2A 客户端上下文 */
  private ClientCallContext clientCallContext;

  @Builder
  public A2aInvokeHandler(A2aAgentDTO agent, A2aTaskInfo taskInfo, @Nullable String clientId, ReplyHandler replyHandler,
                          boolean skipAuthRequiredMessage, @Nullable Boolean stream, @Nullable Long notificationFlowId) {
    this.agent = agent;
    this.taskInfo = taskInfo;
    this.clientId = clientId;
    this.replyHandler = replyHandler;
    this.skipAuthRequiredMessage = skipAuthRequiredMessage;
    // 默认为流式输出
    this.stream = !Boolean.FALSE.equals(stream) && agent.getAgentCard().capabilities().streaming();
    // 只有非流式输出时才处理通知
    this.notificationFlowId = !this.stream ? notificationFlowId : null;
  }

  /**
   * 初始化
   */
  public void initialize() {
    try {
      ClientBuilder clientBuilder = Client.builder(agent.getAgentCard())
        .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig(httpClient));

      // 非流式输出时需要增加客户端配置
      if (!stream) {
        ClientConfig.Builder clientConfigBuilder = ClientConfig.builder().setStreaming(false);
        // 指定了工作流时才传通知配置
        if (notificationFlowId != null) {
          String url = NOTIFICATION_CALLBACK_URL + "?tenantId=" + agent.getTenantId() + "&flowId=" + notificationFlowId;
          String token = notificationContextCache.newToken();
          A2aNotificationContext notificationContext = new A2aNotificationContext();
          notificationContext.setTenantId(agent.getTenantId());
          notificationContext.setFlowId(notificationFlowId);
          notificationContext.setLoginInfo(SessionUtil.getOptionalLoginInfo());
          notificationContext.setAgentId(agent.getAgentId());
          notificationContext.setTaskId(taskInfo.getTaskId());
          notificationContextCache.saveContext(token, notificationContext);
          PushNotificationConfig notificationConfig = new PushNotificationConfig(url, token, null, null);
          // 使用通知机制时，启用 polling 以避免 A2A 服务器阻塞等待任务结束
          clientConfigBuilder.setPolling(true);
          clientConfigBuilder.setPushNotificationConfig(notificationConfig);
        }
        clientBuilder.clientConfig(clientConfigBuilder.build());
      }
      this.client = clientBuilder.build();
    }
    catch (Exception e) {
      logger.error("Failed to initialize A2A client: tenantId={}, agentId={}", agent.getTenantId(), agent.getAgentId(), e);
      throw new BssException("A2A 客户端初始化失败: " + ExpUtil.getMsg(e), e);
    }
    this.clientCallContext = buildClientCallContext(agent);
    this.msgId = replyHandler.newMsgId();
  }

  /**
   * 构造客户端调用上下文
   */
  private static ClientCallContext buildClientCallContext(A2aAgentDTO agent) {
    // A2A 平台 + 服务中配置的请求头，汇总，注意忽略大小写
    Map<String, String> headers = new CaseInsensitiveMap<>();

    // 添加 A2A 平台的请求头
    Map<String, String> platformHeaders = A2aUtil.getPlatformHeaders(agent.getTenantId(), agent.getPlatformId());
    headers.putAll(platformHeaders);

    // 添加 A2A 服务的请求头
    A2aUtil.fillHeaders(headers, agent.getAuthConfig());

    // 暂不根据智能体卡片中声明的请求头过滤，以兼容一些不标准的服务器
    return new ClientCallContext(Map.of(), headers);
  }

  /**
   * 调用 A2A 服务
   *
   * @param message 用户消息
   */
  public void invoke(Message message) {
    sseEmitterCache.addResource(clientId, (Closeable) this::onCancel);
    // 调用流式接口时，会异步触发 handleEvent，这里需要阻塞等待；调用非流式接口时，会阻塞直到请求结束
    try {
      client.sendMessage(message, List.of(this::handleEvent), this::onError, clientCallContext);
    }
    catch (Exception e) {
      finished = true;
      logger.error("Failed to send A2A message: tenantId={}, agentId={}", agent.getTenantId(), agent.getAgentId(), e);
      throw new BssException("调用 A2A 服务失败: " + ExpUtil.getMsg(e), e);
    }

    // 阻塞等待处理完成
    waitComplete();

    // 记录完整回复到会话历史中
    if (!reasoningContentCollector.isEmpty()) {
      replyHandler.addStreamMessage(ChatMessageType.REASONING, msgId, startTime, reasoningContentCollector.toString());
    }
    if (!replyContentCollector.isEmpty()) {
      replyHandler.addStreamMessage(ChatMessageType.TEXT, msgId, startTime, replyContentCollector.toString());
    }

    // 使用通知模式时，下次会话不能复用 taskId, 因为不确定任务的状态（可能在执行中，也可能已结束）
    if (notificationFlowId != null) {
      taskInfoCache.delete(taskInfo);
    }
  }

  /**
   * 获取完整的思考内容
   */
  @Nullable
  public String getReasoningContent() {
    return reasoningContentCollector.isEmpty() ? null : reasoningContentCollector.toString();
  }

  /**
   * 获取完整的回复内容
   */
  @Nullable
  public String getReplyContent() {
    return replyContentCollector.isEmpty() ? null : replyContentCollector.toString();
  }

  /**
   * 流式请求的异常处理器
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void onError(@Nullable Throwable throwable) {
    // 成功
    if (throwable == null) {
      finished = true;
      countDownLatch.countDown();
    }
    // 失败，但请求已完成，忽略异常
    else if (finished) {
      logger.warn("Failed to process A2A message, already finished: tenantId={}, agentId={}, clientId={}", agent.getTenantId(), agent.getAgentId(), clientId, throwable);
    }
    else {
      logger.error("Failed to process A2A message: tenantId={}, agentId={}, clientId={}", agent.getTenantId(), agent.getAgentId(), clientId, throwable);
      exception = throwable;
      finished = true;
      countDownLatch.countDown();
    }
  }

  /**
   * 中断请求处理器（一般在用户中断请求时触发）
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void onCancel() {
    if (finished || StringUtils.isEmpty(taskInfo.getTaskId())) {
      return;
    }
    try {
      client.cancelTask(new TaskIdParams(taskInfo.getTaskId()));
    }
    catch (Exception e) {
      logger.warn("Failed to cancel A2A task: tenantId={}, agentId={}, taskId={}", agent.getTenantId(), agent.getAgentId(), taskInfo.getTaskId(), e);
    }
  }

  /**
   * 处理 A2A 服务器返回的事件
   */
  @SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.UnusedPrivateMethod"})
  private void handleEvent(ClientEvent event, AgentCard agentCard) {
    logger.trace("Received a2a server event: type={}, data={}", event.getClass().getName(), LogUtil.limitJsonLength(event, Integer.MAX_VALUE));
    switch (event) {
      // 返回消息表示处理完成
      case MessageEvent messageEvent -> {
        String replyText = A2aUtil.extractTextFromMessage(messageEvent.getMessage());
        sendMessage(false, null, replyText);
        // 尽早更新 finished, 以确保 onError 中能取到最新值
        finished = true;
        countDownLatch.countDown();
      }
      // 返回任务，可能是处理完成，也可能是 message/send + 异步通知返回的未完成状态
      case TaskEvent taskEvent -> {
        taskInfoCache.save(taskInfo, taskEvent.getTask());
        String replyText = A2aUtil.extractReplyFromTask(taskEvent.getTask());
        TaskState taskState = taskEvent.getTask().getStatus().state();
        sendMessage(taskState == TaskState.WORKING, null, replyText);
        // submitted 状态可能会通过 task 事件返回，可能不太标准，但 A2A 协议官方示例也有这样的，需要兼容下
        if (isFinished(taskState)) {
          finished = true;
          countDownLatch.countDown();
        }
        // 非流式输出时，只会触发一次事件，无论任务状态是什么都要结束
        else if (!stream) {
          countDownLatch.countDown();
        }
      }
      // 任务更新事件，可能会返回消息、产物、状态
      case TaskUpdateEvent taskUpdateEvent -> {
        // 保存任务信息
        TaskState taskState = taskUpdateEvent.getTask().getStatus().state();
        if (!Objects.equals(taskInfo.getTaskId(), taskUpdateEvent.getTask().getId()) || !Objects.equals(taskInfo.getTaskState(), taskState)) {
          taskInfoCache.save(taskInfo, taskUpdateEvent.getTask());
        }
        handleTaskUpdateEvent(taskUpdateEvent.getUpdateEvent());
        // 检查本轮会话是否结束（不会再收到新的消息）
        if (isFinished(taskState)) {
          finished = true;
          countDownLatch.countDown();
        }
      }
      default ->
        logger.warn("Unknown a2a server event: type={}, data={}", event.getClass().getName(), LogUtil.limitJsonLength(event, Integer.MAX_VALUE));
    }
  }

  /**
   * 处理任务更新事件
   */
  private void handleTaskUpdateEvent(UpdateEvent event) {
    // 任务状态更新，可能会返回消息
    if (event instanceof TaskStatusUpdateEvent taskStatusUpdateEvent) {
      Message message = taskStatusUpdateEvent.getStatus().message();
      if (message != null) {
        String replyText = A2aUtil.extractTextFromMessage(message);
        if (skipAuthRequiredMessage && taskStatusUpdateEvent.getStatus().state() == TaskState.AUTH_REQUIRED) {
          authMessage = replyText;
        }
        else {
          // working 状态的消息当作思考内容
          sendMessage(taskStatusUpdateEvent.getStatus().state() == TaskState.WORKING, null, replyText);
        }
      }
    }
    // 产物更新，可能会返回回复（包括流式输出的回复）
    else if (event instanceof TaskArtifactUpdateEvent taskArtifactUpdateEvent) {
      Artifact artifact = taskArtifactUpdateEvent.getArtifact();
      if (artifact != null) {
        String replyText = A2aUtil.extractTextFromArtifact(artifact);
        sendMessage(false, artifact.artifactId(), replyText);
      }
    }
    else {
      logger.warn("Unknown a2a task update event: type={}, data={}", event.getClass().getName(), LogUtil.limitJsonLength(event, Integer.MAX_VALUE));
    }
  }

  /**
   * 检查本轮会话是否结束（不会再收到新的消息）
   */
  private boolean isFinished(TaskState taskState) {
    return taskState.isFinal() || taskState == TaskState.INPUT_REQUIRED || taskState == TaskState.AUTH_REQUIRED;
  }

  /**
   * 发送回复到聊天窗口中
   */
  private void sendMessage(boolean isReasoning, @Nullable String artifactId, @Nullable String text) {
    if (StringUtils.isEmpty(text)) {
      return;
    }
    if (isReasoning) {
      // 不同消息之间用空行分隔，以确保渲染 Markdown 时显示为不同段落
      String finalText = reasoningContentCollector.isEmpty() ? text : ("\n\n" + text);
      // 同一个产物表示流式输出的文本内容，直接拼接
      reasoningContentCollector.append(finalText);
      replyHandler.sendStreamText(ChatMessageType.REASONING, msgId, finalText);
    }
    else {
      String finalText;
      // 不同产物之间（或消息与产物之间）用空行分隔，以确保渲染 Markdown 时显示为不同段落
      if (!replyContentCollector.isEmpty() && (artifactId == null || !Objects.equals(lastArtifactId, artifactId))) {
        finalText = "\n\n" + text;
      }
      else {
        // 同一个产物表示流式输出的文本内容，直接拼接
        finalText = text;
      }
      lastArtifactId = artifactId;
      replyContentCollector.append(finalText);
      replyHandler.sendStreamText(ChatMessageType.TEXT, msgId, finalText);
    }
  }

  /**
   * 阻塞等待处理完成
   */
  private void waitComplete() {
    try {
      Integer timeoutSeconds = SystemParameter.INVOKE_A2A_TIMEOUT.getRequiredIntegerValueFromDb();
      boolean success = countDownLatch.await(timeoutSeconds, TimeUnit.SECONDS);
      if (!success) {
        throw new BssException("调用 A2A 服务超时");
      }
    }
    catch (InterruptedException e) {
      if (sseEmitterCache.isCancelled(clientId)) {
        throw new BssException("用户取消", e);
      }
      throw new BssException("调用 A2A 服务中断", e);
    }
    finally {
      finished = true;
    }
    // 执行失败
    if (exception != null) {
      throw new BssException("调用 A2A 服务失败: " + ExpUtil.getMsg(exception), exception);
    }
  }

}
