package com.iwhalecloud.bote.service.skill.impl.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.step.IfStep.BranchSpec;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphEdgeDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.dto.skill.ServiceNodeDiffTreeDTO;
import com.iwhalecloud.bote.dto.skill.StandardServiceDiffViewDTO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 计算流程或场景两次修改记录的差异
 *
 * @author qian.sisheng
 * @since 2024-11-4
 */
@Component
public class DiffOperLogHelper {

  /**
   * 构建差异视图对象
   *
   * @param nodes 节点列表
   * @param edges 边列表
   * @param oldNodes 节点列表
   * @param oldEdges 边列表
   * @return 差异视图
   */
  public StandardServiceDiffViewDTO buildDiffView(List<SceneGraphNodeDTO> nodes, List<SceneGraphEdgeDTO> edges, List<SceneGraphNodeDTO> oldNodes,
    List<SceneGraphEdgeDTO> oldEdges) {
    // 构造节点树
    List<SceneGraphNodeDTO> oldTreeNodes = new ArrayList<>();
    List<String> oldVisitedNodes = new ArrayList<>();
    treeNode(oldNodes, oldEdges, StepType.START, oldTreeNodes, new HashSet<>(), oldVisitedNodes);
    List<SceneGraphNodeDTO> treeNodes = new ArrayList<>();
    List<String> visitedNodes = new ArrayList<>();
    treeNode(nodes, edges, StepType.START, treeNodes, new HashSet<>(), visitedNodes);

    // 计算节点差异
    List<ServiceNodeDiffTreeDTO> nodeDifferences = diffGraphNode(oldTreeNodes, treeNodes);

    // 计算正在使用的节点
    List<SceneGraphNodeDTO> oldUseNodes = tileNode(oldTreeNodes);
    List<SceneGraphNodeDTO> useNodes = tileNode(treeNodes);

    StandardServiceDiffViewDTO view = new StandardServiceDiffViewDTO();
    view.setOldNodes(oldTreeNodes);
    view.setNodes(treeNodes);
    view.setNodeDifferences(nodeDifferences);
    view.setOldUnUseNodes(CollectionUtils.emptyIfNull(oldNodes).stream().filter(node -> !oldUseNodes.contains(node)).collect(Collectors.toList()));
    view.setUnUseNodes(CollectionUtils.emptyIfNull(nodes).stream().filter(node -> !useNodes.contains(node)).collect(Collectors.toList()));
    return view;
  }

  /**
   * 比较节点差异
   *
   * @param oldNodes 旧节点列表
   * @param nodes 新节点列表
   * @return 节点差异树
   */
  public List<ServiceNodeDiffTreeDTO> diffGraphNode(List<SceneGraphNodeDTO> oldNodes, List<SceneGraphNodeDTO> nodes) {
    List<ServiceNodeDiffTreeDTO> trees = new ArrayList<>();
    // 右边节点列表中上个已处理节点的索引
    int lastPos = -1;
    // 遍历左边节点列表
    for (SceneGraphNodeDTO oldNode : CollectionUtils.emptyIfNull(oldNodes)) {
      // 检查右边有没有对应的节点。只检查同级节点
      int pos = IterableUtils.indexOf(nodes, n -> oldNode.getNodeCode().equals(n.getNodeCode()));
      // 右边没有，表示删除
      if (pos == -1) {
        ServiceNodeDiffTreeDTO tree = ServiceNodeDiffTreeDTO.ofDeleted(oldNode);
        if (CollectionUtils.isNotEmpty(oldNode.getChildrenNodes())) {
          tree.setChildren(diffGraphNode(ListUtils.emptyIfNull(oldNode.getChildrenNodes()), Collections.emptyList()));
        }
        trees.add(tree);
        continue;
      }
      // 右边节点前面有尚未处理的节点，表示是新增的
      if (pos > lastPos + 1) {
        nodes.subList(lastPos + 1, pos).forEach(p -> {
          ServiceNodeDiffTreeDTO tree = ServiceNodeDiffTreeDTO.ofAdded(p);
          if (CollectionUtils.isNotEmpty(p.getChildrenNodes())) {
            tree.setChildren(diffGraphNode(Collections.emptyList(), ListUtils.emptyIfNull(p.getChildrenNodes())));
          }
          trees.add(tree);
        });
      }
      lastPos = pos;
      SceneGraphNodeDTO node = nodes.get(pos);
      // 检查节点配置是否相等
      boolean equal = diffNode(oldNode, node);
      ServiceNodeDiffTreeDTO tree = ServiceNodeDiffTreeDTO.of(oldNode, node, equal);
      // 递归比较子节点
      if (CollectionUtils.isNotEmpty(oldNode.getChildrenNodes()) || CollectionUtils.isNotEmpty(node.getChildrenNodes())) {
        tree.setChildren(diffGraphNode(ListUtils.emptyIfNull(oldNode.getChildrenNodes()), ListUtils.emptyIfNull(node.getChildrenNodes())));
      }
      trees.add(tree);
    }
    // 左边节点遍历完了，右边尚未处理的节点都是新增节点
    if (lastPos < nodes.size() - 1) {
      nodes.subList(lastPos + 1, nodes.size()).forEach(p -> {
        ServiceNodeDiffTreeDTO tree = ServiceNodeDiffTreeDTO.ofAdded(p);
        if (CollectionUtils.isNotEmpty(p.getChildrenNodes())) {
          tree.setChildren(diffGraphNode(Collections.emptyList(), ListUtils.emptyIfNull(p.getChildrenNodes())));
        }
        trees.add(tree);
      });
    }
    return trees;
  }

