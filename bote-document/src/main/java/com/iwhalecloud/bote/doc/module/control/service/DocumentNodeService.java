package com.iwhalecloud.bote.doc.module.control.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.cache.DocumentLibraryCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.exception.DocumentNodeAccessDenyException;
import com.iwhalecloud.bote.doc.common.exception.NodeOperationDeniedException;
import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.common.support.tree.DefaultTreeBuildFactory;
import com.iwhalecloud.bote.doc.common.support.tree.NodeSortHelper;
import com.iwhalecloud.bote.doc.consts.DocLockConsts;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.DocRoleEnum;
import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import com.iwhalecloud.bote.doc.consts.TargetTypeEnum;
import com.iwhalecloud.bote.doc.consts.VisibilityScopeEnum;
import com.iwhalecloud.bote.doc.integration.PortalOrgIntegration;
import com.iwhalecloud.bote.doc.module.control.dto.MoveLibraryInfoDTO;
import com.iwhalecloud.bote.doc.module.control.vo.DocFolderSubsVo;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentReleasedDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentNodeMoveLibraryOpRo;
import com.iwhalecloud.bote.doc.module.document.dto.NodeMoveLibraryDocumentDataOpRo;
import com.iwhalecloud.bote.doc.module.document.dto.request.DocFolderSubsQueryParams;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentPermissionMapper;
import com.iwhalecloud.bote.doc.module.person.entity.UserHomepagePinEntity;
import com.iwhalecloud.bote.doc.module.person.mapper.FavoriteMapper;
import com.iwhalecloud.bote.doc.module.person.mapper.HomepageMapper;
import com.iwhalecloud.bote.doc.module.control.base.ControlRoleDict;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRoleManager;
import com.iwhalecloud.bote.doc.module.control.mapper.DocumentNodeMapper;
import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bote.doc.module.control.vo.NodeInfoTreeVo;
import com.iwhalecloud.bote.doc.module.control.vo.NodeInfoVo;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentNodeMoveOpRo;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentNodeTreeDTO;
import com.iwhalecloud.bote.doc.module.document.dto.NodePathDTO;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocumentEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentMapper;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryPermissionService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.stream.Collectors.toList;

