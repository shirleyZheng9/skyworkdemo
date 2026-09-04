package com.iwhalecloud.bote.service.scene.runner.impl;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.model.SkillToolDTO;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.scene.SceneChatContext;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.service.orchestration.SceneStepRegistry;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bote.service.scene.runner.AbstractSceneRunner;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 自主规划智能体执行器
 *
 * @author bianjp
 * @since 2026-04-24
 */
@Service
@RequiredArgsConstructor
public class SimpleSceneRunner extends AbstractSceneRunner {

  /**
   * 执行简单场景
   */
  @Override
  public OrchestrationEngineResponse run(SimpleBotSceneDTO scene, SceneChatParamsDTO sceneChatParams) {
    Date startTime = new Date();
    OrchestrationEngineResponse response = new OrchestrationEngineResponse();
    Long modelId = resolveModelId(sceneChatParams);
    LlmClient modelClient = modelClientCache.getLlmClient(sceneChatParams.getTenantId(), modelId);
    SceneChatContext context = buildContext(scene, sceneChatParams, modelClient, modelId);

    try {
      // 没有工具时使用流式输出，用户体验更好
      if (CollectionUtils.isEmpty(context.getTools())) {
        doExecuteSimpleSceneWithStreamOutput(context, modelClient, startTime);
      }
      else {
        Assert.isTrue(modelClient.supportsFunctionCall(), () -> "大模型 " + modelClient.defaultModel() + "[id=" + modelId + "] 不支持函数调用");
        String msgId = sceneChatParams.getReplyHandler().newMsgId();
        // 收集完整的正文内容。流式函数调用时，大模型在返回工具调用前可能会先返回一些文本，每次调用大模型都可能返回文本，需要合并为一条消息
        StringBuilder contentCollector = new StringBuilder();
        doExecuteSimpleScene(context, sceneChatParams, modelClient, msgId, contentCollector);
        if (!contentCollector.isEmpty()) {
          sceneChatParams.getReplyHandler().addStreamMessage(ChatMessageType.TEXT, msgId, startTime, contentCollector.toString());
        }
      }
      response.setSuccess(true);
    }
    catch (BssException e) {
      response.setSuccess(false);
      response.setFailMsg(e.getMessage());
      response.setException(e);
    }
    catch (Exception e) {
      String msg = ExpUtil.getMsg(e);
      response.setSuccess(false);
      response.setFailMsg(msg);
      response.setException(new BssException("智能体执行失败: " + msg, e));
    }
    response.setStepLogs(context.getStepLogs());
    response.setTimeSpent(System.currentTimeMillis() - startTime.getTime());
    if (Boolean.TRUE.equals(response.getSuccess()) && sceneChatParams.getReplyHandler() != null) {
      response.setReplies(sceneChatParams.getReplyHandler().getReplies());
    }
    if (Boolean.TRUE.equals(sceneChatParams.getLogEnabled())) {
      flowTraceLogService.addLog(sceneChatParams, response, startTime);
    }
    return response;
  }

  /**
   * 执行简单场景，流式输出
   */
  private void doExecuteSimpleSceneWithStreamOutput(SceneChatContext context, LlmClient modelClient, Date startTime) {
    ChatCompletionRequest chatCompletionRequest = context.newRequest();
    Optional<OrchestrationStepRunLog> log = context.newStepLog("llm", "大模型");
    log.ifPresent(l -> l.setInput(chatCompletionRequest));
    try {
      String msgId = context.getSceneChatParams().getReplyHandler().newMsgId();
      Consumer<ChatCompletionResponse> eventHandler = event -> {
        String content = event.getDeltaContent();
        if (StringUtils.isNotEmpty(content)) {
          context.getSceneChatParams().getReplyHandler().sendStreamText(ChatMessageType.TEXT, msgId, content);
        }
      };
      ChatCompletionResponse response = modelClient.chatCompletionStreamBlockingAndCollect(chatCompletionRequest, eventHandler, SseUtil.requestListener);
      log.ifPresent(l -> l.succeed(response));
      String replyText = StringUtils.defaultString(response.getMessageContent());
      context.getSceneChatParams().getReplyHandler().addStreamMessage(ChatMessageType.TEXT, msgId, startTime, replyText);
      // 记录完整回复内容到数据库
      context.addMessage(new AssistantMessage(replyText));
      saveNewMessages(context);
    }
    catch (Exception e) {
      log.ifPresent(l -> l.fail(e));
      throw e;
    }
  }

