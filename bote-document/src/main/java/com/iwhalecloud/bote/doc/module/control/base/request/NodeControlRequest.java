package com.iwhalecloud.bote.doc.module.control.base.request;

import com.iwhalecloud.bote.doc.consts.PermissionModeEnum;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.consts.SubjectTypeEnum;
import com.iwhalecloud.bote.doc.module.control.base.ControlRoleDict;
import com.iwhalecloud.bote.doc.module.control.base.ControlType;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder;
import com.iwhalecloud.bote.doc.module.control.base.SubjectBuilder.ControlSubject;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRoleManager;
import com.iwhalecloud.bote.doc.module.control.model.ControlRoleInfo;
import com.iwhalecloud.bote.doc.module.control.model.SimpleNodeInfo;
import com.iwhalecloud.bote.doc.module.control.service.LibraryPermissionInheritanceService;
import com.iwhalecloud.bote.doc.module.control.service.NodeRoleService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentPermissionService;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Setter;

/**
 * 节点控制执行器。
 *
 */
public class NodeControlRequest extends AbstractControlRequest {

  private final List<ControlSubject> controlSubjects;

  private final List<String> nodeIds;

  private final LibraryPermissionInheritanceService libraryPermissionService;
  private final IDocumentPermissionService documentPermissionService;

  @Setter
  private boolean shareTreeBuilding;

  public NodeControlRequest(List<ControlSubject> subjects, List<String> nodeIds) {
    this.controlSubjects = subjects;
    this.nodeIds = nodeIds;
    this.libraryPermissionService = SpringUtil.getBean(LibraryPermissionInheritanceService.class);
    this.documentPermissionService = SpringUtil.getBean(IDocumentPermissionService.class);
  }

  /**
   * 查找节点。
   *
   * @param simpleNodeInfos 节点列表
   * @param nodeId 节点ID
   * @return 节点信息
   */
  private static SimpleNodeInfo findNode(List<SimpleNodeInfo> simpleNodeInfos, String nodeId) {
    SimpleNodeInfo node = null;
    for (SimpleNodeInfo simpleNodeInfo : simpleNodeInfos) {
      if (simpleNodeInfo.getNodeId().equals(nodeId)) {
        node = simpleNodeInfo;
        break;
      }
    }
    return node;
  }

  /**
   * 按逆序查询父节点。
   *
   * @param simpleNodeInfos 节点信息列表
   * @param node 节点
   * @return 节点ID列表
   */
  private static List<String> findParentNodeIdFromBottom2Top(List<SimpleNodeInfo> simpleNodeInfos,
                                                             SimpleNodeInfo node) {
    // 按逆序查找父节点，从下到上
    List<String> parents = new ArrayList<>();
    // 查找所有配置了权限的父节点，从下到上
    findParentNodeRole(simpleNodeInfos, node, parents::add);
    return parents;
  }

  /**
   * 查找启用角色权限配置的父节点。
   *
   * @param simpleNodeInfos 节点列表
   * @param roleInfo 节点信息
   * @param parents 具有权限的父节点
   */
  private static void findParentNodeRole(List<SimpleNodeInfo> simpleNodeInfos,
                                         SimpleNodeInfo roleInfo, Consumer<String> parents) {
    for (SimpleNodeInfo simpleNodeInfo : simpleNodeInfos) {
      if (roleInfo.getParentId().equals(simpleNodeInfo.getNodeId())) {
        if (!simpleNodeInfo.isExtend()) {
          parents.accept(simpleNodeInfo.getNodeId());
        }
        findParentNodeRole(simpleNodeInfos, simpleNodeInfo, parents);
      }
    }
  }

  /**
   * 计算节点角色。
   *
   * @param nodeRoleMap 节点角色键值对
   * @param nodeId 节点ID
   * @param subjectList 成员所属的组织单元列表
   * @return 节点角色
   */
  private static Set<String> calNodeRoles(Map<String, List<ControlRoleInfo>> nodeRoleMap,
                                          String nodeId,
                                          List<ControlSubject> subjectList) {
    List<ControlRoleInfo> permissions = nodeRoleMap.get(nodeId);
    if (permissions == null) {
      return new HashSet<>();
    }
    // 节点角色对应的组织单元
    Map<String, List<ControlSubject>> roleSubjectMap = permissions.stream()
      .collect(Collectors.groupingBy(ControlRoleInfo::getRole,
        Collectors.mapping(SubjectBuilder::fromControlRole, Collectors.toList())));
    return roleSubjectMap.entrySet()
      .stream()
      .filter(entry -> subjectList.stream().anyMatch(m -> entry.getValue().contains(m)))
      .map(Map.Entry::getKey).collect(Collectors.toSet());
  }

  @Override
  public List<ControlSubject> getControlSubjects() {
    return this.controlSubjects;
  }

