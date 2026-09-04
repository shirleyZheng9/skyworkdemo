package com.iwhalecloud.bote.llm.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * {@link MarkdownHelper} 单元测试
 *
 * <p>覆盖 extractJsonFromMarkdown/parseJson/parseJsonAndAutoFix/tryFixJson，含大模型自动修复 mock。</p>
 */
class MarkdownHelperTest {

  // ==================== extractJsonFromMarkdown ====================

  @Test
  void extractJsonFromMarkdown_plainJson() {
    assertThat(MarkdownHelper.extractJsonFromMarkdown("{\"a\":1}")).isEqualTo("{\"a\":1}");
  }

  @Test
  void extractJsonFromMarkdown_codeBlock() {
    String markdown = "前文\n```json\n{\"a\":1}\n```\n后文";
    // 源码返回 "```json" 与闭合 "```" 之间的原文（含换行）
    assertThat(MarkdownHelper.extractJsonFromMarkdown(markdown)).contains("{\"a\":1}");
  }

  @Test
  void extractJsonFromMarkdown_lastBlock() {
    String markdown = "```json\n{\"a\":1}\n```\n```json\n{\"b\":2}\n```";
    // 有多个代码块时取最后一个
    assertThat(MarkdownHelper.extractJsonFromMarkdown(markdown)).contains("{\"b\":2}");
  }

  @Test
  void extractJsonFromMarkdown_nullOrEmpty() {
    assertThat(MarkdownHelper.extractJsonFromMarkdown(null)).isNull();
    assertThat(MarkdownHelper.extractJsonFromMarkdown("")).isNull();
    assertThat(MarkdownHelper.extractJsonFromMarkdown("   ")).isNull();
  }

  @Test
  void extractJsonFromMarkdown_notStartsWithBraceAndNoBlock() {
    assertThat(MarkdownHelper.extractJsonFromMarkdown("hello")).isNull();
  }

  @Test
  void extractJsonFromMarkdown_missingCloseTag_throws() {
    assertThatThrownBy(() -> MarkdownHelper.extractJsonFromMarkdown("```json\n{\"a\":1}"))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("缺少结束标记");
  }

  // ==================== parseJson(String) -> Map ====================

  @Test
  void parseJson_map_success() {
    Map<String, Object> result = MarkdownHelper.parseJson("{\"a\":1}");
    assertThat(result).containsEntry("a", 1);
  }

  @Test
  void parseJson_map_emptyWhenNull() {
    // "no json" 不以 { 开头、无代码块 -> extractJsonFromMarkdown 返回 null -> 返回空 Map
    Map<String, Object> result = MarkdownHelper.parseJson("no json");
    assertThat(result).isNotNull().isEmpty();
  }

  // ==================== parseJson(String, Class) ====================

  @Test
  void parseJson_class_success() {
    @SuppressWarnings("unchecked")
    Map<String, Object> result = MarkdownHelper.parseJson("{\"a\":1}", Map.class);
    assertThat(result).containsEntry("a", 1);
  }

  @Test
  void parseJson_null_returnsNull() {
    Map<String, Object> result = MarkdownHelper.parseJson(null, Map.class);
    assertThat(result).isNull();
  }

  @Test
  void parseJson_invalid_throws() {
    assertThatThrownBy(() -> MarkdownHelper.parseJson("{bad}", Map.class))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("不合法");
  }

  @Test
  void parseJson_supportsComments() {
    // objectMapper 启用了 ALLOW_COMMENTS
    Map<String, Object> result = MarkdownHelper.parseJson("{\"a\":1 /* c */}");
    assertThat(result).containsEntry("a", 1);
  }

  // ==================== parseJsonAndAutoFix ====================

  @Test
  void parseJsonAndAutoFix_valid_returnsNode() {
    JsonNode node = MarkdownHelper.parseJsonAndAutoFix("{\"a\":1}", null);
    assertThat(node).isNotNull();
    assertThat(node.path("a").asInt()).isEqualTo(1);
  }

  @Test
  void parseJsonAndAutoFix_invalid_callsLlmAndFixes() {
    LlmClient client = mock(LlmClient.class);
    when(client.chatCompletion(any(ChatCompletionRequest.class)))
        .thenReturn(ChatCompletionResponse.ofMessage(new AssistantMessage("{\"a\":1}")));

    // {a:1} 是不合法 JSON（key 未加引号），LLM 修复后返回 {"a":1}
    JsonNode node = MarkdownHelper.parseJsonAndAutoFix("{a:1}", client);
    assertThat(node).isNotNull();
    assertThat(node.path("a").asInt()).isEqualTo(1);
    verify(client).chatCompletion(any(ChatCompletionRequest.class));
  }

  @Test
  void parseJsonAndAutoFix_fixStillInvalid_throws() {
    LlmClient client = mock(LlmClient.class);
    // LLM 返回的仍然是坏 JSON
    when(client.chatCompletion(any(ChatCompletionRequest.class)))
        .thenReturn(ChatCompletionResponse.ofMessage(new AssistantMessage("{b:1}")));

    assertThatThrownBy(() -> MarkdownHelper.parseJsonAndAutoFix("{a:1}", client))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("不合法");
  }

  @Test
  void parseJsonAndAutoFix_nullInput_returnsNull() {
    JsonNode node = MarkdownHelper.parseJsonAndAutoFix(null, null);
    assertThat(node).isNull();
  }

  // ==================== tryFixJson ====================

  @Test
  void tryFixJson_responseWithCodeBlock_extracts() {
    LlmClient client = mock(LlmClient.class);
    String content = "```json\n{\"k\":1}\n```";
    when(client.chatCompletion(any(ChatCompletionRequest.class)))
        .thenReturn(ChatCompletionResponse.ofMessage(new AssistantMessage(content)));

    String result = MarkdownHelper.tryFixJson(client, "{k:1}");
    assertThat(result).contains("{\"k\":1}");
  }

  @Test
  void tryFixJson_responsePlain_returnsContent() {
    LlmClient client = mock(LlmClient.class);
    when(client.chatCompletion(any(ChatCompletionRequest.class)))
        .thenReturn(ChatCompletionResponse.ofMessage(new AssistantMessage("{\"k\":1}")));

    String result = MarkdownHelper.tryFixJson(client, "{k:1}");
    assertThat(result).isEqualTo("{\"k\":1}");
  }

  @Test
  void tryFixJson_llmThrows_returnsNull() {
    LlmClient client = mock(LlmClient.class);
    when(client.chatCompletion(any(ChatCompletionRequest.class)))
        .thenThrow(new RuntimeException("LLM error"));

    String result = MarkdownHelper.tryFixJson(client, "{k:1}");
    assertThat(result).isNull();
  }
}