  private boolean diffNode(SceneGraphNodeDTO oldNode, SceneGraphNodeDTO node) {
    return Objects.equals(oldNode.getNodeName(), node.getNodeName()) && Objects.equals(node.getNodeData(), oldNode.getNodeData())
      && Objects.equals(node.getNodeType(), oldNode.getNodeType()) && Objects.equals(node.getX(), oldNode.getX())
      && Objects.equals(node.getY(), oldNode.getY()) && ListUtils.isEqualList(node.getPortsItems(), oldNode.getPortsItems());
  }

  public void treeNode(List<SceneGraphNodeDTO> nodes, List<SceneGraphEdgeDTO> edges, String targetCode, List<SceneGraphNodeDTO> treeNodes,
    Set<SceneGraphNodeDTO> parallelEndNodes, List<String> visitedNodes) {
    if (CollectionUtils.isEmpty(nodes) || CollectionUtils.isEmpty(edges)) {
      return;
    }
    SceneGraphNodeDTO node = findNode(nodes, targetCode);
    if (node == null) {
      return;
    }
    // 检查是否已经访问过该节点，避免回环
    if (visitedNodes.contains(node.getNodeCode())) {
      return;
    }
    // 记录当前节点为已访问
    visitedNodes.add(node.getNodeCode());
    if (StepType.PARALLEL_END.equals(node.getNodeType())) {
      parallelEndNodes.add(node);
      return;
    }
    treeNodes.add(node);
    if (isComplexNode(node)) {
      processComplexNode(nodes, edges, node, parallelEndNodes, visitedNodes);
      // 并行节点，需要添加一个结束节点
      if (StepType.PARALLEL.equals(node.getNodeType())) {
        Iterator<SceneGraphNodeDTO> iterator = parallelEndNodes.iterator();
        if (iterator.hasNext()) {
          SceneGraphNodeDTO next = iterator.next();
          treeNodes.add(next);
          parallelEndNodes.remove(next);
          processSimpleNode(nodes, edges, next, treeNodes, parallelEndNodes, visitedNodes);
        }
      }
    }
    else {
      processSimpleNode(nodes, edges, node, treeNodes, parallelEndNodes, visitedNodes);
    }
  }

  private SceneGraphNodeDTO findNode(List<SceneGraphNodeDTO> nodes, String targetCode) {
    if (StepType.START.equals(targetCode)) {
      return IterableUtils.find(nodes, p -> StepType.START.equals(p.getNodeType()));
    }
    else {
      return IterableUtils.find(nodes, p -> p.getNodeCode().equals(targetCode));
    }
  }

  private boolean isComplexNode(SceneGraphNodeDTO node) {
    return StepType.IF.equals(node.getNodeType()) || StepType.PARALLEL.equals(node.getNodeType());
  }

