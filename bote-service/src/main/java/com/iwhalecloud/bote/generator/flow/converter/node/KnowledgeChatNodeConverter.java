package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bote.dto.generator.flow.node.KnowledgeChatNodeData;

/**
 * 知识问答节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class KnowledgeChatNodeConverter extends AbstractNodeConverter<KnowledgeChatNodeData> {
  public KnowledgeChatNodeConverter() {
    super(KnowledgeChatNodeData.class);
  }
}
