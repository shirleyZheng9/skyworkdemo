package com.iwhalecloud.bote.doc.module.document.service.helper;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.cache.DocumentAttachmentCache;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import java.io.InputStream;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentAttachmentHelper {
  private final IDocumentAttachmentService documentAttachmentService;
  private final DocumentAttachmentCache documentAttachmentCache;
  private final IFileStoreService fileStoreService;
  public InputStream loadAttachment(String documentId, String fileName, boolean backend) {
    Long attachmentId = documentAttachmentService.resolveAttachmentId(fileName);

    if (attachmentId == null) {
      return null;
    }
    Long currentLoginUserId = null;
    if (!backend) {
      currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    }

    DocumentAttachmentDTO attachmentWithCheck = documentAttachmentCache.getAttachmentWithCheck(attachmentId, currentLoginUserId, backend);

    if (attachmentWithCheck.getAttachmentId() == null || !Objects.equals(documentId, attachmentWithCheck.getDocumentId())) {
      return null;
    }
    return fileStoreService.downloadFileStream(attachmentWithCheck);
  }
}
