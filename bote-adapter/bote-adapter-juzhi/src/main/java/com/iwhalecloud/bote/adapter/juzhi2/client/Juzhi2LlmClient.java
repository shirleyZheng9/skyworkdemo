package com.iwhalecloud.bote.adapter.juzhi2.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.adapter.juzhi.JuzhiApiHelper;
import com.iwhalecloud.bote.adapter.juzhi2.config.Juzhi2LlmProperties;
import com.iwhalecloud.bote.adapter.juzhi2.helper.Juzhi2ClientHelper;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.FunctionMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import okhttp3.HttpUrl;
import okhttp3.sse.EventSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 二级聚智大语言模型客户端
 *
 * @author bianjp
 * @since 2025-05-13
 */
public class Juzhi2LlmClient implements LlmClient {
  private static final Logger logger = LoggerFactory.getLogger(Juzhi2LlmClient.class);

  /** 聚智大模型配置 */
  private final Juzhi2LlmProperties properties;

  public Juzhi2LlmClient(Juzhi2LlmProperties properties) {
    this.properties = properties;
  }

  @Override
  @Nullable
  public ModelConfigInfoDTO getModelConfigInfo() {
    return properties.getModelConfig();
  }

  @Override
  public int contextLength() {
    return properties.getContextLength() != null ? properties.getContextLength() : 0;
  }

  @Override
  public boolean supportsVision() {
    return false;
  }

  @Override
  public String defaultModel() {
    return "二级聚智";
  }

  @Override
  public ChatCompletionResponse chatCompletion(ChatCompletionRequest request, @Nullable Consumer<Object> requestListener) {
    String requestTime = DateUtil.formatCompact();
    HttpUrl url = Juzhi2ClientHelper.signUrl(properties.getAssistantCode());
    String traceId = Juzhi2ClientHelper.newTraceId();
    Map<String, Object> params = buildRequestParams(traceId, properties.getStartNodeId(), request);
    // 收集完整回复内容
    StringBuilder content = new StringBuilder();
    BiConsumer<String, JsonNode> messageHandler = (msgText, msgData) -> {
      // 片段消息内容
      String text = msgData.path("payload").path("output").path("payload").path("text").asText(null);
      if (StringUtils.isNotEmpty(text)) {
        content.append(text);
      }
    };
    Juzhi2ClientHelper.invokeApiAndWait(traceId, url, params, messageHandler);
    AssistantMessage message = new AssistantMessage(content.toString());
    // 上报调用大模型的记录
    JuzhiApiHelper.saveDialog(requestTime, request.getMessages(), message.getContent());
    return ChatCompletionResponse.ofMessage(message);
  }

  @Override
  public EventSource chatCompletionStream(ChatCompletionRequest request, Consumer<ChatCompletionResponse> partialHandler, Consumer<BssException> completionHandler) {
    String requestTime = DateUtil.formatCompact();
    HttpUrl url = Juzhi2ClientHelper.signUrl(properties.getAssistantCode());
    String traceId = Juzhi2ClientHelper.newTraceId();
    Map<String, Object> params = buildRequestParams(traceId, properties.getStartNodeId(), request);
    // 收集完整回复内容
    StringBuilder content = new StringBuilder();
    BiConsumer<String, JsonNode> messageHandler = (msgText, msgData) -> {
      // 片段消息内容
      String text = msgData.path("payload").path("output").path("payload").path("text").asText(null);
      if (StringUtils.isNotEmpty(text)) {
        content.append(text);
        partialHandler.accept(ChatCompletionResponse.ofDelta(new AssistantMessage(text), false));
      }
    };
    Consumer<BssException> finalCompletionHandler = e -> {
      // 上报调用大模型的记录
      if (e == null) {
        // 发送 finish_reason, 避免 DocChain 不能正确返回参考文档
        partialHandler.accept(ChatCompletionResponse.ofDelta(new AssistantMessage(""), true));
        JuzhiApiHelper.saveDialog(requestTime, request.getMessages(), content.toString());
      }
      completionHandler.accept(e);
    };
    return Juzhi2ClientHelper.invokeApi(traceId, url, params, messageHandler, finalCompletionHandler);
  }

  /**
   * 构造请求参数
   */
  private Map<String, Object> buildRequestParams(String traceId, String startNodeId, ChatCompletionRequest request) {
    String userPrompt = buildUserPrompt(request);
    Map<String, String> userInput = ImmutableMap.of("USER_INPUT", userPrompt);
    Map<String, Object> input = ImmutableMap.of(startNodeId, userInput);
    Map<String, Object> payload = ImmutableMap.of("input", input);
    return Juzhi2ClientHelper.buildRequestParams(traceId, properties.getAssistantCode(), payload);
  }

  /**
   * 构造用户提示词
   */
  private String buildUserPrompt(ChatCompletionRequest request) {
    List<Message> messages = request.getMessages();
    if (messages.size() == 1) {
      Message message = messages.get(0);
      if (message instanceof SystemMessage) {
        return ((SystemMessage) message).getContent();
      }
      else if (message instanceof UserMessage) {
        Assert.isTrue(((UserMessage) message).getContent() instanceof String, "用户消息只支持文本内容");
        return (String) ((UserMessage) message).getContent();
      }
      else {
        throw new BssException("只有一条消息时类型必须是系统消息或用户消息，实际是 " + message.getRole());
      }
    }

    StringBuilder sb = new StringBuilder("以下是大模型和用户之间的会话记录(system 表示系统提示词，user 表示用户消息，assistant 表示大模型的回复，tool 表示工具调用的结果)，请回答最后一条用户消息:");
    for (Message message : messages) {
      switch (message.getRole()) {
        case SYSTEM:
          sb.append("<system>").append(StringUtils.trimToEmpty(((SystemMessage) message).getContent())).append("</system>\n");
          break;
        case USER:
          Assert.isTrue(((UserMessage) message).getContent() instanceof String, "用户消息只支持文本内容");
          sb.append("<user>").append(StringUtils.trimToEmpty((String) ((UserMessage) message).getContent())).append("</user>\n");
          break;
        case ASSISTANT:
          sb.append("<assistant>").append(StringUtils.trimToEmpty(((AssistantMessage) message).getContent())).append("</assistant>\n");
          break;
        case FUNCTION:
          sb.append("<tool name=\"").append(((FunctionMessage) message).getName()).append("\">")
            .append(StringUtils.trimToEmpty(((FunctionMessage) message).getContent()))
            .append("</tool>\n");
          break;
        default:
          throw new BssException("未知的消息类型: " + message.getRole());
      }
    }
    return sb.toString().trim();
  }

}
