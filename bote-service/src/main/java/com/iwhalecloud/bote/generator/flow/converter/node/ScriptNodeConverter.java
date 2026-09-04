package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.generator.flow.node.ScriptNodeData;
import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * 代码块节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class ScriptNodeConverter extends AbstractNodeConverter<ScriptNodeData> {
  public ScriptNodeConverter() {
    super(ScriptNodeData.class);
  }

  @Override
  protected void simplifyNodeData(ScriptNodeData data) {
    data.setScriptType(StringUtils.lowerCase(data.getScriptType()));
    data.setParameters(simplifyParameter(data.getParameters()));
    data.setOutData(simplifyParameter(data.getOutData()));
  }

  @Override
  protected void supplementNodeData(FlowConverterContext context, ScriptNodeData data) {
    data.setScriptType(BaseConsts.SCRIPT_TYPE_PYTHON3.equalsIgnoreCase(data.getScriptType()) ? BaseConsts.SCRIPT_TYPE_PYTHON3 : BaseConsts.SCRIPT_TYPE_GROOVY);
    data.setParameters(supplementParameter(data.getParameters()));
    data.setOutData(supplementParameter(data.getOutData()));
  }
}
