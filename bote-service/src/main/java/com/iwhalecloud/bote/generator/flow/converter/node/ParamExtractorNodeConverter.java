package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bote.dto.generator.flow.node.ParamExtractorNodeData;

/**
 * 参数提取节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class ParamExtractorNodeConverter extends AbstractNodeConverter<ParamExtractorNodeData> {
  public ParamExtractorNodeConverter() {
    super(ParamExtractorNodeData.class);
  }

  @Override
  protected void simplifyNodeData(ParamExtractorNodeData data) {
    data.setParameters(simplifyParameter(data.getParameters()));
    data.setOutData(null);
  }

  @Override
  protected void supplementNodeData(FlowConverterContext context, ParamExtractorNodeData data) {
    data.setParameters(supplementParameter(data.getParameters()));
    data.setOutData(data.getParameters());
  }
}
