package com.iwhalecloud.bote.doc.cache;

import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import com.iwhalecloud.bss.litchi.cache.helper.BaseSecondaryCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 文档访问
 *
 * @author Aiqing
 * @since 2025/9/9
 */
@Component
public class DocumentAttachmentCache extends BaseSecondaryCache<DocumentAttachmentDTO> {

  private static final Integer DISTRIBUTION_CACHE_EXPIRE_MINUTE = 5;
  private static final Integer LOCAL_CACHE_EXPIRE_MINUTE = 2;

  private final IDocumentAttachmentService attachmentService;
  private final ControlTemplate controlTemplate;
  private final DcDocumentNodeCache dcDocumentNodeCache;

  public DocumentAttachmentCache(IDocumentAttachmentService documentAttachmentService,
                                 ControlTemplate controlTemplate,
                                 DcDocumentNodeCache dcDocumentNodeCache) {
    super(DocCacheConsts.GROUP_DOC, DocCacheConsts.CACHE_PREFIX_DOCUMENT_ATTACHMENT);
    super.useLocalCache = false;
    // 5分钟本地缓存失效
    super.localCacheExpireInMinutes = LOCAL_CACHE_EXPIRE_MINUTE;

    this.attachmentService = documentAttachmentService;
    this.controlTemplate = controlTemplate;
    this.dcDocumentNodeCache = dcDocumentNodeCache;
  }

  /**
   * 是否有文档的访问权限
   *
   * @param attachmentId 文档ID
   * @param userId 用户ID
   * @return 是否有权限
   */
  public DocumentAttachmentDTO getAttachmentWithCheck(Long attachmentId, Long userId) {
    return getAttachmentWithCheck(attachmentId, userId, false);
  }
  /**
   * 是否有文档的访问权限
   *
   * @param attachmentId 文档ID
   * @param userId 用户ID
   * @return 是否有权限
   */
  public DocumentAttachmentDTO getAttachmentWithCheck(Long attachmentId, @Nullable Long userId, boolean backend) {
    String cacheKey = buildCacheKey(attachmentId, userId);
    DocumentAttachmentDTO attachmentDTO = this.get(cacheKey);
    if (attachmentDTO != null) {
      return attachmentDTO;
    }
    DocumentAttachmentDTO attachmentInfo = attachmentService.getAttachmentInfo(attachmentId);
    if (attachmentInfo == null) {
      DocumentAttachmentDTO emptyDTO = new DocumentAttachmentDTO();
      this.put(cacheKey, emptyDTO, DISTRIBUTION_CACHE_EXPIRE_MINUTE * 60);
      return emptyDTO;
    }
    TenantContextHolder.setIgnore(false);
    TenantContextHolder.setTenantId(attachmentInfo.getTenantId());
    String documentId = attachmentInfo.getDocumentId();
    String libraryId = dcDocumentNodeCache.getLibraryIdByDocument(documentId);
    if (StringUtils.isBlank(libraryId)) {
      DocumentAttachmentDTO emptyDTO = new DocumentAttachmentDTO();
      this.put(cacheKey, emptyDTO, DISTRIBUTION_CACHE_EXPIRE_MINUTE * 60);
      return emptyDTO;
    }

    if (!backend) {
      boolean hasNodePermission = controlTemplate.hasNodePermission(libraryId, userId, documentId,
        NodePermission.READ_NODE);
      if (!hasNodePermission) {
        DocumentAttachmentDTO emptyDTO = new DocumentAttachmentDTO();
        this.put(cacheKey, emptyDTO, DISTRIBUTION_CACHE_EXPIRE_MINUTE * 60);
        return emptyDTO;
      }
    }
    this.put(cacheKey, attachmentInfo, DISTRIBUTION_CACHE_EXPIRE_MINUTE * 60);
    return attachmentInfo;
  }

  private String buildCacheKey(Long attachmentId, @Nullable Long userId) {
    return attachmentId + "_" + userId;
  }
}
