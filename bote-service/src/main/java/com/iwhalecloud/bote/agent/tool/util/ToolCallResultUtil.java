package com.iwhalecloud.bote.agent.tool.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.agent.tool.support.ToolExecutionResult;
import com.iwhalecloud.bote.agent.tools.A2uiTools.A2uiEvent;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.service.orchestration.runner.step.PageStepRunner;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 工具调用结果工具类
 *
 * @author bianjp
 * @since 2026-05-20
 */
public final class ToolCallResultUtil {
  /** 输出结果中的 BOTE 事件标签 */
  private static final Pattern BOTE_EVENT_TAG_PATTERN = Pattern.compile("<bote:event\\s+type=\"([^\"]+)\">\\s*(.*?)\\s*</bote:event>", Pattern.DOTALL);

  private ToolCallResultUtil() {
  }

  /**
   * 从工具调用结果中提取特殊事件
   *
   * @param output 工具调用结果
   * @return 包含特殊事件的工具执行结果，没有特殊事件时返回 null
   */
  @Nullable
  public static ToolExecutionResult extractEvent(String output, @Nullable Long tenantId) {
    // 快速检测
    if (StringUtils.isEmpty(output) || !output.contains("<bote:event")) {
      return null;
    }
    Matcher matcher = BOTE_EVENT_TAG_PATTERN.matcher(output);
    if (!matcher.find()) {
      return null;
    }
    String eventType = StringUtils.trimToNull(matcher.group(1));
    String json = StringUtils.trimToNull(matcher.group(2));
    if (eventType == null || json == null) {
      return null;
    }
    ChatMessageType msgType = mapMessageType(eventType);
    if (msgType == null) {
      return null;
    }
    Object msgContent = parseEventContent(msgType, json, tenantId);
    if (msgContent == null) {
      return null;
    }
    return ToolExecutionResult.builder()
      .success(true)
      .result(msgType == ChatMessageType.A2UI ? "Rendered A2UI card successfully" : "Done")
      .returnDirect(true)
      .msgType(msgType)
      .msgContent(msgContent)
      .build();
  }

  /**
   * 将事件类型编码映射为消息类型
   */
  @Nullable
  private static ChatMessageType mapMessageType(String eventType) {
    if (ChatMessageType.A2UI.getCode().equals(eventType)) {
      return ChatMessageType.A2UI;
    }
    if (ChatMessageType.PAGE.getCode().equals(eventType)) {
      return ChatMessageType.PAGE;
    }
    return null;
  }

  /**
   * 解析事件 JSON 内容，JSON 非法时返回 null
   */
  @Nullable
  private static Object parseEventContent(ChatMessageType msgType, String json, @Nullable Long tenantId) {
    try {
      JsonNode node = JsonUtil.getObjectMapper().readTree(json);
      if (msgType == ChatMessageType.A2UI) {
        return parseA2uiEventData(node);
      }
      if (msgType == ChatMessageType.PAGE && tenantId != null) {
        return parsePageData(tenantId, node);
      }
      return JsonUtil.getObjectMapper().readValue(json, Object.class);
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * 解析 A2UI 事件的数据
   */
  @Nullable
  private static List<A2uiEvent> parseA2uiEventData(JsonNode node) {
    List<A2uiEvent> events = JsonUtil.convert(node, new TypeReference<>() {
    });
    if (!events.isEmpty()) {
      return events;
    }
    return null;
  }

  /**
   * 解析页面事件的数据
   */
  @Nullable
  private static Map<String, Object> parsePageData(Long tenantId, JsonNode node) {
    long pageId = node.path("pageId").asLong();
    if (pageId > 0) {
      JsonNode parametersNode = node.path("parameters");
      Map<String, Object> parameters = !parametersNode.isObject() ? null : JsonUtil.convert(parametersNode, new TypeReference<>() {
      });
      return PageStepRunner.buildPageOutput(tenantId, null, pageId, parameters);
    }
    return null;
  }
}
