package com.iwhalecloud.bote.adapter.juzhi1.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.adapter.juzhi.JuzhiApiHelper;
import com.iwhalecloud.bote.adapter.juzhi1.config.Juzhi1LlmProperties;
import com.iwhalecloud.bote.common.util.SseApiUtil;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionChoice;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.Function;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.FunctionMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 一级聚智大语言模型客户端
 *
 * @author bianjp
 * @since 2025-05-17
 */
@RequiredArgsConstructor
public class Juzhi1LlmClient implements LlmClient {
  private static final Logger logger = LoggerFactory.getLogger(Juzhi1LlmClient.class);

  private final Juzhi1LlmProperties properties;

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
    return properties.getModel();
  }

  @Override
  public ChatCompletionResponse chatCompletion(ChatCompletionRequest request, @Nullable Consumer<Object> requestListener) {
    String requestTime = DateUtil.formatCompact();
    // 构造请求体
    String requestBodyStr = buildRequestBody(request, requestTime);
    RequestBody requestBody = JuzhiApiHelper.buildOkHttpRequestBody(requestBodyStr);
    HttpUrl url = JuzhiApiHelper.getApiUrl();
    logger.debug("Request juzhi1 llm api start: url={}, requestTime={}, body={}", url, requestTime, requestBodyStr);
    // 调用接口
    JsonNode response = ModelHttpClient.post(url, null, requestBody, requestListener, JsonNode.class);
    logger.debug("Request juzhi1 llm api end: requestTime={}, response={}", requestTime, response);
    // 解析响应
    AssistantMessage assistantMessage = extractAssistantMessage(response);
    // 上报调用大模型的记录
    JuzhiApiHelper.saveDialog(requestTime, request.getMessages(), assistantMessage.getContent());
    return ChatCompletionResponse.ofMessage(assistantMessage);
  }

  /**
   * 提取返回的助手消息
   */
  private static AssistantMessage extractAssistantMessage(JsonNode response) {
    String resCode = response.path("resCode").asText("");
    if (StringUtils.isNotEmpty(resCode)) {
      String resMsg = response.path("resMsg").asText("");
      logger.error("Failed to invoke juzhi1 llm api: response={}", response);
      throw new BssException("调用聚智能开大模型接口失败: resCode=" + resCode + ", resMsg=" + resMsg);
    }
    String respCode = response.path("respCode").asText("");
    if (!"00000".equals(respCode)) {
      logger.error("Failed to invoke juzhi1 llm api: response={}", response);
      String respDesc = response.path("respDesc").asText("");
      throw new BssException("调用聚智能开大模型接口失败: respCode=" + respCode + ", respDesc=" + respDesc);
    }
    JsonNode body = response.path("result").path("ROOT").path("BODY");
    String returnCode = body.path("RETURN_CODE").asText("");
    if (!"0".equals(returnCode)) {
      logger.error("Failed to invoke juzhi1 llm api: response={}", response);
      String returnMsg = body.path("RETURN_MSG").asText("");
      throw new BssException("调用聚智能开大模型接口失败: returnCode=" + returnCode + ", returnMsg=" + returnMsg);
    }

    JsonNode outData = body.path("OUT_DATA");
    int operResult = outData.path("OPR_RESULT").asInt(-1);
    if (operResult != 200) {
      logger.error("Failed to invoke juzhi1 llm api: response={}", response);
      String resultDesc = outData.path("RESULT_DESC").asText("");
      throw new BssException("调用聚智能开大模型接口失败: oprResult=" + operResult + ", resultDesc=" + resultDesc);
    }
    String resText = outData.path("RES").asText("");
    if (StringUtils.isEmpty(resText)) {
      logger.error("Failed to invoke juzhi1 llm api: response={}", response);
      throw new BssException("调用聚智能开大模型接口失败: RES 为空");
    }
    JsonNode resNode = JsonUtil.readTree(resText);
    String role = resNode.path("role").asText("");
    if (!MessageRole.ASSISTANT.getCode().equals(role)) {
      logger.error("Failed to invoke juzhi1 llm api: response={}", response);
      throw new BssException("调用聚智能开大模型接口失败，消息角色错误: " + role);
    }
    return JsonUtil.convert(resNode, AssistantMessage.class);
  }

  /**
   * 构造请求体
   */
  private String buildRequestBody(ChatCompletionRequest request, String requestTime) {
    // 都是必填参数，不传会报错
    Map<String, Object> generateCfg = new LinkedHashMap<>();
    generateCfg.put("TEMPERATURE", request.getTemperature() != null ? request.getTemperature().toString() : "1.0");
    generateCfg.put("TOP_P", request.getTopP() != null ? request.getTopP().toString() : "1");
    generateCfg.put("TOP_K", "1");
    Map<String, Object> busiInfo = new LinkedHashMap<>();
    busiInfo.put("MODEL", properties.getModel());
    busiInfo.put("PROVINCE", JuzhiApiHelper.getProvince());
    busiInfo.put("CITY", JuzhiApiHelper.getCity(properties.getCity()));
    busiInfo.put("APPLICATION", properties.getApplication());
    busiInfo.put("GENERATE_CFG", generateCfg);
    busiInfo.put("MESSAGES", convertMessages(request.getMessages()));
    busiInfo.put("TOOLS", convertTools(request.getTools()));
    return JuzhiApiHelper.buildRequestBody(requestTime, properties.getFuncCode(), "大模型对话接口", busiInfo);
  }

  /**
   * 转换消息列表
   */
  private List<Map<String, Object>> convertMessages(List<Message> messages) {
    List<Map<String, Object>> result = new ArrayList<>(messages.size());
    for (Message message : messages) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("ROLE", message.getRole().getCode());
      if (message instanceof SystemMessage) {
        map.put("CONTENT", ((SystemMessage) message).getContent());
      }
      else if (message instanceof UserMessage) {
        map.put("CONTENT", ((UserMessage) message).getContent());
      }
      else if (message instanceof AssistantMessage) {
        map.put("CONTENT", ((AssistantMessage) message).getContent());
        if (((AssistantMessage) message).getFunctionCall() != null) {
          map.put("FUNCTION_CALL", ((AssistantMessage) message).getFunctionCall());
        }
      }
      // 工具消息转为函数消息
      else if (message instanceof ToolMessage) {
        map.put("ROLE", MessageRole.FUNCTION.getCode());
        // 工具调用 ID 实际为函数名称
        map.put("NAME", ((ToolMessage) message).getToolCallId());
        map.put("CONTENT", ((ToolMessage) message).getContent());
      }
      else if (message instanceof FunctionMessage) {
        map.put("NAME", ((FunctionMessage) message).getName());
        map.put("CONTENT", ((FunctionMessage) message).getContent());
      }
      else {
        throw new BssException("不支持的消息类型: " + message.getClass().getName());
      }
      result.add(map);
    }
    return result;
  }

  /**
   * 转换工具列表
   */
  private List<Map<String, Object>> convertTools(List<Tool> tools) {
    if (CollectionUtils.isEmpty(tools)) {
      return Collections.emptyList();
    }
    List<Map<String, Object>> result = new ArrayList<>(tools.size());
    for (Tool tool : tools) {
      Function function = tool.getFunction();
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("NAME", function.getName());
      map.put("DESCRIPTION", function.getDescription());
      map.put("PARAMETERS", function.getParameters());
      result.add(map);
    }
    return result;
  }

  @Override
  public EventSource chatCompletionStream(ChatCompletionRequest request, Consumer<ChatCompletionResponse> partialHandler, Consumer<BssException> completionHandler) {
    return SseApiUtil.wrapBlockingApi(request, partialHandler, completionHandler, this::chatCompletionStreamBlocking);
  }

  @Override
  public void chatCompletionStreamBlocking(ChatCompletionRequest request, Consumer<ChatCompletionResponse> eventHandler,
                                           @Nullable Consumer<Object> requestListener) {
    // 不支持流式接口，使用非流式接口模拟
    ChatCompletionResponse response = chatCompletion(request, requestListener);
    // message 改为 delta
    ChatCompletionChoice choice = response.getChoices().getFirst();
    choice.setDelta(choice.getMessage());
    choice.setMessage(null);
    eventHandler.accept(response);
  }
}
