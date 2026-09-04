package com.iwhalecloud.bote.doc.module.knowledge.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.beyond.BeyondAuthHelper;
import com.iwhalecloud.bote.common.enums.BeyondSseEvent;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.ServletUtil;
import com.iwhalecloud.bote.common.util.SseApiUtil;
import com.iwhalecloud.bote.config.properties.BeyondProperties;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeClient;
import com.iwhalecloud.bote.doc.module.knowledge.helper.KnowledgeChatResponseCollector;
import com.iwhalecloud.bote.dto.beyond.BeyondChatEndEventDTO;
import com.iwhalecloud.bote.dto.beyond.BeyondKnowledgeRetrieveResponse;
import com.iwhalecloud.bote.dto.beyond.BeyondReferenceDocumentDTO;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallTextItem;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ServerSentEvent;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import okhttp3.sse.EventSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 百应知识库客户端
 *
 * @author bianjp
 * @since 2025-07-21
 */
@Component
@ConditionalOnBooleanProperty("beyond.enabled")
@RequiredArgsConstructor
public class BeyondKnowledgeClient implements KnowledgeClient {
  private static final Logger logger = LoggerFactory.getLogger(BeyondKnowledgeClient.class);

  private final BeyondProperties properties;
  private final BeyondAuthHelper beyondAuthHelper;

  @Override
  public int getOrder() {
    return 1000;
  }

  @Override
  public String getKnowledgeType() {
    return "beyond";
  }

