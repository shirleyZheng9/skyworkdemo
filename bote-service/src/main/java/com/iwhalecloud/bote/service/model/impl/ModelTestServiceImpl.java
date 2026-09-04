package com.iwhalecloud.bote.service.model.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.ModelClientUtil;
import com.iwhalecloud.bote.common.util.ModelConfigUtil;
import com.iwhalecloud.bote.common.util.TikaUtil;
import com.iwhalecloud.bote.dto.model.FunctionCallingTestResult;
import com.iwhalecloud.bote.dto.model.query.LargeModelTestParams;
import com.iwhalecloud.bote.dto.model.query.TestChatMessage;
import com.iwhalecloud.bote.llm.client.EmbeddingClient;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.config.EmbeddingProperties;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bote.llm.client.consts.FunctionCallMode;
import com.iwhalecloud.bote.llm.client.consts.JsonSchemaDataType;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ImageUrl;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.MessageContent;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bote.service.model.IModelTestService;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 模型测试服务
 *
 * @author bianjp
 * @since 2025-12-17
 */
@Service
@RequiredArgsConstructor
public class ModelTestServiceImpl implements IModelTestService {

  /** 视觉模型支持的最大图片大小: 10MB */
  private static final int MAX_IMAGE_SIZE = 1024 * 1024 * 10;

  /** 支持的图片类型 */
  private static final List<String> ALLOWED_IMAGE_TYPES = List.of("png", "apng", "jpg", "jpeg", "webp", "bmp");

  private final ModelClientCache modelClientCache;
  private final IFileStoreService fileStoreService;

  /**
   * 测试嵌入模型
   */
  @Override
  public float[] testEmbedding(LargeModelTestParams params) {
    EmbeddingClient client;
    if (params.getModelId() != null) {
      client = modelClientCache.getEmbeddingClient(params.getTenantId(), params.getModelId());
    }
    else {
      EmbeddingProperties properties = ModelConfigUtil.buildEmbeddingProperties(params);
      client = ModelClientUtil.createEmbeddingClient(properties);
    }

    SseUtil.setClientId(params.getClientId());
    try {
      return client.embedding(params.getQuestion(), SseUtil.requestListener);
    }
    finally {
      SseUtil.removeClientId();
    }
  }

  @Override
  public ChatCompletionResponse testLlm(LargeModelTestParams params) {
    LlmClient client = getLlmClient(params);
    ChatCompletionRequest request = buildLlmRequest(params);
    SseUtil.setClientId(params.getClientId());
    try {
      return client.chatCompletion(request, SseUtil.requestListener);
    }
    finally {
      SseUtil.removeClientId();
    }
  }

  @Override
  public void testLlmStream(LargeModelTestParams params, Consumer<ChatCompletionResponse> partialHandler) {
    LlmClient client = getLlmClient(params);
    ChatCompletionRequest request = buildLlmRequest(params);
    client.chatCompletionStreamBlocking(request, partialHandler, SseUtil.requestListener);
  }

  /**
   * 构造大语言模型请求对象
   *
   * @param params 测试参数
   * @return 聊天请求对象
   */
  private ChatCompletionRequest buildLlmRequest(LargeModelTestParams params) {
    ChatCompletionRequest.ChatCompletionRequestBuilder builder = ChatCompletionRequest.builder();

    // 添加历史消息
    List<Message> historyMessages = convertHistoryToMessages(params.getHistory());
    if (!historyMessages.isEmpty()) {
      builder.addMessages(historyMessages);
    }

    // 构造当前轮用户消息
    List<MessageContent> currentFileContents = convertFileIdsToMessageContents(params.getFileIds());
    UserMessage userMessage = buildUserMessage(currentFileContents, params.getQuestion());
    builder.addMessage(userMessage);

    builder.sourceFrom(ModelConsts.SOURCE_TEST);
    builder.tenantId(params.getTenantId());
    return builder.build();
  }

  /**
   * 将历史对话消息列表转换为 LLM 消息列表
   *
   * @param history 历史消息列表
   * @return LLM 消息列表
   */
  private List<Message> convertHistoryToMessages(List<TestChatMessage> history) {
    if (CollectionUtils.isEmpty(history)) {
      return List.of();
    }
    List<Message> messages = new ArrayList<>(history.size());
    for (TestChatMessage msg : history) {
      String role = msg.getRole();
      if ("user".equalsIgnoreCase(role)) {
        // 用户消息：解析附带的图片文件ID
        List<MessageContent> fileContents = convertFileIdsToMessageContents(msg.getFileIds());
        messages.add(buildUserMessage(fileContents, msg.getContent()));
      }
      else if ("assistant".equalsIgnoreCase(role)) {
        messages.add(new AssistantMessage(msg.getContent()));
      }
      else if ("system".equalsIgnoreCase(role)) {
        messages.add(new SystemMessage(msg.getContent()));
      }
    }
    return messages;
  }

