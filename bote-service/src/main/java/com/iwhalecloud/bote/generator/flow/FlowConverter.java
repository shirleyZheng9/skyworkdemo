package com.iwhalecloud.bote.generator.flow;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.generator.flow.SimplifiedEdgeDTO;
import com.iwhalecloud.bote.dto.generator.flow.SimplifiedFlowDTO;
import com.iwhalecloud.bote.dto.generator.flow.SimplifiedNodeDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphEdgeDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphEndpointDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.EndNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.IfNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.KnowledgeChatNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.KnowledgeRetrievalNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.LlmNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.LlmSkillNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.PageFuncNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.PageNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.ParamExtractorNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.QuestionClassifierNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.ReplyNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.ScriptNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.ServiceNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.SetVariableNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.SqlNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.StartNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.ToolboxNodeConverter;
import com.iwhalecloud.bote.generator.flow.converter.node.WorkflowNodeConverter;
import com.iwhalecloud.bote.service.skill.ISkillFlowManageService;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 工作流转换器
 *
 * <p>用于在大模型生成的流程图和前端使用的流程图之间互转</p>
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Service
@RequiredArgsConstructor
public class FlowConverter {
  private final ISkillFlowManageService flowManageService;

  /** 节点转换器映射 */
  private final Map<String, AbstractNodeConverter<?>> nodeConverterMap = ImmutableMap.<String, AbstractNodeConverter<?>>builder()
    .put(StepType.START, new StartNodeConverter())
    .put(StepType.END, new EndNodeConverter())
    .put(StepType.REPLY, new ReplyNodeConverter())
    .put(StepType.SET_VARIABLE, new SetVariableNodeConverter())
    .put(StepType.IF, new IfNodeConverter())
    .put(StepType.PAGE, new PageNodeConverter())
    .put(StepType.PAGE_FUNC, new PageFuncNodeConverter())
    .put(StepType.TOOLBOX, new ToolboxNodeConverter())
    .put(StepType.SCRIPT, new ScriptNodeConverter())
    .put(StepType.SERVICE, new ServiceNodeConverter())
    .put(StepType.SQL, new SqlNodeConverter())
    .put(StepType.LLM_SKILL, new LlmSkillNodeConverter())
    .put(StepType.WORKFLOW, new WorkflowNodeConverter())
    .put(StepType.PARAM_EXTRACTOR, new ParamExtractorNodeConverter())
    .put(StepType.QUESTION_CLASSIFIER, new QuestionClassifierNodeConverter())
    .put(StepType.KNOWLEDGE_RETRIEVAL, new KnowledgeRetrievalNodeConverter())
    .put(StepType.KNOWLEDGE_CHAT, new KnowledgeChatNodeConverter())
    .put(StepType.LLM, new LlmNodeConverter())
    .build();

  /**
   * 将工作流转为大模型理解的流程图
   *
   * @param tenantId 租户 ID
   * @param flowId 工作流 ID
   * @return 简化的流程对象
   */
  public SimplifiedFlowDTO convert(Long tenantId, Long flowId) {
    // 查询工作流
    SkillFlowDTO flow = flowManageService.findSkillFlow(tenantId, flowId);
    flow.parseGraph();
    flow.parseParams();
    Assert.notNull(flow.getGraph(), "空流程");
    Assert.isTrue(CollectionUtils.size(flow.getGraph().getNodes()) > 1, "流程节点数量应大于 1");
    Assert.notEmpty(flow.getGraph().getEdges(), "流程线条为空");

    // 简化节点
    List<SimplifiedNodeDTO> simplifiedNodes = new ArrayList<>(flow.getGraph().getNodes().size());
    for (SceneGraphNodeDTO node : flow.getGraph().getNodes()) {
      AbstractNodeConverter<?> converter = nodeConverterMap.get(node.getNodeType());
      Assert.notNull(converter, () -> "不支持的节点类型: " + node.getNodeType());
      simplifiedNodes.add(converter.convertNode(node));
    }

    // 简化线条
    List<SimplifiedEdgeDTO> simplifiedEdges = simplifyEdges(flow.getGraph().getEdges());

    SimplifiedFlowDTO dto = new SimplifiedFlowDTO();
    dto.setName(flow.getFlowName());
    dto.setCode(flow.getFlowCode());
    dto.setFlowType(flow.getFlowType());
    dto.setRequest(AbstractNodeConverter.simplifyParameter(flow.getRequest()));
    if (!flow.isMultiStep()) {
      dto.setResponse(AbstractNodeConverter.simplifyParameter(flow.getResponse()));
    }
    dto.setVariables(AbstractNodeConverter.simplifyParameters(flow.getVariables()));
    dto.setNodes(simplifiedNodes);
    dto.setEdges(simplifiedEdges);
    return dto;
  }