  @Override
  public List<String> getControlIds() {
    return this.nodeIds;
  }

  @Override
  public ControlType getType() {
    return ControlType.NODE;
  }

  /**
   * 获取路径父节点（从当前节点到根节点）
   */
  private static List<SimpleNodeInfo> getPathParentNodes(String nodeId, List<SimpleNodeInfo> nodeList) {
    List<SimpleNodeInfo> pathParentNodes = new ArrayList<>();
    SimpleNodeInfo currentNode = findNode(nodeList, nodeId);

    while (currentNode != null) {
      pathParentNodes.add(currentNode);
      if (currentNode.getParentId() == null) {
        break;
      }
      currentNode = findNode(nodeList, currentNode.getParentId());
    }

    return pathParentNodes;
  }

  @Override
  public ControlRoleDict execute() {
    List<SimpleNodeInfo> nodeList = getSimpleNodeInfos();
    if (nodeList == null || nodeList.isEmpty()) {
      return ControlRoleDict.create();
    }
    return executeWithNodePermissionLogic(nodeList);
  }

  /**
   * 使用文档权限逻辑执行权限获取
   */
  private ControlRoleDict executeWithNodePermissionLogic(List<SimpleNodeInfo> nodeList) {
    List<String> controlIds = getControlIds();
    if (controlIds.isEmpty()) {
      return ControlRoleDict.create();
    }
    // 批量获取权限信息，避免循环查询数据库
    return executeWithBatchPermissionLogic(nodeList, controlIds);
  }

  /**
   * 批量权限获取逻辑，优化性能
   */
  private ControlRoleDict executeWithBatchPermissionLogic(List<SimpleNodeInfo> nodeList, List<String> controlIds) {
    ControlRoleDict roleDict = ControlRoleDict.create();

    // 构建节点映射
    Map<String, SimpleNodeInfo> nodeMap = nodeList.stream()
      .collect(Collectors.toMap(SimpleNodeInfo::getNodeId, Function.identity(), (x, y) -> x));

    // 一次查询所有涉及节点的权限配置
    List<String> allRelNodeIdList = new ArrayList<>(nodeMap.keySet());
    // 批量获取所有节点的权限配置
    List<ControlRoleInfo> allControlRoleInfos = documentPermissionService.selectControlDocumentRoleInfoByControlIds(allRelNodeIdList);
    Map<String, List<ControlRoleInfo>> nodeRoleMap = allControlRoleInfos.stream()
      .collect(Collectors.groupingBy(ControlRoleInfo::getControlId));

    // 批量获取文档库继承权限
    Map<String, ControlRole> libraryInheritedRoles = getBatchLibraryInheritedRoles(controlIds);

    // 为每个节点计算权限
    for (String nodeId : controlIds) {
      SimpleNodeInfo nodeInfo = nodeMap.get(nodeId);
      if (nodeInfo == null) {
        continue;
      }
      ControlRole nodeRole = calculateNodeRoleWithBatchData(nodeInfo, nodeRoleMap, libraryInheritedRoles, nodeList);
      if (nodeRole != null) {
        ControlRole finalRole = calcRoleIfCreator(nodeInfo, nodeRole);
        roleDict.put(nodeId, finalRole);
      }
    }

    return roleDict;
  }

  private ControlRole calcRoleIfCreator(SimpleNodeInfo nodeInfo, ControlRole nodeRole) {
    if (nodeRole.isAdmin()) {
      return nodeRole;
    }
    // 非管理员角色，判断是否是文档的创建人
    boolean isCreator = this.getControlSubjects().stream()
      .anyMatch(subject -> Objects.equals(subject.getSubjectId(), nodeInfo.getCreatorId())
        && Objects.equals(subject.getSubjectType(), SubjectTypeEnum.USER));
    // 如果是创建者，且拥有编辑权限，权限提升至Owner
    if (isCreator && Objects.equals(nodeRole.getRoleTag(), DocRoleEnum.DOC_EDIT.getCode())) {
      return ControlRoleManager.parseNodeRole(DocRoleEnum.DOC_OWNER.getCode());
    }
    return nodeRole;
  }

  /**
   * 批量获取文档库继承权限
   */
  private Map<String, ControlRole> getBatchLibraryInheritedRoles(List<String> nodeIds) {
    return libraryPermissionService.batchGetLibraryInheritedRoles(nodeIds, getControlSubjects());
  }