  @Override
  public KnowledgeRecallResponse recall(KnowledgeRecallParamDTO params) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("query_type", "embedding");
    data.put("query", params.getQuery());
    data.put("dataset_ids", params.getKnowledgeIds());
    HttpHeaders headers = beyondAuthHelper.buildHttpHeaders();
    BeyondKnowledgeRetrieveResponse retrieveResponse = HttpUtil.post(properties.getKnowledgeRecallApiUrl(), data, ParameterizedTypeReference.forType(BeyondKnowledgeRetrieveResponse.class), headers);
    Assert.notNull(retrieveResponse, "调用百应知识检索接口异常,返回为空");
    if (!"0".equals(retrieveResponse.getResultCode())) {
      throw new BssException("调用百应知识检索接口异常: " + retrieveResponse.getResultMsg());
    }
    return convertToKnowledgeRecallResponse(retrieveResponse);
  }

  /**
   * 转换百应检索响应为知识召回响应
   */
  private KnowledgeRecallResponse convertToKnowledgeRecallResponse(BeyondKnowledgeRetrieveResponse response) {
    KnowledgeRecallResponse result = new KnowledgeRecallResponse();
    if (CollectionUtils.isEmpty(response.getResultObject())) {
      return result;
    }
    List<KnowledgeRecallTextItem> textItems = new ArrayList<>();
    for (BeyondKnowledgeRetrieveResponse.ResultItem resultItem : response.getResultObject()) {
      if (CollectionUtils.isEmpty(resultItem.getText())) {
        continue;
      }
      for (BeyondKnowledgeRetrieveResponse.TextItem textItem : resultItem.getText()) {
        if (textItem.getData() == null) {
          continue;
        }
        KnowledgeRecallTextItem item = new KnowledgeRecallTextItem();
        item.setScore(textItem.getScore());
        item.setContent(textItem.getData().getContent());
        item.setDocId(String.valueOf(textItem.getData().getDocumentId()));
        item.setDocName(textItem.getData().getDocumentName());
        item.setHeading(textItem.getData().getHeadingChain());
        textItems.add(item);
      }
    }
    result.setText(textItems);
    return result;
  }

  @Override
  public KnowledgeChatResponse chat(KnowledgeChatParamsDTO params) {
    // 百应不支持非流式的问答接口，使用流式接口阻塞等待实现
    return chatStreamBlocking(params, null, SseUtil.requestListener);
  }

  @Override
  public EventSource chatStream(KnowledgeChatParamsDTO params, Consumer<SseEvent> partialHandler, Consumer<BssException> completionHandler) {
    return SseApiUtil.wrapBlockingApi(params, partialHandler, completionHandler, this::chatStreamBlocking);
  }

  @Override
  public KnowledgeChatResponse chatStreamBlocking(KnowledgeChatParamsDTO params, @Nullable Consumer<SseEvent> eventHandler, @Nullable Consumer<Object> requestListener) {
    // 获取百应调用博特 SSE 接口时请求头中指定的语言
    String language = null;
    HttpServletRequest request = ServletUtil.getRequest();
    if (request != null) {
      language = request.getHeader("Language");
    }
    List<Long> beyondKnowledgeIds = params.getKnowledgeIds();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("language", StringUtils.defaultIfEmpty(language, "zh-CN"));
    data.put("chatContent", params.getQuestion());
    data.put("datasetIdList", beyondKnowledgeIds);
    Headers headers = beyondAuthHelper.buildOkHttpHeaders();
    KnowledgeChatResponseCollector responseCollector = new KnowledgeChatResponseCollector(eventHandler);
    BeyondKnowledgeChatEventListener eventListener = new BeyondKnowledgeChatEventListener(responseCollector);
    ModelHttpClient.sseBlocking("POST", properties.getKnowledgeChatApiUrl(), headers, data, requestListener, eventListener);
    return responseCollector.getResponse();
  }

  /**
   * 百应知识问答 SSE 事件监听器
   */
  private record BeyondKnowledgeChatEventListener(Consumer<SseEvent> partialHandler) implements Consumer<ServerSentEvent> {

    @Override
    @SuppressWarnings("PMD.GuardLogStatement")
    public void accept(ServerSentEvent event) {
      logger.trace("Received beyond SSE event: id={}, type={}, data={}", event.id(), event.event(), event.data());
      BeyondSseEvent eventType = BeyondSseEvent.of(event.event());
      if (eventType == null) {
        logger.error("Unknown beyond SSE event: id={}, type={}, data={}", event.id(), event.event(), event.data());
        return;
      }

      // 报错
      if (eventType == BeyondSseEvent.ERROR) {
        Map<String, Object> map = JsonUtil.parseJson(event.data(), new TypeReference<>() {
        });
        String error = StringUtils.defaultIfEmpty(MapUtils.getString(map, "message"), "未知错误");
        throw new BssException("调用百应知识库失败: " + error);
      }

      switch (eventType) {
        case REASONING_DELTA:
        case ANSWER_DELTA:
          ChatCompletionResponse response = JsonUtil.parseJsonRequired(event.data(), ChatCompletionResponse.class);
          String content = response.getDeltaContent();
          if (StringUtils.isNotEmpty(content)) {
            SseEvent sseEvent = eventType == BeyondSseEvent.REASONING_DELTA ? SseEvent.ofReasoning(content) : SseEvent.ofText(content);
            partialHandler.accept(sseEvent);
          }
          break;
        case END:
          handleEndEvent(event.data());
          break;
        default:
          // 忽略不需要关心的事件
          break;
      }
    }

    /**
     * 处理结束事件
     */
    private void handleEndEvent(String data) {
      BeyondChatEndEventDTO event = JsonUtil.parseJson(data, BeyondChatEndEventDTO.class);
      if (event == null) {
        return;
      }
      if (CollectionUtils.isNotEmpty(event.getRelatedResources())) {
        List<ReferenceDocumentDTO> references = event.getRelatedResources().stream().map(BeyondReferenceDocumentDTO::convert).collect(Collectors.toList());
        partialHandler.accept(SseEvent.ofReferences(references));
      }
      if (CollectionUtils.isNotEmpty(event.getRelatedQuestions())) {
        partialHandler.accept(SseEvent.ofQuestions(event.getRelatedQuestions()));
      }
    }
  }

}
