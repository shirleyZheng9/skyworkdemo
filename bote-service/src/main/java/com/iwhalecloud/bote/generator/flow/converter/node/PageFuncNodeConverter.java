package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bote.dto.generator.flow.node.PageFuncNodeData;

/**
 * 页面函数节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class PageFuncNodeConverter extends AbstractNodeConverter<PageFuncNodeData> {
  public PageFuncNodeConverter() {
    super(PageFuncNodeData.class);
  }

  @Override
  protected void simplifyNodeData(PageFuncNodeData data) {
    data.setParameters(simplifyParameter(data.getParameters()));
  }

  @Override
  protected void supplementNodeData(FlowConverterContext context, PageFuncNodeData data) {
    data.setParameters(supplementParameter(data.getParameters()));
  }
}
