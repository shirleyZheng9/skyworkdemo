package com.iwhalecloud.bote.llm.helper;

import com.iwhalecloud.bote.llm.client.consts.JsonSchemaDataType;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.FunctionMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.MessageContent;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * token 计数器
 *
 * <p>用于预估 token 数量。不同供应商、同一供应商的不同模型的计算方式都不一样，无法准确计算，统一使用 OpenAI 的算法做估算。</p>
 *
 * <p>实现逻辑参考 <a href="https://github.com/langchain4j/langchain4j/blob/main/langchain4j-open-ai/src/main/java/dev/langchain4j/model/openai/OpenAiTokenizer.java">langchain4j</a></p>
 *
 * @author bianjp
 * @since 2024-11-01
 */
public final class TokenCounter {
  private TokenCounter() {
  }

  /** 编码注册器 */
  private static final EncodingRegistry encodingRegistry = Encodings.newLazyEncodingRegistry();

  /**
   * 预估文本的 token 数量
   *
   * @param model 模型名称
   * @param text 文本
   * @return token 数量
   */
  public static int estimate(String model, @Nullable String text) {
    return estimateText(getEncoding(model), text);
  }

  /**
   * 预估工具的 token 数量
   *
   * @param model 模型名称
   * @param tools 工具列表
   * @return token 数量
   */
  public static int estimateTools(String model, @Nullable List<Tool> tools) {
    if (CollectionUtils.isEmpty(tools)) {
      return 0;
    }
    Encoding encoding = getEncoding(model);
    // 有工具时固定要占用一些 token
    int tokenCount = 16;
    for (Tool tool : tools) {
      tokenCount += 6;
      tokenCount += estimateText(encoding, tool.getFunction().getName());
      tokenCount += estimateText(encoding, tool.getFunction().getDescription());
      tokenCount += estimateToolParameter(encoding, tool.getFunction().getParameters());
    }
    return tokenCount;
  }

  /**
   * 预估工具参数的 token 数量
   */
  private static int estimateToolParameter(Encoding encoding, @Nullable JsonSchemaNode node) {
    if (node == null) {
      return 0;
    }
    int tokenCount = 3;
    switch (ObjectUtils.getIfNull(node.getType(), JsonSchemaDataType.STRING)) {
      case OBJECT:
        for (Entry<String, JsonSchemaNode> entry : MapUtils.emptyIfNull(node.getProperties()).entrySet()) {
          tokenCount += estimateText(encoding, entry.getKey());
          tokenCount += estimateToolParameter(encoding, entry.getValue());
        }
        break;
      case ARRAY:
        tokenCount += estimateToolParameter(encoding, node.getItems());
        break;
      default:
        tokenCount += estimateText(encoding, node.getDescription());
    }
    return tokenCount;
  }

  /**
   * 预估消息的 token 数量
   *
   * @param model 模型名称
   * @param message 消息
   * @return token 数量
   */
  public static int estimate(String model, Message message) {
    Encoding encoding = getEncoding(model);
    // 每条消息固定会占用几个 token
    int tokenCount = 5;
    if (message instanceof SystemMessage) {
      tokenCount += estimateText(encoding, ((SystemMessage) message).getContent());
    }
    else if (message instanceof AssistantMessage) {
      tokenCount += estimateAssistantMessage(encoding, (AssistantMessage) message);
    }
    else if (message instanceof UserMessage) {
      tokenCount += estimateUserMessage(encoding, (UserMessage) message);
    }
    else if (message instanceof ToolMessage) {
      tokenCount += estimateText(encoding, ((ToolMessage) message).getContent());
    }
    else if (message instanceof FunctionMessage) {
      tokenCount += estimateText(encoding, ((FunctionMessage) message).getContent());
    }
    else {
      throw new IllegalArgumentException("Unsupported message type: " + message.getClass().getName());
    }
    return tokenCount;
  }

  /**
   * 预估用户消息的 token 数量
   */
  private static int estimateUserMessage(Encoding encoding, UserMessage message) {
    int tokenCount = estimateText(encoding, message.getName());
    if (message.getContent() instanceof List) {
      for (Object item : (List<?>) message.getContent()) {
        Assert.isTrue(item instanceof MessageContent, "消息内容的列表元素必须是 MessageContent");
        if (((MessageContent) item).getImageUrl() != null) {
          tokenCount += 85;
        }
        else {
          tokenCount += estimateText(encoding, ((MessageContent) item).getText());
        }
      }
    }
    else {
      tokenCount += estimateText(encoding, Objects.toString(message.getContent(), null));
    }
    return tokenCount;
  }

  /**
   * 预估助手消息的 token 数量
   */
  private static int estimateAssistantMessage(Encoding encoding, AssistantMessage message) {
    int tokenCount = estimateText(encoding, message.getContent());
    if (CollectionUtils.isNotEmpty(message.getToolCalls())) {
      tokenCount += 6;
      for (ToolCall toolCall : message.getToolCalls()) {
        tokenCount += estimateText(encoding, toolCall.getFunction().getName());
        tokenCount += estimateText(encoding, toolCall.getFunction().getArguments());
      }
    }
    else if (message.getFunctionCall() != null) {
      tokenCount += 6;
      tokenCount += estimateText(encoding, message.getFunctionCall().getName());
      tokenCount += estimateText(encoding, message.getFunctionCall().getArguments());
    }
    return tokenCount;
  }

  /**
   * 预估文本的 token 数量
   */
  private static int estimateText(Encoding encoding, @Nullable String text) {
    return text == null || text.isEmpty() ? 0 : encoding.countTokensOrdinary(text);
  }

  /**
   * 获取编码器
   */
  private static Encoding getEncoding(String model) {
    // gpt-3.5, gpt-4 单独处理，其它当作 gpt-4o
    // 实测通义千问的计算结果与 gpt-4o 接近，而 gpt-4 的计算结果明显偏大
    // https://platform.openai.com/tokenizer
    // https://dashscope.console.aliyun.com/tokenizer
    if (StringUtils.isNotEmpty(model) && (model.contains("gpt-3") || (model.contains("gpt-4") && !model.contains("gpt-4o")))) {
      return encodingRegistry.getEncodingForModel(ModelType.GPT_4);
    }
    return encodingRegistry.getEncodingForModel(ModelType.GPT_4O);
  }
}
