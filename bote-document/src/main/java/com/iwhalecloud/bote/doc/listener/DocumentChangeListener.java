package com.iwhalecloud.bote.doc.listener;

import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.doc.cache.PinnedDocumentCache;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.consts.DocumentActionTypeEnum;
import com.iwhalecloud.bote.doc.consts.TargetTypeEnum;
import com.iwhalecloud.bote.doc.listener.event.DocChangeEventMessage;
import com.iwhalecloud.bote.doc.module.document.enums.RecordTypeEnum;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentActivityService;
import com.iwhalecloud.bote.doc.module.person.service.IFavoriteService;
import com.iwhalecloud.bote.doc.module.person.service.IHomepageService;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorEventListener;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 文档库文档更新事件
 *
 * @author Aiqing
 * @since 2025/8/19
 */
@Component
@RequiredArgsConstructor
public class DocumentChangeListener implements DisruptorEventListener<DocChangeEventMessage> {

  private static final Logger logger = LoggerFactory.getLogger(DocumentChangeListener.class);

  private final IDocumentActivityService documentActivityService;

  private final IHomepageService homepageService;

  private final PinnedDocumentCache pinnedDocumentCache;

  private final IFavoriteService favoriteService;

  @Override
  public void onEvent(DocChangeEventMessage event) {
    DocumentActionTypeEnum changeType = event.getChangeType();
    String documentId = event.getDocumentId();
    String libraryId = event.getLibraryId();
    Long updatorId = event.getUpdatorId();
    Long spaceId = event.getSpaceId();

    if (updatorId == null) {
      logger.warn("文档事件消费跳过，触发用户ID为空, documentId:{}", documentId);
      return;
    }

    try {
      Long tenantId = event.getTenantId();
      TenantContextHolder.setTenantId(tenantId);
      TenantContextHolder.setIgnore(false);

      recordActivity(event, documentId, libraryId, updatorId, changeType);
      afterActivity(changeType, documentId, updatorId, tenantId, spaceId);

    }
    finally {
      TenantContextHolder.clear();
    }
  }

  /**
   * 事件记录逻辑
   *
   * @param event 文档变更事件
   * @param documentId 文档ID
   * @param libraryId 文档库ID
   * @param updatorId 更新者ID
   * @param changeType 变更类型
   */
  private void recordActivity(DocChangeEventMessage event, String documentId, String libraryId, Long updatorId,
                              DocumentActionTypeEnum changeType) {
    // 插入文档库动态
    if (Objects.equals(changeType, DocumentActionTypeEnum.VIEW)) {
      // 如果是查看事件，查询最近1小时内是否有记录，有则忽略
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime lastHourDate = now.minusHours(1);
      Long existActivityCount = documentActivityService.countByCondition(documentId, libraryId, updatorId,
        DocumentActionTypeEnum.VIEW.getCode(), lastHourDate, now);
      if (existActivityCount != null && existActivityCount > 0) {
        return;
      }
    }
    if (Objects.equals(changeType, DocumentActionTypeEnum.EDIT)) {
      if (RecordTypeEnum.CORRECTION.getCode().equals(event.getMode())) {
        return;
      }
      // 如果是查看事件，查询最近1小时内是否有记录，有则忽略
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime lastHourDate = now.minusMinutes(30);
      Long existActivityCount = documentActivityService.countByCondition(documentId, libraryId, updatorId,
        DocumentActionTypeEnum.EDIT.getCode(), lastHourDate, now);
      if (existActivityCount != null && existActivityCount > 0) {
        return;
      }
    }
    documentActivityService.recordActivity(documentId, libraryId, updatorId, event.getChangeType().name(),
      event.getClientIp(), null, event.getTenantId(), event.getDocumentName(), event.getTargetLibraryId());
  }

  /**
   * 事件后附加逻辑处理
   *
   * @param changeType 变更类型
   * @param documentId 文档ID
   * @param updatorId 更新者ID
   * @param tenantId 租户ID
   */
  private void afterActivity(DocumentActionTypeEnum changeType, String documentId, Long updatorId, Long tenantId, Long spaceId) {
    if (Objects.equals(changeType, DocumentActionTypeEnum.DELETE)) {
      if (documentId != null && updatorId != null) {
        homepageService.unpinResourceByTenant(tenantId, documentId, TargetTypeEnum.DOCUMENT.getCode(), spaceId);
        pinnedDocumentCache.clearTenantCache(tenantId, spaceId);
        favoriteService.removeTenantFavorite(tenantId, documentId, TargetTypeEnum.DOCUMENT.getCode(), spaceId);
        Long spaceTenantId = TenantIdUtil.getSpaceTenantId(spaceId);
        if (spaceTenantId != null) {
          homepageService.unpinResourceByTenant(spaceTenantId, documentId, TargetTypeEnum.DOCUMENT.getCode(), spaceId);
          pinnedDocumentCache.clearTenantCache(spaceTenantId, spaceId);
          favoriteService.removeTenantFavorite(spaceTenantId, documentId, TargetTypeEnum.DOCUMENT.getCode(), spaceId);
        }
      }
    }
    if (Objects.equals(changeType, DocumentActionTypeEnum.EDIT)) {
      if (documentId != null && updatorId != null) {
        pinnedDocumentCache.clearTenantCache(tenantId, spaceId);
        Long spaceTenantId = TenantIdUtil.getSpaceTenantId(spaceId);
        if (spaceTenantId != null) {
          pinnedDocumentCache.clearTenantCache(spaceTenantId, spaceId);
        }
      }
    }
  }
}
