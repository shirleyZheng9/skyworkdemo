package com.iwhalecloud.bote.common.diffc.persist.impl.knowledge;

import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：文档
 *
 * @author auto
 * @since 2024-09-20
 */
@Component
public final class DocumentDifferencePersistence extends BaseRootPersistence<DocumentDTO> {

  public DocumentDifferencePersistence(DocumentManageMapper documentManageMapper) {
    setAddConsumer(documentManageMapper::insertDocument);
    setBatchAddConsumer(documentManageMapper::batchInsertDocument);
    setModifyConsumer(documentManageMapper::updateDocument);
  }

}
