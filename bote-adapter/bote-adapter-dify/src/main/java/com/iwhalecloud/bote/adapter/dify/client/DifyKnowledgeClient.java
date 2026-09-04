package com.iwhalecloud.bote.adapter.dify.client;

import com.iwhalecloud.bote.adapter.dify.dto.ChatFlowBlockingResponse;
import com.iwhalecloud.bote.adapter.dify.dto.KnowledgeSearchResponse;
import com.iwhalecloud.bote.adapter.dify.util.ReasoningContentUtil;
import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.util.SseApiUtil;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeClient;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallTextItem;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import okhttp3.sse.EventSource;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

/**
 * dify 知识库客户端
 *
 * @author qian.sisheng
 * @since 2025-10-13
 */
@Component
@ConditionalOnBooleanProperty("knowledge.dify.enabled")
@RequiredArgsConstructor
public class DifyKnowledgeClient implements KnowledgeClient {

  @Override
  public String getKnowledgeType() {
    return "dify";
  }

  @Override
  public KnowledgeRecallResponse recall(KnowledgeRecallParamDTO params) {
    KnowledgeSearchResponse knowledgeSearchResponse = DifyApiHelper.knowledgeSearch(params);
    KnowledgeRecallResponse recallResponse = new KnowledgeRecallResponse();
    if (knowledgeSearchResponse == null) {
      return recallResponse;
    }
    // 转换结果
    recallResponse.setText(CollectionUtils.emptyIfNull(knowledgeSearchResponse.getRecords()).stream().map(record -> {
      KnowledgeRecallTextItem item = new KnowledgeRecallTextItem();
      item.setContent(record.getSegment().getContent());
      item.setScore(record.getScore());
      if (record.getSegment() != null && record.getSegment().getDocument() != null) {
        item.setDocId(String.valueOf(record.getSegment().getDocument().getId()));
        item.setDocName(record.getSegment().getDocument().getName());
      }
      return item;
    }).toList());
    return recallResponse;
  }

  @Override
  public KnowledgeChatResponse chat(KnowledgeChatParamsDTO params) {
    ChatFlowBlockingResponse chatFlowBlockingResponse = DifyApiHelper.knowledgeChat(params);
    KnowledgeChatResponse response = new KnowledgeChatResponse();
    if (chatFlowBlockingResponse != null) {
      String content = chatFlowBlockingResponse.getAnswer();
      // 如果不显示思考内容，去除思考内容
      if (!ThinkingStrategy.shouldShowReasoning(params.getThinkingStrategy()) && ReasoningContentUtil.hasReasoningContent(content)) {
        response.setAnswer(ReasoningContentUtil.removeReasoningContent(content));
      } else {
        response.setAnswer(content);
      }
    }
    return response;
  }

  @Override
  public EventSource chatStream(KnowledgeChatParamsDTO params, Consumer<SseEvent> partialHandler, Consumer<BssException> completionHandler) {
    return SseApiUtil.wrapBlockingApi(params, partialHandler, completionHandler, this::chatStreamBlocking);
  }

  @Override
  public KnowledgeChatResponse chatStreamBlocking(KnowledgeChatParamsDTO params, @Nullable Consumer<SseEvent> eventHandler, @Nullable Consumer<Object> requestListener) {
    return DifyApiHelper.knowledgeChatStream(params, eventHandler, requestListener);
  }

  @Override
  public int getOrder() {
    return 4000;
  }
}
