package com.iwhalecloud.bote.doc.module.knowledge.adapter;

import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.SseApiUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.listener.event.DocKnowledgeQaEventMessage;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeClient;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.AbstractKnowledgeRecallItem;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallImageItem;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallTextItem;
import com.iwhalecloud.bote.dto.knowledge.SearchKnowledgeResponse;
import com.iwhalecloud.bote.dto.knowledge.SearchKnowledgeResponse.KnowledgeScoreItem;
import com.iwhalecloud.bote.dto.knowledge.SimpleDocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.SimpleKnowledgeDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorUtil;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import okhttp3.sse.EventSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * DocChain 知识库客户端
 *
 * @author bianjp
 * @since 2025-05-13
 */
@Component
@ConditionalOnBooleanProperty(name = "knowledge.docChain.enabled", matchIfMissing = true)
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class DocChainKnowledgeClient implements KnowledgeClient {
  private static final Logger logger = LoggerFactory.getLogger(DocChainKnowledgeClient.class);

  private final DocChainAdapter docChainAdapter;

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public String getKnowledgeType() {
    return KnowledgeConsts.KNOWLEDGE_TYPE_DOC_CHAIN;
  }

  @Override
  public KnowledgeRecallResponse recall(KnowledgeRecallParamDTO params) {
    boolean isChatExcel = IterableUtils.matchesAny(params.getKnowledgeList(), p -> !Boolean.TRUE.equals(p.getCommonTopic()));
    Date start = new Date();
    Long tenantId = params.getKnowledgeList().getFirst().getTenantId();
    if (tenantId == null) {
      tenantId = params.getTenantId();
    }

    SearchKnowledgeResponse response = docChainAdapter.recall(tenantId, params.getTopicIds(), params.getDocIds(), params.getQuery(),
      params.getMaxNum(), params.getMinScore(), isChatExcel);

    KnowledgeRecallResponse finalResponse = new KnowledgeRecallResponse();
    if (CollectionUtils.isNotEmpty(response.getText())) {
      finalResponse.setText(response.getText().stream()
        // 忽略非法数据
        .filter(scoreItem -> scoreItem.getData() != null && scoreItem.getData().getDocId() != null)
        .map(scoreItem -> convertKnowledgeRecallItem(scoreItem, new KnowledgeRecallTextItem(scoreItem), params.getKnowledgeList()))
        .collect(Collectors.toList()));
    }
    if (CollectionUtils.isNotEmpty(response.getImage())) {
      finalResponse.setImage(response.getImage().stream()
        // 忽略非法数据
        .filter(scoreItem -> scoreItem.getData() != null && scoreItem.getData().getDocId() != null)
        .map(scoreItem -> convertKnowledgeRecallItem(scoreItem, new KnowledgeRecallImageItem(scoreItem), params.getKnowledgeList()))
        .collect(Collectors.toList()));
    }
    finalResponse.setData(response.getData());
    saveRecallLog(params, start, finalResponse);
    return finalResponse;
  }

  @Override
  public KnowledgeChatResponse chat(KnowledgeChatParamsDTO params) {
    if (params.getUserId() == null) {
      params.setUserId(SessionUtil.getOptionalUserId(-1L));
    }
    Date startTime = new Date();
    KnowledgeChatResponse response = docChainAdapter.chat(params, params.getTopicIds(), params.getDocIds());
    // 保存对话日志
    saveChatLog(startTime, params, response);
    return response;
  }

  @Override
  public EventSource chatStream(KnowledgeChatParamsDTO params, Consumer<SseEvent> partialHandler, Consumer<BssException> completionHandler) {
    return SseApiUtil.wrapBlockingApi(params, partialHandler, completionHandler, this::chatStreamBlocking);
  }

  @Override
  public KnowledgeChatResponse chatStreamBlocking(KnowledgeChatParamsDTO params,
                                                  @Nullable Consumer<SseEvent> eventHandler,
                                                  @Nullable Consumer<Object> requestListener) {
    if (params.getUserId() == null) {
      params.setUserId(SessionUtil.getOptionalUserId(-1L));
    }
    Date startTime = new Date();
    KnowledgeChatResponse response;
    try {
      response = docChainAdapter.chatStreamBlocking(params, params.getTopicIds(), params.getDocIds(), eventHandler, requestListener);
    }
    catch (BssException e) {
      logger.error("Knowledge chat stream blocking failed: params={}, error={}", params, e.getMessage());
      throw e;
    }
    catch (Exception e) {
      logger.error("Knowledge chat stream blocking failed: params={}", params, e);
      throw new BssException("知识问答失败: " + ExpUtil.getMsg(e), e);
    }

    // 保存对话日志
    saveChatLog(startTime, params, response);
    return response;
  }

  /**
   * 保存对话日志
   */
  private void saveRecallLog(KnowledgeRecallParamDTO params, Date startTime, KnowledgeRecallResponse finalResponse) {
    try {
      long duration = System.currentTimeMillis() - startTime.getTime();

      DocKnowledgeQaEventMessage eventMessage = new DocKnowledgeQaEventMessage();
      eventMessage.setActionType(DocBaseConsts.KNOWLEDGE_QA_TYPE_RECALL);
      eventMessage.setKnowledgeIds(params.getKnowledgeList().stream().map(SimpleKnowledgeDTO::getKnowledgeId).collect(Collectors.toList()));
      eventMessage.setTenantId(params.getTenantId());
      eventMessage.setQuestion(params.getQuery());
      try {
        eventMessage.setUserId(SessionUtil.getLoginInfo().getUserId());
      }
      catch (Exception e) {
        eventMessage.setUserId(-1L);
      }
      eventMessage.setTimeSpent(duration);
      eventMessage.setFinalResponse(finalResponse);

      DisruptorUtil.getInstance().produce(eventMessage);  // ← 异步发送
    }
    catch (Exception e) {
      logger.error("保存召回日志失败: tenantId={}, question={}", params.getTenantId(), params.getQuery(), e);
    }
  }

  /**
   * 保存对话日志
   */
  private void saveChatLog(Date startTime, KnowledgeChatParamsDTO params, KnowledgeChatResponse response) {
    try {
      if (params.isAdvanceRecord()) { //提前记录
        return;
      }
      long duration = System.currentTimeMillis() - startTime.getTime();
      DocKnowledgeQaEventMessage eventMessage = new DocKnowledgeQaEventMessage();
      eventMessage.setChatLogId(response.getChatLogId());
      eventMessage.setActionType(DocBaseConsts.KNOWLEDGE_QA_TYPE_QA);
      eventMessage.setText(response.getAnswer());
      eventMessage.setClientId(params.getClientId());
      eventMessage.setKnowledgeIds(params.getKnowledgeIds());
      eventMessage.setTenantId(params.getTenantId());
      eventMessage.setQuestion(params.getQuestion());
      eventMessage.setBotId(params.getBotId());
      eventMessage.setUserId(params.getUserId());
      eventMessage.setReferences(response.getReferences());
      eventMessage.setTimeSpent(duration);

      DisruptorUtil.getInstance().produce(eventMessage);  // ← 异步发送
    }
    catch (Exception e) {
      logger.error("保存对话日志失败: tenantId={}, question={}", params.getTenantId(), params.getQuestion(), e);
    }
  }

  /**
   * 转换召回项
   */
  private <T extends AbstractKnowledgeRecallItem> T convertKnowledgeRecallItem(KnowledgeScoreItem scoreItem, T item,
    List<SimpleKnowledgeDTO> knowledgeList) {
    Long docId = scoreItem.getData().getDocId();
    // 部分项目，通过博特使用知识召回，需要文件相关参数
    for (SimpleKnowledgeDTO knowledge : knowledgeList) {
      SimpleDocumentDTO document = IterableUtils.find(knowledge.getDocuments(), p -> Objects.equals(docId, p.getExtSystemId()));
      if (document != null) {
        item.setFileInfoId(document.getFileInfoId());
      }
    }
    return item;
  }

}
