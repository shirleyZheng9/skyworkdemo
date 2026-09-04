package com.iwhalecloud.bote.doc.common.diffc.factory;

import com.iwhalecloud.bote.doc.common.diffc.persist.impl.knowledge.KnowledgeBaseDifferencePersistence;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bss.litchi.diffc.factory.BatchPersistenceFactory;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务工厂初始化
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public class DocPersistenceFactoryInitializer {

  public DocPersistenceFactoryInitializer() {
    // 知识库
    BatchPersistenceFactory.register(KnowledgeBaseDTO.class, KnowledgeBaseDifferencePersistence.class);

  }

}
