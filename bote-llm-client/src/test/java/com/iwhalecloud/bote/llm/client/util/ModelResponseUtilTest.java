package com.iwhalecloud.bote.llm.client.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.FunctionCall;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.Usage;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link ModelResponseUtil} 单元测试
 *
 * <p>覆盖 fixParallelToolCalls 与 mergeStreamResponses（含工具/函数调用合并、异常分支）。</p>
 */
class ModelResponseUtilTest {

  // ==================== fixParallelToolCalls ====================

  @Test
  void fixParallelToolCalls_multipleToolCalls_keepsFirst() {
    AssistantMessage message = new AssistantMessage();
    message.setContent("");
    List<ToolCall> toolCalls = new ArrayList<>();
    toolCalls.add(new ToolCall(0, "call_1", "getWeather", "{\"city\":\"x\"}"));
    toolCalls.add(new ToolCall(1, "call_2", "getTime", "{}"));
    message.setToolCalls(toolCalls);

    ChatCompletionResponse response = ChatCompletionResponse.ofMessage(message);
    ModelResponseUtil.fixParallelToolCalls(response);

    List<ToolCall> result = response.getMessage().getToolCalls();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getFunction().getName()).isEqualTo("getWeather");
  }

  @Test
  void fixParallelToolCalls_singleOrEmpty_unchanged() {
    // 0 toolCalls -> unchanged (null)
    AssistantMessage msg0 = new AssistantMessage("hi");
    ChatCompletionResponse resp0 = ChatCompletionResponse.ofMessage(msg0);
    ModelResponseUtil.fixParallelToolCalls(resp0);
    assertThat(resp0.getMessage().getToolCalls()).isNull();

    // 1 toolCall -> unchanged
    AssistantMessage msg1 = new AssistantMessage();
    msg1.setContent("");
    msg1.setToolCalls(Collections.singletonList(new ToolCall(0, "call_1", "getWeather", "{}")));
    ChatCompletionResponse resp1 = ChatCompletionResponse.ofMessage(msg1);
    ModelResponseUtil.fixParallelToolCalls(resp1);
    assertThat(resp1.getMessage().getToolCalls()).hasSize(1);
  }

  // ==================== mergeStreamResponses ====================

  @Test
  void mergeStreamResponses_contentConcatenated() {
    ChatCompletionResponse d1 = ChatCompletionResponse.ofDelta(new AssistantMessage("Hello"), false);
    ChatCompletionResponse d2 = ChatCompletionResponse.ofDelta(new AssistantMessage(" world"), false);

    ChatCompletionResponse merged = ModelResponseUtil.mergeStreamResponses(List.of(d1, d2));

    assertThat(merged.getMessage().getContent()).isEqualTo("Hello world");
    assertThat(merged.getChoices().get(0).getFinishReason()).isEqualTo("stop");
    assertThat(merged.getObject()).isEqualTo("chat.completion");
  }

  @Test
  void mergeStreamResponses_reasoningConcatenated() {
    AssistantMessage m1 = new AssistantMessage();
    m1.setReasoningContent("思考1");
    AssistantMessage m2 = new AssistantMessage();
    m2.setReasoningContent("思考2");

    ChatCompletionResponse merged = ModelResponseUtil.mergeStreamResponses(List.of(
        ChatCompletionResponse.ofDelta(m1, false),
        ChatCompletionResponse.ofDelta(m2, false)));

    assertThat(merged.getMessage().getReasoningContent()).isEqualTo("思考1思考2");
  }

  @Test
  void mergeStreamResponses_toolCallsMerged() {
    // 分片返回同一 index 的工具调用：name 在一片，arguments 分两片
    AssistantMessage m1 = new AssistantMessage();
    m1.setToolCalls(List.of(new ToolCall(0, "call_1", "getWeather", null)));
    AssistantMessage m2 = new AssistantMessage();
    m2.setToolCalls(List.of(new ToolCall(0, null, null, "{\"city\"")));
    AssistantMessage m3 = new AssistantMessage();
    m3.setToolCalls(List.of(new ToolCall(0, null, null, ":\"x\"}")));

    ChatCompletionResponse merged = ModelResponseUtil.mergeStreamResponses(List.of(
        ChatCompletionResponse.ofDelta(m1, false),
        ChatCompletionResponse.ofDelta(m2, false),
        ChatCompletionResponse.ofDelta(m3, false)));

    List<ToolCall> toolCalls = merged.getMessage().getToolCalls();
    assertThat(toolCalls).hasSize(1);
    assertThat(toolCalls.get(0).getId()).isEqualTo("call_1");
    assertThat(toolCalls.get(0).getFunction().getName()).isEqualTo("getWeather");
    assertThat(toolCalls.get(0).getFunction().getArguments()).isEqualTo("{\"city\":\"x\"}");
  }

  @Test
  void mergeStreamResponses_functionCallMerged() {
    // delta 带 functionCall（无 toolCalls）-> 合并为 1 个 FunctionCall
    AssistantMessage m1 = new AssistantMessage();
    m1.setFunctionCall(new FunctionCall("getWeather", "{\"city\""));
    AssistantMessage m2 = new AssistantMessage();
    m2.setFunctionCall(new FunctionCall(null, ":\"x\"}"));

    ChatCompletionResponse merged = ModelResponseUtil.mergeStreamResponses(List.of(
        ChatCompletionResponse.ofDelta(m1, false),
        ChatCompletionResponse.ofDelta(m2, false)));

    FunctionCall fc = merged.getMessage().getFunctionCall();
    assertThat(fc).isNotNull();
    assertThat(fc.getName()).isEqualTo("getWeather");
    assertThat(fc.getArguments()).isEqualTo("{\"city\":\"x\"}");
    assertThat(merged.getMessage().getToolCalls()).isNull();
  }

  @Test
  void mergeStreamResponses_picksIdModelUsage() {
    ChatCompletionResponse d1 = ChatCompletionResponse.ofDelta(new AssistantMessage("a"), false);
    d1.setId("id1");
    d1.setModel("gpt-4o");

    ChatCompletionResponse d2 = ChatCompletionResponse.ofDelta(new AssistantMessage("b"), false);
    Usage usage1 = new Usage();
    d2.setUsage(usage1);

    ChatCompletionResponse d3 = ChatCompletionResponse.ofDelta(new AssistantMessage("c"), false);
    Usage usage2 = new Usage();
    d3.setUsage(usage2);

    ChatCompletionResponse merged = ModelResponseUtil.mergeStreamResponses(List.of(d1, d2, d3));

    // id/model 取首个非空
    assertThat(merged.getId()).isEqualTo("id1");
    assertThat(merged.getModel()).isEqualTo("gpt-4o");
    // usage 取最后非空（reversed 遍历）
    assertThat(merged.getUsage()).isSameAs(usage2);
  }

  @Test
  void mergeStreamResponses_empty_throws() {
    assertThatThrownBy(() -> ModelResponseUtil.mergeStreamResponses(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("流式响应列表不能为空");
    assertThatThrownBy(() -> ModelResponseUtil.mergeStreamResponses(List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("流式响应列表不能为空");
  }

  @Test
  void mergeStreamResponses_toolCallMissingName_throws() {
    AssistantMessage m1 = new AssistantMessage();
    m1.setToolCalls(List.of(new ToolCall(0, "call_1", null, "{\"city\":\"x\"}")));

    assertThatThrownBy(() -> ModelResponseUtil.mergeStreamResponses(
        List.of(ChatCompletionResponse.ofDelta(m1, false))))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("工具名称不能为空");
  }

  @Test
  void mergeStreamResponses_functionCallMissingName_throws() {
    AssistantMessage m1 = new AssistantMessage();
    m1.setFunctionCall(new FunctionCall(null, "{\"city\":\"x\"}"));

    assertThatThrownBy(() -> ModelResponseUtil.mergeStreamResponses(
        List.of(ChatCompletionResponse.ofDelta(m1, false))))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("函数名称不能为空");
  }
}
