package com.iwhalecloud.bote.doc.module.knowledge.adapter;

import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeClient;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.WeKnoraKnowledgeClientHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.WeKnoraLoginHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.util.SseApiUtil;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.WeKnoraChatSessionService;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req.WeKnoraSearchRequest;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraChatChunk;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraSearchResultDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

import lombok.RequiredArgsConstructor;
import okhttp3.sse.EventSource;

/**
 * WeKnora 业务辅助类，使用 Bearer Token 鉴权
 *
 * <p>基于 {@link WeKnoraLoginHelper#buildOkHttpHeaders(Long)} 获取
 * {@code Authorization: Bearer {token}} 鉴权 Header，封装模型列表查询、
 * 知识库列表查询、知识问答（SSE）和知识检索等业务操作。</p>
 *
 * @author huangyunming
 * @since 2026-04-02
 */
@Component
@ConditionalOnBooleanProperty("knowledge.weknora.enabled")
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class WeKnoraKnowledgeClient implements KnowledgeClient {

  private static final Logger logger = LoggerFactory.getLogger(WeKnoraKnowledgeClient.class);

  private final WeKnoraKnowledgeClientHelper helper;
  private final WeKnoraLoginHelper loginHelper;
  private final WeKnoraChatSessionService chatSessionService;

  @Override
  public String getKnowledgeType() {
    return "weKnora";
  }

  /**
   * 编排/通用召回：优先 {@link KnowledgeRecallParamDTO#getWeKnoraKnowledgeBaseIds()}
   */
  @Override
  public KnowledgeRecallResponse recall(KnowledgeRecallParamDTO params) {
    Long tenantId = params.getTenantId();
    List<String> weKnoraKbIds = WeKnoraKnowledgeClientHelper.resolveWeKnoraKbIdsForRecall(params);
    if (CollectionUtils.isEmpty(weKnoraKbIds)) {
      if (logger.isWarnEnabled()) {
        logger.warn("WeKnora recall: no valid WeKnora knowledge base id");
      }
      return new KnowledgeRecallResponse();
    }
    WeKnoraSearchRequest.WeKnoraSearchRequestBuilder builder = WeKnoraSearchRequest.builder()
      .query(params.getQuery());
    if (weKnoraKbIds.size() == 1) {
      builder.knowledgeBaseId(weKnoraKbIds.get(0));
    }
    else {
      builder.knowledgeBaseIds(weKnoraKbIds);
    }
    List<WeKnoraSearchResultDTO> results = helper.knowledgeSearch(tenantId, builder.build());
    KnowledgeRecallResponse response = new KnowledgeRecallResponse();
    if (CollectionUtils.isNotEmpty(results)) {
      response.setText(results.stream()
        .map(WeKnoraKnowledgeClientHelper::convertToRecallTextItem)
        .collect(Collectors.toList()));
    }
    return response;
  }

  /**
   * 知识问答（阻塞，收集完整回答）
   */
  @Override
  public KnowledgeChatResponse chat(KnowledgeChatParamsDTO params) {
    return chatStreamBlocking(params, null, SseUtil.requestListener);
  }

  /**
   * 知识问答流式（异步 EventSource）
   */
  @Override
  public EventSource chatStream(KnowledgeChatParamsDTO params, Consumer<SseEvent> partialHandler,
                                Consumer<BssException> completionHandler) {
    return SseApiUtil.wrapBlockingApi(params, partialHandler, completionHandler, this::chatStreamBlocking);
  }

  /**
   * 知识问答流式（同步阻塞，直到 SSE 结束）
   */
  @Override
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public KnowledgeChatResponse chatStreamBlocking(KnowledgeChatParamsDTO params,
    @Nullable Consumer<SseEvent> eventHandler,
    @Nullable Consumer<Object> requestListener) {
    Long tenantId = params.getTenantId();
    Long creatorId = WeKnoraKnowledgeClientHelper.resolveCreatorId(params);
    List<String> weKnoraKbIds = WeKnoraKnowledgeClientHelper.resolveWeKnoraKbIds(params);
    if (CollectionUtils.isEmpty(weKnoraKbIds)) {
      throw new BssException("WeKnora 知识问答：未配置 WeKnora 知识库 ID");
    }
    String weKnoraSessionId = resolveWeKnoraSessionIdForChat(params, tenantId, creatorId);
    StringBuilder answerBuilder = new StringBuilder();
    List<ReferenceDocumentDTO> references = new ArrayList<>();
    Consumer<WeKnoraChatChunk> chunkHandler = chunk -> processChatChunk(chunk, answerBuilder, references, eventHandler);
    helper.knowledgeChatStream(tenantId, weKnoraSessionId, params.getQuestion(), weKnoraKbIds, requestListener, chunkHandler, params.getModelId());
    if (eventHandler != null && CollectionUtils.isNotEmpty(references)) {
      eventHandler.accept(SseEvent.ofReferences(references));
    }
    KnowledgeChatResponse response = new KnowledgeChatResponse();
    response.setAnswer(answerBuilder.toString());
    response.setReferences(references.isEmpty() || !params.isWithReferences() ? null : references);
    return response;
  }

  /**
   * 处理 WeKnora 聊天片段
   */
  private void processChatChunk(WeKnoraChatChunk chunk, StringBuilder answerBuilder,
    List<ReferenceDocumentDTO> references,
    @Nullable Consumer<SseEvent> eventHandler) {
    if (Boolean.TRUE.equals(chunk.getDone()) && !"answer".equals(chunk.getResponseType())) {
      return;
    }

    String responseType = chunk.getResponseType();
    if ("references".equals(responseType)) {
      processReferencesChunk(chunk, references);
    }
    else if ("answer".equals(responseType)) {
      processAnswerChunk(chunk, answerBuilder, eventHandler);
    }
    else if (isCompatibleContentChunk(chunk)) {
      processCompatibleChunk(chunk, answerBuilder, eventHandler);
    }
  }

  /**
   * 处理 WeKnora 聊天片段中的参考文献
   */
  private void processReferencesChunk(WeKnoraChatChunk chunk, List<ReferenceDocumentDTO> references) {
    if (CollectionUtils.isNotEmpty(chunk.getKnowledgeReferences())) {
      references.addAll(WeKnoraKnowledgeClientHelper.convertReferences(chunk.getKnowledgeReferences()));
    }
  }

  /**
   * 处理 WeKnora 聊天片段中的答案
   */
  private void processAnswerChunk(WeKnoraChatChunk chunk, StringBuilder answerBuilder, @Nullable Consumer<SseEvent> eventHandler) {
    String content = chunk.getContent();
    if (StringUtils.isNotEmpty(content)) {
      answerBuilder.append(content);
      if (eventHandler != null) {
        eventHandler.accept(SseEvent.ofText(content));
      }
    }
  }

  /**
   * 判断 WeKnora 聊天片段是否为兼容内容片段
   */
  private boolean isCompatibleContentChunk(WeKnoraChatChunk chunk) {
    return (chunk.getResponseType() == null || chunk.getResponseType().isBlank()) && StringUtils.isNotEmpty(chunk.getContent())
      && !Boolean.TRUE.equals(chunk.getDone());
  }

  /**
   * 处理 WeKnora 聊天片段中的兼容内容
   */
  private void processCompatibleChunk(WeKnoraChatChunk chunk, StringBuilder answerBuilder, @Nullable Consumer<SseEvent> eventHandler) {
    String content = chunk.getContent();
    answerBuilder.append(content);
    if (eventHandler != null) {
      eventHandler.accept(SseEvent.ofText(content));
    }
  }

  /**
   * 解析 WeKnora 会话 ID：{@code conversationId} 有效时作为博特会话标识落库；否则使用 {@code clientId}；
   * 均为空或 {@code conversationId == -1} 且未传 {@code clientId} 时不落库，每次在 WeKnora 新建会话。
   */
  private String resolveWeKnoraSessionIdForChat(KnowledgeChatParamsDTO params, Long tenantId, Long creatorId) {
    String convId = params.getConversationId();
    if (convId != null && !"-1".equals(convId)) {
      return chatSessionService.getOrCreateSessionId(tenantId, creatorId, convId);
    }
    return loginHelper.createSession(tenantId, new HashMap<>()).getId();
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
