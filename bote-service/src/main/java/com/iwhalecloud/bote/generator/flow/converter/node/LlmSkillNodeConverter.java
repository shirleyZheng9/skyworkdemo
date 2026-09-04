package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bote.dto.generator.flow.node.LlmSkillNodeData;

/**
 * 大模型能力节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class LlmSkillNodeConverter extends AbstractNodeConverter<LlmSkillNodeData> {
  public LlmSkillNodeConverter() {
    super(LlmSkillNodeData.class);
  }

  @Override
  protected void simplifyNodeData(LlmSkillNodeData data) {
    data.setParameters(simplifyParameter(data.getParameters()));
  }

  @Override
  protected void supplementNodeData(FlowConverterContext context, LlmSkillNodeData data) {
    data.setParameters(supplementParameter(data.getParameters()));
  }
}
