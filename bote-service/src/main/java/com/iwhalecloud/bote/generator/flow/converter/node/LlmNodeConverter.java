package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bote.dto.generator.flow.node.LlmNodeData;

/**
 * 大模型节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class LlmNodeConverter extends AbstractNodeConverter<LlmNodeData> {
  public LlmNodeConverter() {
    super(LlmNodeData.class);
  }
}
