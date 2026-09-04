package com.iwhalecloud.bote.doc.common.diffc.persist.impl.knowledge;

import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.KnowledgeBaseManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：知识库
 *
 * @author auto
 * @since 2024-09-20
 */
@Component
public final class KnowledgeBaseDifferencePersistence extends BaseRootPersistence<KnowledgeBaseDTO> {
  public KnowledgeBaseDifferencePersistence(KnowledgeBaseManageMapper knowledgeBaseManageMapper) {
    setAddConsumer(knowledgeBaseManageMapper::insertKnowledgeBase);
    setModifyConsumer(knowledgeBaseManageMapper::updateKnowledgeBase);
  }
}
