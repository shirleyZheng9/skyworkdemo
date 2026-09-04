package com.iwhalecloud.bote.doc.listener;

import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.listener.event.DocKnowledgeQaEventMessage;
import com.iwhalecloud.bote.doc.module.knowledge.service.IBtDcQaRecordManageService;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorEventListener;

import lombok.RequiredArgsConstructor;

/**
 * 监听文档更新时间，重建知识库文档，需处理event的异常
 *
 * @author Aiqing
 * @since 2025/9/30
 */
@Component
@RequiredArgsConstructor
public class DocKnowledgeQaRecordListener implements DisruptorEventListener<DocKnowledgeQaEventMessage> {
  private static final Logger logger = LoggerFactory.getLogger(DocKnowledgeQaRecordListener.class);

  private final IBtDcQaRecordManageService iBtDcQaRecordManageService;

  @Override
  public void onEvent(DocKnowledgeQaEventMessage event) {
    try {
      if (DocBaseConsts.KNOWLEDGE_QA_TYPE_QA.equals(event.getActionType())) {
        KnowledgeChatParamsDTO params = KnowledgeChatParamsDTO.builder()
          .knowledgeIds(event.getKnowledgeIds())
          .botId(event.getBotId())
          .clientId(event.getClientId())
          .tenantId(event.getTenantId())
          .userId(event.getUserId())
          .question(event.getQuestion()).build();
        iBtDcQaRecordManageService.saveAuestionsAndAnswerRecord(params, event.getReferences(), event.getText(), event.getChatLogId(), event.getTimeSpent());
      }
      else if (DocBaseConsts.KNOWLEDGE_QA_TYPE_RECALL.equals(event.getActionType())) {
        iBtDcQaRecordManageService.saveAuestionsAndReCallRecord(event.getFinalResponse(), event.getTenantId(), event.getTimeSpent(), event.getKnowledgeIds(), event.getQuestion(), event.getUserId());
      }

    }
    catch (Exception e) {
      logger.error("DocKnowledgeQaRecordListener 记录知识飞轮 error", e);
    }
  }
}
