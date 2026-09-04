package com.iwhalecloud.bote.doc.common.support.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 基于单向链表的节点排序辅助类
 *
 * @author Aiqing
 * @since 2025/9/8
 */
public final class NodeSortHelper {
  private NodeSortHelper() {
  }

  /**
   * 对节点按照前置节点关系进行排序
   *
   * @param nodeList 节点列表
   * @return 排序后的节点ID列表
   */
  public static List<String> sortNodeAtSameLevel(List<? extends SortableNode> nodeList) {
    return sortNodeAtSameLevel(nodeList, null);
  }

  public static List<String> sortNodeAtSameLevel(List<? extends SortableNode> nodeList, Predicate<SortableNode> filter) {
    // 空值检查
    if (nodeList == null || nodeList.isEmpty()) {
      return new ArrayList<>();
    }
    // 如果节点数量超过1000，直接返回节点ID列表，避免深度递归
    if (nodeList.size() > 1000) {
      return nodeList.stream().map(SortableNode::getNodeId).collect(Collectors.toList());
    }
    List<SortableNode> firstNodes = nodeList.stream()
      .filter(i -> i.getPreNodeId() == null)
      .collect(Collectors.toList());
    Map<String, List<SortableNode>> preNodeIdToNodesMap = new LinkedHashMap<>();
    for (SortableNode node : nodeList) {
      String preNodeId = node.getPreNodeId();
      List<SortableNode> sufNodes =
        preNodeIdToNodesMap.computeIfAbsent(preNodeId, k -> new ArrayList<>());
      sufNodes.add(node);
    }
    List<String> sortedNodeIds = new ArrayList<>();
    sufNodeRecurrence(firstNodes, filter, preNodeIdToNodesMap, sortedNodeIds::add);
    if (sortedNodeIds.size() == nodeList.size()) {
      return sortedNodeIds;
    }
    List<String> nodeIdList = nodeList.stream().map(SortableNode::getNodeId).collect(Collectors.toList());
    nodeList.stream()
      .filter(i -> i.getPreNodeId() != null && !nodeIdList.contains(i.getPreNodeId()))
      .forEach(node -> {
        List<SortableNode> suffixNodes = Collections.singletonList(node);
        sufNodeRecurrence(suffixNodes, filter, preNodeIdToNodesMap, sortedNodeIds::add);
      });
    return sortedNodeIds;
  }

  private static void sufNodeRecurrence(List<SortableNode> nodes,
                                        Predicate<SortableNode> filter,
                                        Map<String, List<SortableNode>> preNodeIdToNodesMap,
                                        Consumer<String> action) {
    nodes.stream()
      .filter(item -> filter == null || filter.test(item))
      .map(SortableNode::getNodeId)
      .forEach(action);
    for (SortableNode node : nodes) {
      String nodeId = node.getNodeId();
      if (preNodeIdToNodesMap.containsKey(nodeId)) {
        List<SortableNode> sufNodes = preNodeIdToNodesMap.get(nodeId);
        sufNodeRecurrence(sufNodes, filter, preNodeIdToNodesMap, action);
      }
    }
  }
}
