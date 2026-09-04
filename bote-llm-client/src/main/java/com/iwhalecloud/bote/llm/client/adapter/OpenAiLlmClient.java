package com.iwhalecloud.bote.llm.client.adapter;

import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bote.llm.client.consts.FunctionCallMode;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.Function;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.FunctionMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bote.llm.client.util.ModelResponseUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import okhttp3.sse.EventSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * OpenAI 大语言模型客户端
 *
 * <p>适用于兼容 OpenAI 接口协议的大语言模型，包括 ChatGPT, 通义千问, 浩鲸 GPT-Proxy</p>
 *
 * @author bianjp
 * @since 2024-08-01
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class OpenAiLlmClient extends AbstractModelClient<LlmProperties> implements LlmClient {
  private static final Logger logger = LoggerFactory.getLogger(OpenAiLlmClient.class);

  public OpenAiLlmClient(LlmProperties properties) {
    super(properties);
  }

  @Override
  @Nullable
  public ModelConfigInfoDTO getModelConfigInfo() {
    return properties.getModelConfig();
  }

  @Override
  public int contextLength() {
    return properties.getContextLength() != null ? properties.getContextLength() : -1;
  }

  @Override
  public boolean supportsVision() {
    return Boolean.TRUE.equals(properties.getSupportsVision());
  }

  @Override
  public boolean supportsFunctionCall() {
    return properties.getFunctionCallMode() != FunctionCallMode.NONE;
  }

  @Override
  public boolean supportsStreamingFunctionCall() {
    return Boolean.TRUE.equals(properties.getStreamingFunctionCall());
  }

  @Override
  public ChatCompletionResponse chatCompletion(ChatCompletionRequest request, @Nullable Consumer<Object> requestListener) {
    preProcessRequest(request);
    // 不能设置为 null, 有些平台会默认为 true
    request.setStream(false);

    ChatCompletionResponse response = ModelHttpClient.post(apiUrl, resolveHeaders(), request, requestListener);
    if (CollectionUtils.isEmpty(response.getChoices())) {
      logger.error("Invalid llm response: url={}, model={}, response={}, request={}", properties.getUrl(), properties.getModel(),
        JsonUtil.toJsonString(response), JsonUtil.toJsonString(request));
      throw new BssException("调用大模型失败, choices 为空");
    }
    response.assertSuccess();

    ModelResponseUtil.fixParallelToolCalls(response);
    preProcessResponse(request, response);
    return response;
  }

  /**
   * 预处理响应对象
   */
  private void preProcessResponse(ChatCompletionRequest request, ChatCompletionResponse response) {
    AssistantMessage message = response.getChoices().getFirst().getMessage();
    // 转换思考内容
    if (request.isConvertReasoning() && StringUtils.isEmpty(message.getReasoningContent())) {
      String content = StringUtils.trimToEmpty(message.getContent());
      if (content.startsWith("<think>")) {
        int endPos = content.indexOf("</think>");
        if (endPos > 0) {
          String reasoningContent = content.substring("<think>".length(), endPos).trim();
          String remainingContent = content.substring(endPos + "</think>".length()).trim();
          message.setReasoningContent(reasoningContent);
          message.setContent(remainingContent);
        }
      }
    }
  }

  /**
   * 预处理请求对象
   */
  private void preProcessRequest(ChatCompletionRequest request) {
    // 设置默认请求参数
    request.setModel(ObjectUtils.getIfNull(request.getModel(), properties.getModel()));
    request.setTemperature(ObjectUtils.getIfNull(request.getTemperature(), properties.getTemperature()));
    request.setMaxTokens(ObjectUtils.getIfNull(request.getMaxTokens(), properties.getMaxTokens()));
    request.addDefaultExtParams(properties.getExtReqParams());

    // 禁用并行工具调用。返回多个工具调用时难以处理（尤其是部分工具调用执行失败的情况）
    if (CollectionUtils.isNotEmpty(request.getTools())) {
      request.setParallelToolCalls(false);
    }

    // 处理请求中的函数调用
    processFunctionCallRequest(request);
    // 处理函数参数
    processFunctionParameters(request);
  }

  /**
   * 处理请求中的函数调用
   */
  private void processFunctionCallRequest(ChatCompletionRequest request) {
    // 未传函数时不需要处理
    if (CollectionUtils.isEmpty(request.getTools()) && CollectionUtils.isEmpty(request.getFunctions())) {
      return;
    }
    if (properties.getFunctionCallMode() == FunctionCallMode.NONE) {
      throw new BssException("大模型 " + properties.getModel() + " 不支持函数调用");
    }

    // 模式为 function 时需要做兼容处理
    if (properties.getFunctionCallMode() != FunctionCallMode.FUNCTION) {
      return;
    }

    // 将 tools 参数改为 functions 参数
    if (CollectionUtils.isNotEmpty(request.getTools())) {
      request.setFunctions(request.getTools().stream().map(Tool::getFunction).collect(Collectors.toList()));
      request.setFunctionCall(request.getToolChoice());
      request.setTools(null);
      request.setToolChoice(null);
    }

    // 替换助手消息、工具消息
    // 修改前拷贝一份列表，避免影响原列表
    List<Message> messages = new ArrayList<>(request.getMessages());
    request.setMessages(messages);
    for (int i = 0; i < messages.size(); i++) {
      Message message = messages.get(i);
      // 将助手消息中的 tool_calls 改为 function_call
      if (message instanceof AssistantMessage assistantMessage) {
        if (CollectionUtils.isNotEmpty(assistantMessage.getToolCalls())) {
          assistantMessage.setFunctionCall(assistantMessage.getToolCalls().getFirst().getFunction());
          assistantMessage.setToolCalls(null);
        }
      }
      // 将工具消息改为函数消息
      // ToolMessage 中不包含函数名称，需要从前面对应的助手消息中找出来
      else if (message instanceof ToolMessage) {
        Assert.isTrue(i > 0, "工具消息前面必须有助手消息");
        Message previousMessage = messages.get(i - 1);
        Assert.isTrue(previousMessage instanceof AssistantMessage, "工具消息的前一条消息必须是助手消息");
        AssistantMessage assistantMessage = (AssistantMessage) previousMessage;
        Assert.isTrue(assistantMessage.hasToolCall(), "工具消息的前一条助手消息必须包含工具调用");
        Assert.isTrue(Objects.equals(assistantMessage.getToolCall().getId(), ((ToolMessage) message).getToolCallId()), "工具消息与前一条助手消息的 toolCallId 不一致");
        String functionName = assistantMessage.getToolCall().getFunction().getName();
        messages.set(i, new FunctionMessage(functionName, ((ToolMessage) message).getContent()));
      }
    }
  }

  /**
   * 处理函数参数
   */
  private void processFunctionParameters(ChatCompletionRequest request) {
    // 通义千问如果不传 parameters 或者传 null 会报错
    if (CollectionUtils.isNotEmpty(request.getFunctions())) {
      for (Function function : request.getFunctions()) {
        if (function.getParameters() == null) {
          function.setParameters(JsonSchemaNode.EMPTY);
        }
      }
    }
  }

  @Override
  public EventSource chatCompletionStream(ChatCompletionRequest request, Consumer<ChatCompletionResponse> partialHandler, Consumer<BssException> completionHandler) {
    preProcessRequest(request);
    request.setStream(true);
    ChatCompletionEventSourceListener eventSourceListener = ChatCompletionEventSourceListener.builder()
      .url(apiUrl)
      .partialHandler(partialHandler)
      .completionHandler(completionHandler)
      .convertReasoning(request.isConvertReasoning())
      .build();
    return ModelHttpClient.postSse(apiUrl, resolveHeaders(), request, eventSourceListener);
  }

  @Override
  public ChatCompletionResponse chatCompletionStreamBlockingAndCollect(ChatCompletionRequest request,
                                                                       @Nullable Consumer<ChatCompletionResponse> eventHandler,
                                                                       @Nullable Consumer<Object> requestListener) {
    preProcessRequest(request);
    request.setStream(true);
    ChatCompletionResponse response = ModelHttpClient.chatCompletionsStreamAndCollect(apiUrl, resolveHeaders(), request, eventHandler, requestListener);
    ModelResponseUtil.fixParallelToolCalls(response);
    return response;
  }

  @Override
  public void chatCompletionStreamBlocking(ChatCompletionRequest request, Consumer<ChatCompletionResponse> eventHandler,
                                           @Nullable Consumer<Object> requestListener) {
    preProcessRequest(request);
    request.setStream(true);
    ModelHttpClient.chatCompletionsStream(apiUrl, resolveHeaders(), request, eventHandler, requestListener);
  }

}