  private void processComplexNode(List<SceneGraphNodeDTO> nodes, List<SceneGraphEdgeDTO> edges, SceneGraphNodeDTO node,
    Set<SceneGraphNodeDTO> parallelEndNodes, List<String> visitedNodes) {
    List<SceneGraphEdgeDTO> sourceEdges = edges.stream().filter(p -> p.getSource().getCell().equals(node.getNodeCode())).collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(sourceEdges)) {
      List<SceneGraphNodeDTO> children = new ArrayList<>();
      List<BranchSpec> branches = getBranches(node);
      if (CollectionUtils.isEmpty(branches)) {
        return;
      }
      addElseBranchIfNeeded(sourceEdges, branches, node);
      for (BranchSpec branch : branches) {
        SceneGraphEdgeDTO edge = IterableUtils.find(sourceEdges, p -> branch.getBranchCode().equals(p.getSource().getPort()));
        if (edge == null) {
          continue;
        }
        SceneGraphNodeDTO virtual = createVirtualNode(branch);
        children.add(virtual);
        node.setChildrenNodes(children);
        List<SceneGraphNodeDTO> virtualChildren = new ArrayList<>();
        virtual.setChildrenNodes(virtualChildren);
        List<String> branchVisitedNodes = new ArrayList<>(visitedNodes);
        treeNode(nodes, edges, edge.getTarget().getCell(), virtualChildren, parallelEndNodes, branchVisitedNodes);
      }
    }
  }

  @Nullable
  private List<BranchSpec> getBranches(SceneGraphNodeDTO node) {
    Map<String, Object> nodeData = node.getNodeData();
    return JsonUtil.parseJson(JsonUtil.toJsonString(MapUtils.getObject(nodeData, "branches")), new TypeReference<List<BranchSpec>>() {
    });
  }

  private void addElseBranchIfNeeded(List<SceneGraphEdgeDTO> sourceEdges, List<BranchSpec> branches, SceneGraphNodeDTO node) {
    if (!StepType.PARALLEL.equals(node.getNodeType()) && IterableUtils.find(sourceEdges, p -> "branchElse".equals(p.getSource().getPort())) != null) {
      BranchSpec branch = new BranchSpec();
      branch.setBranchName("其它");
      branch.setBranchCode("branchElse");
      branches.add(branch);
    }
  }

  private SceneGraphNodeDTO createVirtualNode(BranchSpec branch) {
    SceneGraphNodeDTO virtual = new SceneGraphNodeDTO();
    virtual.setNodeCode(branch.getBranchCode());
    virtual.setNodeType("virtual");
    virtual.setNodeName(branch.getBranchName());
    return virtual;
  }

  private void processSimpleNode(List<SceneGraphNodeDTO> nodes, List<SceneGraphEdgeDTO> edges, SceneGraphNodeDTO node,
    List<SceneGraphNodeDTO> newNodes, Set<SceneGraphNodeDTO> parallelEndNodes, List<String> visitedNodes) {
    SceneGraphEdgeDTO edge = IterableUtils.find(edges, p -> p.getSource().getCell().equals(node.getNodeCode()));
    // 处理循环节点
    if (StepType.LOOP.equals(node.getNodeType())) {
      String loopStartNodeCode = node.getChildrenCodes().get(0);
      SceneGraphNodeDTO loopStartNode = IterableUtils.find(nodes, p -> loopStartNodeCode.equals(p.getNodeCode()));
      if (loopStartNode == null) {
        return;
      }
      List<SceneGraphNodeDTO> childrenNodes = new ArrayList<>();
      childrenNodes.add(loopStartNode);
      node.setChildrenNodes(childrenNodes);
      SceneGraphEdgeDTO sourceEdge = IterableUtils.find(edges, p -> p.getSource().getCell().equals(loopStartNode.getNodeCode()));
      if (sourceEdge != null) {
        treeNode(nodes, edges, sourceEdge.getTarget().getCell(), childrenNodes, parallelEndNodes, visitedNodes);
      }
      return;
    }
    if (edge != null) {
      treeNode(nodes, edges, edge.getTarget().getCell(), newNodes, parallelEndNodes, visitedNodes);
    }
  }

  /**
   * 将树形节点平铺
   *
   * @param treeNodes 树形节点
   * @return 平铺节点
   */
  public List<SceneGraphNodeDTO> tileNode(List<SceneGraphNodeDTO> treeNodes) {
    List<SceneGraphNodeDTO> nodes = new ArrayList<>();
    if (CollectionUtils.isEmpty(treeNodes)) {
      return nodes;
    }
    for (SceneGraphNodeDTO treeNode : treeNodes) {
      if (!"virtual".equals(treeNode.getNodeType())) {
        nodes.add(treeNode);
      }
      tileChildNode(treeNode, nodes);
    }
    return nodes;
  }

  private void tileChildNode(SceneGraphNodeDTO treeNode, List<SceneGraphNodeDTO> nodes) {
    if (CollectionUtils.isEmpty(treeNode.getChildrenNodes())) {
      return;
    }
    for (SceneGraphNodeDTO childrenNode : treeNode.getChildrenNodes()) {
      if (!"virtual".equals(childrenNode.getNodeType())) {
        nodes.add(childrenNode);
      }
      tileChildNode(childrenNode, nodes);
    }
  }
}
