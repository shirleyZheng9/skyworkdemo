package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.LlmStep;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.MessageContent;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * 大模型步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class LlmStepRunner extends AbstractLlmStepRunner<LlmStep> {
  @Override
  protected void doRun(SceneOrchestrationContext context, LlmStep step) {
    Assert.hasText(step.getModelId(), "模型 ID 不能为空");
    Assert.notEmpty(step.getMessages(), "消息列表不能为空");

    // 获取模型 ID
    Long modelId = getEffectiveModelId(context, step.getModelId());
    LlmClient modelClient = modelClientCache.getLlmClient(context.getTenantId(), modelId);

    // 解析图片
    List<MessageContent> imageMessageContents = parseVisonImages(step.getVision(), modelClient, modelId);

    // 解析提示词
    List<Message> messages = buildMessages(context, modelId, step, imageMessageContents);
    // 是否开启了推理
    boolean thinkingEnabled = ThinkingStrategy.shouldShowReasoning(step.getThinkingStrategy());

    // 记录节点入参日志
    Optional<OrchestrationStepRunLog> logOptional = context.getLastStepRunLogOptional();
    logOptional.ifPresent(log -> {
      Map<String, Object> input = new HashMap<>();
      input.put("modelId", modelId);
      input.put("modelName", modelClient.defaultModel());
      input.put("messages", messages);
      log.setInput(input);
    });

    OrchestrationStepRunLog log = logOptional.orElse(null);
    // 流式输出
    if (Boolean.TRUE.equals(step.getStream())) {
      // 如果是任务工作流，阻塞等待。请求耗时太长时，使用流式调用可以避免网关超时
      if (!Boolean.TRUE.equals(context.getDsl().getChatflow())) {
        ChatCompletionRequest request = ChatCompletionRequest.builder().tenantId(context.getTenantId()).messages(messages).customModelConfig(step.getCustomModelConfig()).build();
        ChatCompletionResponse response = modelClient.chatCompletionStreamBlockingAndCollect(request, null, SseUtil.requestListener);
        logOptional.ifPresent(l -> l.addLog("%s", JsonUtil.toJsonString(response)));
        context.setStepOutput(step, buildStepOutput(response, thinkingEnabled));
        return;
      }
      SseInvoker invoker = new SseInvoker(partialHandler -> {
        Consumer<ChatCompletionResponse> messageHandler = wrapPartialHandler(thinkingEnabled, partialHandler);
        ChatCompletionResponse response = invokeLlmStream(context, step, modelClient, messages, messageHandler, step.getCustomModelConfig());
        logOptional.ifPresent(l -> l.addLog("%s", JsonUtil.toJsonString(response)));
        context.setStepOutput(step, buildStepOutput(response, thinkingEnabled), log);
      });
      invoker.setErrorCallback(e -> {
        // 删除出参中的 SseInvoker, 避免在异常分支中重复触发
        context.setStepOutput(step, ImmutableMap.of("text", ""), log);
        processStreamException(context, step, e, log);
      });
      context.setStepOutput(step, ImmutableMap.of("text", invoker));
      return;
    }

    // 调用大模型
    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder().tenantId(context.getTenantId()).messages(messages).customModelConfig(step.getCustomModelConfig()).build();
    ChatCompletionResponse response = modelClient.chatCompletion(chatCompletionRequest, SseUtil.requestListener);
    logOptional.ifPresent(l -> l.addLog("%s", JsonUtil.toJsonString(response)));
    context.setStepOutput(step, buildStepOutput(response, thinkingEnabled));
  }

  /**
   * 包装流式消息处理器，用于转发流式输出
   */
  private Consumer<ChatCompletionResponse> wrapPartialHandler(boolean thinkingEnabled, Consumer<SseEvent> partialHandler) {
    if (thinkingEnabled) {
      return response -> {
        String reasoningContent = response.getDeltaReasoningContent();
        if (StringUtils.isNotEmpty(reasoningContent)) {
          partialHandler.accept(SseEvent.ofReasoning(reasoningContent));
          return;
        }
        String content = response.getDeltaContent();
        if (StringUtils.isNotEmpty(content)) {
          partialHandler.accept(SseEvent.ofText(content));
        }
      };
    }
    return response -> {
      String content = response.getDeltaContent();
      if (StringUtils.isNotEmpty(content)) {
        partialHandler.accept(SseEvent.ofText(content));
      }
    };
  }

  /**
   * 构造出参
   */
  private Map<String, Object> buildStepOutput(ChatCompletionResponse response, boolean thinkingEnabled) {
    Map<String, Object> output = new LinkedHashMap<>();
    output.put("text", response.getMessageContent());
    if (thinkingEnabled) {
      String reasoningContent = response.getReasoningContent();
      if (StringUtils.isNotEmpty(reasoningContent)) {
        output.put("reasoning", reasoningContent);
      }
    }
    return output;
  }

  /**
   * 构造消息列表
   */
  private List<Message> buildMessages(SceneOrchestrationContext context, Long modelId, LlmStep step, List<MessageContent> imageMessageContents) {
    List<Message> result = new ArrayList<>();
    // 添加节点配置的消息列表
    addMessages(context.getTenantId(), modelId, step.getMessages(), result);
    // 添加历史消息
    result.addAll(loadHistoryMessages(context, step.getMemory()));
    // 添加用户消息
    result.add(buildUserMessage(context.getTenantId(), modelId, step.getPromptId(), step.getPromptParameters(), step.getUserMessage(), imageMessageContents));
    return result;
  }

}
