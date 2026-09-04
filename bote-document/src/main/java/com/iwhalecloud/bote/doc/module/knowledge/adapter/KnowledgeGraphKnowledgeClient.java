package com.iwhalecloud.bote.doc.module.knowledge.adapter;

import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.config.properties.KnowledgeGraphProperties;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeClient;
import com.iwhalecloud.bote.doc.module.knowledge.listener.KnowledgeGraphWebSocketListener;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.req.KnowledgeGraphRetrieveRequest;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp.KnowledgeGraphRetrieveContentItemResponse;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp.KnowledgeGraphRetrieveResponse;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgeGraphChatSessionService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.KnowledgeGraphClientHelper;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallTextItem;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.sse.EventSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

/**
 * knowledgeGraph 知识库客户端
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty("knowledge.knowledgeGraph.enabled")
public class KnowledgeGraphKnowledgeClient implements KnowledgeClient {
  private final KnowledgeGraphClientHelper helper;
  private final IKnowledgeGraphChatSessionService sessionService;
  private final KnowledgeGraphProperties properties;

  @Override
  public int getOrder() {
    return 5000;
  }

  @Override
  public String getKnowledgeType() {
    return KnowledgeConsts.KNOWLEDGE_TYPE_KNOWLEDGE_GRAPH;
  }

  /**
   * 检索接口适配：POST /api/retrieve
   */
  @Override
  public KnowledgeRecallResponse recall(KnowledgeRecallParamDTO params) {
    Long tenantId = params.getTenantId();
    KnowledgeGraphRetrieveRequest request = new KnowledgeGraphRetrieveRequest();
    request.setDatabase(params.getKnowledgeGraphName());
    request.setQuery(params.getQuery());
    request.setTopK(params.getMaxNum());
    request.setScoreThreshold(params.getMinScore() == null ? null : params.getMinScore().doubleValue());
    KnowledgeGraphRetrieveResponse payload = helper.retrieve(tenantId, request);
    List<KnowledgeRecallTextItem> items = new ArrayList<>();
    if (payload != null && payload.getContents() != null) {
      for (KnowledgeGraphRetrieveContentItemResponse content : payload.getContents()) {
        KnowledgeRecallTextItem item = new KnowledgeRecallTextItem();
        item.setChunkId(content.getChunkId());
        item.setDocName(content.getName());
        item.setContent(content.getText());
        if (content.getScore() != null) {
          item.setScore(content.getScore());
        }
        items.add(item);
      }
    }
    KnowledgeRecallResponse recallResponse = new KnowledgeRecallResponse();
    recallResponse.setText(items);
    return recallResponse;
  }

  @Override
  public KnowledgeChatResponse chat(KnowledgeChatParamsDTO params) {
    return chatStreamBlocking(params, null, SseUtil.requestListener);
  }

  /**
   * 问答流式适配：WS /ws/complete，并桥接为 SseEvent
   */
  @Override
  public EventSource chatStream(KnowledgeChatParamsDTO params, Consumer<SseEvent> partialHandler, Consumer<BssException> completionHandler) {
    Long tenantId = params.getTenantId();
    Long userId = resolveUserId();
    String database = resolveDatabaseForChat(params);
    String sessionId = getOrCreateSessionId(params, tenantId, userId, database);
    String wsUrl = toWebsocketUrl(properties.getKnowledgeChatApiUrl());
    KnowledgeGraphWebSocketListener listener = new KnowledgeGraphWebSocketListener(database, params.getQuestion(), sessionId,
      params.getPromptTemplate(), params.isWithReferences(), partialHandler, completionHandler);
    Headers headers = new Headers.Builder().add("Cookie", helper.getCookieInfo(tenantId)).build();
    Request request = new Request.Builder().url(wsUrl).headers(headers).build();
    WebSocket webSocket = ModelHttpClient.getClient().newWebSocket(request, listener);
    return new EventSource() {
      @Override
      public void cancel() {
        webSocket.cancel();
      }

      @Override
      @NotNull
      public Request request() {
        return request;
      }
    };
  }

  private Long resolveUserId() {
    return SessionUtil.getOptionalUserId(-1L);
  }

  /**
   * 获取或创建第三方 session_id
   */
  private String getOrCreateSessionId(KnowledgeChatParamsDTO params, Long tenantId, Long userId, String database) {
    String boteSessionId = params.getConversationId();
    if (StringUtils.isEmpty(boteSessionId)) {
      return createSession(tenantId, params.getQuestion());
    }
    return sessionService.getOrCreateSessionId(tenantId, userId, boteSessionId, database,
      () -> createSession(tenantId, params.getQuestion()));
  }

  /**
   * 解析问答使用的知识库名
   */
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  private String resolveDatabaseForChat(KnowledgeChatParamsDTO params) {
    if (CollectionUtils.isNotEmpty(params.getExtKnowledgeIds())) {
      return params.getExtKnowledgeIds().getFirst();
    }
    throw new BssException("knowledgeGraph 问答缺少知识库");
  }

  /**
   * 创建第三方会话
   */
  private String createSession(Long tenantId, String question) {
    return helper.createSession(tenantId, question);
  }

  /**
   * http 地址转 ws/wss 地址
   */
  private static String toWebsocketUrl(String httpUrl) {
    if (httpUrl.startsWith("https://")) {
      return "wss://" + httpUrl.substring("https://".length());
    }
    if (httpUrl.startsWith("http://")) {
      return "ws://" + httpUrl.substring("http://".length());
    }
    return "ws://" + httpUrl;
  }

}
