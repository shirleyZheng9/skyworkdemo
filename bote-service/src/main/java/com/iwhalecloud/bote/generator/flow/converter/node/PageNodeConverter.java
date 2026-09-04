package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bote.dto.generator.flow.node.PageNodeData;

/**
 * 页面节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class PageNodeConverter extends AbstractNodeConverter<PageNodeData> {
  public PageNodeConverter() {
    super(PageNodeData.class);
  }

  @Override
  protected void simplifyNodeData(PageNodeData data) {
    data.setParameters(simplifyParameter(data.getParameters()));
  }

  @Override
  protected void supplementNodeData(FlowConverterContext context, PageNodeData data) {
    data.setParameters(supplementParameter(data.getParameters()));
  }
}