  /**
   * 将文件ID列表转换为图片消息内容列表
   *
   * @param fileIds 文件ID列表
   * @return 图片消息内容列表，fileIds 为空时返回空列表
   */
  private List<MessageContent> convertFileIdsToMessageContents(List<Long> fileIds) {
    if (CollectionUtils.isEmpty(fileIds)) {
      return List.of();
    }
    List<MessageContent> contents = new ArrayList<>(fileIds.size());
    for (Long fileId : fileIds) {
      contents.add(buildImageMessageContent(fileId));
    }
    return contents;
  }

  /**
   * 根据文件ID构造图片消息内容
   *
   * @param fileId 文件ID
   * @return 图片消息内容
   */
  private MessageContent buildImageMessageContent(Long fileId) {
    String dataUri = resolveImageDataUri(fileId);
    return new MessageContent(new ImageUrl(dataUri));
  }

  /**
   * 从文件服务下载图片并转换为 Base64 data URI
   *
   * @param fileId 文件ID
   * @return Base64 data URI 字符串，如 "data:image/png;base64,..."
   */
  private String resolveImageDataUri(Long fileId) {
    // 查询文件信息
    FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
    Assert.notNull(fileInfo, "文件不存在: fileId=" + fileId);

    // 校验文件大小
    Assert.isTrue(fileInfo.getFileSize() == null || fileInfo.getFileSize() <= MAX_IMAGE_SIZE,
      "文件大小超出限制（最大10MB）: fileId=" + fileId);

    // 获取文件类型与 MIME 类型
    String fileType = getFileType(fileInfo);
    String mimeType = fileType != null ? TikaUtil.detect("1." + fileType) : null;
    Assert.isTrue(fileType != null && mimeType != null, "文件类型未知: fileId=" + fileId);
    Assert.isTrue(ALLOWED_IMAGE_TYPES.contains(fileType) && mimeType.startsWith("image/"),
      "不支持的图片格式（支持 png/jpg/jpeg/webp/bmp）: fileType=" + fileType);

    // 下载文件内容并转 Base64
    byte[] bytes = fileStoreService.downloadFile(fileId);
    Assert.isTrue(bytes != null && bytes.length > 0, "文件内容为空: fileId=" + fileId);

    return "data:" + mimeType + ";base64," + Base64.encodeBase64String(bytes);
  }

  /**
   * 获取文件扩展名
   *
   * @param fileInfo 文件信息
   * @return 文件扩展名，未知时返回 null
   */
  @Nullable
  private String getFileType(FileInfoVO fileInfo) {
    // 优先使用 fileType 字段
    String fileType = fileInfo.getFileType();
    if (StringUtils.isEmpty(fileType) && StringUtils.isNotEmpty(fileInfo.getFileName())) {
      // 从文件名中推断
      fileType = FilenameUtils.getExtension(fileInfo.getFileName());
    }
    return StringUtils.lowerCase(StringUtils.trimToNull(fileType));
  }

  /**
   * 构造用户消息
   *
   * @param fileContents 图片消息内容列表，为空时构造纯文本消息
   * @param messageText  消息文本
   * @return 用户消息对象
   */
  private UserMessage buildUserMessage(List<MessageContent> fileContents, String messageText) {
    if (CollectionUtils.isEmpty(fileContents)) {
      return new UserMessage(messageText);
    }
    List<MessageContent> contentList = new ArrayList<>(fileContents.size() + 1);
    contentList.addAll(fileContents);
    if (StringUtils.isNotEmpty(messageText)) {
      contentList.add(new MessageContent(messageText));
    }
    return new UserMessage(contentList);
  }

  /**
   * 获取大语言模型客户端
   */
  private LlmClient getLlmClient(LargeModelTestParams params) {
    if (params.getModelId() != null) {
      return modelClientCache.getLlmClient(params.getTenantId(), params.getModelId());
    }
    LlmProperties properties = ModelConfigUtil.buildLlmProperties(params);
    return ModelClientUtil.createLlmClient(params.getProtocolType(), properties);
  }

