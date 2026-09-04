package com.iwhalecloud.bote.adapter.juzhi2.client;

import com.iwhalecloud.bote.adapter.juzhi.JuzhiApiHelper;
import com.iwhalecloud.bote.adapter.juzhi2.dto.Juzhi2ChatMessage;
import com.iwhalecloud.bote.adapter.juzhi2.helper.Juzhi2ClientHelper;
import com.iwhalecloud.bote.adapter.juzhi2.helper.Juzhi2ReasoningStreamParser;
import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallTextItem;
import com.iwhalecloud.bote.dto.knowledge.SimpleKnowledgeDTO;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeClient;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import okhttp3.HttpUrl;
import okhttp3.sse.EventSource;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 二级聚智知识库客户端
 *
 * @author bianjp
 * @since 2025-05-13
 */
@Component
@ConditionalOnBooleanProperty("knowledge.juzhi2.enabled")
public class Juzhi2KnowledgeClient implements KnowledgeClient {
  private static final Logger logger = LoggerFactory.getLogger(Juzhi2KnowledgeClient.class);

  @Override
  public int getOrder() {
    return 2000;
  }

  @Override
  public String getKnowledgeType() {
    return "juzhi2";
  }

  @Override
  public KnowledgeRecallResponse recall(KnowledgeRecallParamDTO recallParams) {
    String assistantCode = Juzhi2ClientHelper.getKnowledgeRecallAssistantCode();
    HttpUrl url = Juzhi2ClientHelper.signUrl(assistantCode);
    String traceId = Juzhi2ClientHelper.newTraceId();
    Map<String, Object> params = buildRecallParams(recallParams, traceId, assistantCode);
    AtomicReference<List<KnowledgeRecallTextItem>> recallResponseReference = new AtomicReference<>();

    BiConsumer<String, JsonNode> messageHandler = (msgText, msgData) -> {
      JsonNode partsNode = msgData.path("payload").path("output").path("payload").path("recallParts");
      if (partsNode.isMissingNode()) {
        logger.warn("No recall results found: traceId={}, message={}", traceId, msgText);
      }
      else if (!partsNode.isArray()) {
        logger.warn("Invalid recall results found: traceId={}, message={}", traceId, msgText);
      }
      else {
        List<KnowledgeRecallTextItem> textItems = new ArrayList<>(partsNode.size());
        partsNode.elements().forEachRemaining(item -> textItems.add(parseRecallItem(item)));
        recallResponseReference.set(textItems);
      }
    };

    Juzhi2ClientHelper.invokeApiAndWait(traceId, url, params, messageHandler);
    KnowledgeRecallResponse response = new KnowledgeRecallResponse();
    response.setText(recallResponseReference.get());
    return response;
  }

  /**
   * 构造知识检索的参数
   */
  private Map<String, Object> buildRecallParams(KnowledgeRecallParamDTO recallParams, String traceId, String assistantCode) {
    String startNodeId = Juzhi2ClientHelper.getKnowledgeRecallStartNodeId();
    String juzhiKnowledgeId = getJunzhiKnowledgeId(recallParams);
    Map<String, Object> startNodeParams = new HashMap<>();
    startNodeParams.put("USER_INPUT", recallParams.getQuery());
    startNodeParams.put("dbNames", ImmutableList.of(juzhiKnowledgeId));
    startNodeParams.put("num", ObjectUtils.getIfNull(recallParams.getMaxNum(), 10));
    Map<String, Object> payload = ImmutableMap.of("input", ImmutableMap.of(startNodeId, startNodeParams));
    return Juzhi2ClientHelper.buildRequestParams(traceId, assistantCode, payload);
  }

