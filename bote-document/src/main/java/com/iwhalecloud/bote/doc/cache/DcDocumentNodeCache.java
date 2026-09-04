package com.iwhalecloud.bote.doc.cache;

import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bote.doc.module.control.service.DocumentNodeService;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 *
 * @author Aiqing
 * @since 2025/9/13
 */
@Component
public class DcDocumentNodeCache extends AbstractTenantCache<NodeBaseInfoDTO> {

  private final DocumentNodeService documentNodeService;

  public DcDocumentNodeCache(DocumentNodeService documentNodeService) {
    super(DocCacheConsts.GROUP_DOC, DocCacheConsts.CACHE_DOCUMENT_NODE_PREFIX);
    this.documentNodeService = documentNodeService;
  }

  /**
   * 查询文档节点信息
   *
   * @param documentId 文档ID
   * @return 文档信息
   */
  @Nullable
  public NodeBaseInfoDTO getDocumentNodeInfo(String documentId) {
    return super.get(documentId);
  }

  @Override
  protected NodeBaseInfoDTO loadByKey(String key) {
    return documentNodeService.queryBaseInfo(key);
  }

  @Nullable
  public String getLibraryIdByDocument(String documentId) {
    NodeBaseInfoDTO nodeInfo = this.getDocumentNodeInfo(documentId);
    if (nodeInfo == null) {
      return null;
    }
    return nodeInfo.getLibraryId();
  }
}
