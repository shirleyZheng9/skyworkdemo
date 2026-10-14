package com.iwhalecloud.bote.doc.common.diffc.factory;

import com.iwhalecloud.bote.doc.enums.DocSequences;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.cmsr.tiangong.diffc.factory.IDSupplierFactory;
import org.springframework.stereotype.Component;

/**
 * 主键提供者工厂初始化
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public class DocIDSupplierFactoryInitializer {
  public DocIDSupplierFactoryInitializer() {
    // 知识库
    IDSupplierFactory.register(KnowledgeBaseDTO.class, DocSequences.KNOWLEDGE_BASE_KNOWLEDGE_ID::next);

  }
}