  /**
   * 执行简单场景
   */
  private void doExecuteSimpleScene(SceneChatContext context, SceneChatParamsDTO sceneChatParams, LlmClient modelClient,
                                    String msgId, StringBuilder contentCollector) {
    AssistantMessage message = null;
    boolean stream = modelClient.supportsStreamingFunctionCall();
    // 限制工具调用次数，避免死循环
    int toolCallLimit = SystemParameter.LLM_TOOL_CALL_LIMIT.getRequiredIntegerValueFromDb();
    for (int i = 0; i < toolCallLimit; i++) {
      ChatCompletionRequest chatCompletionRequest = context.newRequest();
      Optional<OrchestrationStepRunLog> log = context.newStepLog("llm", "大模型");
      log.ifPresent(l -> l.setInput(chatCompletionRequest));
      try {
        ChatCompletionResponse chatCompletionResponse;
        if (stream) {
          chatCompletionResponse = invokeLlmStream(sceneChatParams, modelClient, chatCompletionRequest, msgId, contentCollector);
        }
        else {
          chatCompletionResponse = invokeLlm(sceneChatParams, modelClient, chatCompletionRequest, msgId, contentCollector);
        }
        log.ifPresent(l -> l.succeed(chatCompletionResponse));
        message = chatCompletionResponse.getMessage();
      }
      catch (Exception e) {
        log.ifPresent(l -> l.fail(e));
        throw e;
      }
      context.addMessage(message);
      // 保存消息记录
      saveNewMessages(context);
      // 没有工具调用时结束循环
      if (!message.hasToolCall()) {
        break;
      }
      // 调用工具
      ToolCall toolCall = message.getToolCall();
      String toolCode = toolCall.getFunction().getName();
      SkillToolDTO sceneTool = IterableUtils.find(context.getSceneTools(), t -> toolCode.equals(t.getTool().getFunction().getName()));
      Assert.notNull(sceneTool, () -> "技能不存在: " + toolCode);
      Object result = invokeTool(sceneTool, context, sceneChatParams, toolCall.getId(), toolCall.getFunction().getArguments());
      // 如果是前端动作类型，需要将调用结果直接返回给前端。暂时不能记录工具调用结果，需要等前端完成操作再调用后端时才有最终结果
      if (sceneTool.isFrontendActions()) {
        return;
      }
      // 返回 OrchestrationEngineResponse 表示调用的是对话型工作流，当作普通技能处理，只是返回的不是出参，而是回复或页面
      else if (result instanceof OrchestrationEngineResponse) {
        // TODO 怎么记录工具消息？可能有时工作流的回复需要作为工具消息，而有时工作流返回的页面/页面函数的回调需要作为工具消息
        return;
      }
      // 记录工具消息
      context.addMessage(new ToolMessage(toolCall.getId(), result));
      // 保存消息记录
      saveNewMessages(context);

      // 知识问答节点内已经输出回复，不需要再调用大模型，但仍需要将回复保存为工具消息
      if (StepType.KNOWLEDGE_CHAT.equals(sceneTool.getSkillType())
        || StepType.WEKNORA_CHAT.equals(sceneTool.getSkillType())
        || StepType.KNOWLEDGE_GRAPH_CHAT.equals(sceneTool.getSkillType())) {
        return;
      }
    }
    Assert.notNull(message, "调用大模型失败");
    Assert.isTrue(!message.hasToolCall(), "调用工具次数超出限制");
  }

  /**
   * 自主规划模式调用大模型，使用流式函数调用
   */
  private static ChatCompletionResponse invokeLlmStream(SceneChatParamsDTO sceneChatParams, LlmClient client, ChatCompletionRequest request,
                                                        String msgId, StringBuilder contentCollector) {
    // 在本次调用中是否是首次收到事件
    AtomicBoolean isFirstEvent = new AtomicBoolean(true);
    Consumer<ChatCompletionResponse> eventHandler = response -> {
      String content = response.getDeltaContent();
      if (StringUtils.isEmpty(content)) {
        return;
      }
      // 首次收到事件时，需要检查之前调用大模型是否已经输出过文本
      // 有些大模型在返回工具调用的同时会返回文本内容，两次调用大模型生成的不同文本内容需要分隔为不同段落
      if (isFirstEvent.compareAndSet(true, false) && !contentCollector.isEmpty()) {
        content = "\n\n" + content;
      }
      contentCollector.append(content);
      sceneChatParams.getReplyHandler().sendStreamText(ChatMessageType.TEXT, msgId, content);
    };
    return client.chatCompletionStreamBlockingAndCollect(request, eventHandler, SseUtil.requestListener);
  }

  /**
   * 自主规划模式调用大模型，不使用流式输出
   */
  private ChatCompletionResponse invokeLlm(SceneChatParamsDTO sceneChatParams, LlmClient client, ChatCompletionRequest request,
                                           String msgId, StringBuilder contentCollector) {
    ChatCompletionResponse response = client.chatCompletion(request, SseUtil.requestListener);
    AssistantMessage message = response.getMessage();
    if (!message.hasToolCall() && StringUtils.isNotEmpty(message.getContent())) {
      if (!contentCollector.isEmpty()) {
        contentCollector.append("\n\n");
      }
      contentCollector.append(message.getContent());
      sceneChatParams.getReplyHandler().sendStreamText(ChatMessageType.TEXT, msgId, message.getContent());
    }
    return response;
  }

  /**
   * 调用工具
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private Object invokeTool(SkillToolDTO sceneTool, SceneChatContext context, SceneChatParamsDTO sceneChatParams, String toolCallId, @Nullable String toolArgumentsJson) {
    // 解析工具参数
    Map<String, Object> toolArguments = ParamConverterUtil.parseToolArgumentsJson(toolArgumentsJson);
    // 调用工具

    AbstractStepRunner runner = SceneStepRegistry.getRunner(sceneTool.getSkillType());
    AbstractStep step = sceneTool.getStep();
    Optional<OrchestrationStepRunLog> log = context.newStepLog(step);
    try {
      Object result = runner.runAsTool(sceneChatParams, step, toolCallId, toolArguments, log);
      log.ifPresent(l -> {
        if (result instanceof OrchestrationEngineResponse response) {
          l.succeed(response.getReplies());
        }
        else {
          l.succeed(result);
        }
      });
      return result == null ? "" : result;
    }
    catch (Exception e) {
      log.ifPresent(l -> l.fail(e));
      throw e;
    }
  }

}
