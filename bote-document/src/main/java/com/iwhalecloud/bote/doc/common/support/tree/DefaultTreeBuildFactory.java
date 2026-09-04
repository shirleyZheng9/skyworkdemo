package com.iwhalecloud.bote.doc.common.support.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;

/**
 * <p>
 * default recursive tool, used to traverse nodes that have parent-child relationships.
 * such as menu trees, dictionary trees, and so on.
 * </p>
 */
public class DefaultTreeBuildFactory<T extends Tree<T>> extends AbstractTreeBuildFactory<T> {

  /**
   * the root node's id.
   */
  public static final String ROOT_PARENT_ID = "0";

  private String rootNode = ROOT_PARENT_ID;

  public DefaultTreeBuildFactory() {
  }

  public DefaultTreeBuildFactory(String rootNode) {
    this.rootNode = rootNode;
  }

  /**
   * query a collection of child nodes.
   *
   * @param node node to be queried
   * @param childNodeLists the child nodes of the queried node
   * @param parentIdToChildrenMap 父节点ID到子节点列表的映射，用于优化查找性能
   */
  private void buildChildNodes(T node, List<T> childNodeLists, Map<String, List<T>> parentIdToChildrenMap) {
    if (node == null || parentIdToChildrenMap == null) {
      return;
    }

    List<T> nodeSubLists = getSubChildLevelOne(node, parentIdToChildrenMap);

    if (CollectionUtils.isNotEmpty(nodeSubLists)) {
      for (T nodeSubList : nodeSubLists) {
        buildChildNodes(nodeSubList, new ArrayList<>(), parentIdToChildrenMap);
      }
    }

    childNodeLists.addAll(nodeSubLists);
    node.setChildrenNodes(childNodeLists);
  }

  /**
   * gets the node's child nodes.
   * 使用 HashMap 优化，将 O(n) 查找降为 O(1)
   *
   * @param node the node to be queried
   * @param parentIdToChildrenMap 父节点ID到子节点列表的映射
   * @return 子节点列表
   */
  private List<T> getSubChildLevelOne(T node, Map<String, List<T>> parentIdToChildrenMap) {
    if (parentIdToChildrenMap == null || node == null) {
      return new ArrayList<>();
    }
    String nodeId = node.getNodeId();
    List<T> children = parentIdToChildrenMap.get(nodeId);
    return children != null ? children : new ArrayList<>();
  }

  /**
   * 预先构建 parentId -> List<children> 的映射，优化后续查找性能
   * 将 O(n) 查找降为 O(1)
   *
   * @param nodes 节点列表
   * @return 父节点ID到子节点列表的映射
   */
  private Map<String, List<T>> buildParentIdToChildrenMap(List<T> nodes) {
    if (CollectionUtils.isEmpty(nodes)) {
      return new HashMap<>();
    }

    Map<String, List<T>> map = new HashMap<>(nodes.size());
    for (T node : nodes) {
      String parentId = node.getNodeParentId();
      if (parentId != null) {
        map.computeIfAbsent(parentId, k -> new ArrayList<>()).add(node);
      }
    }
    return map;
  }

  @Override
  protected List<T> beforeBuild(List<T> nodes) {
    // By default, no preprocessing before build.
    return nodes;
  }

  @Override
  protected List<T> executeBuilding(List<T> nodes) {
    if (CollectionUtils.isEmpty(nodes)) {
      return nodes;
    }

    // 预先构建 parentId -> List<children> 的映射，优化后续查找性能
    Map<String, List<T>> parentIdToChildrenMap = buildParentIdToChildrenMap(nodes);

    // 只对根节点进行构建，子节点会在递归中自动构建
    // 这样可以避免重复构建，提升性能
    for (T treeNode : nodes) {
      String parentId = treeNode.getNodeParentId();
      // 只处理根节点（parentId 等于 rootNode）
      if (rootNode.equals(parentId)) {
        this.buildChildNodes(treeNode, new ArrayList<>(), parentIdToChildrenMap);
      }
    }
    return nodes;
  }

  @Override
  protected List<T> afterBuild(List<T> nodes) {
    //remove all secondary nodes
    List<T> results = new ArrayList<>();
    for (T node : nodes) {
      if (node.getNodeParentId().equals(rootNode)) {
        results.add(node);
      }
    }
    return results;
  }
}
