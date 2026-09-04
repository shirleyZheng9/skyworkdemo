package com.iwhalecloud.bote.doc.cache;

import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryService;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 文档库缓存
 *
 * @author Aiqing
 * @since 2025/9/11
 */
@Component
public class DocumentLibraryCache extends AbstractTenantCache<DocumentLibraryDTO> {

  public static final Integer LOCAL_CACHE_EXPIRE_MINUTE = 5;
  private final DocumentLibraryService documentLibraryService;

  public DocumentLibraryCache(DocumentLibraryService documentLibraryService) {
    super(DocCacheConsts.GROUP_DOC, DocCacheConsts.CACHE_PREFIX_DOCUMENT_LIBRARY);
    this.documentLibraryService = documentLibraryService;
    this.localCacheExpireInMinutes = LOCAL_CACHE_EXPIRE_MINUTE;
  }

  @Override
  protected DocumentLibraryDTO loadByKey(String key) {
    return documentLibraryService.findByLibraryId(key);
  }

  /**
   * 获取文档库详情
   *
   * @param libraryId 文档库ID
   * @return 文档库详情
   */
  @Nullable
  public DocumentLibraryDTO getByLibraryId(String libraryId) {
    return super.get(libraryId);
  }

  /**
   * 移除文档库缓存
   *
   * @param libraryId 文档库ID
   */
  public void deleteLibraryCache(String libraryId) {
    super.delete(libraryId);
  }

  /**
   * 获取文档库名称
   *
   * @param libraryId 文档库ID
   * @return 文档库名称
   */
  @Nullable
  public String getLibraryName(String libraryId) {
    DocumentLibraryDTO documentLibraryDTO = this.get(libraryId);
    if (documentLibraryDTO == null) {
      return null;
    }
    return documentLibraryDTO.getLibraryName();
  }
}
