package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.generator.flow.node.ServiceNodeData;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 服务节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class ServiceNodeConverter extends AbstractNodeConverter<ServiceNodeData> {
  public ServiceNodeConverter() {
    super(ServiceNodeData.class);
  }

  @Override
  protected void simplifyNodeData(ServiceNodeData data) {
    data.setParameters(simplifyParameter(data.getParameters()));
    data.setOutData(null);
  }

  @Override
  protected void supplementNodeData(FlowConverterContext context, ServiceNodeData data) {
    data.setParameters(supplementParameter(data.getParameters()));
    if (data.getServiceId() != null) {
      SkillServiceDTO service = flowAiQueryMapper.selectApiServiceById(context.getTenantId(), data.getServiceId());
      if (service != null && StringUtils.isNotEmpty(service.getResponseJson())) {
        data.setOutData(JsonUtil.parseJson(service.getResponseJson(), ParameterSpec.class));
      }
    }
  }
}
