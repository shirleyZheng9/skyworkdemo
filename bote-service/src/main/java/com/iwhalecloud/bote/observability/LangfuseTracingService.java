package com.iwhalecloud.bote.observability;

import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.config.properties.LangfuseProperties;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeCallObserver;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import com.iwhalecloud.bote.llm.client.dto.Usage;
import com.iwhalecloud.bote.llm.client.util.LlmTraceUtil;
import com.iwhalecloud.bote.mcp.client.McpToolCallObserver;
import com.iwhalecloud.bote.mcp.dto.request.CallToolRequest;
import com.iwhalecloud.bote.mcp.dto.response.CallToolResult;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * 将 LLM / MCP / 知识库调用上报到 Langfuse（Ingestion API），并串联为同一 Trace 下的调用树。
 * <pre>
 *   Trace (traceId)
 *     └─ agent-turn (root span)
 *          ├─ chat-completion (generation)
 *          │    ├─ mcp:tool
 *          │    └─ knowledge-recall
 *          └─ chat-completion (generation)
 * </pre>
 *
 * @author cursor
 * @since 2026-07-23
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class LangfuseTracingService implements McpToolCallObserver, KnowledgeCallObserver {
  private static final Logger logger = LoggerFactory.getLogger(LangfuseTracingService.class);
  private static final int MAX_PAYLOAD_CHARS = 50_000;
  private static final String ROOT_SPAN_NAME = "agent-turn";

  private final LangfuseProperties properties;

  private final ExecutorService executor = Executors.newFixedThreadPool(2, new ThreadFactory() {
    private final AtomicInteger seq = new AtomicInteger();

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "langfuse-trace-" + seq.incrementAndGet());
      t.setDaemon(true);
      return t;
    }
  });

  /**
   * 开启一轮对话的根 Trace / root span。应在对话入口调用，保证后续 LLM/MCP/Knowledge 挂到同一树下。
   *
   * @param preferredTraceId 请求携带的 traceId，可空（空则自动生成）
   * @param turnName         Trace 名称，如 chat / flow
   * @param input            本轮输入摘要，可空
   * @param sessionId        会话 ID，可空
   * @return 实际使用的 traceId
   */
  public String beginAgentTurn(@Nullable String preferredTraceId, String turnName, @Nullable Object input, @Nullable Long sessionId) {
    if (!properties.isConfigured()) {
      if (StringUtils.isNotBlank(preferredTraceId)) {
        LlmTraceUtil.setTraceId(preferredTraceId);
      }
      else {
        LlmTraceUtil.ensureTraceId();
      }
      return LlmTraceUtil.getTraceId();
    }
    if (StringUtils.isNotBlank(preferredTraceId)) {
      LlmTraceUtil.setTraceId(preferredTraceId);
    }
    final String traceId = LlmTraceUtil.ensureTraceId();
    // 若入口重复调用且已有 root，复用
    if (StringUtils.isNotBlank(LlmTraceUtil.getRootObservationId())) {
      return traceId;
    }
    final String rootSpanId = UUID.randomUUID().toString();
    LlmTraceUtil.setRootObservationId(rootSpanId);
    LlmTraceUtil.setCurrentObservationId(null);

    final String name = StringUtils.defaultIfBlank(turnName, ROOT_SPAN_NAME);
    final Instant start = Instant.now();
    final Object truncatedInput = truncate(input);
    final Long userId = SessionUtil.getOptionalUserId();
    Long tenantId = null;
    if (SceneContextUtil.hasContext()) {
      tenantId = SceneContextUtil.getContext().getRequest().getTenantId();
      if (sessionId == null) {
        sessionId = SceneContextUtil.getContext().getRequest().getConversationId();
      }
    }
    final Long finalTenantId = tenantId;
    final Long finalSessionId = sessionId;
    executor.execute(() -> {
      try {
        sendRootStart(traceId, rootSpanId, name, truncatedInput, start, userId, finalTenantId, finalSessionId);
      }
      catch (Exception e) {
        logger.warn("Failed to send Langfuse agent-turn start: {}", e.getMessage());
        if (logger.isDebugEnabled()) {
          logger.debug("Langfuse agent-turn start error detail", e);
        }
      }
    });
    return traceId;
  }

  /**
   * 结束本轮对话根 span（不清理 ThreadLocal，由入口 finally 调用 {@link LlmTraceUtil#clearTraceId()}）。
   */
  public void endAgentTurn(@Nullable Object output) {
    if (!properties.isConfigured()) {
      return;
    }
    final String traceId = LlmTraceUtil.getTraceId();
    final String rootSpanId = LlmTraceUtil.getRootObservationId();
    if (StringUtils.isBlank(traceId) || StringUtils.isBlank(rootSpanId)) {
      return;
    }
    final Instant end = Instant.now();
    final Object truncatedOutput = truncate(output);
    executor.execute(() -> {
      try {
        sendRootEnd(traceId, rootSpanId, truncatedOutput, end);
      }
      catch (Exception e) {
        logger.warn("Failed to send Langfuse agent-turn end: {}", e.getMessage());
        if (logger.isDebugEnabled()) {
          logger.debug("Langfuse agent-turn end error detail", e);
        }
      }
    });
  }

  /**
   * 异步上报一次 chat completion。
   * Generation 挂在 root 下；上报前把 currentObservationId 设为 generationId，便于后续 MCP/Knowledge 挂到该 generation 下。
   */
  public void recordChatCompletion(ModelConfigInfoDTO modelConfig, ChatCompletionRequest request, ChatCompletionResponse response) {
    if (!properties.isConfigured()) {
      return;
    }
    ensureRootIfAbsent("chat-completion", request != null ? request.getMessages() : null,
      request != null ? request.getSessionId() : null);

    final String model = resolveModel(modelConfig, request);
    final String traceId = StringUtils.defaultIfBlank(
      request != null ? request.getTraceId() : null,
      LlmTraceUtil.ensureTraceId());
    final String generationId = UUID.randomUUID().toString();
    // Generation 作为 root 的子节点；工具调用挂到本 generation 下
    final String parentObservationId = LlmTraceUtil.getRootObservationId();
    LlmTraceUtil.setCurrentObservationId(generationId);

    final Instant start = request != null && request.getStartTime() != null
      ? request.getStartTime().toInstant() : Instant.now();
    final Instant end = Instant.now();
    final Object input = truncate(request != null ? request.getMessages() : null);
    final Object output = resolveOutput(response);
    final Usage usage = response != null ? response.getUsage() : null;
    final Long userId = SessionUtil.getOptionalUserId();
    Long tenantId = request != null ? request.getTenantId() : null;
    Long sessionId = request != null ? request.getSessionId() : null;
    final String sourceFrom = request != null ? request.getSourceFrom() : null;
    if (SceneContextUtil.hasContext()) {
      if (tenantId == null) {
        tenantId = SceneContextUtil.getContext().getRequest().getTenantId();
      }
      if (sessionId == null) {
        sessionId = SceneContextUtil.getContext().getRequest().getConversationId();
      }
    }
    final Long finalTenantId = tenantId;
    final Long finalSessionId = sessionId;
    executor.execute(() -> {
      try {
        sendGeneration(traceId, generationId, parentObservationId, model, input, output, usage, start, end,
          userId, finalTenantId, finalSessionId, sourceFrom);
      }
      catch (Exception e) {
        logger.warn("Failed to send Langfuse generation: {}", e.getMessage());
        if (logger.isDebugEnabled()) {
          logger.debug("Langfuse ingest error detail", e);
        }
      }
    });
  }

  @Override
  public void onToolCall(String clientName, CallToolRequest request, @Nullable CallToolResult result,
                         long startEpochMs, long endEpochMs, @Nullable Throwable error) {
    if (!properties.isConfigured() || request == null) {
      return;
    }
    ensureRootIfAbsent("mcp-tool-call", null, null);

    final String mcpClient = clientName;
    final String toolName = StringUtils.defaultIfBlank(request.getName(), "unknown-tool");
    final String traceId = LlmTraceUtil.ensureTraceId();
    final String spanId = UUID.randomUUID().toString();
    final String parentObservationId = LlmTraceUtil.getParentObservationId();
    final Instant start = Instant.ofEpochMilli(startEpochMs);
    final Instant end = Instant.ofEpochMilli(endEpochMs);
    final Object input = truncate(Map.of(
      "tool", toolName,
      "arguments", request.getArguments() != null ? request.getArguments() : Map.of()
    ));
    final Object output = error != null
      ? truncate(Map.of("error", StringUtils.defaultString(error.getMessage())))
      : truncate(result);
    final boolean isError = error != null || (result != null && Boolean.TRUE.equals(result.getIsError()));
    final Long userId = SessionUtil.getOptionalUserId();
    Long tenantId = null;
    Long sessionId = null;
    if (SceneContextUtil.hasContext()) {
      tenantId = SceneContextUtil.getContext().getRequest().getTenantId();
      sessionId = SceneContextUtil.getContext().getRequest().getConversationId();
    }
    final Long finalTenantId = tenantId;
    final Long finalSessionId = sessionId;
    executor.execute(() -> {
      try {
        sendChildSpan(traceId, spanId, parentObservationId, "mcp:" + toolName, input, output, start, end, isError,
          userId, finalTenantId, finalSessionId, Map.of(
            "type", "tool",
            "mcpClient", mcpClient,
            "toolName", toolName
          ));
      }
      catch (Exception e) {
        logger.warn("Failed to send Langfuse MCP span: {}", e.getMessage());
        if (logger.isDebugEnabled()) {
          logger.debug("Langfuse MCP ingest error detail", e);
        }
      }
    });
  }

  @Override
  public void onRecall(String knowledgeType, KnowledgeRecallParamDTO params, @Nullable KnowledgeRecallResponse response,
                       long startEpochMs, long endEpochMs, @Nullable Throwable error) {
    if (!properties.isConfigured() || params == null) {
      return;
    }
    ensureRootIfAbsent("knowledge-recall", params.getQuery(), null);

    final String type = StringUtils.defaultIfBlank(knowledgeType, "unknown");
    final String traceId = LlmTraceUtil.ensureTraceId();
    final String spanId = UUID.randomUUID().toString();
    final String parentObservationId = LlmTraceUtil.getParentObservationId();
    final Instant start = Instant.ofEpochMilli(startEpochMs);
    final Instant end = Instant.ofEpochMilli(endEpochMs);
    Map<String, Object> inputMap = new LinkedHashMap<>();
    inputMap.put("query", params.getQuery());
    inputMap.put("knowledgeId", params.getKnowledgeId());
    inputMap.put("knowledgeIds", params.getKnowledgeIds());
    inputMap.put("maxNum", params.getMaxNum());
    inputMap.put("minScore", params.getMinScore());
    final Object input = truncate(inputMap);
    final Object output = error != null
      ? truncate(Map.of("error", StringUtils.defaultString(error.getMessage())))
      : truncate(summarizeRecall(response));
    final boolean isError = error != null;
    final Long userId = SessionUtil.getOptionalUserId();
    final Long tenantId = params.getTenantId();
    executor.execute(() -> {
      try {
        sendChildSpan(traceId, spanId, parentObservationId, "knowledge-recall", input, output, start, end, isError,
          userId, tenantId, null, Map.of(
            "type", "retriever",
            "knowledgeType", type
          ));
      }
      catch (Exception e) {
        logger.warn("Failed to send Langfuse knowledge recall span: {}", e.getMessage());
        if (logger.isDebugEnabled()) {
          logger.debug("Langfuse knowledge recall ingest error detail", e);
        }
      }
    });
  }

  @Override
  public void onChat(String knowledgeType, KnowledgeChatParamsDTO params, @Nullable KnowledgeChatResponse response,
                     long startEpochMs, long endEpochMs, @Nullable Throwable error) {
    if (!properties.isConfigured() || params == null) {
      return;
    }
    ensureRootIfAbsent("knowledge-chat", params.getQuestion(), null);

    final String type = StringUtils.defaultIfBlank(knowledgeType, "unknown");
    final String traceId = LlmTraceUtil.ensureTraceId();
    final String spanId = UUID.randomUUID().toString();
    final String parentObservationId = LlmTraceUtil.getParentObservationId();
    final Instant start = Instant.ofEpochMilli(startEpochMs);
    final Instant end = Instant.ofEpochMilli(endEpochMs);
    Map<String, Object> inputMap = new LinkedHashMap<>();
    inputMap.put("question", params.getQuestion());
    inputMap.put("knowledgeIds", params.getKnowledgeIds());
    inputMap.put("knowledgeType", type);
    final Object input = truncate(inputMap);
    final Object output = error != null
      ? truncate(Map.of("error", StringUtils.defaultString(error.getMessage())))
      : truncate(summarizeChat(response));
    final boolean isError = error != null;
    final Long userId = SessionUtil.getOptionalUserId();
    final Long tenantId = params.getTenantId();
    executor.execute(() -> {
      try {
        sendChildSpan(traceId, spanId, parentObservationId, "knowledge-chat", input, output, start, end, isError,
          userId, tenantId, null, Map.of(
            "type", "span",
            "knowledgeType", type
          ));
      }
      catch (Exception e) {
        logger.warn("Failed to send Langfuse knowledge chat span: {}", e.getMessage());
        if (logger.isDebugEnabled()) {
          logger.debug("Langfuse knowledge chat ingest error detail", e);
        }
      }
    });
  }

  /**
   * 入口未调用 beginAgentTurn 时的兜底：同步创建 root，保证至少能挂到同一棵树。
   */
  private void ensureRootIfAbsent(String fallbackName, @Nullable Object input, @Nullable Long sessionId) {
    if (StringUtils.isNotBlank(LlmTraceUtil.getRootObservationId())) {
      return;
    }
    beginAgentTurn(LlmTraceUtil.getTraceId(), fallbackName, input, sessionId);
  }

  private void sendRootStart(String traceId, String rootSpanId, String turnName, @Nullable Object input, Instant start,
                             @Nullable Long userId, @Nullable Long tenantId, @Nullable Long sessionId) {
    String now = Instant.now().toString();
    List<Map<String, Object>> batch = new ArrayList<>(2);

    Map<String, Object> traceBody = new LinkedHashMap<>();
    traceBody.put("id", traceId);
    traceBody.put("name", turnName);
    traceBody.put("timestamp", start.toString());
    if (userId != null) {
      traceBody.put("userId", String.valueOf(userId));
    }
    if (sessionId != null) {
      traceBody.put("sessionId", String.valueOf(sessionId));
    }
    Map<String, Object> metadata = new HashMap<>();
    if (tenantId != null) {
      metadata.put("tenantId", tenantId);
    }
    if (!metadata.isEmpty()) {
      traceBody.put("metadata", metadata);
    }
    traceBody.put("tags", List.of("bote", "agent-turn"));
    if (input != null) {
      traceBody.put("input", input);
    }
    batch.add(event("trace-create", now, traceBody));

    Map<String, Object> spanBody = new LinkedHashMap<>();
    spanBody.put("id", rootSpanId);
    spanBody.put("traceId", traceId);
    spanBody.put("name", ROOT_SPAN_NAME);
    spanBody.put("startTime", start.toString());
    if (input != null) {
      spanBody.put("input", input);
    }
    spanBody.put("metadata", Map.of("type", "agent"));
    batch.add(event("span-create", now, spanBody));
    ingest(batch, traceId);
  }

  private void sendRootEnd(String traceId, String rootSpanId, @Nullable Object output, Instant end) {
    String now = Instant.now().toString();
    Map<String, Object> spanBody = new LinkedHashMap<>();
    spanBody.put("id", rootSpanId);
    spanBody.put("traceId", traceId);
    spanBody.put("endTime", end.toString());
    if (output != null) {
      spanBody.put("output", output);
    }
    List<Map<String, Object>> batch = List.of(event("span-update", now, spanBody));

    Map<String, Object> traceBody = new LinkedHashMap<>();
    traceBody.put("id", traceId);
    if (output != null) {
      traceBody.put("output", output);
    }
    // 再发一条 trace-create upsert output（同 id 合并）
    List<Map<String, Object>> fullBatch = new ArrayList<>(2);
    fullBatch.addAll(batch);
    fullBatch.add(event("trace-create", now, traceBody));
    ingest(fullBatch, traceId);
  }

  private void sendGeneration(String traceId, String generationId, @Nullable String parentObservationId, String model,
                              Object input, Object output, @Nullable Usage usage, Instant start, Instant end,
                              @Nullable Long userId, @Nullable Long tenantId, @Nullable Long sessionId,
                              @Nullable String sourceFrom) {
    String now = Instant.now().toString();
    List<Map<String, Object>> batch = new ArrayList<>(2);

    // 轻量 upsert：补充 session/user，不覆盖 turn 名称以外的关键字段由首条 begin 决定
    Map<String, Object> traceBody = new LinkedHashMap<>();
    traceBody.put("id", traceId);
    if (userId != null) {
      traceBody.put("userId", String.valueOf(userId));
    }
    if (sessionId != null) {
      traceBody.put("sessionId", String.valueOf(sessionId));
    }
    Map<String, Object> metadata = new HashMap<>();
    if (tenantId != null) {
      metadata.put("tenantId", tenantId);
    }
    if (StringUtils.isNotBlank(sourceFrom)) {
      metadata.put("sourceFrom", sourceFrom);
    }
    if (!metadata.isEmpty()) {
      traceBody.put("metadata", metadata);
    }
    batch.add(event("trace-create", now, traceBody));

    Map<String, Object> generationBody = new LinkedHashMap<>();
    generationBody.put("id", generationId);
    generationBody.put("traceId", traceId);
    if (StringUtils.isNotBlank(parentObservationId)) {
      generationBody.put("parentObservationId", parentObservationId);
    }
    generationBody.put("name", "chat-completion");
    generationBody.put("startTime", start.toString());
    generationBody.put("endTime", end.toString());
    generationBody.put("model", model);
    generationBody.put("input", input);
    generationBody.put("output", output);
    if (usage != null) {
      Map<String, Object> usageMap = new LinkedHashMap<>();
      usageMap.put("input", usage.getPromptTokens());
      usageMap.put("output", usage.getCompletionTokens());
      usageMap.put("total", usage.getTotalTokens());
      usageMap.put("unit", "TOKENS");
      generationBody.put("usage", usageMap);
    }
    batch.add(event("generation-create", now, generationBody));
    ingest(batch, traceId);
  }

  private void sendChildSpan(String traceId, String spanId, @Nullable String parentObservationId, String spanName,
                             Object input, Object output, Instant start, Instant end, boolean isError,
                             @Nullable Long userId, @Nullable Long tenantId, @Nullable Long sessionId,
                             Map<String, Object> spanMetadata) {
    String now = Instant.now().toString();
    List<Map<String, Object>> batch = new ArrayList<>(2);

    // 仅轻量 upsert session/user，避免用子调用名称覆盖整条 Trace
    Map<String, Object> traceBody = new LinkedHashMap<>();
    traceBody.put("id", traceId);
    if (userId != null) {
      traceBody.put("userId", String.valueOf(userId));
    }
    if (sessionId != null) {
      traceBody.put("sessionId", String.valueOf(sessionId));
    }
    if (tenantId != null) {
      Map<String, Object> metadata = new HashMap<>(spanMetadata);
      metadata.put("tenantId", tenantId);
      traceBody.put("metadata", metadata);
    }
    batch.add(event("trace-create", now, traceBody));

    Map<String, Object> spanBody = new LinkedHashMap<>();
    spanBody.put("id", spanId);
    spanBody.put("traceId", traceId);
    if (StringUtils.isNotBlank(parentObservationId)) {
      spanBody.put("parentObservationId", parentObservationId);
    }
    spanBody.put("name", spanName);
    spanBody.put("startTime", start.toString());
    spanBody.put("endTime", end.toString());
    spanBody.put("input", input);
    spanBody.put("output", output);
    spanBody.put("metadata", spanMetadata);
    if (isError) {
      spanBody.put("level", "ERROR");
      spanBody.put("statusMessage", "call failed");
    }
    batch.add(event("span-create", now, spanBody));
    ingest(batch, traceId);
  }

  private void ingest(List<Map<String, Object>> batch, String traceId) {
    Map<String, Object> payload = Map.of("batch", batch);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth());
    HttpEntity<String> entity = new HttpEntity<>(JsonUtil.toJsonString(payload), headers);
    ResponseEntity<String> resp = HttpUtil.getRestTemplate().exchange(properties.getIngestionUrl(), HttpMethod.POST, entity, String.class);
    if (!resp.getStatusCode().is2xxSuccessful()) {
      logger.warn("Langfuse ingest non-2xx: status={}, body={}", resp.getStatusCode().value(), resp.getBody());
    }
    else if (logger.isDebugEnabled()) {
      logger.debug("Langfuse ingest ok: traceId={}", traceId);
    }
  }

  private static Map<String, Object> event(String type, String timestamp, Map<String, Object> body) {
    Map<String, Object> event = new LinkedHashMap<>();
    event.put("id", UUID.randomUUID().toString());
    event.put("type", type);
    event.put("timestamp", timestamp);
    event.put("body", body);
    return event;
  }

  private String basicAuth() {
    String raw = properties.getPublicKey() + ":" + properties.getSecretKey();
    return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  private static String resolveModel(ModelConfigInfoDTO modelConfig, ChatCompletionRequest request) {
    if (request != null && StringUtils.isNotBlank(request.getModel())) {
      return request.getModel();
    }
    if (modelConfig != null && StringUtils.isNotBlank(modelConfig.getModelCode())) {
      return modelConfig.getModelCode();
    }
    if (modelConfig != null && StringUtils.isNotBlank(modelConfig.getModelName())) {
      return modelConfig.getModelName();
    }
    return "unknown";
  }

  @Nullable
  private static Object summarizeRecall(@Nullable KnowledgeRecallResponse response) {
    if (response == null) {
      return null;
    }
    Map<String, Object> summary = new LinkedHashMap<>();
    summary.put("textCount", response.getText() != null ? response.getText().size() : 0);
    summary.put("imageCount", response.getImage() != null ? response.getImage().size() : 0);
    summary.put("dataCount", response.getData() != null ? response.getData().size() : 0);
    summary.put("result", response);
    return summary;
  }

  @Nullable
  private static Object summarizeChat(@Nullable KnowledgeChatResponse response) {
    if (response == null) {
      return null;
    }
    Map<String, Object> summary = new LinkedHashMap<>();
    summary.put("answer", response.getAnswer());
    summary.put("referenceCount", response.getReferences() != null ? response.getReferences().size() : 0);
    summary.put("chatLogId", response.getChatLogId());
    return summary;
  }

  @Nullable
  private static Object resolveOutput(@Nullable ChatCompletionResponse response) {
    if (response == null) {
      return null;
    }
    try {
      return truncate(response.getMessage());
    }
    catch (Exception ignored) {
      return truncate(response.getMessageContent());
    }
  }

  @Nullable
  private static Object truncate(@Nullable Object value) {
    if (value == null) {
      return null;
    }
    String json = JsonUtil.toJsonString(value);
    if (json != null && json.length() > MAX_PAYLOAD_CHARS) {
      return json.substring(0, MAX_PAYLOAD_CHARS) + "...[truncated]";
    }
    return value;
  }
}
