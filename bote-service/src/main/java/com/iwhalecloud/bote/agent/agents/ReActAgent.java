package com.iwhalecloud.bote.agent.agents;

import com.iwhalecloud.bote.agent.event.AgentEvent;
import com.iwhalecloud.bote.agent.event.NonStreamResponseEvent;
import com.iwhalecloud.bote.agent.event.SseAgentEvent;
import com.iwhalecloud.bote.agent.event.StreamResponseEndEvent;
import com.iwhalecloud.bote.agent.event.StreamResponseEvent;
import com.iwhalecloud.bote.agent.event.ToolCallEndEvent;
import com.iwhalecloud.bote.agent.event.ToolCallStartEvent;
import com.iwhalecloud.bote.agent.memory.ShortTermMemory;
import com.iwhalecloud.bote.agent.memory.impl.InMemoryMemory;
import com.iwhalecloud.bote.agent.skill.AgentSkillSpec;
import com.iwhalecloud.bote.agent.tool.callback.ToolCallback;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.agent.tool.support.ToolExecutionResult;
import com.iwhalecloud.bote.agent.tool.util.ToolCallUtil;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.ToolCallEvent;
import com.iwhalecloud.bote.common.sse.event.ToolCallResultEvent;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.agent.MessageMetadata;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.chat.SessionMsgExtParamsDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgExtParamsDTO.SessionMsgExtParamsDTOBuilder;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Builder;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.function.Consumers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * ReAct (Reasoning and Acting) 智能体
 *
 * @author bianjp
 * @since 2026-03-09
 */
@SuppressWarnings({"PMD.AvoidCatchingThrowable", "PMD.GuardLogStatement"})
public class ReActAgent {
  private static final Logger logger = LoggerFactory.getLogger(ReActAgent.class);
  /** 调用大模型失败时的最大重试次数（不包含第一次） */
  private static final long LLM_MAX_RETRY = 3;
  /** 调用大模型失败时重试的基础延迟（毫秒） */
  private static final long LLM_RETRY_BASE_DELAY = 1000;
  /** 判断调用大模型失败时是否可重试的异常关键字列表 */
  private static final List<String> LLM_RETRYABLE_KEYWORDS = List.of(
    "500", "502", "503", "504",
    "internal_error", "internal_server_error",
    "server disconnected",
    "timeout", "超时",
    "rate", "limit",
    "overloaded", "负载", "过载",
    "限制",
    "try again", "重试", "尝试"
  );

  /** 模型客户端 */
  private final LlmClient modelClient;
  /** 系统提示词 */
  @Nullable
  private final String systemPrompt;
  /** 用户提示词 */
  private final String userPrompt;
  /** 工具调用上下文 */
  @Nullable
  private final ToolContext toolContext;
  /** 短期记忆 */
  private final ShortTermMemory memory;
  /** 日志创建器，不需要开启日志时返回 null */
  private final java.util.function.Function<String, ChatTraceLogBuilder> logCreator;

  /** 工具回调列表 */
  private final List<ToolCallback> toolCallbacks;
  /** 技能列表 */
  private final List<AgentSkillSpec> skills;

  @Builder
  public ReActAgent(LlmClient modelClient, @Nullable String systemPrompt, String userPrompt, @Nullable ToolContext toolContext,
                    @Nullable List<ToolCallback> toolCallbacks, @Nullable List<AgentSkillSpec> skills,
                    @Nullable ShortTermMemory memory, @Nullable java.util.function.Function<String, ChatTraceLogBuilder> logCreator) {
    this.modelClient = modelClient;
    this.systemPrompt = systemPrompt;
    this.userPrompt = userPrompt;
    this.toolContext = toolContext;
    this.memory = memory != null ? memory : new InMemoryMemory();
    this.logCreator = logCreator != null ? logCreator : stepName -> null;
    this.toolCallbacks = ListUtils.emptyIfNull(toolCallbacks);
    this.skills = ListUtils.emptyIfNull(skills);
  }

  /**
   * 流式调用
   */
  public ChatCompletionResponse stream(@Nullable Consumer<AgentEvent> eventHandler) {
    Consumer<AgentEvent> handler = eventHandler != null ? eventHandler : Consumers.nop();
    return invoke(modelClient.supportsStreamingFunctionCall(), handler);
  }

