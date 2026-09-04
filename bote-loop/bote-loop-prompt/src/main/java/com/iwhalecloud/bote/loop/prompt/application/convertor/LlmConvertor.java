package com.iwhalecloud.bote.loop.prompt.application.convertor;

import com.google.common.collect.Maps;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.Usage;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ImageUrl;
import com.iwhalecloud.bote.llm.client.dto.message.MessageContent;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ContentPart;
import com.iwhalecloud.bote.loop.prompt.domain.entity.FunctionCall;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Message;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ModelConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Reply;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ReplyItem;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Role;
import com.iwhalecloud.bote.loop.prompt.domain.entity.TokenUsage;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;

public final class LlmConvertor {

  private LlmConvertor() {
    // 工具类，禁止实例化
  }

  public static void toModel(ModelConfig modelConfig, SimpleLargeModelDTO model) {
    Double temperature = modelConfig.getTemperature();
    Integer maxTokens = modelConfig.getMaxTokens();
    Double frequencyPenalty = modelConfig.getFrequencyPenalty();
    Double presencePenalty = modelConfig.getPresencePenalty();
    Integer topK = modelConfig.getTopK();
    Double topP = modelConfig.getTopP();
    model.setTemperature(BigDecimal.valueOf(temperature));
    model.setMaxTokens(maxTokens);

    Map<String, Object> extAttrMap = Maps.newHashMap();
    extAttrMap.put("frequencyPenalty", frequencyPenalty);
    extAttrMap.put("presencePenalty", presencePenalty);
    extAttrMap.put("topK", topK);
    extAttrMap.put("topP", topP);

    String extAttrJson = JsonUtil.toJsonString(extAttrMap);
    model.setExtAttrJson(extAttrJson);
  }

  public static List<com.iwhalecloud.bote.llm.client.dto.message.Message> toMessage(List<Message> messages) {
    return messages.stream().map(LlmConvertor::toMessage).collect(Collectors.toList());
  }

  public static com.iwhalecloud.bote.llm.client.dto.message.Message toMessage(Message message) {

    // 处理图片转换
    List<MessageContent> messageContents = Lists.newArrayList();
    if (message.getParts() != null) {
      for (ContentPart part : message.getParts()) {
        String url = part.getImageUrl().getUrl();
        MessageContent messageContent = new MessageContent(new ImageUrl(url));
        messageContents.add(messageContent);
      }
      if (!messageContents.isEmpty()) {
        messageContents.add(new MessageContent(message.getContent()));
      }
    }

    MessageRole role = MessageRole.valueOf(message.getRole().getValue().toUpperCase());
    switch (role) {
      case MessageRole.SYSTEM:
        return new SystemMessage(message.getContent());
      case MessageRole.USER:
        return messageContents.isEmpty() ? new UserMessage(message.getContent()) : new UserMessage(messageContents);
      case MessageRole.ASSISTANT:
        if (StringUtils.isNotEmpty(message.getToolCallId())) {
          List<ToolCall> toolCalls = message.getToolCalls().stream().map(LlmConvertor::tpToolCall).collect(Collectors.toList());
          return new AssistantMessage(toolCalls);
        }
        // 避免 content=null 导致调用外部接口时报错
        return new AssistantMessage(StringUtils.defaultString(message.getContent()));
      case MessageRole.TOOL:
        return new ToolMessage(message.getToolCallId(), message.getContent());
      default:
        throw new IllegalStateException("未知角色");
    }
  }

  public static com.iwhalecloud.bote.llm.client.dto.ToolCall tpToolCall(com.iwhalecloud.bote.loop.prompt.domain.entity.ToolCall toolCall) {
    return new com.iwhalecloud.bote.llm.client.dto.ToolCall(toolCall.getId(), toFunctionCall(toolCall.getFunctionCall()));
  }

  public static com.iwhalecloud.bote.llm.client.dto.FunctionCall toFunctionCall(FunctionCall functionCall) {
    return new com.iwhalecloud.bote.llm.client.dto.FunctionCall(functionCall.getName(), functionCall.getArguments());
  }

  public static Reply toReply(ChatCompletionResponse response) {

    Usage usage = response.getUsage();
    long inputTokens = usage == null ? 0 : usage.getPromptTokens();
    long outputTokens = usage == null ? 0 : usage.getCompletionTokens();

    return Reply.builder()
      .item(ReplyItem.builder()
        .message(Message.builder()
          .content(response.getMessage().getContent())
          .role(Role.ASSISTANT)
          .reasoningContent(response.getReasoningContent())
          .build())
        .tokenUsage(TokenUsage.builder()
          .inputTokens(inputTokens)
          .outputTokens(outputTokens)
          .build())
        .build())
      .build();
  }

  public static ReplyItem toReplyItem(ChatCompletionResponse response) {
    Usage usage = response.getUsage();
    long inputTokens = usage == null ? 0 : usage.getPromptTokens();
    long outputTokens = usage == null ? 0 : usage.getCompletionTokens();

    return ReplyItem.builder()
      .message(Message.builder()
        .content(response.getMessage().getContent())
        .role(Role.ASSISTANT)
        .reasoningContent(response.getReasoningContent())
        .build())
      .tokenUsage(TokenUsage.builder()
        .inputTokens(inputTokens)
        .outputTokens(outputTokens)
        .build())
      .build();
  }

}
