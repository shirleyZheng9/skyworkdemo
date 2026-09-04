package com.iwhalecloud.bote.service.orchestration.converter;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.LoopStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.service.orchestration.SceneStepRegistry;
import com.iwhalecloud.bote.service.orchestration.helper.SceneGraphValidator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 场景 DSL 转换器
 *
 * @author bianjp
 * @since 2024-09-14
 */
public final class SceneDslConverter {
  private SceneDslConverter() {
  }

  /**
   * 转换流程
   */
  public static SceneDslDTO convert(SkillFlowDTO flow) {
    if (!flow.isMultiStep() && CollectionUtils.isNotEmpty(flow.getGraph().getNodes())) {
      Assert.isTrue(flow.getGraph().getNodes().stream().noneMatch(SceneGraphNodeDTO::isChatOnly), "任务型工作流不支持使用回复、页面、页面函数节点");
    }
    SceneDslDTO dsl = new SceneDslDTO();
    dsl.setInput(flow.getRequest());
    dsl.setVariables(CollectionUtils.isNotEmpty(flow.getVariables()) ? flow.getVariables() : null);
    dsl.setSteps(convertSteps(flow.getTenantId(), flow.getGraph(), flow.getFlowSteps()));
    return dsl;
  }

  /**
   * 转换场景
   */
  public static SceneDslDTO convert(BotSceneDTO scene) {
    SceneDslDTO dsl = new SceneDslDTO();
    dsl.setVariables(CollectionUtils.isNotEmpty(scene.getVariables()) ? scene.getVariables() : null);
    dsl.setInput(scene.getRequest());
    dsl.setSteps(convertSteps(scene.getTenantId(), scene.getGraph(), scene.getFlowSteps()));
    return dsl;
  }

  /**
   * 转换步骤
   */
  private static List<AbstractStep> convertSteps(Long tenantId, SceneGraphDTO graph, List<SimpleFlowStepDTO> flowSteps) {
    // 上下文
    ConverterContext context = new ConverterContext(tenantId, graph);
    // 转换后的步骤列表
    List<AbstractStep> steps = new ArrayList<>();
    // 已转换的节点集合，避免重复转换
    Set<SceneGraphNodeDTO> convertedNodes = new HashSet<>();
    // 待转换的节点栈
    Deque<SceneGraphNodeDTO> stack = new ArrayDeque<>();
    stack.push(context.getStartNode());

    // 深度优先转换节点
    SceneGraphNodeDTO node;
    AbstractStep step = null;
    while (!stack.isEmpty()) {
      node = stack.removeLast();
      if (!StepType.PARALLEL_END.equals(node.getNodeType())) {
        step = SceneStepRegistry.getConverter(node.getNodeType()).convert(node, context);
        step.setNext(findNextNode(node, context));
        // 步骤备注信息
        String code = step.getCode();
        SimpleFlowStepDTO flowStep = IterableUtils.find(CollectionUtils.emptyIfNull(flowSteps), p -> Objects.equals(code, p.getNodeCode()));
        if (flowStep != null) {
          step.setDesc(flowStep.getNodeName());
        }
        steps.add(step);
      }
      convertedNodes.add(node);

      // 处理下级节点
      for (Pair<String, SceneGraphNodeDTO> edge : context.getTargetEdges(node)) {
        if (!convertedNodes.contains(edge.getRight())) {
          stack.addLast(edge.getRight());
        }
      }

      // 处理循环的子节点
      if (step instanceof LoopStep) {
        SceneGraphNodeDTO loopChildNode = SceneGraphValidator.findLoopChildNode(graph.getNodes(), node, context.getTargetEdgesMap());
        ((LoopStep) step).setChildStep(loopChildNode.getNodeCode());
        stack.addLast(loopChildNode);
      }
    }
    return steps;
  }

  /**
   * 获取节点的下一个节点
   */
  @Nullable
  private static String findNextNode(SceneGraphNodeDTO node, ConverterContext context) {
    // 分支、问题分类没有下级节点
    if (StepType.IF.equals(node.getNodeType()) || StepType.QUESTION_CLASSIFIER.equals(node.getNodeType())) {
      return null;
    }
    String nextNodeCode = null;
    // 并行节点使用所有并行分支后的公共节点作为下一个节点
    if (StepType.PARALLEL.equals(node.getNodeType())) {
      nextNodeCode = SceneGraphValidator.findNextNodeOfParallel(context.getTargetEdgesMap(), node);
    }
    // 非 if, parallel 节点只可能有一个下级节点，作为下一个节点
    else {
      // 检查是否有下级节点，忽略异常分支
      List<Pair<String, SceneGraphNodeDTO>> targetEdges = CollectionUtils.emptyIfNull(context.getTargetEdges(node)).stream()
        .filter(p -> "bottom".equals(p.getLeft())).toList();
      if (!targetEdges.isEmpty()) {
        SceneGraphNodeDTO nextNode = targetEdges.getFirst().getRight();
        // 忽略并行结束节点
        if (!StepType.PARALLEL_END.equals(nextNode.getNodeType())) {
          nextNodeCode = nextNode.getNodeCode();
        }
      }
    }
    return nextNodeCode;
  }

}
