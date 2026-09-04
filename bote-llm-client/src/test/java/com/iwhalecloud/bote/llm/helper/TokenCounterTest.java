package com.iwhalecloud.bote.llm.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.iwhalecloud.bote.llm.client.consts.JsonSchemaDataType;
import com.iwhalecloud.bote.llm.client.dto.FunctionCall;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.llm.client.dto.Function;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.FunctionMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ImageUrl;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.MessageContent;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link TokenCounter} 单元测试
 *
 * <p>覆盖 estimate 文本/消息/工具估算、编码器选择、各消息类型与参数类型分支。</p>
 */
class TokenCounterTest {

  // ==================== estimate(model, text) ====================

  @Test
  void estimate_text_positive() {
    assertThat(TokenCounter.estimate("gpt-4o", "hello world")).isGreaterThan(0);
  }

  @Test
  void estimate_text_nullOrEmpty_zero() {
    assertThat(TokenCounter.estimate("gpt-4o", (String) null)).isZero();
    assertThat(TokenCounter.estimate("gpt-4o", "")).isZero();
  }

  @Test
  void estimate_encodingSelection_gpt4vs4o() {
    String text = "hello world token estimation test";
    int gpt4 = TokenCounter.estimate("gpt-4", text);
    int gpt4o = TokenCounter.estimate("gpt-4o", text);
    assertThat(gpt4).isGreaterThan(0);
    assertThat(gpt4o).isGreaterThan(0);
  }

  @Test
  void estimate_qwen_defaultsToGpt4o() {
    // qwen-plus 不含 gpt-3/gpt-4(非4o) -> 默认 GPT_4O
    int qwen = TokenCounter.estimate("qwen-plus", "hello world");
    int gpt4o = TokenCounter.estimate("gpt-4o", "hello world");
    assertThat(qwen).isEqualTo(gpt4o);
  }

  // ==================== estimate(model, Message) ====================

  @Test
  void estimate_systemMessage() {
    int textTokens = TokenCounter.estimate("gpt-4o", "abc");
    int msgTokens = TokenCounter.estimate("gpt-4o", new SystemMessage("abc"));
    assertThat(msgTokens).isEqualTo(5 + textTokens);
    assertThat(msgTokens).isGreaterThan(5);
  }

  @Test
  void estimate_userMessage() {
    int textTokens = TokenCounter.estimate("gpt-4o", "abc");
    int msgTokens = TokenCounter.estimate("gpt-4o", new UserMessage("abc"));
    assertThat(msgTokens).isEqualTo(5 + textTokens);
  }

  @Test
  void estimate_assistantMessage() {
    int textTokens = TokenCounter.estimate("gpt-4o", "abc");
    int msgTokens = TokenCounter.estimate("gpt-4o", new AssistantMessage("abc"));
    assertThat(msgTokens).isEqualTo(5 + textTokens);
  }

  @Test
  void estimate_assistantMessageWithToolCall() {
    AssistantMessage msg = new AssistantMessage();
    msg.setContent("hi");
    msg.setToolCalls(List.of(new ToolCall(0, "call_1", "getWeather", "{\"city\":\"x\"}")));

    int withToolCall = TokenCounter.estimate("gpt-4o", msg);
    int plain = TokenCounter.estimate("gpt-4o", new AssistantMessage("hi"));
    // 多出 6 + name tokens + arguments tokens
    assertThat(withToolCall).isGreaterThan(plain);
    int nameTokens = TokenCounter.estimate("gpt-4o", "getWeather");
    int argsTokens = TokenCounter.estimate("gpt-4o", "{\"city\":\"x\"}");
    assertThat(withToolCall).isEqualTo(plain + 6 + nameTokens + argsTokens);
  }

  @Test
  void estimate_assistantMessageWithFunctionCall() {
    AssistantMessage msg = new AssistantMessage();
    msg.setContent("hi");
    msg.setFunctionCall(new FunctionCall("getWeather", "{\"city\":\"x\"}"));

    int withFc = TokenCounter.estimate("gpt-4o", msg);
    int plain = TokenCounter.estimate("gpt-4o", new AssistantMessage("hi"));
    assertThat(withFc).isGreaterThan(plain);
    int nameTokens = TokenCounter.estimate("gpt-4o", "getWeather");
    int argsTokens = TokenCounter.estimate("gpt-4o", "{\"city\":\"x\"}");
    assertThat(withFc).isEqualTo(plain + 6 + nameTokens + argsTokens);
  }

  @Test
  void estimate_toolMessage() {
    int textTokens = TokenCounter.estimate("gpt-4o", "content");
    int msgTokens = TokenCounter.estimate("gpt-4o", new ToolMessage("id", "content"));
    assertThat(msgTokens).isEqualTo(5 + textTokens);
  }

