package com.iwhalecloud.bote.doc.module.knowledge.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.req.KnowledgeGraphWebsocketCompleteRequest;
import com.iwhalecloud.bote.dto.knowledge.ReferenceChunkDTO;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * knowledgeGraph WebSocket 监听器
 *
 * @author qian.sisheng
 * @since 2026-04-14
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class KnowledgeGraphWebSocketListener extends WebSocketListener {
  private static final Logger logger = LoggerFactory.getLogger(KnowledgeGraphWebSocketListener.class);

  /** knowledgeGraph 知识库名称 */
  private final String database;
  /** 用户问题 */
  private final String question;
  /** 会话ID */
  private final String sessionId;
  /** 提示词 */
  private final String prompt;
  /** 是否返回召回内容 */
  private final Boolean withReferences;
  /** 部分结果处理函数 */
  private final Consumer<SseEvent> partialHandler;
  /** 完成处理函数 */
  private final Consumer<BssException> completionHandler;
  /** 召回引用只发送一次 */
  private final AtomicBoolean sentReferences = new AtomicBoolean(false);
  /** 完成回调只触发一次 */
  private final AtomicBoolean completed = new AtomicBoolean(false);
  /** 延迟发送的引用信息（文本完成后再输出） */
  private List<ReferenceDocumentDTO> pendingReferences;

  @Override
  public void onOpen(WebSocket webSocket, Response response) {
    logger.debug("knowledgeGraph websocket opened: code={}, dataBase={}, question={}, sessionId={}", response.code(), database, question, sessionId);
    KnowledgeGraphWebsocketCompleteRequest request = new KnowledgeGraphWebsocketCompleteRequest();
    request.setDatabase(database);
    request.setQuestion(question);
    request.setSessionId(sessionId);
    request.setInstruct(prompt == null ? "" : prompt);
    webSocket.send(JsonUtil.toJsonString(request));
  }

  @Override
  public void onMessage(WebSocket webSocket, String text) {
    logger.debug("Received knowledgeGraph websocket message: message={}", text);
    JsonNode data;
    try {
      data = JsonUtil.readTree(text);
    }
    catch (Exception e) {
      logger.error("Failed to parse knowledgeGraph websocket message: {}", text, e);
      invokeCompletionHandler(e);
      closeWebsocket(webSocket, "parse error");
      return;
    }
    int code = data.path("code").asInt(Integer.MIN_VALUE);
    if (code == -1) {
      if (logger.isErrorEnabled()) {
        logger.error("knowledgeGraph websocket error: code=-1, message={}", data.path("error_msg").asText("knowledgeGraph 调用失败"));
      }
      invokeCompletionHandler(new BssException(data.path("error_msg").asText("knowledgeGraph 调用失败")));
      closeWebsocket(webSocket, "message error");
      return;
    }
    // code=0: 召回内容（references）
    if (code == 0 && sentReferences.compareAndSet(false, true)) {
      pendingReferences = parseReferences(data.path("data").path("contents"));
      return;
    }
    // code=1: 答案分片
    if (code == 1) {
      String answer = data.path("answer").asText("");
      if (StringUtils.isNotEmpty(answer)) {
        partialHandler.accept(SseEvent.ofText(answer));
      }
    }
  }

  @Override
  @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
  public void onFailure(WebSocket webSocket, Throwable throwable, @Nullable Response response) {
    invokeCompletionHandler(throwable);
  }

  @Override
  public void onClosing(WebSocket webSocket, int code, String reason) {
    completeSuccess();
    webSocket.close(code, reason);
  }

  @Override
  public void onClosed(WebSocket webSocket, int code, String reason) {
    completeSuccess();
  }

  /**
   * 将召回内容转换为 references 事件结构
   */
  private List<ReferenceDocumentDTO> parseReferences(JsonNode contents) {
    if (!contents.isArray() || contents.isEmpty()) {
      return null;
    }
    Map<String, ReferenceDocumentDTO> documentMap = new LinkedHashMap<>();
    for (JsonNode item : contents) {
      String documentName = item.path("name").asText("");
      ReferenceDocumentDTO document = documentMap.computeIfAbsent(documentName, key -> {
        ReferenceDocumentDTO referenceDocument = new ReferenceDocumentDTO();
        referenceDocument.setId(UUID.randomUUID().toString());
        referenceDocument.setDownloadEnabled(false);
        referenceDocument.setOpenEnabled(false);
        referenceDocument.setType(KnowledgeConsts.REFERENCE_DOC);
        referenceDocument.setName(key);
        referenceDocument.setChunks(new ArrayList<>());
        return referenceDocument;
      });
      ReferenceChunkDTO chunk = new ReferenceChunkDTO();
      chunk.setChunkId(UUID.randomUUID().toString());
      chunk.setName(item.path("text").asText(""));
      if (!item.path("score").isMissingNode() && !item.path("score").isNull()) {
        chunk.setScore(item.path("score").asText());
      }
      document.getChunks().add(chunk);
    }
    return new ArrayList<>(documentMap.values());
  }

  /**
   * 关闭连接
   */
  private void closeWebsocket(WebSocket webSocket, String reason) {
    try {
      webSocket.close(1000, reason);
    }
    catch (Exception e) {
      logger.warn("Failed to close knowledgeGraph websocket");
    }
  }

  /**
   * 调用结束回调（只触发一次）
   */
  private void invokeCompletionHandler(@Nullable Throwable throwable) {
    if (!completed.compareAndSet(false, true)) {
      return;
    }
    if (throwable == null) {
      completionHandler.accept(null);
    }
    else if (throwable instanceof BssException) {
      completionHandler.accept((BssException) throwable);
    }
    else {
      completionHandler.accept(new BssException("knowledgeGraph WS 调用失败", throwable));
    }
  }

  /**
   * 正常完成时：文本结束后输出 references，再触发完成回调
   */
  private void completeSuccess() {
    if (CollectionUtils.isNotEmpty(pendingReferences) && withReferences) {
      partialHandler.accept(SseEvent.ofReferences(pendingReferences));
    }
    invokeCompletionHandler(null);
  }
}