  /**
   * 非流式调用
   */
  public ChatCompletionResponse call(@Nullable Consumer<AgentEvent> eventHandler) {
    Consumer<AgentEvent> handler = eventHandler != null ? eventHandler : Consumers.nop();
    return invoke(false, handler);
  }

  /**
   * 非流式调用
   */
  public ChatCompletionResponse call() {
    return invoke(false, Consumers.nop());
  }

  /**
   * 调用大模型
   *
   * @param streaming 是否使用流式输出
   */
  private ChatCompletionResponse invoke(boolean streaming, Consumer<AgentEvent> eventHandler) {
    int toolCallLimit = SystemParameter.LLM_TOOL_CALL_LIMIT.getRequiredIntegerValueFromDb();
    AssistantMessage message = null;
    // 添加系统消息
    if (StringUtils.isNotEmpty(systemPrompt)) {
      memory.addMessage(new SystemMessage(systemPrompt), null);
    }
    // 添加用户消息
    memory.addMessage(new UserMessage(userPrompt), null);
    ChatCompletionRequest request = ChatCompletionRequest.builder().model(modelClient.defaultModel()).build();
    if (toolContext != null) {
      request.setSessionId(toolContext.sessionId());
      request.setTenantId(toolContext.tenantId());
      request.setBotId(toolContext.botId());
      request.setSourceFrom(ModelConsts.SOURCE_BOTECLAW);
    }
    Consumer<ChatCompletionResponse> partialHandler = res -> eventHandler.accept(new StreamResponseEvent(res));
    Date startTime;
    Date endTime;
    for (int i = 0; i < toolCallLimit; i++) {
      List<Message> messages = buildMessages();
      request.setTools(toolCallbacks.stream().map(ToolCallback::getTool).toList());
      request.setMessages(messages);
      int count = i + 1;
      Optional<ChatTraceLogBuilder> logOptional = Optional.ofNullable(logCreator.apply("大模型"));

      // 调用大模型
      startTime = new Date();
      logOptional.ifPresent(log -> log.input(request).addLog("第 %d 次调用大模型", count));
      ChatCompletionResponse response = invokeLlm(streaming, request, partialHandler, eventHandler, logOptional.orElse(null));
      endTime = new Date();

      message = response.getMessage();
      // 没有工具调用，直接返回
      if (!message.hasToolCall()) {
        memory.addMessage(message, MessageMetadata.builder().startTime(startTime).endTime(endTime).build());
        return response;
      }
      ToolCall toolCall = message.getToolCall();
      // 消除 toolCallId 重复
      fixToolCallIdDuplication(toolCall, messages);
      String toolName = toolCall.getFunction().getName();
      ToolCallback toolCallback = IterableUtils.find(toolCallbacks, t -> toolName.equals(t.getToolName()));
      boolean hideToolCall = toolCallback != null && toolCallback.hideToolCall();
      SessionMsgExtParamsDTO extParams = hideToolCall ? SessionMsgExtParamsDTO.builder().hideToolCall(true).build() : null;
      memory.addMessage(message, MessageMetadata.builder().startTime(startTime).endTime(endTime).extParams(extParams).build());

      // 调用工具
      startTime = new Date();
      ToolExecutionResult result = processToolCall(toolCall, toolCallback, eventHandler);
      endTime = new Date();
      MessageMetadata toolMessageMetadata = MessageMetadata.builder()
        .msgType(result.getMsgType())
        .startTime(startTime)
        .endTime(endTime)
        .extParams(buildMsgExtParams(result))
        .build();
      memory.addMessage(new ToolMessage(toolCall.getId(), result.getResult()), toolMessageMetadata);
      if (result.getMsgType() != null) {
        eventHandler.accept(new SseAgentEvent(result.getMsgType(), result.getMsgContent()));
      }
      // 有些工具结果需要直接返回
      if (Boolean.TRUE.equals(result.getReturnDirect())) {
        return response;
      }
    }
    Assert.notNull(message, "LLM_TOOL_CALL_LIMIT 配置错误");
    throw new BssException("调用工具次数超出限制");
  }

