package com.iwhalecloud.bote.service.orchestration.helper;

import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphEdgeDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 场景流程图校验器
 *
 * @author bianjp
 * @since 2024-09-14
 */
public final class SceneGraphValidator {
  private SceneGraphValidator() {
  }

  /**
   * 校验基础信息
   */
  public static SceneGraphNodeDTO validateBasic(SceneGraphDTO graph) {
    List<SceneGraphNodeDTO> nodes = ListUtils.emptyIfNull(graph.getNodes());
    // 校验节点
    List<SceneGraphNodeDTO> startsNodes = ListUtils.select(nodes, n -> StepType.START.equals(n.getNodeType()));
    Assert.notEmpty(startsNodes, "请添加开始节点");
    Assert.isTrue(startsNodes.size() == 1, "只能有一个开始节点");
    Assert.isTrue(nodes.size() > 1, "请至少添加两个节点");

    // 校验节点编码
    for (SceneGraphNodeDTO node : nodes) {
      Assert.hasLength(node.getNodeCode(), "节点编码不能为空");
      Assert.hasLength(node.getNodeType(), "节点类型不能为空");
    }
    Assert.isTrue(nodes.stream().map(SceneGraphNodeDTO::getNodeCode).distinct().count() == nodes.size(), "节点编码不能重复");

    SceneGraphNodeDTO startNode = startsNodes.get(0);

    // 校验线条
    List<SceneGraphEdgeDTO> edges = ListUtils.emptyIfNull(graph.getEdges());
    Assert.isTrue(edges.stream().anyMatch(e -> startNode.getNodeCode().equals(e.getSource().getCell())), "请连接开始节点与其它节点");
    Assert.isTrue(edges.stream().noneMatch(e -> e.getSource() == null || e.getTarget() == null), "线条的起始或结束节点不能为空");
    return startNode;
  }

  /**
   * 校验线条
   */
  public static void validateEdges(SceneGraphNodeDTO startNode, List<SceneGraphNodeDTO> nodes, Map<SceneGraphNodeDTO, List<Pair<String, SceneGraphNodeDTO>>> targetEdgesMap) {
    validateTargetEdgeCount(targetEdgesMap);
    validateNoCycle(startNode, targetEdgesMap);
    validateParallel(targetEdgesMap);
    validateLoop(nodes, targetEdgesMap);
  }

  /**
   * 校验下级节点数量
   */
  private static void validateTargetEdgeCount(Map<SceneGraphNodeDTO, List<Pair<String, SceneGraphNodeDTO>>> targetEdgesMap) {
    // 只有 if 节点允许有多个下级节点
    for (Entry<SceneGraphNodeDTO, List<Pair<String, SceneGraphNodeDTO>>> entry : targetEdgesMap.entrySet()) {
      if (entry.getValue().size() <= 1) {
        continue;
      }
      // 排除异常分支
      long count = entry.getValue().stream().filter(e -> !SceneConsts.EXCEPTION_EDGE_PORT.equals(e.getLeft())).count();
      if (count > 1) {
        SceneGraphNodeDTO node = entry.getKey();
        if (!StepType.IF.equals(node.getNodeType()) && !StepType.PARALLEL.equals(node.getNodeType()) && !StepType.QUESTION_CLASSIFIER.equals(node.getNodeType())) {
          throw new BssException("节点【" + node.getNodeName() + "】不能有多个下级节点");
        }
      }
    }
  }

  /**
   * 校验不能有环
   *
   * <p>参考 {@link com.google.common.graph.Graphs#hasCycle(com.google.common.graph.Graph)} 的实现逻辑。</p>
   */
  @SuppressWarnings("UnstableApiUsage")
  private static void validateNoCycle(SceneGraphNodeDTO startNode, Map<SceneGraphNodeDTO, List<Pair<String, SceneGraphNodeDTO>>> targetEdgesMap) {
    // 暂时放开不能有环的限制
    if (!targetEdgesMap.isEmpty()) {
      return;
    }
    Deque<MutablePair<SceneGraphNodeDTO, Deque<SceneGraphNodeDTO>>> stack = new ArrayDeque<>();
    stack.addLast(MutablePair.of(startNode, null));
    Map<SceneGraphNodeDTO, Boolean> visitedNodes = new HashMap<>();

    while (!stack.isEmpty()) {
      MutablePair<SceneGraphNodeDTO, Deque<SceneGraphNodeDTO>> top = stack.peekLast();

      SceneGraphNodeDTO node = top.left;
      if (top.right == null) {
        Boolean state = visitedNodes.get(node);
        if (Boolean.TRUE.equals(state)) {
          stack.removeLast();
          continue;
        }
        if (Boolean.FALSE.equals(state)) {
          throw new BssException(String.format("节点【%s(%s)】存在环", node.getNodeName(), node.getNodeCode()));
        }

        visitedNodes.put(node, false);
        top.right = targetEdgesMap.get(node).stream().map(Pair::getRight).collect(Collectors.toCollection(ArrayDeque::new));
      }

      if (!top.right.isEmpty()) {
        SceneGraphNodeDTO nextNode = top.right.remove();
        stack.addLast(MutablePair.of(nextNode, null));
        continue;
      }

      stack.removeLast();
      visitedNodes.put(node, true);
    }
  }

