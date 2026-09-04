package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.iwhalecloud.bote.doc.module.library.service.IDocumentLibraryInitService;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

public final class DocumentLibraryInitServiceHelper {
  private static volatile IDocumentLibraryInitService documentLibraryInitService;

  /**
   * 私有构造函数，防止实例化
   */
  private DocumentLibraryInitServiceHelper() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * 初始化个人文档库
   * @param userId
   * @param tenantId
   * @param spaceId
   * @return
   */
  public static String initializeUserDocumentLibrary(Long userId, Long tenantId, Long spaceId) {
    if (documentLibraryInitService == null) {
      synchronized (DocumentLibraryInitServiceHelper.class) {
        if (documentLibraryInitService == null) {
          documentLibraryInitService = SpringUtil.getBean(IDocumentLibraryInitService.class);
        }
      }
    }
    return documentLibraryInitService.initializeUserDocumentLibrary(userId, tenantId, spaceId);
  }
}
