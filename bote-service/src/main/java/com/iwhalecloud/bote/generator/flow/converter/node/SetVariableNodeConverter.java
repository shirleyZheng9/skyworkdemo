package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bote.dto.generator.flow.node.SetVariableNodeData;

/**
 * 设置变量节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class SetVariableNodeConverter extends AbstractNodeConverter<SetVariableNodeData> {
  public SetVariableNodeConverter() {
    super(SetVariableNodeData.class);
  }

  @Override
  protected void simplifyNodeData(SetVariableNodeData data) {
    data.setVariables(simplifyParameters(data.getVariables()));
  }
}