  /**
   * 校验并行节点
   */
  private static void validateParallel(Map<SceneGraphNodeDTO, List<Pair<String, SceneGraphNodeDTO>>> targetEdgesMap) {
    // TODO 完善并行节点的校验
    for (Entry<SceneGraphNodeDTO, List<Pair<String, SceneGraphNodeDTO>>> entry : targetEdgesMap.entrySet()) {
      if (!StepType.PARALLEL.equals(entry.getKey().getNodeType())) {
        continue;
      }
      SceneGraphNodeDTO parallelNode = entry.getKey();
      long parallelEndCount = entry.getValue().stream()
        .map(p -> findParallelEndNode(targetEdgesMap, p.getRight()))
        .filter(Objects::nonNull)
        .distinct()
        .count();
      if (parallelEndCount > 1) {
        throw new BssException("并行节点【" + parallelNode.getNodeName() + "】的分支未连接到同一个并行结束节点");
      }
    }
  }

  /**
   * 校验循环节点
   */
  private static void validateLoop(List<SceneGraphNodeDTO> nodes, Map<SceneGraphNodeDTO, List<Pair<String, SceneGraphNodeDTO>>> targetEdgesMap) {
    for (SceneGraphNodeDTO node : nodes) {
      if (!StepType.LOOP.equals(node.getNodeType())) {
        continue;
      }
      findLoopChildNode(nodes, node, targetEdgesMap);
    }
  }

  /**
   * 获取循环节点的子节点（循环开始节点连接的节点）
   */
  public static SceneGraphNodeDTO findLoopChildNode(List<SceneGraphNodeDTO> nodes, SceneGraphNodeDTO node, Map<SceneGraphNodeDTO, List<Pair<String, SceneGraphNodeDTO>>> targetEdgesMap) {
    // 解析循环的子节点
    Assert.notEmpty(node.getChildrenCodes(), () -> String.format("循环节点【%s】必须有子节点", node.getNodeName()));
    // 第一个子节点是虚拟的循环开始节点
    String loopStartNodeCode = node.getChildrenCodes().get(0);
    SceneGraphNodeDTO loopStartNode = IterableUtils.find(nodes, n -> loopStartNodeCode.equals(n.getNodeCode()));
    Assert.notNull(loopStartNode, () -> String.format("循环节点【%s】的子节点不存在: %s", node.getNodeName(), loopStartNodeCode));
    Assert.isTrue(StepType.LOOP_START.equals(loopStartNode.getNodeType()), () -> String.format("循环节点【%s】的第一个子节点必须是循环开始: %s", node.getNodeName(), loopStartNodeCode));
    // 循环开始节点只能连接一个节点，表示循环内的第一个节点
    List<Pair<String, SceneGraphNodeDTO>> loopStartEdges = targetEdgesMap.get(loopStartNode);
    Assert.notEmpty(loopStartEdges, () -> String.format("循环节点【%s】的循环开始节点未连接其它节点", node.getNodeName()));
    Assert.isTrue(loopStartEdges.size() == 1, () -> String.format("循环节点【%s】的循环开始节点只能连接一个节点", node.getNodeName()));
    return loopStartEdges.get(0).getRight();
  }

  /**
   * 获取并行节点后面的第一个并行结束节点
   */
  @Nullable
  public static SceneGraphNodeDTO findParallelEndNode(Map<SceneGraphNodeDTO, List<Pair<String, SceneGraphNodeDTO>>> targetEdgesMap, SceneGraphNodeDTO node) {
    // 找出下级节点列表
    List<Pair<String, SceneGraphNodeDTO>> successors = targetEdgesMap.get(node);
    if (CollectionUtils.isEmpty(successors)) {
      return null;
    }
    // TODO 完善找并行结束节点的逻辑
    // 检查下级节点中是否有并行结束节点，如果有直接返回
    Pair<String, SceneGraphNodeDTO> endNode = IterableUtils.find(successors, p -> StepType.PARALLEL_END.equals(p.getRight().getNodeType()));
    if (endNode != null) {
      return endNode.getRight();
    }
    // 递归查找每个下级节点的后代节点，直到找到一个并行结束节点
    for (Pair<String, SceneGraphNodeDTO> successor : successors) {
      SceneGraphNodeDTO parallelEndNode = findParallelEndNode(targetEdgesMap, successor.getRight());
      if (parallelEndNode != null) {
        return parallelEndNode;
      }
    }
    return null;
  }

  /**
   * 获取并行节点的下一个节点（并行结束节点的下一个节点）
   */
  @Nullable
  public static String findNextNodeOfParallel(Map<SceneGraphNodeDTO, List<Pair<String, SceneGraphNodeDTO>>> targetEdgesMap, SceneGraphNodeDTO node) {
    SceneGraphNodeDTO parallelEndNode = findParallelEndNode(targetEdgesMap, node);
    // 没有并行结束节点说明没有下一个节点
    if (parallelEndNode == null) {
      return null;
    }
    // 找出并行结束节点的下级节点
    List<Pair<String, SceneGraphNodeDTO>> parallelEndSuccessors = targetEdgesMap.get(parallelEndNode);
    if (!CollectionUtils.isNotEmpty(parallelEndSuccessors)) {
      return null;
    }
    Assert.isTrue(parallelEndSuccessors.size() == 1, "并行结束节点只能有一个下级节点");
    SceneGraphNodeDTO right = parallelEndSuccessors.get(0).getRight();
    // 如果下级节点还是并行结束节点（并行嵌套），忽略
    if (StepType.PARALLEL_END.equals(right.getNodeType())) {
      return null;
    }
    return right.getNodeCode();
  }
}