/**
 * 文档节点相关查询方法
 *
 * @author Aiqing
 * @since 2025/8/18
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class DocumentNodeService {

  private static final Logger logger = LoggerFactory.getLogger(DocumentNodeService.class);

  private final ControlTemplate controlTemplate;

  private final IDcUserService dcUserService;

  private final DocumentNodeMapper documentNodeMapper;

  private final DocumentMapper documentMapper;

  private final DocumentLibraryCache documentLibraryCache;

  private final DistributedLockFactory distributedLockFactory;

  private final DocumentLibraryPermissionService documentLibraryPermissionService;

  private final DocumentPermissionMapper documentPermissionMapper;

  private final PortalOrgIntegration portalOrgIntegration;

  private final HomepageMapper homepageMapper;

  private final FavoriteMapper favoriteMapper;

  private static List<NodeInfoTreeVo> customSortTree(ControlRoleDict roleDict, List<NodeInfoTreeVo> treeList) {
    // 使用HashMap替代ArrayList.indexOf()，将O(n²)复杂度降为O(n log n)
    List<String> customOrder = new ArrayList<>(roleDict.keySet());
    Map<String, Integer> orderMap = new HashMap<>(customOrder.size());
    for (int i = 0; i < customOrder.size(); i++) {
      orderMap.put(customOrder.get(i), i);
    }
    return ListUtils.emptyIfNull(treeList).stream()
      .sorted(Comparator.comparingInt(node -> orderMap.getOrDefault(node.getNodeId(), Integer.MAX_VALUE)))
      .collect(toList());
  }

  /**
   * 获取文档库的根节点
   *
   * @param libraryId 文档库ID
   * @return 根节点ID
   */
  public String getRootNodeIdByLibraryId(String libraryId, Long tenantId, Long spaceId) {
    return documentNodeMapper.selectRootNodeIdByLibraryId(libraryId, tenantId, spaceId);
  }

  /**
   * 获取文档库的根节点
   *
   * @param libraryId 文档库ID
   * @return 根节点ID
   */
  public DcDocumentEntity getRootNodeIdDcDocumentEntityByLibraryId(String libraryId, Long tenantId, Long spaceId) {
    return documentNodeMapper.selectRootNodeIdDcDocumentEntityByLibraryId(libraryId, tenantId, spaceId);
  }

  /**
   * 查询文档节点的所有上级路径
   *
   * @param libraryId 文档库ID
   * @param nodeId 文档节点ID
   * @return 所有父级的路径
   */
  public List<NodePathDTO> getParentPathByNodeId(String libraryId, String nodeId) {
    List<NodeBaseInfoDTO> parentPathNodes = this.getParentPathNodes(nodeId, true);
    return parentPathNodes.stream().map(i -> {
      if (i.getNodeType().equals(DocumentTypeEnum.ROOT.getCode())) {
        String libraryName = documentLibraryCache.getLibraryName(libraryId);
        // 此处修改先返回文档库的ID
        return new NodePathDTO(libraryId, libraryName);
      }
      return new NodePathDTO(i.getNodeId(), i.getNodeName());
    }).collect(toList());
  }

  /**
   * 查询文档库某节点下的节点树
   *
   * @param libraryId 文档库ID
   * @param nodeId 节点ID
   * @param userId 用户ID
   * @param depth 查询深度
   * @return 文档节点树
   */
  public NodeInfoTreeVo getNodeTree(String libraryId, String nodeId, Long userId, int depth, Long tenantId,  Long envTenantId, Long spaceId) {
    List<String> nodeIds = this.getNodeIdsInNodeTree(nodeId, depth);
    return this.getNodeInfoTreeByNodeIds(libraryId, userId, nodeIds, tenantId, envTenantId, spaceId);
  }

  /**
   * 获取指定节点树中的所有节点ID
   *
   * @param nodeId 起始节点ID
   * @param depth 查询深度，-1表示查询所有层级
   * @return 节点ID列表
   */
  public List<String> getNodeIdsInNodeTree(String nodeId, Integer depth) {
    return this.getNodeIdsInNodeTree(Collections.singletonList(nodeId), depth);
  }

  /**
   * 根据节点ID列表构建节点树
   *
   * @param libraryId 文档库ID
   * @param userId 用户ID
   * @param nodeIds 节点ID列表
   * @return 构建的节点树
   * @throws DocumentNodeAccessDenyException 当用户无权限访问任何节点时抛出
   */
  public NodeInfoTreeVo getNodeInfoTreeByNodeIds(String libraryId, Long userId, List<String> nodeIds, Long tenantId, Long envTenantId, Long spaceId) {
    StopWatch stopWatch = StopWatch.createStarted();
    ControlRoleDict roleDict = controlTemplate.fetchNodeTreeNode(libraryId, userId, nodeIds);
    stopWatch.stop();
    if (logger.isDebugEnabled()) {
      logger.debug("校验权限通过，耗时:{}ms", stopWatch.getDuration().toMillis());
    }
    if (roleDict.isEmpty()) {
      throw new DocumentNodeAccessDenyException();
    }
    List<List<String>> partitioned = ListUtils.partition(new ArrayList<>(roleDict.keySet()), 1000);
    // 使用 parallelStream 并发查询提升性能
    List<NodeInfoTreeVo> treeList = partitioned.parallelStream()
      .flatMap(partition -> documentNodeMapper.selectNodeInfoTreeByNodeIds(partition, userId, tenantId, envTenantId).stream()).toList();
    // 节点切换到内存自定义排序
    List<NodeInfoTreeVo> sortedList = new ArrayList<>(customSortTree(roleDict, treeList));

    setRole(sortedList, roleDict);

    String rootNodeId = documentNodeMapper.selectRootNodeIdByLibraryId(libraryId, tenantId, spaceId);
    boolean hasRootNode = sortedList.stream()
      .anyMatch(item -> Objects.equals(item.getNodeType(), DocumentTypeEnum.ROOT.getCode()));
    if (!hasRootNode) {
      NodeInfoTreeVo rootNode = new NodeInfoTreeVo();
      rootNode.setNodeFavorite(false);
      rootNode.setLibraryId(libraryId);
      rootNode.setPreNodeId(null);
      rootNode.setNodeId(rootNodeId);
      rootNode.setParentId("0");
      rootNode.setNodeType(DocumentTypeEnum.ROOT.getCode());
      sortedList.add(rootNode);
    }
    // 处理断掉的父ID，如果父ID不存在，则全部挂载root节点下
    Map<String, NodeInfoTreeVo> nodeMap = sortedList.stream()
      .collect(Collectors.toMap(NodeInfoTreeVo::getNodeId, Function.identity(), (x, y) -> y));
    sortedList.forEach(item -> {
      String parentId = item.getParentId();
      if (StringUtils.isNotBlank(parentId) && nodeMap.get(parentId) == null && !Objects.equals(
        item.getNodeType(), DocumentTypeEnum.ROOT.getCode())) {
        item.setParentId(rootNodeId);
      }
    });
    List<NodeInfoTreeVo> nodeTrees = new DefaultTreeBuildFactory<NodeInfoTreeVo>().doTreeBuild(sortedList);
    NodeInfoTreeVo result = CollectionUtils.isNotEmpty(nodeTrees) ? nodeTrees.getFirst() : null;
    if (result != null) {
      fillFileCount(result);
    }
    return result;
  }

  /**
   * 填充文件数量
   *
   * @param node 节点
   * @return 文件数量
   */
  private int fillFileCount(NodeInfoTreeVo node) {
    List<NodeInfoTreeVo> children = node.getChildren();
    // 文件节点不需要统计，直接返回1用于父节点累加
    if (CollectionUtils.isEmpty(children)) {
      if (!isFileNode(node.getNodeType())) {
        node.setFileCount(0);
        node.setFileTotalCount(0);
        return 0;
      }
      return 1;
    }
    int directFileCount = 0;
    int totalFileCount = 0;
    int directFolderCount = 0;
    for (NodeInfoTreeVo child : children) {
      int childTotal = fillFileCount(child);
      totalFileCount += childTotal;
      if (isFileNode(child.getNodeType())) {
        directFileCount++;
      }
      if (DocumentTypeEnum.FOLDER.getCode().equals(child.getNodeType())) {
        directFolderCount++;
      }
    }
    boolean isFolder = DocumentTypeEnum.FOLDER.getCode().equals(node.getNodeType());
    boolean isRoot = DocumentTypeEnum.ROOT.getCode().equals(node.getNodeType());
    node.setFileTotalCount(totalFileCount);
    // ROOT和FOLDER都显示：直接子文件夹数 + 直接子文件数
    node.setFileCount(isFolder || isRoot ? directFolderCount + directFileCount : totalFileCount);
    return totalFileCount;
  }

  /**
   * 判断是否为文件节点
   */
  private boolean isFileNode(String nodeType) {
    return !DocumentTypeEnum.FOLDER.getCode().equals(nodeType)
      && !DocumentTypeEnum.ROOT.getCode().equals(nodeType);
  }

  private <T extends NodeInfoVo> void setRole(List<T> list, ControlRoleDict roleDict) {
    for (T node : list) {
      node.setRole(roleDict.get(node.getNodeId()).getRoleTag());
    }
  }

  private List<String> getNodeIdsInNodeTree(List<String> nodeIds, Integer depth) {
    Set<String> nodeIdSet = new LinkedHashSet<>(nodeIds);
    // 此处所有节点均可作为父节点
    List<String> parentIds = nodeIds.stream().distinct().toList();
    while (!parentIds.isEmpty() && depth != 0) {
      // 如果 parentIds 很大，分批查询并并发执行以提升性能
      List<DocumentNodeTreeDTO> subNode = ListUtils.partition(parentIds, 1000).parallelStream().flatMap(partition -> documentNodeMapper.selectDocumentNodeTreeDTOByParentIdIn(partition).stream()).toList();
      if (subNode.isEmpty()) {
        break;
      }
      parentIds = subNode.stream().map(DocumentNodeTreeDTO::getNodeId).filter(i -> !nodeIdSet.contains(i)).toList();
      Map<String, List<DocumentNodeTreeDTO>> parentIdToSubNodeMap = subNode.stream()
        .collect(Collectors.groupingBy(DocumentNodeTreeDTO::getParentId));
      for (List<DocumentNodeTreeDTO> sub : parentIdToSubNodeMap.values()) {
        nodeIdSet.addAll(NodeSortHelper.sortNodeAtSameLevel(sub));
      }
      depth--;
    }
    return new ArrayList<>(nodeIdSet);
  }

  /**
   * 获取指定节点的所有父级路径节点
   *
   * @param nodeIds 节点ID列表
   * @param includeRootNode 是否包含根节点
   * @return 父级路径节点列表
   */
  public List<NodeBaseInfoDTO> getParentPathNodes(List<String> nodeIds, boolean includeRootNode) {
    Map<String, NodeBaseInfoDTO> nodeIdToNodeMap = new LinkedHashMap<>();
    Set<String> parentIds = new HashSet<>(nodeIds);
    while (!parentIds.isEmpty()) {
      List<NodeBaseInfoDTO> nodes = documentNodeMapper.selectNodeBaseInfosByNodeIds(parentIds);
      parentIds = new HashSet<>();
      for (NodeBaseInfoDTO node : nodes) {
        if (nodeIdToNodeMap.containsKey(node.getNodeId()) || (!includeRootNode && node.getNodeType()
          .equals(DocumentTypeEnum.ROOT.getCode()))) {
          continue;
        }
        if (!nodeIdToNodeMap.containsKey(node.getParentId())) {
          parentIds.add(node.getParentId());
        }
        nodeIdToNodeMap.put(node.getNodeId(), node);
      }
    }
    return new ArrayList<>(nodeIdToNodeMap.values());
  }

  /**
   * 获取指定节点的所有父级路径节点
   *
   * @param nodeId 节点ID
   * @param includeRootNode 是否包含根节点
   * @return 父级路径节点列表， 按照 从 子 到父的顺序： 当前节点 -> 父节点A -> 父节点B
   */
  public List<NodeBaseInfoDTO> getParentPathNodes(String nodeId, boolean includeRootNode) {
    return this.getParentPathNodes(Collections.singletonList(nodeId), includeRootNode);
  }

  /**
   * 获取指定节点的子节点列表
   *
   * @param libraryId 文档库ID
   * @param memberId 用户ID
   * @param nodeId 父节点ID
   * @param documentType 筛选的文档类型，null表示不筛选
   * @return 子节点信息列表
   */
  public List<NodeInfoVo> getChildNodesByNodeId(String libraryId, Long memberId, String nodeId,
    DocumentTypeEnum documentType) {
    List<DocumentNodeTreeDTO> subNode = documentNodeMapper.selectDocumentNodeTreeDTOByParentIdIn(
      Collections.singleton(nodeId));
    if (subNode.isEmpty()) {
      return new ArrayList<>();
    }
    List<String> subNodeIds = NodeSortHelper.sortNodeAtSameLevel(subNode,
      (node) -> documentType == null || Objects.equals(((DocumentNodeTreeDTO) node).getNodeType(),
        documentType.name()));
    return this.getNodeInfoByNodeIds(libraryId, memberId, subNodeIds);
  }

  /**
   * 根据节点ID列表获取节点信息
   *
   * @param libraryId 文档库ID
   * @param userId 用户ID
   * @param nodeIds 节点ID列表
   * @return 节点信息列表，按照nodeIds的顺序排序
   */
  public List<NodeInfoVo> getNodeInfoByNodeIds(String libraryId, Long userId, List<String> nodeIds) {
    if (CollectionUtils.isEmpty(nodeIds)) {
      return new ArrayList<>();
    }
    ControlRoleDict roleDict = controlTemplate.fetchNodeRole(libraryId, userId, nodeIds);
    if (roleDict.isEmpty()) {
      return new ArrayList<>();
    }
    // 批量查询节点信息
    List<NodeInfoVo> infos = documentNodeMapper.selectNodeInfoByNodeIds(roleDict.keySet(), userId);
    // 节点切换到内存自定义排序
    List<String> customOrder = new ArrayList<>(roleDict.keySet());
    List<NodeInfoVo> sortedList = ListUtils.emptyIfNull(infos).stream()
      .sorted(Comparator.comparingInt(node -> customOrder.indexOf(node.getNodeId()))).toList();

    setRole(sortedList, roleDict);
    return sortedList;
  }

  /**
   * 查询文档节点ID的上级节点ID, 含当前节点
   *
   * @param nodeId 节点ID
   * @return 上级节点ID
   */
  public List<String> getPathParentNode(String nodeId) {
    List<NodeBaseInfoDTO> parentPathNodes = this.getParentPathNodes(nodeId, false);
    return parentPathNodes.stream().map(NodeBaseInfoDTO::getNodeId).collect(toList());
  }

  /**
   * 定位指定节点，返回包含该节点路径的树结构
   * <p>该方法会找到指定节点的所有父节点路径，并构建包含这些路径的树结构，
   * 用于在前端展示时定位和展开到指定节点。</p>
   *
   * @param libraryId 文档库ID
   * @param userId 用户ID
   * @param nodeId 要定位的节点ID
   * @return 包含节点路径的树结构，如果节点无权限访问则返回null
   */
  public NodeInfoTreeVo position(String libraryId, Long userId, String nodeId, Long tenantId, Long envTenantId, Long spaceId) {
    // 获取节点的所有父节点
    List<String> parentNodeIds = this.getPathParentNode(nodeId);
    // 没有父节点应该报错，
    // 但定位节点不需要直接返回空。
    if (parentNodeIds.isEmpty()) {
      return null;
    }
    // 检查父节点是否有访问权限，否则将无法定位。
    ControlRoleDict roleDict = controlTemplate.fetchNodeRole(libraryId, userId, parentNodeIds);
    if (roleDict.isEmpty()) {
      return null;
    }
    for (String parentNode : parentNodeIds) {
      if (!roleDict.containsKey(parentNode)) {
        return null;
      }
    }
    // 查询根节点
    String rootNodeId = this.getRootNodeIdByLibraryId(libraryId, tenantId, spaceId);
    parentNodeIds.addFirst(rootNodeId);
    parentNodeIds.remove(nodeId);
    // 父节点树节点加载。
    List<DocumentNodeTreeDTO> subNode = documentNodeMapper.selectDocumentNodeTreeDTOByParentIdIn(parentNodeIds);
    Map<String, List<DocumentNodeTreeDTO>> parentIdToSubNodeMap = subNode.stream()
      .collect(Collectors.groupingBy(DocumentNodeTreeDTO::getParentId));
    List<String> viewNodeIds = new ArrayList<>();
    viewNodeIds.add(rootNodeId);
    for (String parentId : parentNodeIds) {
      List<DocumentNodeTreeDTO> sub = parentIdToSubNodeMap.get(parentId);
      viewNodeIds.addAll(NodeSortHelper.sortNodeAtSameLevel(sub));
    }
    return this.getNodeInfoTreeByNodeIds(libraryId, userId, viewNodeIds, tenantId, envTenantId, spaceId);
  }

  /**
   * 根据节点ID获取单个节点信息
   *
   * @param nodeId 节点ID
   * @param role 用户对该节点的角色权限
   * @return 节点信息
   */
  public NodeInfoVo getNodeInfoByNodeId(String nodeId, ControlRole role) {
    NodeInfoVo nodeInfo = documentNodeMapper.selectNodeInfoByNodeId(nodeId);
    nodeInfo.setRole(role.getRoleTag());
    return nodeInfo;
  }

  /**
   * 删除文档节点
   *
   * @param libraryId 文档库ID
   * @param optUserId 操作用户
   * @param documentIds 文档ID, 可多个
   */
  @Transactional
  public void deleteDocumentNode(String libraryId, Long optUserId, String... documentIds) {
    // 去重
    List<String> idList = Arrays.stream(documentIds).distinct().collect(toList());
    for (String documentId : idList) {
      controlTemplate.checkNodePermission(libraryId, optUserId, documentId, NodePermission.REMOVE_NODE,
        DocumentPermConsts.ACCESS_DENIED_CALLBACK);
    }
    List<DcDocumentEntity> nodes = documentMapper.selectByLibraryIdAndDocumentIds(libraryId, idList);
    // 校验删除的是否有根节点
    boolean rootNodeExist = nodes.stream()
      .anyMatch(node -> node.getDocumentType().equals(DocumentTypeEnum.ROOT.getCode()));
    if (rootNodeExist) {
      throw new BssException("禁止删除root节点");
    }
    // 转换删除的节点权限，从继承权限转换为独立权限
    NodeRoleService nodeRoleService = SpringUtil.getBean(NodeRoleService.class);
    nodeRoleService.copyExtendNodeRoleIfExtend(optUserId, idList);
    // 获取所有下级节点进行删除
    List<String> nodeIds = this.getNodeIdsInNodeTree(idList, -1);
    if (CollectionUtils.isNotEmpty(nodeIds)) {
      // todo 删除扩展表记录
      // todo 删除文档的文件
      // todo 删除文档数据
      // todo 删除表格数据

      Collection<String> subNodeIds = CollectionUtils.disjunction(nodeIds, idList);
      if (!subNodeIds.isEmpty()) {
        documentMapper.rubbishByDocumentIds(libraryId, subNodeIds, optUserId);
      }
      // todo 删除文档附件
    }
    for (DcDocumentEntity node : nodes) {
      DistributedLock lock = distributedLockFactory.getBizLock(DocLockConsts.DOCUMENT_DELETE_NODE_LOCK,
        node.getParentId());
      try {
        if (lock.tryLock(2, TimeUnit.MINUTES)) {
          String nodeId = node.getDocumentId();
          documentMapper.rubbishByDocumentIds(libraryId, Collections.singleton(nodeId), optUserId);
          // 更新节点对应的前置节点， 将删除节点的前一个节点与后一个节点连接
          documentNodeMapper.updatePreNodeIdByJoinSelf(nodeId, node.getParentId());
          // 验证是否存在有效的置顶记录
          List<UserHomepagePinEntity> pinEntities = homepageMapper.selectActivePinsByTenantAndResource(
            node.getTenantId(), node.getDocumentId(), TargetTypeEnum.DOCUMENT.getCode(), SpaceContextHolder.getRequiredSpaceId());
          if (CollectionUtils.isNotEmpty(pinEntities)) {
            homepageMapper.updatePrevPinIdsForTenantUnpin(node.getTenantId(), node.getDocumentId(),
              TargetTypeEnum.DOCUMENT.getCode());
            homepageMapper.updatePinStatusToInvalidByTenant(node.getTenantId(), node.getDocumentId(),
              TargetTypeEnum.DOCUMENT.getCode());
          }
          favoriteMapper.deleteTenantFavoriteByTarget(node.getTenantId(), node.getDocumentId(),
            TargetTypeEnum.DOCUMENT.getCode(), SpaceContextHolder.getRequiredSpaceId());
          libraryCompute(node.getLibraryId());

        }
        else {
          throw new BssException("操作冲突");
        }
      }
      catch (InterruptedException e) {
        throw new BssException("操作冲突", e);
      }
      finally {
        lock.unlock();
      }
    }
  }

  /**
   * 移动文档节点
   *
   * @param userId 操作用户
   * @param opRo 请求参数
   * @return 变化的节点ID
   */
  @Transactional
  public List<String> move(Long userId, @Valid DocumentNodeMoveOpRo opRo) {
    String documentId = opRo.getDocumentId();
    DcDocumentEntity documentEntity = documentMapper.selectByDocumentId(opRo.getDocumentId());
    if (Objects.equals(documentEntity.getDocumentType(), DocumentTypeEnum.ROOT.getCode())) {
      throw new BssException("不允许操作此节点");
    }
    String preNodeId = opRo.getPreNodeId();
    if (opRo.getDocumentId().equalsIgnoreCase(preNodeId)) {
      throw new BssException("400", "非法参数");
    }
    if (opRo.getDocumentId().equalsIgnoreCase(opRo.getParentId())) {
      throw new BssException("400", "非法参数");
    }
    // 当此节点是文件夹时，禁止移动到子节点和后代节点中
    // 如果移动的是文件夹，校验文件夹的父ID不能是现有的下级节点
    if (documentEntity.getDocumentType().equals(DocumentTypeEnum.FOLDER.getCode())) {
      List<String> nodeIds = this.getNodeIdsInNodeTree(documentId, -1);
      if (CollectionUtils.isNotEmpty(nodeIds) && nodeIds.contains(opRo.getParentId())) {
        throw new BssException("400", "非法参数");
      }
    }
    String parentId = documentEntity.getParentId();

    // 记录数据变化的节点
    List<String> nodeIds = new ArrayList<>();
    nodeIds.add(documentId);
    if (!documentEntity.getParentId().equals(opRo.getParentId())) {
      // 跨文件夹移动
      parentId = opRo.getParentId();
      // 记录新旧位置的父节点
      nodeIds.add(documentEntity.getParentId());
      nodeIds.add(opRo.getParentId());
    }
    else {
      // 在同一级别排序，新旧前置节点相同，即没有发生移动
      if (Optional.ofNullable(preNodeId).orElse("")
        .equals(Optional.ofNullable(documentEntity.getPrevDocumentId()).orElse(""))) {
        return new ArrayList<>();
      }
    }
    // 记录新旧位置的下一个节点
    String suffixNodeId = documentNodeMapper.selectNodeIdByPreNodeId(documentId);
    nodeIds.add(suffixNodeId);

    DistributedLock lock = distributedLockFactory.getBizLock(DocLockConsts.DOCUMENT_MOVE_NODE_LOCK, parentId);
    try {
      if (lock.tryLock()) {
        doNodeMove(userId, documentEntity, documentId, parentId, preNodeId, nodeIds);
      }
      else {
        throw new BssException("操作频繁,请重试");
      }
    }
    finally {
      lock.unlock();
    }
    return nodeIds;
  }

  private void doNodeMove(Long userId, DcDocumentEntity documentEntity, String documentId, String parentId,
    String preNodeId, List<String> nodeIds) {
    // 更新后续节点的前置节点
    // 为该节点的前置节点 (A <- X <- C => A <- C)
    documentNodeMapper.updatePreNodeIdBySelf(documentEntity.getPrevDocumentId(), documentId,
      documentEntity.getParentId());
    // 更新移动前后节点的序列关系
    // (D <- E => D <- X <- E)
    String sufNodeId = documentNodeMapper.selectNodeIdByParentIdAndPreNodeId(parentId, preNodeId);
    if (sufNodeId != null) {
      nodeIds.add(sufNodeId);
      documentNodeMapper.updatePreNodeIdByNodeId(documentId, sufNodeId);
    }
    // 更新此节点的信息
    documentNodeMapper.updateInfoByNodeId(documentId, parentId, preNodeId, userId);
  }

  public NodeBaseInfoDTO queryBaseInfo(String nodeId) {
    return documentNodeMapper.selectNodeBaseInfoByNodeId(nodeId);
  }

  /**
   * 根据文档库ID查询所有文档非root节点ID
   *
   * @param libraryId 文档库ID
   * @return 文档节点ID列表
   */
  public List<String> selectWithoutRootByLibraryId(String libraryId) {
    return documentNodeMapper.selectWithoutRootByLibraryId(libraryId);
  }

  /**
   * 检查是否有父节点的权限 1. 个人文档库忽略检查 2. 无上级节点，需要检查文档库是否有可编辑权限 3. 检查是否有某个具体节点的编辑权限
   *
   * @param documentLibrary 文档库
   * @param userId 用户ID
   */
  public ControlRole checkParentNodeEditPermission(DocumentLibraryDTO documentLibrary, Long userId, String parentId, String documentId) {
    String visibilityScope = documentLibrary.getVisibilityScope();
    String libraryId = documentLibrary.getLibraryId();
    if (VisibilityScopeEnum.PRIVATE.getCode().equals(visibilityScope) && Objects.equals(userId,
      documentLibrary.getOwnerId())) {
      // 个人文档库
      return ControlRoleManager.parseNodeRole(DocRoleEnum.DOC_OWNER.getCode());
    }
    // 添加一个是否是文件文件本身的编辑权限
    if (documentId != null) { // 如果是文件重新上传那么需要判断是否具有文档的编辑权限或者是否为拥有者
      DocRoleEnum permission = queryDocumentMaxPermission(documentId);
      if (permission != null && permission.getLevel() <= DocRoleEnum.DOC_EDIT.getLevel()) {
        return ControlRoleManager.parseNodeRole(DocRoleEnum.DOC_EDIT.getCode());
      }
    }

    if (StringUtils.isEmpty(parentId)) {
      LibraryRoleEnum libraryRoleEnum = documentLibraryPermissionService.queryUserLibraryMaxRole(libraryId, userId);
      if (libraryRoleEnum.getLevel() > LibraryRoleEnum.EDIT.getLevel()) {
        throw new BssException("无权限对此文档库进行编辑操作");
      }
      return ControlRoleManager.parseNodeRole(libraryRoleEnum.getCode());
    }
    else {
      ControlRole controlRole = controlTemplate.fetchNodeRole(libraryId, userId, parentId);
      if (!controlRole.hasPermission(NodePermission.CREATE_NODE)) {
        throw new NodeOperationDeniedException();
      }
      return controlRole;
    }
  }

  private DocRoleEnum queryDocumentMaxPermission(String documentId) {
    DcDocumentEntity documentDTO = documentMapper.selectByDocumentId(documentId);
    if (documentDTO == null) {
      throw new BssException("文档不存在");
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Long spaceId = documentDTO.getSpaceId();
    List<OrgDTO> orgList = portalOrgIntegration.queryUserOrgList(spaceId, userId);
    List<Long> orgs = null;
    if (CollectionUtils.isNotEmpty(orgList)) {
      orgs = orgList.stream().map(OrgDTO::getOrgId).collect(toList());
    }
    if (SessionUtil.isSuperAdmin(userId)) {
      return DocRoleEnum.getByCode(DocRoleEnum.DOC_MANAGE.getCode());
    }
    if (documentDTO.getCreatorId().equals(userId)) {
      return DocRoleEnum.getByCode(DocRoleEnum.DOC_OWNER.getCode());
    }

    List<String> permissionTypes = documentPermissionMapper.selectUserPermissionByDocumentId(documentId,
      userId, orgs);
    if (CollectionUtils.isNotEmpty(permissionTypes)) {
      if (permissionTypes.contains(DocRoleEnum.DOC_MANAGE.getCode())) {
        return DocRoleEnum.getByCode(DocRoleEnum.DOC_MANAGE.getCode());
      }
      if (permissionTypes.contains(DocRoleEnum.DOC_EDIT.getCode())) {
        return DocRoleEnum.getByCode(DocRoleEnum.DOC_EDIT.getCode());
      }
    }
    return null;
  }

  private void libraryCompute(String libraryId) {
    DocumentLibraryDTO documentLibraryDTO = documentLibraryCache.getByLibraryId(libraryId);
    if (documentLibraryDTO != null) {
      ResourceElementFactory.get(OperClassEnum.LIBRARY.name()).submit(documentLibraryDTO.getTenantId(), documentLibraryDTO.getId());
    }
  }

  /**
   * 搜索文档库内文档
   *
   * @param libraryId 文档库ID
   * @param keyword 文档名称
   * @return 文档列表
   */
  public List<NodeBaseInfoDTO> searchDocument(String libraryId, String keyword) {
    return documentNodeMapper.searchByNodeName(libraryId, keyword, DocBaseConsts.DOCUMENT_SEARCH_MAX_COUNT);
  }

  /**
   * 拖动节点位置
   * 1、首先需要校验是否具有原文件的删除权限和新文档库的新增权限
   * 2、原文件需要处理原文档库对应的数据的上下级链表关系
   * 3、添加到新文档库的时候需要判断是放到某个文件夹下还是同级下，都放到最前面
   */
  @Transactional
  public NodeMoveLibraryDocumentDataOpRo moveLibrary(DocumentNodeMoveLibraryOpRo nodeOpRo) {
    if (nodeOpRo.getDocumentId().equals(nodeOpRo.getParentId())) {
      return null;
    }
    MoveLibraryInfoDTO moveLibraryInfoDTO = checkMoveLibrary(nodeOpRo);
    // 构建发布移进移出事件的数据
    NodeMoveLibraryDocumentDataOpRo data = new NodeMoveLibraryDocumentDataOpRo();
    data.setDocumentId(nodeOpRo.getDocumentId());
    data.setTargetLibraryId(nodeOpRo.getLibraryId());
    data.setLibraryId(moveLibraryInfoDTO.getSrcDocument().getLibraryId());
    data.setSubIds(this.getNodeIdsInNodeTree(Collections.singletonList(nodeOpRo.getDocumentId()), -1));
    data.setTenantId(nodeOpRo.getTenantId());
    data.setSpaceId(nodeOpRo.getSpaceId());

    // 将原链表解构出来
    resetOldNode(moveLibraryInfoDTO.getSrcDocument());
    // 设置移动的文档库
    resetNodeInfo(moveLibraryInfoDTO.getSrcDocument(), nodeOpRo, moveLibraryInfoDTO.getPreDocument());
    // 添加到新文档库下或者新文档库文件夹下
    resetNewNode(nodeOpRo.getParentId(), moveLibraryInfoDTO.getSrcDocument(), nodeOpRo.getSubType());
    return data;
  }

  /**
   * 设置参数
   */
  private void resetNodeInfo(DcDocumentEntity documentEntity, DocumentNodeMoveLibraryOpRo nodeOpRo,
    DcDocumentEntity preDocumentEntity) {
    documentEntity.setLibraryId(nodeOpRo.getLibraryId());
    if (CommonConsts.TRUE.equals(nodeOpRo.getSubType())) {
      documentEntity.setParentId(nodeOpRo.getParentId());
      documentEntity.setPrevDocumentId(null);
    }
    else {
      documentEntity.setPrevDocumentId(nodeOpRo.getParentId());
      // 同级的话需要找到这个上个节点的父节点
      documentEntity.setParentId(preDocumentEntity.getParentId());
    }
  }

  /**
   * 校验移动文档库前校验
   */
  private MoveLibraryInfoDTO checkMoveLibrary(DocumentNodeMoveLibraryOpRo nodeOpRo) {
    DcDocumentEntity documentEntity = documentMapper.selectByDocumentId(nodeOpRo.getDocumentId());
    if (documentEntity == null) {
      throw new BssException("文件或者文件夹不存在");
    }
    MoveLibraryInfoDTO moveLibraryInfoDTO = new MoveLibraryInfoDTO();
    if (Objects.equals(documentEntity.getDocumentType(), DocumentTypeEnum.ROOT.getCode())) {
      throw new BssException("不允许操作此节点");
    }
    moveLibraryInfoDTO.setSrcDocument(documentEntity);
    if (StringUtils.isNotEmpty(nodeOpRo.getParentId())) {
      DcDocumentEntity parentDto = documentMapper.selectByDocumentId(nodeOpRo.getParentId());
      if (parentDto == null) {
        throw new BssException("父节点文件夹不存在");
      }
      if (!nodeOpRo.getLibraryId().equals(parentDto.getLibraryId())) {
        throw new BssException("父节点和目标文档库不是同一个库");
      }
      if (!DocumentTypeEnum.FOLDER.getCode().equals(parentDto.getDocumentType())
        && !DocumentTypeEnum.ROOT.getCode().equals(parentDto.getDocumentType())) {
        throw new BssException("父节点类型不是文件夹或者根目录");
      }
      if (DocumentTypeEnum.ROOT.getCode().equals(parentDto.getDocumentType())) {
        // 默认到子级下
        nodeOpRo.setSubType(CommonConsts.TRUE);
      }
      moveLibraryInfoDTO.setPreDocument(parentDto);
    }
    else {
      // 默认在根节点下
      nodeOpRo.setParentId(documentNodeMapper.selectRootNodeIdByLibraryId(nodeOpRo.getLibraryId(),
        nodeOpRo.getTenantId(), nodeOpRo.getSpaceId()));
      // 默认到子级下
      nodeOpRo.setSubType(CommonConsts.TRUE);
    }
    checkNodeMoveLibraryPermission(nodeOpRo, documentEntity);
    return moveLibraryInfoDTO;
  }

  private void checkNodeMoveLibraryPermission(DocumentNodeMoveLibraryOpRo nodeOpRo, DcDocumentEntity documentEntity) {
    if (documentMapper.existsDocumentByName(nodeOpRo.getLibraryId(), nodeOpRo.getParentId(), documentEntity.getDocumentId(), documentEntity.getDocumentName(), documentEntity.getDocumentType(), documentEntity.getContentSource())) {
      throw new BssException("该文档库的同级文件夹下存在相同名字的文档或者文件夹！");
    }
    Long optUserId = SessionUtil.getLoginInfo().getUserId();
    controlTemplate.checkNodePermission(documentEntity.getLibraryId(), optUserId, nodeOpRo.getDocumentId(),
      NodePermission.REMOVE_NODE, DocumentPermConsts.ACCESS_DENIED_CALLBACK);
    DocumentLibraryDTO documentLibrary = documentLibraryCache.getByLibraryId(nodeOpRo.getLibraryId());
    if (documentLibrary == null) {
      throw new BssException("文档库不存在");
    }
    // 检查文档库的编辑权限
    checkParentNodeEditPermission(documentLibrary, optUserId, nodeOpRo.getParentId(), null);
  }

  /**
   * 解构原文档库相关链表
   */
  private void resetOldNode(DcDocumentEntity node) {
    DistributedLock lock = distributedLockFactory.getBizLock(DocLockConsts.DOCUMENT_DELETE_NODE_LOCK,
      node.getParentId());
    try {
      if (lock.tryLock(2, TimeUnit.MINUTES)) {
        // 更新节点对应的前置节点， 将删除节点的前一个节点与后一个节点连接
        documentNodeMapper.updatePreNodeIdByJoinSelf(node.getDocumentId(), node.getParentId());
      }
      else {
        throw new BssException("操作冲突");
      }
    }
    catch (InterruptedException e) {
      throw new BssException("操作冲突", e);
    }
    finally {
      lock.unlock();
    }
  }

  /**
   * 到新文档库重置链表
   */
  private void resetNewNode(String parentId, DcDocumentEntity documentEntity, String subType) {
    // 新建的文档默认放在最前方
    String firstSiblingDocumentId = documentMapper.selectFirstSiblingDocumentId(parentId);
    if (CommonConsts.TRUE.equals(subType)) {
      List<String> nodeIds = this.getNodeIdsInNodeTree(Collections.singletonList(documentEntity.getDocumentId()), -1);
      if (CollectionUtils.isNotEmpty(nodeIds)) {
        documentMapper.updateMoveLibraryBySubNodeIds(documentEntity.getLibraryId(), nodeIds);
      }
      documentMapper.updateSiblingPreDocumentId(firstSiblingDocumentId, documentEntity.getDocumentId(),
        LocalDateTime.now());
    }
    else {
      documentMapper.updatePreDocumentId(documentEntity.getDocumentId(), documentEntity.getPrevDocumentId());
    }
    documentMapper.updateMoveLibrary(documentEntity);
  }

  public DocumentReleasedDTO queryDocumentReleased(String documentId) {
    return documentMapper.queryDocumentReleased(documentId);
  }

  /**
   * 分页查询文件夹下的子元素（按创建时间降序）
   *
   * @param params    查询参数
   * @param libraryId 文档库ID
   * @return 文件夹下的子元素
   */
  public DocFolderSubsVo getFolderSubs(DocFolderSubsQueryParams params, String libraryId) {
    DocFolderSubsVo result = new DocFolderSubsVo();
    Long userId = SessionUtil.getLoginInfo().getUserId();

    PageInfo<NodeInfoTreeVo> nodes = documentNodeMapper.selectNodeInfoTreePageByNodeId(params, userId,
        params.buildRowBounds()).toPageInfo();
    result.setNodes(nodes);

    if (CollectionUtils.isEmpty(nodes.getList())) {
      result.setFolderCount(0L);
      result.setDocCount(0L);
      return result;
    }

    List<NodeInfoTreeVo> treeList = nodes.getList();

    List<Long> creatorIds = treeList.stream().map(NodeInfoVo::getCreatorId).distinct().toList();
    Map<Long, PortalUserDTO> userMap = dcUserService.findUserMapBatchByIds(creatorIds);
    treeList.forEach(node -> {
      if (userMap.containsKey(node.getCreatorId())) {
        node.setCreatorName(userMap.get(node.getCreatorId()).getUserName());
      }
    });

    List<String> nodeIds = treeList.stream().map(NodeInfoTreeVo::getNodeId).toList();
    ControlRoleDict roleDict = controlTemplate.fetchNodeTreeNode(libraryId, userId, nodeIds);
    setRole(treeList, roleDict);

    result.setFolderCount(documentNodeMapper.countFolderByParentId(params.getDocumentId()));
    result.setDocCount(documentNodeMapper.countDocByParentId(params.getDocumentId()));

    return result;
  }
}
