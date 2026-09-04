package com.iwhalecloud.bote.loop.prompt.domain.component.llm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.ToolCallEvent;
import com.iwhalecloud.bote.common.sse.event.ToolCallResultEvent;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.Usage;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.helper.MarkdownHelper;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Message;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Reply;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ReplyItem;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Role;
import com.iwhalecloud.bote.loop.prompt.domain.entity.TokenUsage;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Data
@Builder
public class SseEmitterConsumer {
  private static final Logger logger = LoggerFactory.getLogger(SseEmitterConsumer.class);

  private LlmClient llmClient;
  private ChatCompletionRequest chatCompletionRequest;
  private Consumer<Reply> replyConsumer;
  private Map<String, String> mockTools;
  private Boolean singleStepDebug;

  public void accept(SseEmitter emitter) {
    // 有工具时使用非流式输出
    if (CollectionUtils.isNotEmpty(chatCompletionRequest.getTools())) {
      chatCompletionWithTool(emitter);
      return;
    }

    // 流式输出
    Consumer<ChatCompletionResponse> eventHandler = response -> {
      String reasoning = response.getDeltaReasoningContent();
      String content = response.getDeltaContent();
      if (StringUtils.isNotEmpty(reasoning)) {
        SseUtil.sendJson(emitter, ChatMessageType.REASONING, reasoning);
      }
      if (StringUtils.isNotEmpty(content)) {
        SseUtil.sendJson(emitter, ChatMessageType.TEXT, content);
      }
    };

    ChatCompletionResponse response = llmClient.chatCompletionStreamBlockingAndCollect(chatCompletionRequest, eventHandler, SseUtil.requestListener);
    // 记录日志
    replyConsumer.accept(buildReply(response));
  }

  private void chatCompletionWithTool(SseEmitter emitter) {
    AssistantMessage message = null;
    // 限制工具调用次数，避免死循环
    int toolCallLimit = SystemParameter.LLM_TOOL_CALL_LIMIT.getRequiredIntegerValueFromDb();
    for (int i = 0; i < toolCallLimit; i++) {
      ChatCompletionResponse chatCompletionResponse = llmClient.chatCompletion(chatCompletionRequest, SseUtil.requestListener);
      message = chatCompletionResponse.getMessage();
      List<com.iwhalecloud.bote.llm.client.dto.message.Message> messages = new ArrayList<>(chatCompletionRequest.getMessages());
      messages.add(message);
      chatCompletionRequest.setMessages(messages);
      // 没有工具调用时结束循环
      if (!message.hasToolCall()) {
        break;
      }
      ToolCall toolCall = message.getToolCall();
      JsonNode json = MarkdownHelper.parseJsonAndAutoFix(toolCall.getFunction().getArguments(), llmClient);
      Map<String, Object> rawParameters = json == null ? null : JsonUtil.convert(json, new TypeReference<>() {
      });
      SseUtil.sendJson(emitter, ChatMessageType.TOOL_CALL, new ToolCallEvent(toolCall.getId(), toolCall.getFunction().getName(), null, rawParameters));
      //单步调试直接返回
      if (singleStepDebug) {
        return;
      }
      // 调用工具
      String toolCode = toolCall.getFunction().getName();
      String toolMock = mockTools.get(toolCode);
      Assert.notNull(toolMock, () -> "函数不存在: " + toolCode);
      // 记录工具消息
      ToolMessage toolMessage = new ToolMessage(toolCall.getId(), toolMock);
      messages.add(toolMessage);
      SseUtil.sendJson(emitter, ChatMessageType.TOOL_CALL_RESULT, new ToolCallResultEvent(toolCall.getId(), toolCall.getFunction().getName(), toolMock, true, 0L));
    }
    Assert.notNull(message, "调用大模型失败");
    SseUtil.sendJson(emitter, ChatMessageType.TEXT, message.getContent());
  }

  private Reply buildReply(ChatCompletionResponse response) {
    long inputTokens = 0L;
    long outputTokens = 0L;
    Usage usage = response.getUsage();
    if (usage != null) {
      inputTokens = usage.getPromptTokens() != null ? usage.getPromptTokens() : 0L;
      outputTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0L;
    }
    ReplyItem replyItem = ReplyItem.builder()
      .message(Message.builder()
        .role(Role.ASSISTANT)
        .reasoningContent(response.getReasoningContent())
        .content(response.getMessageContent())
        .build())
      .finishReason(response.getChoices().getFirst().getFinishReason())
      .tokenUsage(new TokenUsage(inputTokens, outputTokens))
      .build();
    return Reply.builder().item(replyItem).build();
  }

}
