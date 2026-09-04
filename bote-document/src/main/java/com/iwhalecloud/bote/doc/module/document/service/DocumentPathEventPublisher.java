package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.listener.event.DocumentPathChangedEvent;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorUtil;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 文档路径变更事件发布器
 *
 * <p>提供便捷的方法来发布文档路径相关的变更事件，确保缓存能够及时更新。</p>
 *
 * @author Aiqing
 * @since 2025/09/10
 */
@Service
@RequiredArgsConstructor
public class DocumentPathEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(DocumentPathEventPublisher.class);

  private final DocumentChangEventPublisher documentChangEventPublisher;

  /**
   * 发布文档库名称变更事件
   *
   * @param libraryId 文档库ID
   * @param libraryName 新的文档库名称
   * @param operatorId 操作人ID
   * @param spaceId 空间ID
   */
  public void publishLibraryNameChangedEvent(String libraryId, String libraryName, Long operatorId, Long spaceId) {
    try {
      DocumentPathChangedEvent event = new DocumentPathChangedEvent();
      event.setChangeType(DocumentPathChangedEvent.PathChangeType.LIBRARY_NAME_CHANGED);
      event.setLibraryId(libraryId);
      event.setReason("文档库名称修改: " + libraryName);
      event.setOperatorId(operatorId);
      event.setTenantId(TenantContextHolder.getRequiredTenantId());
      event.setSpaceId(spaceId);

      DisruptorUtil.getInstance().produce(event);
      logger.debug("发布文档库名称变更事件: libraryId={}", libraryId);
    }
    catch (Exception e) {
      logger.error("发布文档库名称变更事件失败: libraryId={}", libraryId, e);
    }
  }

  /**
   * 发布文档移动事件
   *
   * @param documentId 移动的文档ID
   * @param libraryId 文档库ID
   * @param operatorId 操作人ID
   * @param spaceId 空间ID
   */
  public void publishDocumentMovedEvent(String documentId, String libraryId, Long operatorId, Long spaceId) {
    try {
      DocumentPathChangedEvent event = new DocumentPathChangedEvent();
      event.setChangeType(DocumentPathChangedEvent.PathChangeType.DOCUMENT_MOVED);
      event.setLibraryId(libraryId);
      event.setAffectedDocumentIds(Collections.singletonList(documentId));
      event.setReason("文档移动: " + documentId);
      event.setOperatorId(operatorId);
      event.setTenantId(TenantContextHolder.getRequiredTenantId());
      event.setSpaceId(spaceId);

      DisruptorUtil.getInstance().produce(event);
      logger.debug("发布文档移动事件: documentId={}", documentId);
    }
    catch (Exception e) {
      logger.error("发布文档移动事件失败: documentId={}", documentId, e);
    }
  }

  /**
   * 发布文档重命名事件
   *
   * @param documentId 重命名的文档ID
   * @param libraryId 文档库ID
   * @param operatorId 操作人ID
   * @param spaceId 空间ID
   */
  public void publishDocumentRenamedEvent(String documentId, String libraryId, Long operatorId, String documentName, Long spaceId) {
    try {
      DocumentPathChangedEvent event = new DocumentPathChangedEvent();
      event.setChangeType(DocumentPathChangedEvent.PathChangeType.DOCUMENT_RENAMED);
      event.setLibraryId(libraryId);
      event.setAffectedDocumentIds(Collections.singletonList(documentId));
      event.setReason("文档重命名: " + documentId);
      event.setOperatorId(operatorId);
      event.setTenantId(TenantContextHolder.getRequiredTenantId());
      event.setSpaceId(spaceId);

      DisruptorUtil.getInstance().produce(event);
      logger.debug("发布文档重命名事件: documentId={}", documentId);

      // 触发文档更新事件
      documentChangEventPublisher.publishRenamedEvent(libraryId, documentId, documentName, operatorId);
    }
    catch (Exception e) {
      logger.error("发布文档重命名事件失败: documentId={}", documentId, e);
    }
  }

  /**
   * 发布文档删除事件
   *
   * @param documentId 删除的文档ID
   * @param libraryId 文档库ID
   * @param operatorId 操作人ID
   * @param spaceId 空间ID
   */
  public void publishDocumentDeletedEvent(String documentId, String libraryId, Long operatorId, Long spaceId) {
    try {
      DocumentPathChangedEvent event = new DocumentPathChangedEvent();
      event.setChangeType(DocumentPathChangedEvent.PathChangeType.DOCUMENT_DELETED);
      event.setLibraryId(libraryId);
      event.setAffectedDocumentIds(Collections.singletonList(documentId));
      event.setReason("文档删除: " + documentId);
      event.setOperatorId(operatorId);
      event.setTenantId(TenantContextHolder.getRequiredTenantId());
      event.setSpaceId(spaceId);

      DisruptorUtil.getInstance().produce(event);
      logger.info("发布文档删除事件: documentId={}", documentId);
    }
    catch (Exception e) {
      logger.error("发布文档删除事件失败: documentId={}", documentId, e);
    }
  }

  /**
   * 发布文档删除事件
   *
   * @param libraryId 文档库ID
   * @param operatorId 操作人ID
   * @param spaceId 空间ID
   */
  public void publishLibraryDeletedEvent(String libraryId, Long operatorId, Long spaceId, List<String> documentIds) {
    try {
      DocumentPathChangedEvent event = new DocumentPathChangedEvent();
      event.setChangeType(DocumentPathChangedEvent.PathChangeType.LIBRARY_DELETED);
      event.setAffectedDocumentIds(documentIds);
      event.setLibraryId(libraryId);
      event.setReason("文档库删除: " + libraryId);
      event.setOperatorId(operatorId);
      event.setTenantId(TenantContextHolder.getRequiredTenantId());
      event.setSpaceId(spaceId);

      DisruptorUtil.getInstance().produce(event);
      logger.info("发布文档库删除事件: libraryId={}", libraryId);
    }
    catch (Exception e) {
      logger.error("发布文档库删除事件失败: libraryId={}", libraryId, e);
    }
  }
}