  /**
   * 构造消息扩展参数
   */
  private SessionMsgExtParamsDTO buildMsgExtParams(ToolExecutionResult result) {
    SessionMsgExtParamsDTOBuilder builder = SessionMsgExtParamsDTO.builder()
      .success(result.isSuccess())
      .spentTime(result.getSpentTime());
    if (result.getMsgType() != null) {
      builder.eventType(result.getMsgType().getCode());
      builder.eventData(result.getMsgContent());
    }
    return builder.build();
  }

  /**
   * 调用大模型
   */
  private ChatCompletionResponse invokeLlm(boolean streaming, ChatCompletionRequest request,
                                           Consumer<ChatCompletionResponse> partialHandler,
                                           Consumer<AgentEvent> eventHandler,
                                           @Nullable ChatTraceLogBuilder log) {
    ChatCompletionResponse response;
    try {
      response = invokeLlmWithRetries(streaming, request, partialHandler, log);
    }
    catch (Throwable e) {
      if (log != null) {
        log.failed().addLog(ExceptionUtils.getStackTrace(e));
      }
      throw e;
    }

    // 下面的逻辑放在 try 外面以避免失败时被误当作模型调用失败

    if (log != null) {
      // modelClient 内部可能会修改 request, 更新一下日志
      log.input(request);
      log.output(JsonUtil.toJsonString(response));
      log.end();
    }

    // 发送事件。放在 try 外部以避免发送事件失败当作模型调用失败
    if (streaming) {
      eventHandler.accept(new StreamResponseEndEvent(response));
    }
    else {
      eventHandler.accept(new NonStreamResponseEvent(response));
    }
    return response;
  }

