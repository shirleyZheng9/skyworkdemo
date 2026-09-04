package com.iwhalecloud.bote.llm.helper;

import com.google.common.collect.ImmutableList;
import com.iwhalecloud.bote.llm.ModelConfigLoader;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.adapter.OpenAiLlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 问题分类辅助器测试
 *
 * @author bianjp
 * @since 2024-10-22
 */
@EnabledIfEnvironmentVariable(named = "LLM_TEST_DEFAULT_API_KEY", matches = ".+")
class QuestionClassifierHelperIT {
  private static final Logger logger = LoggerFactory.getLogger(QuestionClassifierHelperIT.class);

  /**
   * 测试所有模型
   */
  @TestFactory
  List<DynamicTest> testAllModels() {
    return ModelConfigLoader.getModels().entrySet().stream().map(e -> {
      String modelName = e.getKey();
      LlmClient client = new OpenAiLlmClient(e.getValue());
      return DynamicTest.dynamicTest(modelName, () -> {
        testClassifyYesOrNo(client);
        testClassifyAction(client);
      });
    }).collect(Collectors.toList());
  }

  /**
   * 测试判断是否
   */
  private void testClassifyYesOrNo(LlmClient client) {
    List<String> categories = ImmutableList.of("是", "否");
    String instruction = "输入文本是对否创建客户的回复；创建表示是，不创建表示否；如果回复与问题无关则返回 null";
    BiConsumer<String, String> classifier = (answer, question) -> classifyQuestion(client, question, instruction, categories, answer);
    classifier.accept("是", "是");
    classifier.accept("否", "否");
    classifier.accept("是", "创建");
    classifier.accept("否", "不创建");
    classifier.accept(null, "你是谁？");
  }

  /**
   * 测试区分动作
   */
  private void testClassifyAction(LlmClient client) {
    List<String> categories = ImmutableList.of("充值", "积分兑换", "翼支付转账");
    String instruction = "根据关键字精确匹配";
    BiConsumer<String, String> classifier = (answer, question) -> classifyQuestion(client, question, instruction, categories, answer);
    classifier.accept("充值", "{\"recommend\":\"充值\"}");
    classifier.accept("积分兑换", "{\"recommend\":\"积分兑换\"}");
    classifier.accept("翼支付转账", "{\"recommend\":\"翼支付转账\"}");
    classifier.accept(null, "{\"recommend\":\"订购\"}");
  }

  /**
   * 测试问题分类的结果是否正确
   */
  private void classifyQuestion(LlmClient client, String question, @Nullable String instruction, List<String> categories, @Nullable String answer) {
    logger.debug("question: {}", question);
    List<Message> messages = QuestionClassifierHelper.buildMessages(question, instruction, categories, null);
    ChatCompletionResponse response = client.chatCompletion(ChatCompletionRequest.builder().messages(messages).build());
    String responseText = StringUtils.trimToNull(response.getMessageContent());
    assertNotNull(responseText, () -> String.format("question=%s, instruction=%s", question, instruction));
    logger.debug("response: {}", responseText);
    Map<String, Object> result = MarkdownHelper.parseJson(responseText);
    String actual = StringUtils.trimToNull(MapUtils.getString(result, "category"));
    assertEquals(answer, actual, () -> String.format("question=%s, instruction=%s, response=%s", question, instruction, responseText));
  }
}