  /**
   * 解析知识召回的一条数据
   */
  private KnowledgeRecallTextItem parseRecallItem(JsonNode part) {
    KnowledgeRecallTextItem item = new KnowledgeRecallTextItem();
    item.setScore(part.path("score").asDouble());
    item.setDocId(part.path("docId").asText(null));
    item.setDocName(part.path("docName").asText(null));
    item.setHeading(part.path("title").asText(null));
    // content 格式为 "文档名称-标题\n内容"，比如政企运营平台-菜单配置.docx-1. 菜单配置\n菜单配置可以进行新增、修改、删除目录菜单和叶子菜单
    item.setContent(StringUtils.substringAfter(part.path("content").asText(null), '\n'));
    return item;
  }

  @Override
  public KnowledgeChatResponse chat(KnowledgeChatParamsDTO chatParams) {
    String requestTime = DateUtil.formatCompact();
    String assistantCode = getAssistantCode(chatParams);
    HttpUrl url = Juzhi2ClientHelper.signUrl(assistantCode);
    String traceId = Juzhi2ClientHelper.newTraceId();
    Map<String, Object> params = buildRequestParams(traceId, assistantCode, chatParams);
    StringBuilder answer = new StringBuilder();
    StringBuilder reasoning = new StringBuilder();
    ThinkingStrategy thinkingStrategy = chatParams.getThinkingStrategy();
    Juzhi2ReasoningStreamParser streamParser = new Juzhi2ReasoningStreamParser();
    BiConsumer<String, JsonNode> messageHandler = (msgText, msgData) -> {
      handleStreamMessage(traceId, msgText, msgData, streamParser, answer, reasoning, thinkingStrategy, null);
    };
    Juzhi2ClientHelper.invokeApiAndWait(traceId, url, params, messageHandler);
    KnowledgeChatResponse response = new KnowledgeChatResponse();
    response.setReasoning(reasoning.isEmpty() ? null : reasoning.toString());
    response.setAnswer(answer.toString());
    JuzhiApiHelper.saveDialog(requestTime, chatParams.getQuestion(), response.getAnswer(), false);
    return response;
  }

  @Override
  public EventSource chatStream(KnowledgeChatParamsDTO chatParams, Consumer<SseEvent> partialHandler, Consumer<BssException> completionHandler) {
    String requestTime = DateUtil.formatCompact();
    String assistantCode = getAssistantCode(chatParams);
    HttpUrl url = Juzhi2ClientHelper.signUrl(assistantCode);
    String traceId = Juzhi2ClientHelper.newTraceId();
    Map<String, Object> params = buildRequestParams(traceId, assistantCode, chatParams);
    StringBuilder answer = new StringBuilder();
    StringBuilder reasoning = new StringBuilder();
    ThinkingStrategy thinkingStrategy = chatParams.getThinkingStrategy();
    Juzhi2ReasoningStreamParser streamParser = new Juzhi2ReasoningStreamParser();
    BiConsumer<String, JsonNode> messageHandler = (msgText, msgData) -> {
      handleStreamMessage(traceId, msgText, msgData, streamParser, answer, reasoning, thinkingStrategy, partialHandler);
    };
    Consumer<BssException> finalCompletionHandler = e -> {
      if (e == null) {
        JuzhiApiHelper.saveDialog(requestTime, chatParams.getQuestion(), answer.toString(), false);
      }
      completionHandler.accept(e);
    };
    return Juzhi2ClientHelper.invokeApi(traceId, url, params, messageHandler, finalCompletionHandler);
  }

  /**
   * 处理一条 WebSocket 流式消息
   */
  private void handleStreamMessage(String traceId, String msgText, JsonNode msgData, Juzhi2ReasoningStreamParser streamParser,
                                   StringBuilder answer, StringBuilder reasoning, @Nullable ThinkingStrategy thinkingStrategy,
                                   @Nullable Consumer<SseEvent> partialHandler) {
    String rawContent = extractRawTextContent(traceId, msgText, msgData);
    if (rawContent == null) {
      return;
    }
    for (Juzhi2ReasoningStreamParser.Chunk chunk : streamParser.parse(rawContent)) {
      appendStreamChunk(chunk, answer, reasoning, thinkingStrategy, partialHandler);
    }
  }