  @Override
  public FunctionCallingTestResult testFunctionCalling(LargeModelTestParams params) {
    FunctionCallMode functionCallMode = FunctionCallMode.valueOf(params.getFunctionCallMode().toUpperCase());
    Assert.isTrue(functionCallMode == FunctionCallMode.TOOL || functionCallMode == FunctionCallMode.FUNCTION, "函数调用模式错误");

    boolean stream = Boolean.TRUE.equals(params.getStream());
    LlmClient client = getLlmClient(params);
    Tool tool = buildFunctionCallTestTool();

    FunctionCallingTestResult result = new FunctionCallingTestResult();
    result.setSuccess(true);

    // 第一次调用，应该返回工具调用
    ChatCompletionRequest request1 = buildFunctionCallTestRequest1(tool);
    result.setRequest1(request1);
    ChatCompletionResponse response1;
    try {
      if (stream) {
        response1 = client.chatCompletionStreamBlockingAndCollect(request1, null, SseUtil.requestListener);
      }
      else {
        response1 = client.chatCompletion(request1, SseUtil.requestListener);
      }
    }
    catch (Exception e) {
      result.fail("第一轮调用大模型失败: " + ExpUtil.getMsg(e));
      return result;
    }
    result.setResponse1(response1);
    AssistantMessage message1 = response1.getMessage();
    // 校验返回了工具调用
    validateFunctionCall(functionCallMode, message1, result);
    if (Boolean.FALSE.equals(result.getSuccess())) {
      return result;
    }

    // 第二次调用，应基于工具调用结果生成最终回复
    ChatCompletionRequest request2 = buildFunctionCallTestRequest2(message1, tool);
    result.setRequest2(request2);
    ChatCompletionResponse response2;
    try {
      if (stream) {
        response2 = client.chatCompletionStreamBlockingAndCollect(request2, null, SseUtil.requestListener);
      }
      else {
        response2 = client.chatCompletion(request2, SseUtil.requestListener);
      }
    }
    catch (Exception e) {
      result.fail("第二轮调用大模型失败: " + ExpUtil.getMsg(e));
      return result;
    }
    result.setResponse2(response2);
    // 校验基于工具调用结果生成了最终回复
    validateFinalReply(response2.getMessage(), result);

    return result;
  }

  /**
   * 构建测试函数调用的工具
   */
  private Tool buildFunctionCallTestTool() {
    JsonSchemaNode toolInputSchema = JsonSchemaNode.newObject();
    toolInputSchema.addProperty("city", "城市名称", JsonSchemaDataType.STRING, true);
    return new Tool("get_weather", "获取天气信息", toolInputSchema);
  }

  /**
   * 构建测试函数调用的第一轮请求
   */
  private ChatCompletionRequest buildFunctionCallTestRequest1(Tool tool) {
    return ChatCompletionRequest.builder()
      .addUserMessage("今天广州天气怎么样？")
      .tools(List.of(tool))
      .build();
  }

  /**
   * 构建测试函数调用的第二轮请求
   */
  private ChatCompletionRequest buildFunctionCallTestRequest2(AssistantMessage message, Tool tool) {
    return ChatCompletionRequest.builder()
      .addUserMessage("今天广州天气怎么样？")
      .addMessage(message)
      .addMessage(new ToolMessage(message.getToolCall().getId(), "天气: 晴, 温度: 30°C, 湿度: 60%"))
      .tools(List.of(tool))
      .build();
  }

  /**
   * 检验返回了工具调用
   */
  private void validateFunctionCall(FunctionCallMode functionCallMode, AssistantMessage message, FunctionCallingTestResult result) {
    if (!message.hasToolCall()) {
      result.fail("未返回工具调用");
    }
    else if (functionCallMode == FunctionCallMode.TOOL && CollectionUtils.isEmpty(message.getToolCalls())) {
      result.fail("未返回 tool_calls");
    }
    else if (functionCallMode == FunctionCallMode.FUNCTION && message.getFunctionCall() == null) {
      result.fail("未返回 function_call");
    }
    else {
      ToolCall toolCall = message.getToolCall();
      String name = toolCall.getFunction().getName();
      String arguments = StringUtils.trimToNull(toolCall.getFunction().getArguments());
      if (!"get_weather".equals(name)) {
        result.fail("返回的工具名称不正确，应是 get_weather, 实际是 " + name);
      }
      else {
        validateFunctionCallArguments(arguments, result);
      }
    }
  }

  /**
   * 检验工具调用的参数正确
   */
  private void validateFunctionCallArguments(@Nullable String arguments, FunctionCallingTestResult result) {
    if (arguments == null) {
      result.fail("返回的工具参数为空");
    }
    else if (!arguments.startsWith("{") || !arguments.endsWith("}")) {
      result.fail("返回的工具参数不是合法的 JSON 对象: " + arguments);
    }
    else {
      Map<String, Object> params;
      try {
        params = JsonUtil.getObjectMapper().readValue(arguments, new TypeReference<>() {
        });
      }
      catch (JsonProcessingException e) {
        result.fail("返回的工具参数不是合法的 JSON: " + arguments);
        return;
      }
      if (!Map.of("city", "广州").equals(params)) {
        result.fail("返回的工具参数不正确，应是 {\"city\":\"广州\"}, 实际是 " + arguments);
      }
    }
  }

  /**
   * 校验基于工具调用结果生成了最终回复
   */
  private void validateFinalReply(AssistantMessage message, FunctionCallingTestResult result) {
    if (message.hasToolCall()) {
      result.fail("第二轮调用仍返回了工具调用");
    }
    else if (StringUtils.isEmpty(message.getContent())) {
      result.fail("第二轮调用未返回回复内容");
    }
    else if (!message.getContent().contains("晴") || !message.getContent().contains("30")) {
      result.fail("第二轮调用的回复内容不准确: " + message.getContent());
    }
  }
}