  /**
   * 使用批量数据计算节点权限
   */
  private ControlRole calculateNodeRoleWithBatchData(SimpleNodeInfo nodeInfo,
                                                     Map<String, List<ControlRoleInfo>> nodeRoleMap,
                                                     Map<String, ControlRole> libraryInheritedRoles,
                                                     List<SimpleNodeInfo> nodeList) {
    String nodeId = nodeInfo.getNodeId();
    // 这里计算时，不考虑节点创建人作为管理权限，统一使用继承或者配置的权限
    // 获取节点的权限模式
    Integer permissionMode = nodeInfo.getPermissionMode();
    boolean isInheritMode = Objects.equals(permissionMode, PermissionModeEnum.INHERIT.getCode());

    if (isInheritMode) {
      // 继承模式：从文档库和上级目录继承权限
      return calculateInheritPermissionWithBatchData(nodeId, nodeRoleMap, libraryInheritedRoles, nodeList);
    }
    else {
      // 独立模式：直接获取节点权限
      return calculateDirectPermissionWithBatchData(nodeId, nodeRoleMap);
    }
  }

  /**
   * 使用批量数据计算继承权限
   */
  private ControlRole calculateInheritPermissionWithBatchData(String nodeId,
                                                              Map<String, List<ControlRoleInfo>> nodeRoleMap,
                                                              Map<String, ControlRole> libraryInheritedRoles,
                                                              List<SimpleNodeInfo> nodeList) {
    // 1. 检查节点是否有直接配置的权限
    ControlRole directRole = calculateDirectPermissionWithBatchData(nodeId, nodeRoleMap);
    if (directRole != null) {
      return directRole;
    }

    // 2. 获取继承权限（从上级目录和文档库）
    return calculateInheritPermissionOnlyWithBatchData(nodeId, nodeRoleMap, libraryInheritedRoles, nodeList);
  }

  /**
   * 使用批量数据计算纯继承权限
   */
  private ControlRole calculateInheritPermissionOnlyWithBatchData(String nodeId,
                                                                  Map<String, List<ControlRoleInfo>> nodeRoleMap,
                                                                  Map<String, ControlRole> libraryInheritedRoles,
                                                                  List<SimpleNodeInfo> nodeList) {
    // 1. 从上级目录继承权限（由近及远，继承最近的父节点权限）
    List<SimpleNodeInfo> pathParentNodes = getPathParentNodes(nodeId, nodeList);
    // 移除当前节点
    pathParentNodes.removeIf(item -> Objects.equals(nodeId, item.getNodeId()));

    if (!pathParentNodes.isEmpty()) {
      // 由近及远遍历父节点，默认继承离的最近的一个父节点的权限
      for (SimpleNodeInfo parentNode : pathParentNodes) {
        ControlRole parentRole = calculateDirectPermissionWithBatchData(parentNode.getNodeId(), nodeRoleMap);
        if (parentRole != null) {
          return parentRole;
        }
        if (!parentNode.isExtend()) {
          return null;
        }
      }
    }
    // 2. 从文档库继承权限
    return libraryInheritedRoles.get(nodeId);
  }

  /**
   * 使用批量数据计算直接权限
   */
  private ControlRole calculateDirectPermissionWithBatchData(String nodeId,
                                                             Map<String, List<ControlRoleInfo>> nodeRoleMap) {
    // 计算用户在该节点上的角色
    Set<String> roles = calNodeRoles(nodeRoleMap, nodeId, getControlSubjects());
    if (roles.isEmpty()) {
      return null;
    }
    return ControlRoleManager.getTopNodeRole(roles);
  }

  /**
   * 查询权限控制涉及的所有节点，包含其所有上级节点
   *
   * @return 节点信息
   */
  private List<SimpleNodeInfo> getSimpleNodeInfos() {
    // 查询节点对应的父节点。
    // 为了提高性能，一次性查询所有父节点以获取当前节点的所有上级。
    NodeRoleService nodeRoleService = SpringUtil.getBean(NodeRoleService.class);
    List<SimpleNodeInfo> nodeList = nodeRoleService.getNodeInfoWithPermissionStatus(nodeIds);
    // 不是共享树构建，或者共享节点在第一级
    if (!shareTreeBuilding || nodeList.size() == getControlIds().size()) {
      return nodeList;
    }
    // 共享树允许故障的Ghost节点保留权限，
    // 因此不能从上到下计算，而是从共享节点向下计算以避免被直接截断
    // 共享节点在根节点
    SimpleNodeInfo node = findNode(nodeList, getControlIds().get(0));
    if (node == null) {
      return null;
    }
    if (node.isExtend()) {
      // 查找指定权限的最近父节点并截断之前的父节点
      List<String> reverseParentNodeIds = findParentNodeIdFromBottom2Top(nodeList, node);
      if (reverseParentNodeIds.isEmpty()) {
        return nodeList;
      }
      int i = 0;
      for (SimpleNodeInfo simpleNodeInfo : nodeList) {
        if (simpleNodeInfo.getNodeId().equals(reverseParentNodeIds.get(0))) {
          break;
        }
        i++;
      }
      return nodeList.subList(i, nodeList.size());
    }
    else {
      // 共享节点已设置权限，截断所有上级节点
      return nodeList.subList(nodeList.size() - getControlIds().size(), nodeList.size());
    }
  }
}
