package com.iwhalecloud.bote.doc.listener;

import com.iwhalecloud.bote.doc.cache.DocumentPathCache;
import com.iwhalecloud.bote.doc.cache.PinnedDocumentCache;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.listener.event.DocumentPathChangedEvent;
import com.iwhalecloud.bote.doc.listener.event.DocumentPathChangedEvent.PathChangeType;
import com.iwhalecloud.bote.doc.module.control.service.DocumentNodeService;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorEventListener;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 文档路径变更事件监听器
 *
 * <p>监听文档路径相关变更事件，自动清除受影响的路径缓存。</p>
 *
 * @author Aiqing
 * @since 2025/1/15
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class DocumentPathChangeListener implements DisruptorEventListener<DocumentPathChangedEvent> {

  private static final Logger logger = LoggerFactory.getLogger(DocumentPathChangeListener.class);

  private final DocumentPathCache documentPathCache;
  private final DocumentNodeService documentNodeService;
  private final PinnedDocumentCache pinnedDocumentCache;

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void onEvent(DocumentPathChangedEvent event) {
    if (event == null || event.getTenantId() == null) {
      logger.warn("收到空的文档路径变更事件，跳过处理");
      return;
    }
    String libraryId = event.getLibraryId();
    logger.debug("处理文档路径变更事件: type={}, libraryId={}, affectedCount={}, reason={}",
      event.getChangeType(), libraryId,
      CollectionUtils.size(event.getAffectedDocumentIds()), event.getReason());
    try {
      TenantContextHolder.setIgnore(false);
      TenantContextHolder.setTenantId(event.getTenantId());
      List<String> affectedDocumentIds = event.getAffectedDocumentIds();
      if (!Objects.equals(event.getChangeType(), PathChangeType.LIBRARY_NAME_CHANGED)
        && CollectionUtils.isEmpty(affectedDocumentIds)) {
        logger.warn("受影响的文档ID列表为空，跳过缓存清除");
        return;
      }

      // 根据事件类型执行不同的缓存清除策略
      switch (event.getChangeType()) {
        case LIBRARY_NAME_CHANGED:
        case LIBRARY_DELETED:
          handleLibraryNameChanged(event);
          break;
        case DOCUMENT_MOVED:
        case DOCUMENT_RENAMED:
          handleDocumentPathChanged(libraryId, affectedDocumentIds, event.getOperatorId(), event.getSpaceId());
          break;
        case DOCUMENT_DELETED:
          handleDocumentDeleted(libraryId, affectedDocumentIds);
          break;
        default:
          logger.warn("未知的路径变更类型: {}", event.getChangeType());
      }

      logger.debug("文档路径变更事件处理完成: type={}, clearedCount={}", event.getChangeType(),
        affectedDocumentIds == null ? 0 : affectedDocumentIds.size());
    }
    catch (Exception e) {
      logger.error("处理文档路径变更事件失败: event={}", event, e);
      // 不抛出异常，避免影响业务流程
    }
    finally {
      TenantContextHolder.clear();
    }
  }

  /**
   * 处理文档库名称修改
   * 需要清除该文档库下所有文档的路径缓存
   */
  private void handleLibraryNameChanged(DocumentPathChangedEvent event) {
    String libraryId = event.getLibraryId();
    if (libraryId == null) {
      logger.warn("文档库ID为空，无法处理文档库名称修改事件");
      return;
    }
    documentPathCache.clearCacheByLibraryId(event.getLibraryId());
  }

  /**
   * 处理文档路径变更（移动、重命名）
   * 清除受影响的文档及其子文档的路径缓存
   */
  private void handleDocumentPathChanged(String libraryId, List<String> documentIds, Long userId, Long spaceId) {
    for (String documentId : documentIds) {
      try {
        // 获取该文档的所有子文档ID
        List<String> childDocumentIds = documentNodeService.getNodeIdsInNodeTree(documentId, -1);

        // 清除当前文档及其所有子文档的路径缓存
        childDocumentIds.add(documentId);
        documentPathCache.clearCacheByKeys(libraryId, childDocumentIds);
        String cacheKey = userId + ":" + spaceId;
        pinnedDocumentCache.delete(cacheKey);

        logger.debug("文档路径变更，清除缓存: documentId={}, childCount={}",
          documentId, childDocumentIds.size() - 1);
      }
      catch (Exception e) {
        logger.error("清除文档路径缓存失败: documentId={}", documentId, e);
      }
    }
  }

  /**
   * 处理文档删除
   * 直接清除指定文档的路径缓存
   */
  private void handleDocumentDeleted(String libraryId, List<String> documentIds) {
    documentPathCache.clearCacheByKeys(libraryId, documentIds);
    logger.debug("文档删除，清除路径缓存: documentCount={}", documentIds.size());
  }
}
