package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.generator.flow.node.ToolboxNodeData;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 工具箱节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class ToolboxNodeConverter extends AbstractNodeConverter<ToolboxNodeData> {
  public ToolboxNodeConverter() {
    super(ToolboxNodeData.class);
  }

  @Override
  protected void simplifyNodeData(ToolboxNodeData data) {
    data.setParameters(simplifyParameter(data.getParameters()));
    data.setOutData(null);
  }

  @Override
  protected void supplementNodeData(FlowConverterContext context, ToolboxNodeData data) {
    data.setParameters(supplementParameter(data.getParameters()));
    if (data.getFuncId() != null) {
      SkillFunctionDTO function = flowAiQueryMapper.selectFunctionById(context.getTenantId(), data.getFuncId());
      if (function != null && StringUtils.isNotEmpty(function.getRespJson())) {
        data.setOutData(JsonUtil.parseJson(function.getRespJson(), ParameterSpec.class));
      }
    }
  }
}
