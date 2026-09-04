package com.iwhalecloud.bote.llm.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.llm.ModelConfigLoader;
import com.iwhalecloud.bote.llm.client.adapter.OpenAiLlmClient;
import com.iwhalecloud.bote.llm.client.consts.JsonSchemaDataType;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * 大模型客户端测试
 *
 * <p>测试有两个目的: 1. 验证大模型客户端代码正确; 2. 验证大模型的基本能力满足平台要求（流式、函数调用）</p>
 *
 * @author bianjp
 * @since 2024-10-16
 */
@SuppressWarnings("PMD.GuardLogStatement")
@EnabledIfEnvironmentVariable(named = "LLM_TEST_DEFAULT_API_KEY", matches = ".+")
class ModelClientIT {
  private static final Logger logger = LoggerFactory.getLogger(ModelClientIT.class);

  /**
   * 测试所有模型
   */
  @TestFactory
  List<DynamicTest> testAllModels() {
    return ModelConfigLoader.getModels().entrySet().stream().map(e -> {
      String modelName = e.getKey();
      LlmClient client = new OpenAiLlmClient(e.getValue());
      return DynamicTest.dynamicTest(modelName, () -> {
        testSimpleQuestions(client);
        testFunctionCall(client);
      });
    }).collect(Collectors.toList());
  }

  /**
   * 测试简单问题
   */
  private void testSimpleQuestions(LlmClient client) {
    testQuestion(client, null, "中国的首都是哪里？请回复城市名称", "北京");
    testQuestion(client, "你是一个有用的人工智能助手", "你是谁？", "我");
  }

  /**
   * 测试问题并检查回复是否正确
   */
  private void testQuestion(LlmClient client, @Nullable String systemPrompt, String question, String expectedResponse) {
    ChatCompletionRequest request = ChatCompletionRequest.builder().addSystemMessage(systemPrompt).addUserMessage(question).build();
    // 测试非流式调用
    ChatCompletionResponse response = client.chatCompletion(request);
    logger.debug("Request model: {}, actual model: {}", request.getModel(), response.getModel());

    String responseText = response.getMessageContent();
    logger.debug("Question: {}", question);
    logger.debug("Response: {}", responseText);
    assertTrue(responseText.contains(expectedResponse), () -> String.format("question: %s, expected response: %s, actual: %s", question, expectedResponse, responseText));

    // 测试流式调用
    CountDownLatch countDownLatch = new CountDownLatch(1);
    StringBuilder responseTextSb = new StringBuilder();
    AtomicReference<RuntimeException> exceptionHolder = new AtomicReference<>();
    client.chatCompletionStream(request,
      r -> responseTextSb.append(r.getDeltaContent()),
      e -> {
        exceptionHolder.set(e);
        countDownLatch.countDown();
      });
    try {
      if (countDownLatch.await(60, TimeUnit.SECONDS)) {
        RuntimeException e = exceptionHolder.get();
        if (e != null) {
          throw e;
        }
        String text = responseTextSb.toString();
        logger.debug("Stream response: {}", text);
        assertTrue(text.contains(expectedResponse), () -> String.format("question: %s, expected response: %s, actual: %s", question, expectedResponse, text));
      }
      else {
        fail("Stream request timeout");
      }
    }
    catch (InterruptedException e) {
      fail("Stream request interrupted", e);
    }
  }

  /**
   * 测试函数调用
   */
  private void testFunctionCall(LlmClient client) {
    // 获取天气工具
    Tool getWetherTool = new Tool(
      "get_weather",
      "根据地址获取天气",
      JsonSchemaNode.newObject()
        .addProperty("location", "城市", JsonSchemaDataType.STRING, true));

    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addUserMessage("今天广州的天气怎么样？")
      .tools(ImmutableList.of(getWetherTool))
      .build();

    // 测试能否返回函数调用
    ChatCompletionResponse response = client.chatCompletion(request);
    logger.atDebug().setMessage("Function call response: {}").addArgument(() -> JsonUtil.toJsonString(response)).log();
    assertTrue(response.getMessage().hasToolCall(), () -> "No tool call returned: " + JsonUtil.toJsonString(response));
    ToolCall toolCall = response.getMessage().getToolCall();
    assertNotNull(toolCall, () -> "No tool call returned: " + JsonUtil.toJsonString(response));
    assertEquals("get_weather", toolCall.getFunction().getName());
    assertTrue(StringUtils.isNotEmpty(toolCall.getId()), () -> "No tool call id returned: " + JsonUtil.toJsonString(response));
    assertTrue(StringUtils.isNotEmpty(toolCall.getFunction().getArguments()), () -> "No tool call arguments returned: " + JsonUtil.toJsonString(response));
    Map<String, Object> params = JsonUtil.parseJsonRequired(toolCall.getFunction().getArguments(), new TypeReference<Map<String, Object>>() {
    });
    assertEquals("广州", params.get("location"), () -> "Incorrect tool argument: " + JsonUtil.toJsonString(response));

    // 增加函数调用结果，再次调用，检查能否正确处理函数调用的结果
    request.getMessages().add(new AssistantMessage(toolCall));
    request.getMessages().add(new ToolMessage(toolCall.getId(), JsonUtil.toJsonString(ImmutableMap.of("temperature", "25", "humidity", "67", "temperature_unit", "celsius"))));
    ChatCompletionResponse finalResponse = client.chatCompletion(request);
    logger.atDebug().setMessage("Function call final response: {}").addArgument(() -> JsonUtil.toJsonString(finalResponse)).log();
    AssistantMessage assistantMessage = finalResponse.getMessage();
    String assistantMessageContent = assistantMessage.getContent();
    assertFalse(assistantMessage.hasToolCall(), () -> "Unexpected tool call returned: " + JsonUtil.toJsonString(finalResponse) + ", request: " + JsonUtil.toJsonString(request));
    assertTrue(StringUtils.isNotEmpty(assistantMessageContent), () -> "No message content returned: " + JsonUtil.toJsonString(finalResponse));
    assertTrue(assistantMessageContent.contains("25") && assistantMessageContent.contains("67"), () -> "Unexpected response: " + JsonUtil.toJsonString(finalResponse));
  }
}