  /**
   * 将大模型生成的流程图还原为前端使用的流程对象
   *
   * @param tenantId 租户 ID
   * @param flow 简化的流程图
   * @return 流程对象
   */
  public SkillFlowDTO revert(Long tenantId, SimplifiedFlowDTO flow, String flowType) {
    FlowConverterContext context = new FlowConverterContext(tenantId);
    SkillFlowDTO dto = new SkillFlowDTO();
    dto.setFlowName(flow.getName());
    dto.setFlowCode(flow.getCode());
    dto.setFlowType(flowType);
    dto.setRequest(AbstractNodeConverter.supplementParameter(flow.getRequest()));
    if (!dto.isMultiStep()) {
      dto.setResponse(AbstractNodeConverter.supplementParameter(flow.getResponse()));
      context.setResponse(dto.getResponse());
    }
    List<ParameterSpec> variables = flow.getVariables();
    if (CollectionUtils.isNotEmpty(variables)) {
      AbstractNodeConverter.supplementParameter(ParameterSpec.newRoot(variables));
      dto.setVariables(variables);
    }

    // 还原节点
    List<SceneGraphNodeDTO> nodes = new LinkedList<>();
    for (SimplifiedNodeDTO node : flow.getNodes()) {
      AbstractNodeConverter<?> converter = nodeConverterMap.get(node.getType());
      Assert.notNull(converter, () -> "不支持的节点类型: " + node.getType());
      nodes.add(converter.revertNode(context, node));
    }

    // 还原线条
    List<SceneGraphEdgeDTO> edges = supplementEdges(flow, nodes);
    dto.setGraph(new SceneGraphDTO(nodes, edges));
    return dto;
  }

  /**
   * 简化线条
   */
  private static List<SimplifiedEdgeDTO> simplifyEdges(List<SceneGraphEdgeDTO> edges) {
    List<SimplifiedEdgeDTO> simplifiedEdges = new ArrayList<>(edges.size());
    for (SceneGraphEdgeDTO edge : edges) {
      SimplifiedEdgeDTO simplifiedEdge = new SimplifiedEdgeDTO();
      simplifiedEdge.setFrom(edge.getSource().getCell());
      simplifiedEdge.setTo(edge.getTarget().getCell());
      String label;
      // 条件的 branchElse 简化为 else
      if ("branchElse".equals(edge.getSource().getPort())) {
        label = "else";
      }
      // 省略 bottom
      else if (!"bottom".equals(edge.getSource().getPort())) {
        label = edge.getSource().getPort();
      }
      else {
        label = null;
      }
      simplifiedEdge.setLabel(label);
      simplifiedEdges.add(simplifiedEdge);
    }
    return simplifiedEdges;
  }

  /**
   * 还原线条，处理逻辑和 {@link #simplifyEdges} 相反
   */
  private static List<SceneGraphEdgeDTO> supplementEdges(SimplifiedFlowDTO flow, List<SceneGraphNodeDTO> nodes) {
    List<SceneGraphEdgeDTO> edges = new ArrayList<>(flow.getEdges().size());
    for (SimplifiedEdgeDTO edge : flow.getEdges()) {
      SceneGraphEdgeDTO dto = new SceneGraphEdgeDTO();
      String port = StringUtils.defaultIfEmpty(edge.getLabel(), "bottom");
      // 条件的 else 还原为 branchElse
      if ("else".equals(port)) {
        SceneGraphNodeDTO node = IterableUtils.find(nodes, n -> edge.getFrom().equals(n.getNodeCode()));
        if (node != null && StepType.IF.equals(node.getNodeType())) {
          port = "branchElse";
        }
      }
      dto.setSource(new SceneGraphEndpointDTO(edge.getFrom(), port));
      dto.setTarget(new SceneGraphEndpointDTO(edge.getTo(), "top"));
      edges.add(dto);
    }
    return edges;
  }
}
