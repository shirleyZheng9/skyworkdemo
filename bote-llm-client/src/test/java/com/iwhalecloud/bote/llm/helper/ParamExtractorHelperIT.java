package com.iwhalecloud.bote.llm.helper;

import com.iwhalecloud.bote.llm.ModelConfigLoader;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.adapter.OpenAiLlmClient;
import com.iwhalecloud.bote.llm.client.consts.JsonSchemaDataType;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 参数提取辅助器测试
 *
 * @author bianjp
 * @since 2024-10-22
 */
@EnabledIfEnvironmentVariable(named = "LLM_TEST_DEFAULT_API_KEY", matches = ".+")
class ParamExtractorHelperIT {
  private static final Logger logger = LoggerFactory.getLogger(ParamExtractorHelperIT.class);

  /**
   * 测试所有模型
   */
  @TestFactory
  List<DynamicTest> testAllModels() {
    return ModelConfigLoader.getModels().entrySet().stream().map(e -> {
      String modelName = e.getKey();
      LlmClient client = new OpenAiLlmClient(e.getValue());
      return DynamicTest.dynamicTest(modelName, () -> {
        testExtractName(client);
        testExtractNameAndIdNumber(client);
      });
    }).collect(Collectors.toList());
  }

  /**
   * 测试提取人名
   */
  private void testExtractName(LlmClient client) {
    JsonSchemaNode structure = JsonSchemaNode.newObject()
      .addProperty("name", "人名", JsonSchemaDataType.STRING);
    Map<String, Object> params = new LinkedHashMap<>();
    Consumer<String> extractor = text -> extractParams(client, text, null, structure, params);

    // 无人名
    extractor.accept("你是谁？");
    assertNull(StringUtils.trimToNull(MapUtils.getString(params, "name")));
    extractor.accept("我是一个大型语言模型，能够回答问题、创作文字，比如写故事、写公文、写邮件、写剧本等等，还能表达观点，玩游戏等。");
    assertNull(StringUtils.trimToNull(MapUtils.getString(params, "name")));
    // 单独一个人名
    extractor.accept("谢晓峰");
    assertEquals("谢晓峰", params.get("name"));
    // 长文本
    extractor.accept("辛弃疾的一生充满了坎坷与奋斗，他的词作不仅反映了他个人的遭遇和情感，也深刻地反映了南宋时期的社会现实和人民的心声");
    assertEquals("辛弃疾", params.get("name"));
    // 多个人名
    extractor.accept("辛弃疾是中国南宋时期的著名词人，与苏轼并称为“苏辛”，是豪放派词人的代表");
    assertEquals("辛弃疾", params.get("name"));
    // 多个信息
    extractor.accept("客户: 方蓉, 手机号: 13800000000");
    assertEquals("方蓉", params.get("name"));
    // JSON 格式
    extractor.accept("{\"custName\": \"王云才\", \"phone\": \"13800000000\"}");
    assertEquals("王云才", params.get("name"));
  }

  /**
   * 测试提取姓名和身份证号
   */
  private void testExtractNameAndIdNumber(LlmClient client) {
    JsonSchemaNode structure = JsonSchemaNode.newObject()
      .addProperty("name", "人名", JsonSchemaDataType.STRING)
      .addProperty("idNumber", "身份证号", JsonSchemaDataType.STRING);
    String instruction = "身份证号为 18 位数字";
    Map<String, Object> params = new LinkedHashMap<>();
    Consumer<String> extractor = text -> extractParams(client, text, null, structure, params);
    Consumer<String> extractorWithInstruction = text -> extractParams(client, text, instruction, structure, params);

    // 单独一个姓名
    extractor.accept("刘少波");
    assertEquals("刘少波", params.get("name"));
    assertNull(StringUtils.trimToNull(MapUtils.getString(params, "idNumber")));
    // 长文本，只有姓名
    extractor.accept("曹玉雪想办理 169 元套餐");
    assertEquals("曹玉雪", params.get("name"));
    assertNull(StringUtils.trimToNull(MapUtils.getString(params, "idNumber")));
    // 包含姓名、手机号、身份证号，未指定身份证号规则，可能会把手机号当作身份证号
    extractor.accept("刘鸿雁 13800000000 330108198903062632");
    String idNumber = MapUtils.getString(params, "idNumber");
    assertEquals("刘鸿雁", params.get("name"));
    assertTrue(StringUtils.isEmpty(idNumber) || "13800000000".equals(idNumber) || "330108198903062632".equals(idNumber));
    // 指定身份证号规则，确保不会识别错误
    extractorWithInstruction.accept("刘少铭 13800000000 341800198209200396");
    assertEquals("刘少铭", params.get("name"));
    assertEquals("341800198209200396", params.get("idNumber"));
    // JSON 格式
    extractor.accept("{\"custName\": \"胡玫\", \"idNumber\": \"623023198504143291\"}");
    assertEquals("胡玫", params.get("name"));
    assertEquals("623023198504143291", params.get("idNumber"));
    // OCR 识别结果
    extractorWithInstruction.accept("[[[[[57.0, 79.0], [166.0, 79.0], [166.0, 100.0], [57.0, 100.0]], [姓名奥巴马, 0.9964967966079712]], [[[58.0, 113.0], [133.0, 113.0], [133.0, 131.0], [58.0, 131.0]], [性别男, 0.9994357228279114]], [[[123.0, 111.0], [260.0, 109.0], [260.0, 129.0], [123.0, 132.0]], [民旗肯尼亚, 0.8530831336975098]], [[[57.0, 145.0], [96.0, 145.0], [96.0, 164.0], [57.0, 164.0]], [出生, 0.9973839521408081]], [[[110.0, 145.0], [249.0, 145.0], [249.0, 163.0], [110.0, 163.0]], [1961年8月4日, 0.9572808146476746]], [[[56.0, 181.0], [96.0, 181.0], [96.0, 199.0], [56.0, 199.0]], [住址, 0.9900635480880737]], [[[113.0, 182.0], [293.0, 182.0], [293.0, 202.0], [113.0, 202.0]], [华盛顿特区宾夕法尼, 0.9969079494476318]], [[[114.0, 216.0], [274.0, 216.0], [274.0, 236.0], [114.0, 236.0]], [亚大道1600号白宫, 0.9978928565979004]], [[[56.0, 265.0], [378.0, 263.0], [378.0, 283.0], [56.0, 285.0]], [公民身份证号码32622196108040096, 0.9448857307434082]]]]");
    assertEquals("奥巴马", params.get("name"));
    assertEquals("32622196108040096", params.get("idNumber"));
  }

  /**
   * 提取参数
   */
  private void extractParams(LlmClient client, String text, @Nullable String instruction, JsonSchemaNode paramsStructure, Map<String, Object> result) {
    logger.debug("text: {}", text);
    List<Message> messages = ParamExtractorHelper.buildMessages(text, instruction, paramsStructure, null);
    ChatCompletionResponse response = client.chatCompletion(ChatCompletionRequest.builder().messages(messages).build());
    String responseText = StringUtils.trimToNull(response.getMessageContent());
    assertNotNull(responseText, () -> String.format("text=%s, instruction=%s", text, instruction));
    logger.debug("response: {}", responseText);
    Map<String, Object> params = MarkdownHelper.parseJson(responseText);
    result.clear();
    if (!params.isEmpty()) {
      result.putAll(params);
    }
  }
}
