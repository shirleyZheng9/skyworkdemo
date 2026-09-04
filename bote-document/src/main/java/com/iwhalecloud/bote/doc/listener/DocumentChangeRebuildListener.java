package com.iwhalecloud.bote.doc.listener;

import com.iwhalecloud.bote.doc.consts.DocumentActionTypeEnum;
import com.iwhalecloud.bote.doc.listener.event.DocChangeEventMessage;
import com.iwhalecloud.bote.doc.module.document.enums.RecordTypeEnum;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentManageService;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorEventListener;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 监听文档更新时间，重建知识库文档，需处理event的异常
 *
 * @author Aiqing
 * @since 2025/9/30
 */
@Component
@RequiredArgsConstructor
public class DocumentChangeRebuildListener implements DisruptorEventListener<DocChangeEventMessage> {
  private static final Logger logger = LoggerFactory.getLogger(DocumentChangeRebuildListener.class);

  private final IDocumentManageService documentManageService;

  @Override
  public void onEvent(DocChangeEventMessage event) {
    try {
      DocumentActionTypeEnum changeType = event.getChangeType();
      if ((!Objects.equals(changeType, DocumentActionTypeEnum.EDIT)
        && !Objects.equals(changeType, DocumentActionTypeEnum.DOCUMENT_RENAMED) && !Objects.equals(changeType, DocumentActionTypeEnum.RE_UPLOAD))
        || RecordTypeEnum.CORRECTION.getCode().equals(event.getMode())) {
        return;
      }
      // 处理文档重建逻辑
      String documentId = event.getDocumentId();
      logger.trace("在线文档更新， 触发文档重建, documentId:{}", documentId);
      documentManageService.rebuildBtDocumentByDcDocDocId(documentId, event.getTenantId(), event.getUpdatorId(), event.getOnLineEditing());
    }
    catch (Exception e) {
      logger.error("DocumentChangeRebuildListener rebuildBtDocumentByDcDocDocId error", e);
    }
  }
}
