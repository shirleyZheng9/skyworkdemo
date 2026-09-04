package com.iwhalecloud.bote.llm.helper;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.consts.ResponseFormatType;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * Markdown 辅助类
 *
 * @author bianjp
 * @since 2024-10-18
 */
public final class MarkdownHelper {
  private static final Logger logger = LoggerFactory.getLogger(MarkdownHelper.class);

  private MarkdownHelper() {
  }

  /** 自定义的 ObjectMapper */
  @Getter
  private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper().copy()
    // 大模型有时会返回注释，需要兼容
    .enable(Feature.ALLOW_COMMENTS);

  /**
   * 解析 Markdown 文本中的 JSON 代码块中的数据
   *
   * @param markdown Markdown 文本。需要包含 JSON 代码块，或者整个文本是个 JSON 字符串
   * @return JSON 数据。不存在时返回空对象
   * @deprecated 请改用 {@link #parseJson(String)}，暂时保留，避免影响在工具箱、代码块节点使用了这个方法的工作流
   */
  @Deprecated
  public static Map<String, Object> parseMarkdownJson(String markdown) {
    return parseJson(markdown);
  }

  /**
   * 解析 Markdown 文本中的 JSON 代码块中的数据
   *
   * @param markdown Markdown 文本。需要包含 JSON 代码块，或者整个文本是个 JSON 字符串
   * @return JSON 数据。不存在时返回空对象
   */
  public static Map<String, Object> parseJson(String markdown) {
    Map<String, Object> map = parseJson(markdown, new TypeReference<Map<String, Object>>() {
    });
    return map == null ? Collections.emptyMap() : map;
  }


  /**
   * 解析 Markdown 文本中的 JSON 代码块中的数据
   *
   * @param markdown Markdown 文本。需要包含 JSON 代码块，或者整个文本是个 JSON 字符串
   * @return JSON 数据。不存在时返回 null
   */
  @Nullable
  public static <T> T parseJson(String markdown, Class<T> clazz) {
    return doParseJson(markdown, objectMapper.constructType(clazz));
  }

  /**
   * 解析 Markdown 文本中的 JSON 代码块中的数据
   *
   * @param markdown Markdown 文本。需要包含 JSON 代码块，或者整个文本是个 JSON 字符串
   * @return JSON 数据。不存在时返回 null
   */
  @Nullable
  public static <T> T parseJson(String markdown, TypeReference<T> type) {
    return doParseJson(markdown, objectMapper.constructType(type));
  }

  /**
   * 解析 Markdown 文本中的 JSON 代码块中的数据
   */
  @Nullable
  private static <T> T doParseJson(String markdown, JavaType type) {
    String json = extractJsonFromMarkdown(StringUtils.trim(markdown));
    if (StringUtils.isEmpty(json)) {
      return null;
    }
    try {
      return objectMapper.readValue(json, type);
    }
    catch (JsonProcessingException e) {
      logger.warn("Invalid JSON in markdown: {}", markdown, e);
      throw new BssException("Markdown 中的 JSON 不合法", e);
    }
  }

  /**
   * 提取 Markdown 中的 JSON 代码块内容
   */
  @Nullable
  public static String extractJsonFromMarkdown(String markdown) {
    if (StringUtils.isEmpty(markdown)) {
      return null;
    }
    // 如果有多个代码块，取最后一个，以兼容参数提取返回的思考过程中可能存在代码块的情况
    int startPos = markdown.lastIndexOf("```json");
    // 如果没有代码块标记，整个文本当作 JSON
    if (startPos == -1) {
      if (!markdown.startsWith("{")) {
        return null;
      }
      return markdown;
    }

    int endPos = markdown.indexOf("```", startPos + 1);
    if (endPos == -1) {
      logger.warn("Missing close tag in markdown JSON block: {}", markdown);
      throw new BssException("Markdown 中的 JSON 代码块缺少结束标记");
    }
    return markdown.substring(startPos + 7, endPos);
  }

  /**
   * 解析 Markdown 中的 JSON 代码块中的数据，JSON 有语法错误时尝试自动修复
   *
   * @param markdown Markdown 文本。需要包含 JSON 代码块，或者整个文本是个 JSON 字符串
   * @param client 大模型客户端，用于修复 JSON 语法错误
   * @return 解析结果
   */
  @Nullable
  public static JsonNode parseJsonAndAutoFix(String markdown, LlmClient client) {
    String json = extractJsonFromMarkdown(StringUtils.trim(markdown));
    if (StringUtils.isEmpty(json)) {
      return null;
    }

    try {
      return objectMapper.readTree(json);
    }
    catch (JsonProcessingException e) {
      // 尝试自动修复 JSON
      logger.trace("JSON invalid, trying to fix: {}", json);
      String fixedJson = tryFixJson(client, json);
      if (StringUtils.isNotEmpty(fixedJson)) {
        try {
          return objectMapper.readTree(fixedJson);
        }
        catch (Exception e2) {
          // 忽略修复过程中的异常
          logger.warn("Failed to fix json: original={}, fixed={}", json, fixedJson);
        }
      }
      logger.warn("Invalid JSON in markdown: {}", markdown, e);
      throw new BssException("Markdown 中的 JSON 不合法", e);
    }
  }

  /**
   * 尝试修复 JSON
   */
  @Nullable
  public static String tryFixJson(LlmClient client, String json) {
    try {
      ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
        .addSystemMessage("修复下面 JSON 数据中的语法错误（比如双引号不正确、字符串中的双引号未转义）。只输出修复后的 JSON 数据，不要输出多余的内容")
        .addUserMessage(json)
        .responseFormatType(ResponseFormatType.JSON_OBJECT)
        .build();
      ChatCompletionResponse response = client.chatCompletion(chatCompletionRequest);
      String content = response.getMessageContent();
      if (Strings.CS.contains(content, "```json")) {
        return extractJsonFromMarkdown(content);
      }
      return content;
    }
    catch (Exception e) {
      // 忽略修复过程中的异常
      logger.warn("Failed to fix json", e);
      return null;
    }
  }
}