  /**
   * 调用大模型，自动重试
   */
  @SuppressWarnings("BusyWait")
  private ChatCompletionResponse invokeLlmWithRetries(boolean streaming, ChatCompletionRequest request,
                                                      Consumer<ChatCompletionResponse> partialHandler,
                                                      @Nullable ChatTraceLogBuilder log) {
    long retry = 0;
    while (true) {
      try {
        ChatCompletionResponse response;
        if (streaming) {
          response = modelClient.chatCompletionStreamBlockingAndCollect(request, partialHandler, SseUtil.requestListener);
        }
        else {
          response = modelClient.chatCompletion(request, SseUtil.requestListener);
        }
        return response;
      }
      catch (Throwable e) {
        String error = ExpUtil.getMsg(e);
        String lowerCaseError = error.toLowerCase();
        // 检查能否自动重试
        if (retry < LLM_MAX_RETRY && LLM_RETRYABLE_KEYWORDS.stream().anyMatch(lowerCaseError::contains)) {
          retry++;
          if (log != null) {
            log.addLog("[%d/%d] 调用失败，将会重试: %s", retry, LLM_MAX_RETRY, error);
          }
          try {
            Thread.sleep(LLM_RETRY_BASE_DELAY * retry);
          }
          catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw e; //NOPMD - suppressed PreserveStackTrace - 抛出原始异常
          }
          continue;
        }
        throw e;
      }
    }

  }

  /**
   * 消除 toolCallId 重复
   */
  private void fixToolCallIdDuplication(ToolCall toolCall, List<Message> messages) {
    // 复杂场景下 Kimi-k2.5 有时会生成重复的 toll_call_id, 会导致下次加载会话记忆时报错
    // 这里自动检测并消除重复（尾部增加随机字符串）
    String toolCallId = toolCall.getId();
    for (Message msg : messages) {
      if (msg instanceof ToolMessage toolMessage && toolCallId.equals(toolMessage.getToolCallId())) {
        String newToolCallId = toolCallId + "_" + UUID.randomUUID().toString().substring(0, 8);
        toolCall.setId(newToolCallId);
        logger.warn("Detected tool_call_id duplication, regenerated: model={}, toolCallId={}", modelClient.defaultModel(), newToolCallId);
        break;
      }
    }
  }

  /**
   * 处理工具调用
   */
  private ToolExecutionResult processToolCall(ToolCall toolCall, @Nullable ToolCallback toolCallback, Consumer<AgentEvent> eventHandler) {
    String toolName = toolCall.getFunction().getName();
    long startTime = System.currentTimeMillis();
    // 耗时(ms)
    long spentTime = 0;
    // 是否成功
    boolean success = false;
    // 原始出参，或错误信息
    Object output = null;
    // 是否需要发送结束事件
    boolean needSendEndEvent = false;
    // 执行工具调用时的异常
    Throwable exception = null;
    Optional<ChatTraceLogBuilder> logOptional = Optional.ofNullable(logCreator.apply("工具-" + toolName));
    try {
      // 解析参数
      Map<String, Object> parameters = ToolCallback.parseArguments(toolCall.getFunction().getArguments());
      if (toolCallback == null || !toolCallback.hideToolCall()) {
        ToolCallEvent event = new ToolCallEvent(toolCall.getId(), toolName, null, parameters);
        eventHandler.accept(new ToolCallStartEvent(event));
        needSendEndEvent = true;
      }
      Assert.notNull(toolCallback, () -> {
        if (skills.stream().anyMatch(s -> toolName.equals(s.getName()))) {
          return toolName + " is a Skill. Use the `skill` tool to load it";
        }
        return "Tool not found: name=" + toolName;
      });
      logOptional.ifPresent(log -> log.input(toolCall));

      // 调用工具
      Object result = toolCallback.call(parameters, toolContext);
      spentTime = System.currentTimeMillis() - startTime;

      // 处理后的工具执行结果
      ToolExecutionResult toolExecutionResult;
      // 支持工具直接返回 ToolExecutionResult
      if (result instanceof ToolExecutionResult executionResult) {
        success = executionResult.isSuccess();
        output = executionResult.getResult();
        toolExecutionResult = executionResult;
        // 更新耗时
        toolExecutionResult.setSpentTime(spentTime);
        toolExecutionResult.setReturnDirect(ObjectUtils.getIfNull(executionResult.getReturnDirect(), toolCallback.returnDirect()));
      }
      else {
        success = ToolCallUtil.checkToolCallSuccess(toolCallback, result);
        output = result;
        toolExecutionResult = ToolExecutionResult.builder()
          .success(success)
          .spentTime(spentTime)
          .result(ToolCallback.convertResultToString(result))
          .returnDirect(toolCallback.returnDirect())
          .build();
      }
      return toolExecutionResult;
    }
    catch (ToolExecutionException e) {
      exception = e;
      spentTime = System.currentTimeMillis() - startTime;
      output = e.getMessage();
      logger.error("Failed to invoke tool: toolCall={}, error={}", toolCall, output);
      return ToolExecutionResult.builder().success(false).spentTime(spentTime).result((String) output).build();
    }
    catch (Throwable e) {
      exception = e;
      spentTime = System.currentTimeMillis() - startTime;
      output = "Error: " + ExpUtil.getMsg(e);
      logger.error("Failed to invoke tool: toolCall={}", toolCall, e);
      return ToolExecutionResult.builder().success(false).spentTime(spentTime).result((String) output).build();
    }
    finally {
      // 发送工具调用结束事件，避免前端一直卡在执行中
      if (needSendEndEvent) {
        ToolCallResultEvent event = new ToolCallResultEvent(toolCall.getId(), toolName, output, success, spentTime);
        sendToolCallEndEvent(eventHandler, event, startTime);
      }
      // 结束日志
      endToolCallLog(logOptional.orElse(null), success, output, exception);
    }
  }

  /**
   * 发送工具调用结束事件
   */
  private void sendToolCallEndEvent(Consumer<AgentEvent> eventHandler, ToolCallResultEvent event, long startTime) {
    if (event.getSpentTime() == null || event.getSpentTime() == 0) {
      event.setSpentTime(System.currentTimeMillis() - startTime);
    }
    try {
      eventHandler.accept(new ToolCallEndEvent(event));
    }
    catch (Exception e) {
      logger.warn("Failed to send tool call end event", e);
    }
  }

  /**
   * 结束工具调用日志
   */
  private void endToolCallLog(@Nullable ChatTraceLogBuilder log, boolean success, @Nullable Object output, @Nullable Throwable exception) {
    if (log == null) {
      return;
    }
    try {
      log.output(output);
      if (exception != null) {
        log.addLog(ExceptionUtils.getStackTrace(exception));
      }
      if (success) {
        log.end();
      }
      else {
        log.failed();
      }
    }
    catch (Exception e) {
      logger.warn("Failed to end tool call log", e);
    }
  }

  /**
   * 构造消息列表
   */
  private List<Message> buildMessages() {
    memory.compressIfNecessary();
    return memory.getMessages();
  }

}
