package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.convertor;

import com.google.common.collect.Maps;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.Function;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.llm.client.dto.Usage;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FunctionCall;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Message;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ModelConfig;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ReplyItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TokenUsage;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ToolCall;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ToolType;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class LlmConvertor {

  private LlmConvertor() {
    // 工具类，禁止实例化
  }

  public static void toModel(ModelConfig modelConfig, SimpleLargeModelDTO model) {
    Double temperature = modelConfig.getTemperature();
    Integer maxTokens = modelConfig.getMaxTokens();
    Double topP = modelConfig.getTopP();
    model.setTemperature(BigDecimal.valueOf(temperature));
    model.setMaxTokens(maxTokens);

    Map<String, Object> extAttrMap = Maps.newHashMap();
    extAttrMap.put("topP", topP);

    String extAttrJson = JsonUtil.toJsonString(extAttrMap);
    model.setExtAttrJson(extAttrJson);
  }

  public static List<com.iwhalecloud.bote.llm.client.dto.message.Message> toMessage(List<Message> messages) {
    return messages.stream().map(LlmConvertor::toMessage).toList();
  }

  public static List<com.iwhalecloud.bote.llm.client.dto.Tool> toTool(List<com.iwhalecloud.bote.loop.evaluation.domain.entity.Tool> tools) {
    if (CollectionUtils.isEmpty(tools)) {
      return Collections.emptyList();
    }
    return tools.stream().map(LlmConvertor::toTool).toList();
  }

  public static com.iwhalecloud.bote.llm.client.dto.Tool toTool(com.iwhalecloud.bote.loop.evaluation.domain.entity.Tool tool) {
    Tool toolDTO = new Tool();
    Function functionDTO = new Function();
    JsonSchemaNode jsonSchemaNode = JsonSchemaNode.newObject(tool.getFunction().getDescription());
    functionDTO.setName(tool.getFunction().getName());
    functionDTO.setDescription(tool.getFunction().getDescription());
    functionDTO.setParameters(jsonSchemaNode);
    toolDTO.setFunction(functionDTO);
    return toolDTO;
  }

  public static com.iwhalecloud.bote.llm.client.dto.message.Message toMessage(Message message) {

    // 处理图片转换

    MessageRole role = MessageRole.valueOf(message.getRole().getDescription().toUpperCase());
    return switch (role) {
      case MessageRole.SYSTEM -> new SystemMessage(message.getContent().getText());
      case MessageRole.USER -> new UserMessage(message.getContent().getText());
      // 避免 content=null 导致调用外部接口时报错
      case MessageRole.ASSISTANT -> new AssistantMessage(message.getContent().getText());
      case MessageRole.TOOL -> new ToolMessage(null, message.getContent().getText());
      default -> new UserMessage(message.getContent().getText());
    };
  }

  public static com.iwhalecloud.bote.llm.client.dto.ToolCall toToolCall(ToolCall toolCall) {
    return new com.iwhalecloud.bote.llm.client.dto.ToolCall(toolCall.getId(), toFunctionCall(toolCall.getFunctionCall()));
  }

  public static List<com.iwhalecloud.bote.llm.client.dto.ToolCall> toToolCall(List<ToolCall> toolCalls) {
    return toolCalls.stream().map(LlmConvertor::toToolCall).toList();
  }

  public static ToolCall fromToolCall(com.iwhalecloud.bote.llm.client.dto.ToolCall toolCall) {
    ToolCall toolCallDO = new ToolCall();
    toolCallDO.setId(toolCallDO.getId());
    toolCallDO.setType(ToolType.FUNCTION);
    toolCallDO.setFunctionCall(fromFunctionCall(toolCall.getFunction()));
    return toolCallDO;
  }

  public static List<ToolCall> fromToolCall(List<com.iwhalecloud.bote.llm.client.dto.ToolCall> toolCalls) {
    if (CollectionUtils.isEmpty(toolCalls)) {
      return Collections.emptyList();
    }
    return toolCalls.stream().map(LlmConvertor::fromToolCall).toList();
  }

  public static com.iwhalecloud.bote.llm.client.dto.FunctionCall toFunctionCall(FunctionCall functionCall) {
    return new com.iwhalecloud.bote.llm.client.dto.FunctionCall(functionCall.getName(), functionCall.getArguments());
  }

  public static FunctionCall fromFunctionCall(com.iwhalecloud.bote.llm.client.dto.FunctionCall functionCall) {
    return new FunctionCall(functionCall.getName(), functionCall.getArguments());
  }

  public static ReplyItem toReplyItem(ChatCompletionResponse response) {
    Usage usage = response.getUsage();
    long inputTokens = usage == null ? 0 : usage.getPromptTokens();
    long outputTokens = usage == null ? 0 : usage.getCompletionTokens();

    TokenUsage tokenUsage = new TokenUsage();
    tokenUsage.setInputTokens(inputTokens);
    tokenUsage.setOutputTokens(outputTokens);

    ReplyItem replyItem = new ReplyItem();
    replyItem.setContent(response.getMessageContent());
    replyItem.setFinishReason(response.getChoices().get(0).getFinishReason());
    replyItem.setReasoningContent(response.getReasoningContent());
    replyItem.setToolCalls(fromToolCall(response.getMessage().getToolCalls()));
    replyItem.setTokenUsage(tokenUsage);

    return replyItem;
  }

}
