package com.iwhalecloud.bote.agent.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.agent.memory.impl.InMemoryMemory;
import com.iwhalecloud.bote.agent.tools.A2uiTools.A2uiEvent;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.agent.A2uiGenerateResult;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A2UI 生成器
 *
 * <p>
 * 参考资源:
 *  <ul>
 *   <li><a href="https://github.com/google/A2UI">A2UI</a></li>
 *   <li><a href="https://github.com/ag2ai/ag2/blob/main/autogen/agents/experimental/a2ui/a2ui_agent.py">AG2</a></li>
 *   <li><a href="https://github.com/ag-ui-protocol/ag-ui/blob/main/integrations/langgraph/python/examples/agents/a2ui_dynamic_schema/agent.py">ag-ui</a></li>
 * </ul>
 * </p>
 *
 * @author bianjp
 * @since 2026-04-14
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class A2uiGenerator {
  private static final Logger logger = LoggerFactory.getLogger(A2uiGenerator.class);

  /** 大模型输出结果中的 A2UI JSON 分隔符 */
  private static final String A2UI_JSON_DELIMITER = "---a2ui_JSON---";
  /** 系统提示词资源 */
  private static final String PROMPT_RESOURCE = "agent/a2ui/system-prompt.md";
  /** A2UI Schema 资源 */
  private static final String SCHEMA_RESOURCE = "agent/a2ui/a2ui-schema.json";
  /** 提示词模板中的 A2UI Schema 占位符 */
  private static final String SCHEMA_PLACEHOLDER = "{{A2UI_JSON_SCHEMA}}";
  /** 最大重试次数 */
  private static final int MAX_RETRIES = 10;
  /** 系统提示词 */
  private static final String SYSTEM_PROMPT;
  /** A2UI 事件的 JSON Schema */
  private static final Schema A2UI_EVENT_SCHEMA;

  private A2uiGenerator() {
  }

  // 初始化提示词、JSON Schema
  static {
    String promptTemplate;
    String schemaJson;
    try (InputStream promptInputStream = new ClassPathResource(PROMPT_RESOURCE).getInputStream();
         InputStream schemaInputStream = new ClassPathResource(SCHEMA_RESOURCE).getInputStream()) {
      promptTemplate = IOUtils.toString(promptInputStream, StandardCharsets.UTF_8);
      schemaJson = IOUtils.toString(schemaInputStream, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new BssException("加载 A2UI 资源失败: " + e.getMessage(), e);
    }
    SYSTEM_PROMPT = promptTemplate.replace(SCHEMA_PLACEHOLDER, schemaJson);

    Schema result;
    try {
      JsonNode schemaNode = JsonUtil.getObjectMapper().readTree(schemaJson);
      SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7,
        b -> b.nodeReader(nr -> nr.jsonMapper(JsonUtil.getObjectMapper())));
      result = registry.getSchema(schemaNode);
    }
    catch (JsonProcessingException e) {
      throw new BssException("解析 A2UI JSON Schema 失败: " + e.getMessage(), e);
    }
    A2UI_EVENT_SCHEMA = result;
  }

  /**
   * 生成 A2UI 事件列表
   *
   * @param modelClient 模型客户端
   * @param prompt 提示词，描述 A2UI 卡片要求
   * @return 生成结果
   */
  public static A2uiGenerateResult generateA2UI(LlmClient modelClient, String prompt) {
    Assert.hasText(prompt, "提示词不能为空");

    InMemoryMemory memory = new InMemoryMemory();
    String currentPrompt = prompt;

    for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      // 使用 ReActAgent 以复用其自动重试功能
      ReActAgent agent = ReActAgent.builder()
        .modelClient(modelClient)
        .systemPrompt(SYSTEM_PROMPT)
        .userPrompt(currentPrompt)
        .memory(memory)
        .build();
      ChatCompletionResponse response;
      try {
        response = agent.call();
      }
      catch (Exception e) {
        // 调用大模型失败时 ReActAgent 内部已经做了重试，这里不需要再重试
        logger.warn("Failed to generate a2ui: model={}, prompt={}", modelClient.defaultModel(), prompt, e);
        return new A2uiGenerateResult(false, ExpUtil.getMsg(e), null);
      }

      AssistantMessage assistantMessage = response.getMessage();
      String responseText = StringUtils.trimToNull(assistantMessage.getContent());
      // 未生成内容，重试
      if (responseText == null) {
        continue;
      }
      // 返回了报错，结束
      if (!responseText.contains(A2UI_JSON_DELIMITER)) {
        return new A2uiGenerateResult(false, responseText.trim(), null);
      }

      // 提取 JSON
      String json = StringUtils.trimToNull(StringUtils.substringBetween(responseText, A2UI_JSON_DELIMITER));
      if (json == null) {
        logger.debug("Failed to generate a2ui, empty json: retry={}/{}, model={}, prompt={}, response={}",
          attempt, MAX_RETRIES, modelClient.defaultModel(), prompt, responseText);
        currentPrompt = "A2UI 事件列表必须包裹在两个 " + A2UI_JSON_DELIMITER + " 标记之间";
        continue;
      }

      // 校验 JSON 是否合法
      A2uiGenerateResult result = parseAndValidateEvents(json);
      if (result.success()) {
        return result;
      }

      logger.debug("Failed to generate a2ui: retry={}/{}, error={}, model={}, prompt={}, response={}",
        attempt, MAX_RETRIES, result.error(), modelClient.defaultModel(), prompt, responseText);
      currentPrompt = "输出的 A2UI 无效，请根据提示修正:\n" + result.error();
    }

    return new A2uiGenerateResult(false, "生成 A2UI 失败", null);
  }

  /**
   * 解析并校验事件列表
   */
  private static A2uiGenerateResult parseAndValidateEvents(String json) {
    JsonNode root;
    // 解析 JSON
    try {
      root = JsonUtil.getObjectMapper().readTree(json);
    }
    catch (JsonProcessingException e) {
      return new A2uiGenerateResult(false, "JSON 不合法: " + ExpUtil.getMsg(e), null);
    }

    // 必须是数组且不能为空
    if (!root.isArray()) {
      return new A2uiGenerateResult(false, "A2UI 输出必须是 JSON 数组", null);
    }
    if (root.isEmpty()) {
      return new A2uiGenerateResult(false, "A2UI 事件列表不能为空", null);
    }

    // 校验 JSON schema
    String schemaError = validateEventsAgainstJsonSchema(root);
    if (schemaError != null) {
      return new A2uiGenerateResult(false, "不符合 A2UI JSON Schema:\n" + schemaError, null);
    }

    // 转换为事件列表
    List<A2uiEvent> events;
    try {
      events = JsonUtil.getObjectMapper().convertValue(root, new TypeReference<>() {
      });
    }
    catch (Exception e) {
      return new A2uiGenerateResult(false, "无法转换为 A2UI 事件对象: " + ExpUtil.getMsg(e), null);
    }

    // 校验事件顺序
    if (events.getFirst().surfaceUpdate() == null || events.getLast().beginRendering() == null) {
      return new A2uiGenerateResult(false, "事件列表须以 surfaceUpdate 开头，以 beginRendering 结尾", null);
    }
    return new A2uiGenerateResult(true, null, events);
  }

  /**
   * 校验事件列表是否符合 A2UI JSON Schema
   */
  @Nullable
  private static String validateEventsAgainstJsonSchema(JsonNode events) {
    List<String> errorMessages = new ArrayList<>();
    for (int i = 0; i < events.size(); i++) {
      JsonNode item = events.get(i);
      List<Error> errors = A2UI_EVENT_SCHEMA.validate(item);
      for (Error error : errors) {
        String path = error.getInstanceLocation() != null ? error.getInstanceLocation().toString() : "";
        if (StringUtils.isNotEmpty(path)) {
          String property = error.getProperty();
          if (StringUtils.isNotEmpty(property)) {
            path = path + "/" + property;
          }
          errorMessages.add("* %d%s: %s".formatted(i, path, error.getMessage()));
        }
        else {
          errorMessages.add("* %d: %s".formatted(i, error.getMessage()));
        }
      }
    }
    if (errorMessages.isEmpty()) {
      return null;
    }
    return String.join("\n", errorMessages);
  }

}
