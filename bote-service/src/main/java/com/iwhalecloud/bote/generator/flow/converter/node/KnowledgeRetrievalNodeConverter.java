package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bote.dto.generator.flow.node.KnowledgeRetrievalNodeData;

/**
 * 知识检索节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class KnowledgeRetrievalNodeConverter extends AbstractNodeConverter<KnowledgeRetrievalNodeData> {
  public KnowledgeRetrievalNodeConverter() {
    super(KnowledgeRetrievalNodeData.class);
  }
}
