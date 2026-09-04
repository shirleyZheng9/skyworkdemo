package com.iwhalecloud.bote.generator.flow.converter.node;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.generator.flow.node.WorkflowNodeData;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;

/**
 * 工作流节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class WorkflowNodeConverter extends AbstractNodeConverter<WorkflowNodeData> {
  public WorkflowNodeConverter() {
    super(WorkflowNodeData.class);
  }

  @Override
  protected void simplifyNodeData(WorkflowNodeData data) {
    data.setParameters(simplifyParameter(data.getParameters()));
    data.setOutData(null);
  }

  @Override
  protected void supplementNodeData(FlowConverterContext context, WorkflowNodeData data) {
    data.setParameters(supplementParameter(data.getParameters()));
    if (data.getFlowId() != null) {
      data.setOutData(buildOutData(context.getTenantId(), data.getFlowId()));
    }
  }

  /**
   * 构造工作流的出参
   */
  @Nullable
  private ParameterSpec buildOutData(Long tenantId, Long flowId) {
    SkillFlowDTO flow = flowAiQueryMapper.selectFlowById(tenantId, flowId);
    if (flow == null || flow.getParam() == null) {
      return null;
    }
    // 对话型使用流程变量作为出参
    if (flow.isMultiStep()) {
      List<ParameterSpec> parameters = JsonUtil.parseJson(flow.getParam().getVariableJson(), new TypeReference<List<ParameterSpec>>() {
      });
      if (CollectionUtils.isNotEmpty(parameters)) {
        return ParameterSpec.newRoot(parameters);
      }
      return null;
    }
    // 任务型使用流程出参
    return JsonUtil.parseJson(flow.getParam().getResponseJson(), ParameterSpec.class);
  }
}