  /**
   * 提取原始文本片段（不做推理/正文分类）
   */
  @Nullable
  private String extractRawTextContent(String traceId, String msgText, JsonNode msgData) {
    JsonNode msg = msgData.path("payload").path("choices").path("text").path(0);
    if (msg.isMissingNode()) {
      logger.warn("No stream content found: traceId={}, message={}", traceId, msgText);
    }
    else if (!"assistant".equals(msg.path("role").asText())) {
      logger.warn("No assistant message found: traceId={}, message={}", traceId, msgText);
    }
    else {
      String contentType = msg.path("content_type").asText();
      if ("text".equals(contentType)) {
        return msg.path("content").asText(null);
      }
      // 忽略处理进度消息
      if (!"progress".equals(contentType)) {
        logger.warn("Unknow message content type: traceId={}, message={}", traceId, msgText);
      }
    }
    return null;
  }

  /**
   * 追加流式片段并转发 SSE 事件
   */
  private static void appendStreamChunk(Juzhi2ReasoningStreamParser.Chunk chunk, StringBuilder answer, StringBuilder reasoning,
                                      @Nullable ThinkingStrategy thinkingStrategy,
                                      @Nullable Consumer<SseEvent> partialHandler) {
    if (chunk.reasoning()) {
      if (!ThinkingStrategy.shouldShowReasoning(thinkingStrategy)) {
        return;
      }
      reasoning.append(chunk.content());
      if (partialHandler != null) {
        partialHandler.accept(SseEvent.ofReasoning(chunk.content()));
      }
      return;
    }
    answer.append(chunk.content());
    if (partialHandler != null) {
      partialHandler.accept(SseEvent.ofText(chunk.content()));
    }
  }

  /**
   * 获取聚智平台的知识库 ID
   */
  private String getJunzhiKnowledgeId(KnowledgeRecallParamDTO params) {
    String knowledgeId = MapUtils.getString(params.getKnowledge().getKnowledgeTypeExt(), "juzhiKnowledgeId");
    Assert.hasLength(knowledgeId, () -> "知识库未配置聚智平台的知识库 ID: knowledgeId=" + params.getKnowledgeId());
    return knowledgeId;
  }

  /**
   * 获取知识问答的智能体编码
   */
  private String getAssistantCode(KnowledgeChatParamsDTO params) {
    Assert.isTrue(params.getKnowledgeList().size() == 1, "对接聚智不支持同时查询多个知识库");
    SimpleKnowledgeDTO knowledge = params.getKnowledgeList().get(0);
    String assistantCode = MapUtils.getString(knowledge.getKnowledgeTypeExt(), "juzhiAssistantCode");
    Assert.hasLength(assistantCode, () -> "知识库未配置聚智平台的知识问答智能体编码: knowledgeId=" + knowledge.getKnowledgeId());
    return assistantCode;
  }

  /**
   * 构造请求参数
   */
  private Map<String, Object> buildRequestParams(String traceId, String assistantCode, KnowledgeChatParamsDTO chatParams) {
    List<Juzhi2ChatMessage> messages = new ArrayList<>();
    for (Message message : ListUtils.emptyIfNull(chatParams.getHistory())) {
      if (message instanceof AssistantMessage) {
        messages.add(new Juzhi2ChatMessage(MessageRole.ASSISTANT, ((AssistantMessage) message).getContent()));
      }
      else if (message instanceof UserMessage) {
        Object content = ((UserMessage) message).getContent();
        Assert.isTrue(content instanceof String, "对接聚智的知识问答用户消息只支持字符串");
        messages.add(new Juzhi2ChatMessage(MessageRole.USER, (String) content));
      }
      else {
        throw new BssException("未知的知识问答历史消息类型: " + message.getRole().getCode());
      }
    }
    messages.add(new Juzhi2ChatMessage(MessageRole.USER, chatParams.getQuestion()));
    return Juzhi2ClientHelper.buildRequestParams(traceId, assistantCode, ImmutableMap.of("text", messages));
  }
}