  @Test
  void estimate_functionMessage() {
    int textTokens = TokenCounter.estimate("gpt-4o", "content");
    int msgTokens = TokenCounter.estimate("gpt-4o", new FunctionMessage("name", "content"));
    assertThat(msgTokens).isEqualTo(5 + textTokens);
  }

  @Test
  void estimate_userMessageWithImage_adds85() {
    MessageContent imageContent = new MessageContent(new ImageUrl("http://x/a.png"));
    UserMessage msg = new UserMessage(List.of(imageContent));

    int tokens = TokenCounter.estimate("gpt-4o", msg);
    // 5 (base) + 0 (name null) + 85 (image)
    assertThat(tokens).isEqualTo(5 + 85);
  }

  @Test
  void estimate_userMessageWithTextAndImage() {
    MessageContent textContent = new MessageContent("hello");
    MessageContent imageContent = new MessageContent(new ImageUrl("http://x/a.png"));
    UserMessage msg = new UserMessage(List.of(textContent, imageContent));

    int tokens = TokenCounter.estimate("gpt-4o", msg);
    int textTokens = TokenCounter.estimate("gpt-4o", "hello");
    // 5 (base) + 0 (name null) + text tokens + 85 (image)
    assertThat(tokens).isEqualTo(5 + textTokens + 85);
  }

  @Test
  void estimate_unsupportedMessage_throws() {
    Message mockMessage = mock(Message.class);
    assertThatThrownBy(() -> TokenCounter.estimate("gpt-4o", mockMessage))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported message type");
  }

  // ==================== estimateTools ====================

  @Test
  void estimateTools_emptyOrNull_zero() {
    assertThat(TokenCounter.estimateTools("gpt-4o", null)).isZero();
    assertThat(TokenCounter.estimateTools("gpt-4o", List.of())).isZero();
  }

  @Test
  void estimateTools_positive() {
    Tool tool = new Tool(new Function("getWeather", "查天气",
        JsonSchemaNode.newObject().addProperty("city", "城市", JsonSchemaDataType.STRING)));
    int tokens = TokenCounter.estimateTools("gpt-4o", List.of(tool));
    // 16 (base) + 6 + name + desc + params
    assertThat(tokens).isGreaterThan(16);
  }

  @Test
  void estimateTools_objectParameter() {
    // 参数为 OBJECT 含 1 个 STRING 属性 -> 覆盖 OBJECT 分支
    JsonSchemaNode params = JsonSchemaNode.newObject()
        .addProperty("city", "城市", JsonSchemaDataType.STRING);
    Tool tool = new Tool(new Function("getWeather", "查天气", params));
    int tokens = TokenCounter.estimateTools("gpt-4o", List.of(tool));

    int nameTokens = TokenCounter.estimate("gpt-4o", "getWeather");
    int descTokens = TokenCounter.estimate("gpt-4o", "查天气");
    int cityKeyTokens = TokenCounter.estimate("gpt-4o", "city");
    int cityDescTokens = TokenCounter.estimate("gpt-4o", "城市");
    // 16 + 6 + name + desc + (3 + cityKey + (3 + cityDesc))
    assertThat(tokens).isEqualTo(16 + 6 + nameTokens + descTokens + 3 + cityKeyTokens + 3 + cityDescTokens);
  }

  @Test
  void estimateTools_arrayParameter() {
    // 参数为 ARRAY（items 为 STRING 属性）-> 覆盖 ARRAY 分支
    JsonSchemaNode items = JsonSchemaNode.newString("标签");
    JsonSchemaNode params = JsonSchemaNode.newArray("标签数组", items);
    Tool tool = new Tool(new Function("getTags", "获取标签", params));
    int tokens = TokenCounter.estimateTools("gpt-4o", List.of(tool));

    int nameTokens = TokenCounter.estimate("gpt-4o", "getTags");
    int descTokens = TokenCounter.estimate("gpt-4o", "获取标签");
    int itemsDescTokens = TokenCounter.estimate("gpt-4o", "标签");
    // 16 + 6 + name + desc + (3 + (3 + itemsDesc))
    assertThat(tokens).isEqualTo(16 + 6 + nameTokens + descTokens + 3 + 3 + itemsDescTokens);
  }

  @Test
  void estimateTools_nullParameter() {
    // function.parameters=null -> 覆盖 node==null 返回 0 分支
    Function fn = new Function();
    fn.setName("getWeather");
    fn.setDescription("查天气");
    // parameters stays null (no-arg constructor doesn't set it)
    Tool tool = new Tool(fn);
    int tokens = TokenCounter.estimateTools("gpt-4o", List.of(tool));

    int nameTokens = TokenCounter.estimate("gpt-4o", "getWeather");
    int descTokens = TokenCounter.estimate("gpt-4o", "查天气");
    // 16 + 6 + name + desc + 0 (null parameter)
    assertThat(tokens).isEqualTo(16 + 6 + nameTokens + descTokens);
  }
}
